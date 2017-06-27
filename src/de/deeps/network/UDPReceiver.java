package de.deeps.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import de.deeps.Utils;

/**
 * @author Deeps
 */

public abstract class UDPReceiver implements Runnable {

	private final static int BLOCK_SIZE = 1024;

	private boolean isRunning;
	private DatagramSocket receiverSocket;

	public UDPReceiver(int receiverPort) throws SocketException {
		receiverSocket = new DatagramSocket(receiverPort);
		isRunning = true;
		new Thread(this).start();
	}

	@Override
	public void run() {
		while (isRunning) {
			try {
				DatagramPacket receivedPacket = new DatagramPacket(
						new byte[BLOCK_SIZE], BLOCK_SIZE);
				receiverSocket.receive(receivedPacket);
				handleReceivedDatagramPacket(
					Utils.loadObject(receivedPacket.getData()),
					receivedPacket.getAddress());
				Thread.sleep(2L);
			} catch (IOException | InterruptedException
					| ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		receiverSocket.close();
	}

	protected abstract void handleReceivedDatagramPacket(Object loadObject,
			InetAddress address);

	public void shutdown() {
		isRunning = false;
	}

}
