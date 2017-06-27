package de.deeps.modules.pccontroller;

import java.io.IOException;

/**
 * @author Deeps
 */

public class LinuxController implements PCControllerInterface {

	public LinuxController() throws Exception {
		if (!(System.getProperty("os.name").equals("Linux"))) {
			throw new Exception("Wrong OS! Linux required.");
		}
	}

	@Override
	public void shutdown() throws IOException {
		Runtime.getRuntime().exec("sudo halt");
	}

	@Override
	public void sleep() throws IOException {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public void setVolume(int level) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public int getVolume() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public void runProgram(Program program) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public void changeOutputDeviceTo(String device) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public void restartHMS() throws IOException {
		Runtime.getRuntime().exec(".//restartHMS");
	}

}
