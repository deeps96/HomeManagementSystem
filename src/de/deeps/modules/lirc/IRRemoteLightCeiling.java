package de.deeps.modules.lirc;

/**
 * @author Deeps
 */

public class IRRemoteLightCeiling implements IRRemoteInterface {

	public static final String REMOTE_NAME = "light_ceiling";

	public static enum Button {
		POWER_ON,
		POWER_OFF,
		BRIGHTNESS_UP,
		BRIGHTNESS_DOWN,
		CANDLE_MODE,
		LIGHT_BULB_MODE,
		SUN_MODE,
		ICE_MODE,
		EVENING_MODE,
		SLEEP_MODE,
		READING_MODE,
		MEDITATION_MODE,
		MORNING_MODE,
		CIRCLE_MODE,
		PARTY_MODE,
		VACATION_MODE,
		FOREST_MODE,
		OCEAN_MODE,
		FIRE_MODE,
		LOVE_MODE,
		RED,
		YELLOW,
		GREEN,
		BLUE,
		PASTELL,
		PLAIN
	}

	@Override
	public String mapButtonToKeystring(String keyName) {
		Button button = Button.valueOf(keyName);
		switch (button) {
			case POWER_ON:
				return "KEY_POWER";
			case POWER_OFF:
				return "KEY_POWER2";
			case BRIGHTNESS_UP:
				return "KEY_BRIGHTNESSUP";
			case BRIGHTNESS_DOWN:
				return "KEY_BRIGHTNESSDOWN";
			case CANDLE_MODE:
				return "KEY_0";
			case LIGHT_BULB_MODE:
				return "KEY_1";
			case SUN_MODE:
				return "KEY_2";
			case ICE_MODE:
				return "KEY_3";
			case EVENING_MODE:
				return "KEY_4";
			case SLEEP_MODE:
				return "KEY_5";
			case READING_MODE:
				return "KEY_6";
			case MEDITATION_MODE:
				return "KEY_7";
			case MORNING_MODE:
				return "KEY_8";
			case CIRCLE_MODE:
				return "KEY_9";
			case PARTY_MODE:
				return "KEY_F1";
			case VACATION_MODE:
				return "KEY_F2";
			case FOREST_MODE:
				return "KEY_F3";
			case OCEAN_MODE:
				return "KEY_F4";
			case FIRE_MODE:
				return "KEY_F5";
			case LOVE_MODE:
				return "KEY_F6";
			case RED:
				return "KEY_F7";
			case YELLOW:
				return "KEY_F8";
			case GREEN:
				return "KEY_F9";
			case BLUE:
				return "KEY_F10";
			case PASTELL:
				return "KEY_F11";
			case PLAIN:
				return "KEY_F12";
		}
		return null;
	}

	@Override
	public String getRemoteName() {
		return REMOTE_NAME;
	}

}
