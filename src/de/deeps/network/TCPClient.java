package de.deeps.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import de.deeps.Utils;

/**
 * @author Deeps
 */

public abstract class TCPClient implements Runnable {

	private boolean isRunning;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	private Socket socket;

	public TCPClient(InetAddress targetAddress, int targetPort)
			throws IOException {
		this(new Socket(targetAddress, targetPort));
	}

	public TCPClient(Socket socket) throws IOException {
		this.socket = socket;
		dataInputStream = new DataInputStream(socket.getInputStream());
		dataOutputStream = new DataOutputStream(socket.getOutputStream());
		isRunning = true;
		new Thread(this).start();
	}

	public void sendPackage(Object object) {
		try {
			byte[] serializedObject = Utils.serializeObject(object);
			dataOutputStream.writeInt(serializedObject.length);
			dataOutputStream.write(serializedObject);
			dataOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			shutdown();
		}

	}

	@Override
	public void run() {
		while (isRunning) {
			Object receivedObject = receiveObject();
			if (receivedObject != null) {
				handleReceivedObject(receivedObject);
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected Object receiveObject() {
		try {
			if (socket == null || socket.isClosed()) {
				return null;
			}
			int length = dataInputStream.readInt();
			if (length > 0) {
				byte[] serializedObject = new byte[length];
				dataInputStream.readFully(serializedObject);
				return Utils.loadObject(serializedObject);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			shutdown();
		}
		return null;
	}

	protected abstract void handleReceivedObject(Object receivedObject);

	protected abstract void handleShutdown();

	public void shutdown() {
		isRunning = false;
		handleShutdown();
	}

	public InetAddress getConnectedAddress() {
		return socket.getInetAddress();
	}

}
