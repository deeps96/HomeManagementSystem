package de.deeps.modules.pccontroller;

import java.io.File;
import java.io.IOException;

import de.deeps.sounddriver.SoundDriver;

/**
 * @author Deeps
 */

public class WindowsController implements PCControllerInterface {

	private SoundDriver soundDriver;

	public WindowsController() throws Exception {
		if (!(System.getProperty("os.name").contains("Windows"))) {
			throw new Exception("Wrong OS! Windows required.");
		}
		soundDriver = new SoundDriver();
	}

	@Override
	public void shutdown() throws IOException {
		Runtime.getRuntime().exec("shutdown /p /f");
	}

	@Override
	public void sleep() throws IOException {
		Runtime.getRuntime().exec("shutdown /h /f");
	}

	@Override
	public void setVolume(int level) {
		soundDriver.setVolume(level);
	}

	@Override
	public int getVolume() {
		return soundDriver.getVolume();
	}

	@Override
	public void runProgram(Program program) throws IOException {
		String applicationPath = "";
		switch (program) {
			case SPOTIFY:
				applicationPath = "\"C:\\Users\\Deeps\\AppData\\Roaming\\Spotify\\Spotify.exe\"";
				break;
		}
		Runtime.getRuntime().exec("cmd /c call " + applicationPath);
	}

	@Override
	public void changeOutputDeviceTo(String device) throws IOException {
		Runtime.getRuntime()
				.exec("nircmd setdefaultsounddevice \"" + device + "\"");
	}

	@Override
	public void restartHMS() throws IOException {
		new ProcessBuilder()
				.directory(
					new File(
							"E:\\Dropbox\\Private\\PC-Synchronisation\\WorkSpace\\HomeManagementSystem"))
				.command("cmd.exe", "/C", "HMS_start.vbs").start();
	}
}
