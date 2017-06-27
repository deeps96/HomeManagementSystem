package de.deeps.radiosocket;

import java.io.IOException;

/**
 * @author Deeps
 */

public class RadioSocket {

	public static final String DEFAULT_REMOTE = "11111";

	public RadioSocket() throws Exception {
		if (!(System.getProperty("os.name").equals("Linux")
				&& System.getProperty("os.arch").equals("arm"))) {
			throw new Exception("Wrong OS! Raspberry required.");
		}
	}

	public void send(String remote, char socketID, boolean turnOn)
			throws Exception {
		try {
			int button = Character.toLowerCase(socketID) - 96;
			int power = turnOn ? 1 : 0;
			Process process = Runtime.getRuntime().exec(
				"sudo /home/pi/raspberry-remote/send " + remote + " " + button
						+ " " + power);
			if (process.waitFor() != 0) {
				throw new Exception(
						"An error occured while executing the radio command");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
