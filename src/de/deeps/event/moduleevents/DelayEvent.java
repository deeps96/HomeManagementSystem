package de.deeps.event.moduleevents;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class DelayEvent extends Event<DelayEvent.Actions> {

	public static enum Actions {
		DELAY_ACTION_EVENT, DELAY_EVENT, TIME_EVENT, TIME_EVENT_WITH_REPEAT
	};

	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public DelayEvent() {
		this(Source.UNKNOWN);
	}

	public DelayEvent(Source source) {
		super(source);
	}

	public static DelayEvent createDelayActionEvent(Source source, int delayInSeconds,
			String eventName) {
		DelayEvent event = new DelayEvent(source);
		event.setAction(Actions.DELAY_ACTION_EVENT);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(delayInSeconds);
		values.add(eventName);
		event.setValues(values);
		return event;
	}

	public static DelayEvent createDelayEvent(Source source, int delayInSeconds,
			Event<?> delayEvent) {
		DelayEvent event = new DelayEvent(source);
		event.setAction(Actions.DELAY_EVENT);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(delayInSeconds);
		values.add(delayEvent);
		event.setValues(values);
		return event;
	}

	public static DelayEvent createTimeEvent(Source source, String date,
			Event<?> timeEvent) {
		try {
			return createTimeEvent(
				source,
				DATE_FORMATTER.parse(date),
				timeEvent);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static DelayEvent createTimeEvent(Source source, Date date,
			Event<?> timeEvent) {
		DelayEvent event = new DelayEvent(source);
		event.setAction(Actions.TIME_EVENT);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(date);
		values.add(timeEvent);
		event.setValues(values);
		return event;
	}

	public static DelayEvent createTimeEventWithRepeat(Source source,
			String date, Event<?> timeEvent, int repeat) {
		try {
			return createTimeEventWithRepeat(
				source,
				DATE_FORMATTER.parse(date),
				timeEvent,
				repeat);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static DelayEvent createTimeEventWithRepeat(Source source, Date date,
			Event<?> timeEvent, int repeat) {
		DelayEvent event = new DelayEvent(source);
		event.setAction(Actions.TIME_EVENT_WITH_REPEAT);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(date);
		values.add(timeEvent);
		values.add(repeat);
		event.setValues(values);
		return event;
	}
}
