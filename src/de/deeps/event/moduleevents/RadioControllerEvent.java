package de.deeps.event.moduleevents;

import java.util.LinkedList;
import java.util.List;

import de.deeps.event.Event;
import de.deeps.modules.radioremote.RadioRemote;

/**
 * @author Deeps
 */

public class RadioControllerEvent extends Event<RadioControllerEvent.Actions> {

	public static enum Actions {
		SEND_RADIO_SIGNAL_FOR_BUTTON, SEND_RADIO_SIGNAL_FOR_BUTTON_STRING
	};

	public RadioControllerEvent() {
		this(Source.UNKNOWN);
	}

	public RadioControllerEvent(Source source) {
		super(source);
	}

	public static RadioControllerEvent createSendSignalEvent(Source source,
			String buttonID, boolean turnOn) {
		RadioControllerEvent event = new RadioControllerEvent(source);
		event.setAction(Actions.SEND_RADIO_SIGNAL_FOR_BUTTON_STRING);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(buttonID);
		values.add(turnOn);
		event.setValues(values);
		return event;
	}

	public static RadioControllerEvent createSendSignalEvent(Source source,
			RadioRemote.Button button, boolean turnOn) {
		RadioControllerEvent event = new RadioControllerEvent(source);
		event.setAction(Actions.SEND_RADIO_SIGNAL_FOR_BUTTON);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(button);
		values.add(turnOn);
		event.setValues(values);
		return event;
	}
}
