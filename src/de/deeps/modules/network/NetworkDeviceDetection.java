
package de.deeps.modules.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.moduleevents.NetworkDeviceDetectionEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class NetworkDeviceDetection extends Module {

	private final int TIMEOUT = 5000, THRESHOLD = 60000;

	private HashMap<InetAddress, Long> interestingAddresses;

	public NetworkDeviceDetection(EventCollector collector,
			String machineName) {
		super(collector, Name.NETWORK_DEVICE_DETECTION, machineName);
	}

	@Override
	protected void addEventListener() {
	}

	@Override
	protected void initialize() {
		try {
			initializeInterestingAddresses();
			isAvailable = true;
			start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			isAvailable = false;
		}
	}

	private void initializeInterestingAddresses() throws UnknownHostException {
		interestingAddresses = new HashMap<>();
		interestingAddresses.put(
			InetAddress.getByAddress(
				new byte[] { (byte) 192, (byte) 168, (byte) 178, (byte) 24 }),
			0L);
	}

	@Override
	protected void start() {
		super.start();
		if (!isRunning) {
			return;
		}
		for (InetAddress address : interestingAddresses.keySet()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					boolean isReachable = false;
					while (isRunning) {
						try {
							isReachable = address.isReachable(TIMEOUT);
							if (interestingAddresses.get(address) == 0L
									&& isReachable) {
								interestingAddresses.put(
									address,
									System.currentTimeMillis());
								collector.addEvent(
									NetworkDeviceDetectionEvent
											.createDeviceConnected(
												Source.NETWORK_DEVICE_DETECTION,
												address.getHostAddress()));
							} else if (interestingAddresses.get(address) > 0L
									&& System.currentTimeMillis()
											- interestingAddresses
													.get(address) > THRESHOLD
									&& !isReachable) {
								interestingAddresses.put(address, 0L);
								collector.addEvent(
									NetworkDeviceDetectionEvent
											.createDeviceDisconnected(
												Source.NETWORK_DEVICE_DETECTION,
												address.getHostAddress()));
							}
							Thread.sleep(TIMEOUT);
						} catch (InterruptedException | IOException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}

	@Override
	public void updateAvailability() {
	}

}
