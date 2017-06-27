package de.deeps.modules.eventdelayer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.DelayEvent;
import de.deeps.modules.Module;
import de.deeps.modules.rules.RuleManager;
import de.deeps.modules.rules.RuleParser;

/**
 * @author Deeps
 */

public class EventDelayer extends Module {

	public EventDelayer(EventCollector collector, String machineName) {
		super(collector, Name.EVENT_DELAYER, machineName);
	}

	@Override
	protected void addEventListener() {
		addDelayActionEventEventListener();
		addDelayEventEventListener();
		addTimeEventEventListener();
		addTimeEventWithRepeatEventListener();
	}

	private void addTimeEventWithRepeatEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event
						.getAction() == DelayEvent.Actions.TIME_EVENT_WITH_REPEAT;
			}
		};
		collector.addListener(new EventListener(condition, true) {
			@Override
			public void onEventOccured(Event<?> event) {
				timeEvent(
					(Event<?>) event.getValues().get(1),
					(Date) event.getValues().get(0),
					(int) event.getValues().get(2));
			}
		});
	}

	private void addTimeEventEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction() == DelayEvent.Actions.TIME_EVENT;
			}
		};
		collector.addListener(new EventListener(condition, true) {
			@Override
			public void onEventOccured(Event<?> event) {
				timeEvent(
					(Event<?>) event.getValues().get(1),
					(Date) event.getValues().get(0),
					-1);
			}
		});
	}

	protected void timeEvent(Event<?> event, Date date, int repeat) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				collector.addEvent(event);
			}
		};
		if (repeat == -1) {
			timer.schedule(task, date);
		} else {
			timer.schedule(task, date, repeat);
		}
	}

	private void addDelayEventEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction() == DelayEvent.Actions.DELAY_EVENT;
			}
		};
		collector.addListener(new EventListener(condition, true) {
			@Override
			public void onEventOccured(Event<?> event) {
				startNewDelayerTaskFor(
					(Event<?>) event.getValues().get(1),
					(int) event.getValues().get(0));
			}
		});
	}

	private void addDelayActionEventEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event
						.getAction() == DelayEvent.Actions.DELAY_ACTION_EVENT;
			}
		};
		collector.addListener(new EventListener(condition, true) {
			@Override
			public void onEventOccured(Event<?> event) {
				startNewDelayerTaskFor(
					RuleManager.EVENTS_DIR + "/"
							+ (String) event.getValues().get(1) + ".json",
					(int) event.getValues().get(0));
			}
		});
	}

	@Override
	protected void initialize() {
		isAvailable = true;
	}

	private void startNewDelayerTaskFor(String eventPath, int delayInSeconds) {
		Event<?> actionEvent = RuleParser
				.parseActionEventFromJSONFile(eventPath);
		startNewDelayerTaskFor(actionEvent, delayInSeconds);
	}

	private void startNewDelayerTaskFor(Event<?> actionEvent,
			int delayInSeconds) {
		actionEvent.setSource(Source.EVENT_DELAYER);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(delayInSeconds * 1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				collector.addEvent(actionEvent);
			}
		}).start();
	}

	@Override
	public void updateAvailability() {
	}

}
