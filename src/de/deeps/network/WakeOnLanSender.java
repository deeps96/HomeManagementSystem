package de.deeps.network;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Deeps
 */

public class WakeOnLanSender {

	private static final int WOL_PORT = 9;

	private UDPSender sender;

	public WakeOnLanSender() throws SocketException, UnknownHostException {
		sender = new UDPSender(WOL_PORT);
	}

	public void sendWakeOnLanTo(byte[] targetMAC) {
		byte[] bytes = new byte[6 + 16 * targetMAC.length];
		for (int i = 0; i < 6; i++) {
			bytes[i] = (byte) 0xff;
		}
		for (int i = 6; i < bytes.length; i += targetMAC.length) {
			System.arraycopy(targetMAC, 0, bytes, i, targetMAC.length);
		}
		try {
			sender.sendRaw(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
