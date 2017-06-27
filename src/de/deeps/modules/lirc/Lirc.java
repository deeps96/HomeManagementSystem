package de.deeps.modules.lirc;

import java.io.IOException;

import org.lirc.LircClient;
import org.lirc.TcpLircClient;

import de.deeps.event.Event;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.LircEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class Lirc extends Module {

	private static final int LIRC_PORT = 8765;
	private static final String LIRC_ADDRESS = "192.168.178.96";

	private LircClient client;

	public Lirc(EventCollector collector, String machineName) {
		super(collector, Name.LIRC, machineName);
	}

	@Override
	protected void addEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(LircEvent.Actions.SEND_IR_RAW);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				if (isAvailable)
					sendIRCommand(
						(String) event.getValues().get(0),
						(String) event.getValues().get(1),
						(int) event.getValues().get(2));
			}
		});
	}

	protected synchronized void sendIRCommand(String remoteName, String keyName,
			int repeat) {
		try {
			client.sendIrCommand(remoteName, keyName, repeat);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initialize() {
		try {
			client = new TcpLircClient(LIRC_ADDRESS, LIRC_PORT);
			isAvailable = true;
		} catch (IOException e) {
			e.printStackTrace();
			isAvailable = false;
		}
	}

	@Override
	public void updateAvailability() {
		if (client != null) {
			try {
				client.close();
			} catch (IOException e) {
			}
		}
		initialize();
	}

	@Override
	public void stop() {
		super.stop();
		if (isRunning) {
			return;
		}
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void start() {
		super.start();
		if (!isRunning) {
			return;
		}
		initialize();
	}

}
