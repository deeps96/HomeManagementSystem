package de.deeps.sounddriver;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;

/**
 * @author Deeps
 */

public interface Bass extends Library {

	Bass getInstance = (Bass) Native.loadLibrary("bass", Bass.class);

	int BASS_Init(int device, int frequency, int flags, HWND win, HWND classIdentifier);

	int BASS_ErrorGetCode();

	float BASS_GetVolume();

	int BASS_SetVolume(float volume);

}
