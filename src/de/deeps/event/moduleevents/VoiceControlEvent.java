package de.deeps.event.moduleevents;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class VoiceControlEvent extends Event<VoiceControlEvent.Actions> {

	public static enum Actions {
		CAPTURE_SEQUENCE
	}

	public VoiceControlEvent() {
		this(Source.UNKNOWN);
	}

	public VoiceControlEvent(Source source) {
		super(source);
	}

	public static VoiceControlEvent createCaptureSequenceRequest(
			Source source) {
		VoiceControlEvent event = new VoiceControlEvent(source);
		event.setAction(Actions.CAPTURE_SEQUENCE);
		event.setType(Type.REQUEST);
		return event;
	}
}
