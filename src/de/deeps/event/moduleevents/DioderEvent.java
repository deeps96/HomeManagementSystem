package de.deeps.event.moduleevents;

import java.awt.Color;
import java.util.LinkedList;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class DioderEvent extends Event<DioderEvent.Actions> {

	public static enum Actions {
		ON, OFF, SHOW_COLOR
	};

	public DioderEvent() {
		this(Source.UNKNOWN);
	}

	public DioderEvent(Source source) {
		super(source);
	}

	public static DioderEvent createOnRequest(Source source) {
		DioderEvent event = new DioderEvent(source);
		event.setType(Type.REQUEST);
		event.setAction(Actions.ON);
		return event;
	}

	public static DioderEvent createOffRequest(Source source) {
		DioderEvent event = new DioderEvent(source);
		event.setType(Type.REQUEST);
		event.setAction(Actions.OFF);
		return event;
	}

	public static DioderEvent createShowColorRequest(Source source, Color color,
			boolean smooth) {
		DioderEvent event = new DioderEvent(source);
		event.setType(Type.REQUEST);
		event.setAction(Actions.SHOW_COLOR);
		LinkedList<Object> values = new LinkedList<>();
		values.add(color);
		values.add(smooth);
		event.setValues(values);
		return event;
	}

}
