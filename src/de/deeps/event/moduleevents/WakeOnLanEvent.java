package de.deeps.event.moduleevents;

import de.deeps.event.Event;
import de.deeps.modules.wol.WakeOnLan;

/**
 * @author Deeps
 */

public class WakeOnLanEvent extends Event<WakeOnLanEvent.Actions> {

	public static enum Actions {
		WAKE_DEVICE_BY_DEVICE, WAKE_DEVICE_BY_MAC
	}

	public WakeOnLanEvent() {
		this(Source.UNKNOWN);
	}

	public WakeOnLanEvent(Source source) {
		super(source);
	}

	public static WakeOnLanEvent createWakeDeviceByMAC(Source source,
			String mac) {
		WakeOnLanEvent event = new WakeOnLanEvent(source);
		event.setAction(Actions.WAKE_DEVICE_BY_MAC);
		event.setType(Type.REQUEST);
		event.setValue(mac);
		return event;
	}

	public static WakeOnLanEvent createWakeDevice(Source source,
			WakeOnLan.Device device) {
		WakeOnLanEvent event = new WakeOnLanEvent(source);
		event.setAction(Actions.WAKE_DEVICE_BY_DEVICE);
		event.setType(Type.REQUEST);
		event.setValue(device);
		return event;
	}

}
