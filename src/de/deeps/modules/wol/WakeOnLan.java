package de.deeps.modules.wol;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import de.deeps.event.Event;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.WakeOnLanEvent;
import de.deeps.modules.Module;
import de.deeps.network.WakeOnLanSender;

/**
 * @author Deeps
 */

public class WakeOnLan extends Module {

	public static enum Device {
		STAND_PC
	}

	private final HashMap<Device, byte[]> macAddresses = new HashMap<Device, byte[]>() {
		{
			put(
				Device.STAND_PC,
				new byte[] { (byte) -112, (byte) 43, (byte) 52, (byte) 55,
						(byte) -5, (byte) 78 }); // 90:2B:34:37:FB:4E
		}
	};

	private WakeOnLanSender sender;

	public WakeOnLan(EventCollector collector, String machineName) {
		super(collector, Name.WAKE_ON_LAN, machineName);
	}

	@Override
	protected void addEventListener() {
		addWakeByDeviceListener();
		addWakeByMACListener();
	}

	private void addWakeByMACListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(WakeOnLanEvent.Actions.WAKE_DEVICE_BY_MAC);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				byte[] macAddress = getMacBytes(
					(String) event.getValues().get(0));
				if (macAddress != null) {
					sender.sendWakeOnLanTo(macAddress);
				}
			}
		});
	}

	private void addWakeByDeviceListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(WakeOnLanEvent.Actions.WAKE_DEVICE_BY_DEVICE);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				Device device = (Device) event.getValues().get(0);
				byte[] macAddress = macAddresses.get(device);
				if (macAddress != null) {
					sender.sendWakeOnLanTo(macAddress);
				}
			}
		});
	}

	private static byte[] getMacBytes(String macStr)
			throws IllegalArgumentException {
		byte[] bytes = new byte[6];
		String[] hex = macStr.split("(\\:|\\-)");
		if (hex.length != 6) {
			throw new IllegalArgumentException("Invalid MAC address.");
		}
		try {
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) Integer.parseInt(hex[i], 16);
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					"Invalid hex digit in MAC address.");
		}
		return bytes;
	}

	@Override
	protected void initialize() {
		try {
			sender = new WakeOnLanSender();
			isAvailable = true;
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
			isAvailable = false;
		}
	}

	@Override
	public void updateAvailability() {
		initialize();
	}

}
