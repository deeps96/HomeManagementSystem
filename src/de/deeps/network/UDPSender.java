package de.deeps.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.deeps.Utils;

/**
 * @author Deeps
 */

public class UDPSender {

	private DatagramSocket senderSocket;
	private int targetPort;
	private InetAddress targetAddress;

	public UDPSender(InetAddress targetAddress, int targetPort)
			throws SocketException {
		senderSocket = new DatagramSocket();
		senderSocket.setBroadcast(true);
		this.targetAddress = targetAddress;
		this.targetPort = targetPort;
	}

	public UDPSender(int targetPort)
			throws SocketException, UnknownHostException {
		this(getBroadcastAddress(), targetPort);
	}

	private static InetAddress getBroadcastAddress()
			throws UnknownHostException {
		byte[] address = Utils.getLocalAddress().getAddress();
		address[3] = (byte) 255;
		return InetAddress.getByAddress(address);
	}

	public void sendObject(Object object) throws IOException {
		byte[] serializedObject = Utils.serializeObject(object);
		DatagramPacket packet = new DatagramPacket(serializedObject,
				serializedObject.length, targetAddress, targetPort);
		senderSocket.send(packet);
	}

	public void sendRaw(byte[] raw) throws IOException {
		DatagramPacket packet = new DatagramPacket(raw, raw.length,
				targetAddress, targetPort);
		senderSocket.send(packet);
	}

	public void shutdown() {
		senderSocket.close();
	}
}
