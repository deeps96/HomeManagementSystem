package de.deeps.modules.ledboard;

import java.io.IOException;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.LEDBoardEvent;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.ledboard.HardwareDriver;
import de.deeps.ledboard.LedBoard;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class LEDBoard extends Module {

	private LedBoard ledBoard;

	public LEDBoard(EventCollector collector, String machineName) {
		super(collector, Name.LED_BOARD, machineName);
	}

	@Override
	protected void addEventListener() {
		addAnimatedStringListener();
		addStaticStringForSecListener();
		addStaticStringListener();
		addTurnOffListener();
	}

	private void addTurnOffListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(LEDBoardEvent.Actions.TURN_OFF);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				if (isAvailable) {
					ledBoard.off();
				} else {
					collector.addEvent(
						TCPEvent.createSendBroadcastEvent(
							Source.LED_BOARD,
							event));
				}
			}
		});
	}

	private void addStaticStringListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction() == LEDBoardEvent.Actions.SHOW_STATIC;
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				if (isAvailable) {
					ledBoard.off();
					ledBoard.printStaticMessage(
						(String) event.getValues().get(0));
					ledBoard.display();
				} else {
					collector.addEvent(
						TCPEvent.createSendBroadcastEvent(
							Source.LED_BOARD,
							event));
				}
			}
		});
	}

	private void addStaticStringForSecListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event
						.getAction() == LEDBoardEvent.Actions.SHOW_STATIC_FOR_SECONDS;
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				if (isAvailable) {
					showStaticForSecMessage(
						(String) event.getValues().get(0),
						(int) event.getValues().get(1));
				} else {
					collector.addEvent(
						TCPEvent.createSendBroadcastEvent(
							Source.LED_BOARD,
							event));
				}
			}
		});
	}

	private void showStaticForSecMessage(String message, int seconds) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ledBoard.off();
				ledBoard.printStaticMessage(message);
				ledBoard.display();
				try {
					Thread.sleep(seconds * 1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ledBoard.off();
			}
		}).start();
	}

	private void addAnimatedStringListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event
						.getAction() == LEDBoardEvent.Actions.LED_BOARD_SHOW_ANIMATED_STRING;
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				if (isAvailable) {
					ledBoard.off();
					ledBoard.printAnimatedMessage(
						(String) event.getValues().get(0),
						(int) event.getValues().get(1));
					ledBoard.display();
				} else {
					collector.addEvent(
						TCPEvent.createSendBroadcastEvent(
							Source.LED_BOARD,
							event));
				}
			}
		});
	}

	@Override
	protected void initialize() {
		try {
			ledBoard = new LedBoard();
			ledBoard.setBrightness(HardwareDriver.BRIGHTNESS_LEVEL_MAX);
			isAvailable = true;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			isAvailable = false;
		}
	}

	@Override
	public void updateAvailability() {
		if (ledBoard != null) {
			ledBoard.off();
		}
		initialize();
	}

}
