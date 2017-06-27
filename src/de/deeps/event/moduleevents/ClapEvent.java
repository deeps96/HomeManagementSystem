package de.deeps.event.moduleevents;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class ClapEvent extends Event<ClapEvent.Actions> {

	public static enum Actions {
		CLAP_DETECTED, MICRO_CONNECTED, MICRO_DISCONNECTED, STOP, START
	};

	public ClapEvent() {
		this(Source.UNKNOWN);
	}

	public ClapEvent(Source source) {
		super(source);
	}

	public static ClapEvent clapDetected(int claps) {
		ClapEvent event = new ClapEvent(Source.CLAPDETECTOR);
		event.setAction(Actions.CLAP_DETECTED);
		event.setType(Type.DETECT);
		event.setValue(claps);
		return event;
	}

	public static ClapEvent microDisconnected(String errorMessage) {
		ClapEvent event = new ClapEvent(Source.CLAPDETECTOR);
		event.setAction(Actions.MICRO_DISCONNECTED);
		event.setType(Type.DETECT);
		event.setValue(errorMessage);
		return event;
	}

	public static ClapEvent microConnected() {
		ClapEvent event = new ClapEvent(Source.CLAPDETECTOR);
		event.setAction(Actions.MICRO_CONNECTED);
		event.setType(Type.DETECT);
		return event;
	}

	public static ClapEvent stop() {
		ClapEvent event = new ClapEvent(Source.CLAPDETECTOR);
		event.setAction(Actions.STOP);
		event.setType(Type.REQUEST);
		return event;
	}

	public static ClapEvent start() {
		ClapEvent event = new ClapEvent(Source.CLAPDETECTOR);
		event.setAction(Actions.START);
		event.setType(Type.REQUEST);
		return event;
	}
}
