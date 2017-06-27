package de.deeps.modules.bluetooth;

import java.io.IOException;
import java.util.HashMap;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import de.deeps.event.EventCollector;
import de.deeps.event.moduleevents.BluetoothEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class BluetoothDeviceDetection extends Module
		implements DiscoveryListener {

	private static final UUID AVCTP = new UUID(0x0017);

	private DiscoveryAgent discoveryAgent;
	private HashMap<String, DeviceRecord> deviceRecords;

	public BluetoothDeviceDetection(EventCollector collector,
			String machineName) {
		super(collector, Name.BLUETOOTH_DEVICE_DETECTION, machineName);
	}

	@Override
	protected void initialize() {
		deviceRecords = new HashMap<>();
		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			discoveryAgent = localDevice.getDiscoveryAgent();
			isAvailable = true;
			start();
		} catch (BluetoothStateException e) {
			System.err.println(e.getMessage());
			isAvailable = false;
		}
	}

	@Override
	public void updateAvailability() {
		initialize();
	}

	@Override
	protected void start() {
		super.start();
		if (!isRunning) {
			return;
		}
		BluetoothDeviceDetection instance = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					discoveryAgent.startInquiry(DiscoveryAgent.GIAC, instance);
					isRunning = true;
					keepAliveWhileRunning();
				} catch (BluetoothStateException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void keepAliveWhileRunning() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isRunning) {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}

	private void startServiceDiscovery(DeviceRecord record) {
		BluetoothDeviceDetection agent = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int transID = LocalDevice.getLocalDevice()
							.getDiscoveryAgent().searchServices(
								new int[] { 0x0100 },
								new UUID[] { AVCTP },
								record.getDevice(),
								agent);
					record.setLastTransactionID(transID);
				} catch (BluetoothStateException e) {
					addEvent(
						BluetoothEvent.errorLoadingInterface(e.getMessage()));
				}
			}
		}).start();

	}

	@Override
	public void deviceDiscovered(RemoteDevice arg0, DeviceClass arg1) {
		if (!deviceRecords.containsKey(arg0.getBluetoothAddress())) {
			DeviceRecord record = new DeviceRecord(arg0);
			deviceRecords.put(arg0.getBluetoothAddress(), record);
			startServiceDiscovery(record);
		}
	}

	@Override
	public void serviceSearchCompleted(int arg0, int arg1) {
		for (DeviceRecord record : deviceRecords.values()) {
			if (record.getLastTransactionID() == arg0) {
				if (record.hasBeenConnected()) {
					addEvent(
						BluetoothEvent.deviceConnected(record.getDeviceInfo()));
				}
				if (record.hasBeenDisconnected()) {
					addEvent(
						BluetoothEvent
								.deviceDisconnected(record.getDeviceInfo()));
				}
				record.saveCurrentState();
				if (isRunning) {
					startServiceDiscovery(record);
				}
				break;
			}
		}
	}

	@Override
	public void servicesDiscovered(int arg0, ServiceRecord[] arg1) {
		for (int i = 0; i < arg1.length; i++) {
			String url = arg1[i].getConnectionURL(
				ServiceRecord.NOAUTHENTICATE_NOENCRYPT,
				false);
			deviceRecords.get(arg1[i].getHostDevice().getBluetoothAddress())
					.setConnected(url != null);
		}
	}

	@Override
	public void inquiryCompleted(int arg0) {
	}

	private class DeviceRecord {

		private boolean isConnected, wasConnectedLastTime;
		private int lastTransactionID;
		private BluetoothDeviceInfo deviceInfo;
		private RemoteDevice device;

		public DeviceRecord(RemoteDevice device) {
			this.device = device;
			deviceInfo = createBluetoothDeviceInfoForRemoteDevice(device);
			isConnected = false;
			wasConnectedLastTime = false;
		}

		private BluetoothDeviceInfo createBluetoothDeviceInfoForRemoteDevice(
				RemoteDevice device) {
			String name = "unknown";
			try {
				name = device.getFriendlyName(true);
			} catch (IOException e) {
			}
			return new BluetoothDeviceInfo(name, device.getBluetoothAddress());
		}

		public void saveCurrentState() {
			wasConnectedLastTime = isConnected;
			isConnected = false;
		}

		public boolean hasBeenConnected() {
			return !wasConnectedLastTime && isConnected;
		}

		public boolean hasBeenDisconnected() {
			return wasConnectedLastTime && !isConnected;
		}

		public BluetoothDeviceInfo getDeviceInfo() {
			return deviceInfo;
		}

		public void setConnected(boolean isConnected) {
			this.isConnected = isConnected;
		}

		public void setLastTransactionID(int lastTransactionID) {
			this.lastTransactionID = lastTransactionID;
		}

		public int getLastTransactionID() {
			return lastTransactionID;
		}

		public RemoteDevice getDevice() {
			return device;
		}

	}

	@Override
	protected void addEventListener() {
	}
}
