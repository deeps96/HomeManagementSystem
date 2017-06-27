package de.deeps.event.moduleevents;

import de.deeps.event.Event;
import de.deeps.modules.pccontroller.PCControllerInterface;

/**
 * @author Deeps
 */

public class PCControllerEvent extends Event<PCControllerEvent.Actions> {

	public static enum Actions {
		SHUTDOWN_REQUEST,
		SET_VOLUME_REQUEST,
		VOLUME_REQUEST,
		START_PROGRAM_REQUEST,
		VOLUME_INFO,
		SELECT_AUDIO_OUT,
		SLEEP_REQUEST,
		CHANGE_OUTPUT_DEVICE
	};

	public PCControllerEvent() {
		this(Source.UNKNOWN);
	}

	public PCControllerEvent(Source source) {
		super(source);
	}

	public static PCControllerEvent createShutdownRequest(Source source) {
		PCControllerEvent event = new PCControllerEvent(source);
		event.setAction(Actions.SHUTDOWN_REQUEST);
		event.setType(Type.REQUEST);
		return event;
	}

	public static PCControllerEvent createSetVolumeRequest(Source source,
			int volume) {
		PCControllerEvent event = new PCControllerEvent(source);
		event.setAction(Actions.SET_VOLUME_REQUEST);
		event.setType(Type.REQUEST);
		event.setValue(volume);
		return event;
	}

	public static PCControllerEvent createVolumeRequest(Source source) {
		PCControllerEvent event = new PCControllerEvent(source);
		event.setAction(Actions.VOLUME_REQUEST);
		event.setType(Type.REQUEST);
		return event;
	}

	public static PCControllerEvent createVolumeInfo(Source source,
			int volume) {
		PCControllerEvent event = new PCControllerEvent(source);
		event.setAction(Actions.VOLUME_INFO);
		event.setType(Type.INFO);
		event.setValue(volume);
		return event;
	}

	public static PCControllerEvent createStartProgramRequest(Source source,
			PCControllerInterface.Program program) {
		PCControllerEvent event = new PCControllerEvent(source);
		event.setAction(Actions.START_PROGRAM_REQUEST);
		event.setType(Type.REQUEST);
		event.setValue(program);
		return event;
	}

	public static PCControllerEvent createSelectAudioOut(Source source) {
		PCControllerEvent event = new PCControllerEvent(source);
		event.setAction(Actions.SELECT_AUDIO_OUT);
		event.setType(Type.REQUEST);
		return event;
	}

	public static PCControllerEvent createSleepRequest(Source source) {
		PCControllerEvent event = new PCControllerEvent(source);
		event.setAction(Actions.SLEEP_REQUEST);
		event.setType(Type.REQUEST);
		return event;
	}

	public static PCControllerEvent createChangeOutputDeviceRequest(
			Source source, String deviceName) {
		PCControllerEvent event = new PCControllerEvent(source);
		event.setAction(Actions.CHANGE_OUTPUT_DEVICE);
		event.setType(Type.REQUEST);
		event.setValue(deviceName);
		return event;
	}

}
