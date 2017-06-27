package de.deeps.event.moduleevents;

import java.util.LinkedList;
import java.util.List;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class IRRemoteEvent extends Event<IRRemoteEvent.Actions> {

	public static enum Actions {
		PRESS_KEY
	};

	public IRRemoteEvent() {
		this(Source.UNKNOWN);
	}

	public IRRemoteEvent(Source source) {
		super(source);
	}

	public static IRRemoteEvent createPressKeyEvent(Source source,
			String remoteName, String button) {
		IRRemoteEvent event = new IRRemoteEvent(source);
		event.setAction(Actions.PRESS_KEY);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(remoteName);
		values.add(button);
		event.setValues(values);
		return event;
	}

}
