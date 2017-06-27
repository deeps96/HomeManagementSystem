package de.deeps.event.moduleevents;

import java.util.Arrays;
import java.util.LinkedList;

import de.deeps.event.Event;
import de.deeps.modules.bluetooth.BluetoothDeviceInfo;

/**
 * @author Deeps
 */

public class BluetoothEvent extends Event<BluetoothEvent.Actions> {

	public static enum Actions {
		BT_DEVICE_CONNECTED, BT_DEVICE_DISCONNECTED, BT_INTERFACE_LOADING_ERROR
	};

	public BluetoothEvent() {
		this(Source.UNKNOWN);
	}

	public BluetoothEvent(Source source) {
		super(source);
	}

	public static BluetoothEvent deviceConnected(BluetoothDeviceInfo info) {
		BluetoothEvent event = new BluetoothEvent(Source.BLUETOOTH);
		event.setAction(Actions.BT_DEVICE_CONNECTED);
		event.setValues(
			new LinkedList<Object>(
					Arrays.asList(info.getName(), info.getMac())));
		event.setType(Type.DETECT);
		return event;
	}

	public static BluetoothEvent deviceDisconnected(BluetoothDeviceInfo info) {
		BluetoothEvent event = new BluetoothEvent(Source.BLUETOOTH);
		event.setAction(Actions.BT_DEVICE_DISCONNECTED);
		event.setValues(
			new LinkedList<Object>(
					Arrays.asList(info.getName(), info.getMac())));
		event.setType(Type.DETECT);
		return event;
	}

	public static BluetoothEvent errorLoadingInterface(String errorMessage) {
		BluetoothEvent event = new BluetoothEvent(Source.BLUETOOTH);
		event.setAction(Actions.BT_INTERFACE_LOADING_ERROR);
		event.setType(Type.ERROR);
		event.setValue(errorMessage);
		return event;
	}
}
