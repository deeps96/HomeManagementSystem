package de.deeps.event.moduleevents;

import java.util.LinkedList;
import java.util.List;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class LEDBoardEvent extends Event<LEDBoardEvent.Actions> {

	public static enum Actions {
		LED_BOARD_SHOW_ANIMATED_STRING,
		SHOW_STATIC_FOR_SECONDS,
		SHOW_STATIC,
		TURN_OFF
	};

	public LEDBoardEvent() {
		this(Source.UNKNOWN);
	}

	public LEDBoardEvent(Source source) {
		super(source);
	}

	public static LEDBoardEvent createShowAnimatedStringEvent(Source source,
			String message, int repeat) {
		LEDBoardEvent event = new LEDBoardEvent(source);
		event.setAction(Actions.LED_BOARD_SHOW_ANIMATED_STRING);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(message);
		values.add(repeat);
		event.setValues(values);
		return event;
	}

	public static LEDBoardEvent createShowStaticString(Source source,
			String message, int seconds) {
		LEDBoardEvent event = new LEDBoardEvent(source);
		event.setAction(Actions.SHOW_STATIC_FOR_SECONDS);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(message);
		values.add(seconds);
		event.setValues(values);
		return event;
	}

	public static LEDBoardEvent createShowStaticString(Source source,
			String message) {
		LEDBoardEvent event = new LEDBoardEvent(source);
		event.setAction(Actions.SHOW_STATIC);
		event.setType(Type.REQUEST);
		event.setValue(message);
		return event;
	}

	public static LEDBoardEvent createTurnOff(Source source) {
		LEDBoardEvent event = new LEDBoardEvent(source);
		event.setAction(Actions.TURN_OFF);
		event.setType(Type.REQUEST);
		return event;
	}

}
