package de.deeps.sounddriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.platform.win32.WinDef.HWND;

/**
 * @author Deeps
 */

public class SoundDriver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SoundDriver.class);

	private Bass bass;
	private float[] volumes;

	public SoundDriver() throws IOException {
		initialize();
	}

	public void initialize() throws IOException {
		loadVolumeParams();
		bass = Bass.getInstance;
		if (bass.BASS_Init(-1, 44100, 0, new HWND(), new HWND()) == 1) {
			LOGGER.info("Bass-Lib loaded successfully");
		} else {
			throw new IOException(
					"Couldnt initialize Bass-Lib. Returned error code "
							+ bass.BASS_ErrorGetCode());
		}
	}

	private void loadVolumeParams() {
		try {
			volumes = new float[101];
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					SoundDriver.class.getClassLoader().getResourceAsStream(
						"bass-data/VolumeLevels.txt")));
			String line;
			for (int iVolume = 0; iVolume <= 100
					&& (line = reader.readLine()) != null; iVolume++) {
				volumes[iVolume] = Float.parseFloat(line);
			}
			reader.close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}

	public int getVolume() {
		float volume = bass.BASS_GetVolume();
		if (volume == -1.0f) {
			throw new NoSuchElementException(
					"Volume Control not found, or various other error occured");
		}
		return convertVolume(volume);
	}

	public boolean setVolume(int volume) {
		if (volume < 0 || volume > 100) {
			throw new IllegalArgumentException(
					"Volume has to be within [0, 100]");
		}
		return bass.BASS_SetVolume(convertVolume(volume)) == 1;
	}

	private int convertVolume(float volume) {
		float previousDifference = Integer.MAX_VALUE;
		for (int iVolume = 0; iVolume < volumes.length; iVolume++) {
			float difference = Math.abs(volume - volumes[iVolume]);
			if (difference > previousDifference || volume == volumes[iVolume]) {
				return iVolume;
			} else {
				previousDifference = difference;
			}
		}
		throw new NoSuchElementException("Couldnt find matching sound level");
	}

	private float convertVolume(int volume) {
		return volumes[volume];
	}
}
