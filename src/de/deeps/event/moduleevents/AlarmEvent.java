package de.deeps.event.moduleevents;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class AlarmEvent extends Event<AlarmEvent.Actions> {

	public static enum Actions {
		ADD_ALARM, REMOVE_ALARM, SILENCE_ALL
	};

	public AlarmEvent() {
		this(Source.UNKNOWN);
	}

	public AlarmEvent(Source source) {
		super(source);
	}

	public static AlarmEvent createAddAlarm(Source source, String time) {
		AlarmEvent event = new AlarmEvent(source);
		event.setAction(Actions.ADD_ALARM);
		event.setType(Type.REQUEST);
		event.setValue(time);
		return event;
	}

	public static AlarmEvent createRemoveAlarm(Source source, String time) {
		AlarmEvent event = new AlarmEvent(source);
		event.setAction(Actions.REMOVE_ALARM);
		event.setType(Type.REQUEST);
		event.setValue(time);
		return event;
	}

	public static AlarmEvent createSilenceAll(Source source) {
		AlarmEvent event = new AlarmEvent(source);
		event.setAction(Actions.SILENCE_ALL);
		event.setType(Type.REQUEST);
		return event;
	}

}
