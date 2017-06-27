package de.deeps.modules.network;

import java.net.InetAddress;
import java.net.SocketException;

import de.deeps.Utils;
import de.deeps.event.Event;
import de.deeps.event.EventCollector;
import de.deeps.modules.Module;
import de.deeps.network.UDPReceiver;

/**
 * @author Deeps
 */

public class UDPEventReceiver extends Module {

	public static final int UDP_EVENT_RECEIVER_PORT = 6666;

	private InetAddress localAddress;
	private UDPReceiver receiver;

	public UDPEventReceiver(EventCollector collector, String machineName) {
		super(collector, Name.UDP_EVENT_RECEIVER, machineName);
	}

	@Override
	protected void initialize() {
		try {
			localAddress = Utils.getLocalAddress();
			receiver = new UDPReceiver(UDP_EVENT_RECEIVER_PORT) {
				@Override
				protected void handleReceivedDatagramPacket(Object object,
						InetAddress address) {
					if (object instanceof Event
							&& !address.equals(localAddress)) {
						collector.addEvent((Event<?>) object);
					}
				}
			};
			isAvailable = true;
		} catch (SocketException e) {
			e.printStackTrace();
			isAvailable = false;
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

	@Override
	public void updateAvailability() {
		if (receiver != null) {
			receiver.shutdown();
		}
		initialize();
	}

	@Override
	public void stop() {
		super.stop();
		if (!isRunning && receiver != null) {
			receiver.shutdown();
		}
	}

	@Override
	protected void addEventListener() {
	}

}
