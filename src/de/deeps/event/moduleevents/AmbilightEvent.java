package de.deeps.event.moduleevents;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class AmbilightEvent extends Event<AmbilightEvent.Actions> {

	public static enum Actions {
		START_AMBILIGHT_REQUEST, STOP_AMBILIGHT_REQUEST
	};

	public AmbilightEvent() {
		this(Source.UNKNOWN);
	}

	public AmbilightEvent(Source source) {
		super(source);
	}

	public static AmbilightEvent createStartAmbilightRequest(Source source) {
		AmbilightEvent event = new AmbilightEvent(source);
		event.setAction(Actions.START_AMBILIGHT_REQUEST);
		event.setType(Type.REQUEST);
		return event;
	}

	public static AmbilightEvent createStopAmbilightRequest(Source source) {
		AmbilightEvent event = new AmbilightEvent(source);
		event.setAction(Actions.STOP_AMBILIGHT_REQUEST);
		event.setType(Type.REQUEST);
		return event;
	}

}
