package de.deeps.modules;

import de.deeps.event.Event;
import de.deeps.event.EventCollector;

/**
 * @author Deeps
 */

public abstract class Module {

	public static enum Name {
		ALARM_MANAGER,
		AMBILIGHT,
		BLUETOOTH_DEVICE_DETECTION,
		CLAP_DETECTION,
		DIODER_CONTROL,
		EVENT_FORWARDER,
		IR_REMOTE,
		LED_BOARD,
		LIRC,
		MEDIA_PLAYER,
		MODE_SWITCHER,
		NETWORK_DEVICE_DETECTION,
		PC_CONTROLLER,
		RADIO_REMOTE,
		EVENT_DELAYER,
		RULE_MANAGER,
		TCP_EVENT_TRANSMITTER,
		UDP_EVENT_RECEIVER,
		VISUALIZER,
		VOICE_CONTROL,
		WAKE_ON_LAN,
		WEBSITE
	};

	protected boolean isAvailable, isRunning;
	protected EventCollector collector;
	private Name name;
	private String machineName;

	public Module(EventCollector collector, Name name, String machineName) {
		this.collector = collector;
		this.machineName = machineName;
		initialize();
		addEventListener();
	}

	protected abstract void addEventListener();

	protected abstract void initialize();

	public abstract void updateAvailability();

	protected void start() {
		if (isRunning || !isAvailable) {
			return;
		}
		isRunning = true;
	}

	public void stop() {
		if (!isRunning || !isAvailable) {
			return;
		}
		isRunning = false;
	}

	protected void addEvent(Event<?> event) {
		collector.addEvent(event);
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public Name getName() {
		return name;
	}

	public String getMachineName() {
		return machineName;
	}

}
