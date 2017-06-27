package de.deeps.modules.bluetooth;

/**
 * @author Deeps
 */

public class BluetoothDeviceInfo {

	private String name, mac;

	public BluetoothDeviceInfo(String name, String mac) {
		this.name = name;
		this.mac = mac;
	}

	public String getName() {
		return name;
	}

	public String getMac() {
		return mac;
	}

}
