package de.deeps.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Deeps
 */

public abstract class TCPServer implements Runnable {

	private boolean isRunning;
	private List<TCPClient> connections;
	private ServerSocket serverSocket;

	public TCPServer(int receiverPort) throws IOException {
		serverSocket = new ServerSocket(receiverPort);
		connections = new LinkedList<>();
		isRunning = true;
		new Thread(this).start();
	}

	@Override
	public void run() {
		TCPServer instance = this;
		while (isRunning) {
			try {
				connections.add(new TCPClient(serverSocket.accept()) {
					@Override
					protected void handleReceivedObject(Object receivedObject) {
						instance.handleReceivedObject(receivedObject);
					}

					@Override
					protected void handleShutdown() {
						connections.remove(this);
						onTCPClientDisconnect(this);
					}
				});
				onNewTCPClientConnect(connections.get(connections.size() - 1));
				Thread.sleep(50L);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendPackage(Object object) throws IOException {
		for (TCPClient connection : connections) {
			connection.sendPackage(object);
		}
	}

	public void shutdown() {
		isRunning = false;
		for (TCPClient connection : connections) {
			connection.shutdown();
		}
	}

	protected abstract void handleReceivedObject(Object receivedObject);

	protected abstract void onNewTCPClientConnect(TCPClient client);

	protected abstract void onTCPClientDisconnect(TCPClient client);

}
