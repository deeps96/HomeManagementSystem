package de.deeps.modules.lirc;

/**
 * @author Deeps
 */

public interface IRRemoteInterface {
	public abstract String mapButtonToKeystring(String button);

	public abstract String getRemoteName();
}
