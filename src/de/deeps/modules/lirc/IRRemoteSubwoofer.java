package de.deeps.modules.lirc;

/**
 * @author Deeps
 */

public class IRRemoteSubwoofer implements IRRemoteInterface {

	public static final String REMOTE_NAME = "subwoofer";

	public static enum Button {
		POWER_TOGGLE,
		MUTE,
		VOLUME_DOWN,
		VOLUME_UP,
		BALANCE_LEFT,
		BALANCE_RIGHT,
		TREBLE_DOWN,
		TREBLE_UP,
		BASS_DOWN,
		BASS_UP,
		SUBWOOFER_DOWN,
		SUBWOOFER_UP,
		INPUT_CD,
		INPUT_PC,
		INPUT_DIGITAL
	}

	@Override
	public String mapButtonToKeystring(String keyName) {
		Button button = Button.valueOf(keyName);
		switch (button) {
			case BALANCE_LEFT:
				return "KEY_LEFT";
			case BALANCE_RIGHT:
				return "KEY_RIGHT";
			case BASS_DOWN:
				return "KEY_4";
			case BASS_UP:
				return "KEY_3";
			case INPUT_CD:
				return "KEY_CD";
			case INPUT_DIGITAL:
				return "KEY_7";
			case INPUT_PC:
				return "KEY_PC";
			case MUTE:
				return "KEY_MUTE";
			case POWER_TOGGLE:
				return "KEY_POWER";
			case SUBWOOFER_DOWN:
				return "KEY_6";
			case SUBWOOFER_UP:
				return "KEY_5";
			case TREBLE_DOWN:
				return "KEY_2";
			case TREBLE_UP:
				return "KEY_1";
			case VOLUME_DOWN:
				return "KEY_VOLUMEDOWN";
			case VOLUME_UP:
				return "KEY_VOLUMEUP";
		}
		return null;
	}

	@Override
	public String getRemoteName() {
		return REMOTE_NAME;
	}

}
