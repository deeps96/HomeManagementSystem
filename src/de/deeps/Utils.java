package de.deeps;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Enumeration;

/**
 * @author Deeps
 */

public class Utils {

	public static byte[] serializeObject(Object object) throws IOException {
		ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
		ObjectOutputStream objectOutput = new ObjectOutputStream(
				byteArrayOutput);
		objectOutput.writeObject(object);
		objectOutput.flush();
		objectOutput.close();
		return byteArrayOutput.toByteArray();
	}

	public static Object loadObject(byte[] serializedObject)
			throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(
				serializedObject);
		ObjectInputStream objectInput = new ObjectInputStream(byteArrayInput);
		Object object = objectInput.readObject();
		return object;
	}

	public static InetAddress getLocalAddress() {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
					.getNetworkInterfaces();
			InetAddress address = null;
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface ni = networkInterfaces.nextElement();
				Enumeration<InetAddress> nias = ni.getInetAddresses();
				while (nias.hasMoreElements()) {
					InetAddress ia = nias.nextElement();
					if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress()
							&& ia instanceof Inet4Address) {
						address = ia;
					}
				}
			}
			return address;
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] readBytesFromFile(File file)
			throws IOException, URISyntaxException {
		byte[] content = new byte[(int) file.length()];
		FileInputStream fileInputStream = new FileInputStream(file);
		BufferedInputStream byteInputStream = new BufferedInputStream(
				fileInputStream);
		byteInputStream.read(content, 0, content.length);
		fileInputStream.close();
		byteInputStream.close();
		return content;
	}

	public static String readStringFromFile(File file) throws IOException {
		String line;
		StringBuffer content = new StringBuffer();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file)));
		while ((line = reader.readLine()) != null) {
			content.append(line + "\r\n");
		}
		reader.close();
		return content.toString();
	}

	public static void writeStringToFile(String content, File file)
			throws IOException, URISyntaxException {
		writeStringToFile(content, file, false);
	}

	public static void writeStringToFile(String content, File file,
			boolean append) throws IOException, URISyntaxException {
		PrintWriter writer = new PrintWriter(new FileWriter(file, append));
		writer.write(content);
		writer.flush();
		writer.close();
	}
}
