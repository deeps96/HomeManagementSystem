package de.deeps.event.moduleevents;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class ModeEvent extends Event<ModeEvent.Actions> {

	public static enum Actions {
		ENABLE_STANDARD,
		ENABLE_CINEMA,
		ENABLE_WORKING,
		ENABLE_EATING,
		ENABLE_PARTY,
		ENABLE_WAKE_UP_ALARM,
	};

	public ModeEvent() {
		this(Source.UNKNOWN);
	}

	public ModeEvent(Source source) {
		super(source);
	}

	public static ModeEvent createEnableStandard(Source source) {
		ModeEvent event = new ModeEvent(source);
		event.setAction(Actions.ENABLE_STANDARD);
		event.setType(Type.REQUEST);
		return event;
	}

	public static ModeEvent createEnableCinema(Source source) {
		ModeEvent event = new ModeEvent(source);
		event.setAction(Actions.ENABLE_CINEMA);
		event.setType(Type.REQUEST);
		return event;
	}

	public static ModeEvent createEnableWorking(Source source) {
		ModeEvent event = new ModeEvent(source);
		event.setAction(Actions.ENABLE_WORKING);
		event.setType(Type.REQUEST);
		return event;
	}

	public static ModeEvent createEnableEating(Source source) {
		ModeEvent event = new ModeEvent(source);
		event.setAction(Actions.ENABLE_EATING);
		event.setType(Type.REQUEST);
		return event;
	}

	public static ModeEvent createEnableParty(Source source) {
		ModeEvent event = new ModeEvent(source);
		event.setAction(Actions.ENABLE_PARTY);
		event.setType(Type.REQUEST);
		return event;
	}

	public static ModeEvent createEnableWakeUpAlarm(Source source) {
		ModeEvent event = new ModeEvent(source);
		event.setAction(Actions.ENABLE_WAKE_UP_ALARM);
		event.setType(Type.REQUEST);
		return event;
	}

}
