package de.deeps.event.moduleevents;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class NetworkDeviceDetectionEvent
		extends Event<NetworkDeviceDetectionEvent.Actions> {

	public static enum Actions {
		NETWORK_DEVICE_CONNECTED, NETWORK_DEVICE_DISCONNECTED
	}

	public NetworkDeviceDetectionEvent() {
		this(Source.UNKNOWN);
	}

	public NetworkDeviceDetectionEvent(Source source) {
		super(source);
	}

	public static NetworkDeviceDetectionEvent createDeviceConnected(
			Source source, String address) {
		NetworkDeviceDetectionEvent event = new NetworkDeviceDetectionEvent(
				source);
		event.setAction(Actions.NETWORK_DEVICE_CONNECTED);
		event.setType(Type.DETECT);
		event.setValue(address);
		return event;
	}

	public static NetworkDeviceDetectionEvent createDeviceDisconnected(
			Source source, String address) {
		NetworkDeviceDetectionEvent event = new NetworkDeviceDetectionEvent(
				source);
		event.setAction(Actions.NETWORK_DEVICE_DISCONNECTED);
		event.setType(Type.DETECT);
		event.setValue(address);
		return event;
	}

}
