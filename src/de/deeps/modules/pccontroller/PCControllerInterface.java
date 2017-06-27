package de.deeps.modules.pccontroller;

import java.io.IOException;

/**
 * @author Deeps
 */

public interface PCControllerInterface {

	public static enum Program {
		SPOTIFY
	};

	public abstract void shutdown() throws IOException;

	public abstract void sleep() throws IOException;

	public abstract void setVolume(int level);

	public abstract int getVolume();

	public abstract void runProgram(Program program) throws IOException;

	public abstract void changeOutputDeviceTo(String device) throws IOException;

	public abstract void restartHMS() throws IOException;
}
