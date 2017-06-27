package de.deeps.event.moduleevents;

import java.util.LinkedList;
import java.util.List;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class LircEvent extends Event<LircEvent.Actions> {

	public static enum Actions {
		SEND_IR_RAW
	};

	public LircEvent() {
		this(Source.UNKNOWN);
	}

	public LircEvent(Source source) {
		super(source);
	}

	public static LircEvent createSendIR(Source source, String remoteName,
			String keyName, int repeat) {
		LircEvent event = new LircEvent(source);
		event.setAction(Actions.SEND_IR_RAW);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(remoteName);
		values.add(keyName);
		values.add(repeat);
		event.setValues(values);
		return event;
	}

}
