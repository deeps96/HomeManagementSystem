package de.deeps.modules.diodercontrol;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;

import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;
import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import de.deeps.event.Event;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.DioderEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class DioderController extends Module implements Runnable {

	private final float SMOOTHING_FACTOR = 0.7f;
	private final int RED = 0, GREEN = 1, BLUE = 2;

	private Color nextColor;
	private GpioPinPwmOutput[] pins;
	private PCA9685GpioProvider provider;

	public DioderController(EventCollector collector, String machineName) {
		super(collector, Name.DIODER_CONTROL, machineName);
	}

	@Override
	protected void addEventListener() {
		addOffEventListener();
		addOnEventListener();
		addShowColorEventListener();
	}

	private void addOnEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(DioderEvent.Actions.ON);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				start();
			}
		});
	}

	private void addShowColorEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(DioderEvent.Actions.SHOW_COLOR);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				Color color = (Color) event.getValues().get(0);
				boolean smooth = (Boolean) event.getValues().get(1);
				if (!smooth || color.equals(Color.WHITE)
						|| color.equals(Color.BLACK)) {
					nextColor = color;
					return;
				}
				nextColor = color;
				nextColor = new Color(
						(int) (nextColor.getRed() * (1.0f - SMOOTHING_FACTOR)
								+ color.getRed() * SMOOTHING_FACTOR),
						(int) (nextColor.getGreen() * (1.0f - SMOOTHING_FACTOR)
								+ color.getGreen() * SMOOTHING_FACTOR),
						(int) (nextColor.getBlue() * (1.0f - SMOOTHING_FACTOR)
								+ color.getBlue() * SMOOTHING_FACTOR));
			}
		});
	}

	private void addOffEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(DioderEvent.Actions.OFF);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				stop();
			}
		});
	}

	@Override
	public void run() {
		Color currentColor = null;
		while (isRunning) {
			if (!nextColor.equals(currentColor)) {
				currentColor = nextColor;
				showColor(currentColor);
			}
			try {
				Thread.sleep(0L);
			} catch (InterruptedException e) {
			}
		}
	}

	private void showColor(Color color) {
		if (color.equals(Color.BLACK)) {
			provider.reset();
			return;
		}
		int redPWM = (int) (4095.0d / 255.0d * color.getRed());
		int greenPWM = (int) (4095.0d / 255.0d * color.getGreen());
		int bluePWM = (int) (4095.0d / 255.0d * color.getBlue());

		if (redPWM > 0) {
			pins[RED].setPwm(redPWM);
		}
		if (greenPWM > 0) {
			pins[GREEN].setPwm(greenPWM);
		}
		if (bluePWM > 0) {
			pins[BLUE].setPwm(bluePWM);
		}
	}

	@Override
	protected void initialize() {
		if (!(System.getProperty("os.name").equals("Linux")
				&& System.getProperty("os.arch").equals("arm"))) {
			isAvailable = false;
			return;
		}
		nextColor = Color.BLACK;
		try {
			initializePins();
			isAvailable = true;
		} catch (UnsupportedBusNumberException | IOException e) {
			isAvailable = false;
			e.printStackTrace();
		}

	}

	private void initializePins()
			throws UnsupportedBusNumberException, IOException {
		pins = new GpioPinPwmOutput[3];
		// bus id = i2cdetect -y _ <-- this number
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		// address = i2cdetect -y 1 <-- the result of this cmd
		// targetFreq = 1 kHz / 20 (from https://gabriel-lg.github.io/Ambioder/)
		provider = new PCA9685GpioProvider(bus, 0x40, new BigDecimal("200.0"));
		GpioPinPwmOutput[] allPins = provisionPwmOutputs(provider);
		pins[RED] = allPins[2];
		pins[GREEN] = allPins[0];
		pins[BLUE] = allPins[1];
		showColor(Color.BLACK);
	}

	private GpioPinPwmOutput[] provisionPwmOutputs(
			final PCA9685GpioProvider gpioProvider) {
		GpioController gpio = GpioFactory.getInstance();
		GpioPinPwmOutput myOutputs[] = {
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_00,
					"Pulse 00"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_01,
					"Pulse 01"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_02,
					"Pulse 02"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_03,
					"Pulse 03"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_04,
					"Pulse 04"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_05,
					"Pulse 05"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_06,
					"Pulse 06"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_07,
					"Pulse 07"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_08,
					"Pulse 08"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_09,
					"Pulse 09"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_10,
					"Always ON"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_11,
					"Always OFF"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_12,
					"Servo pulse MIN"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_13,
					"Servo pulse NEUTRAL"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_14,
					"Servo pulse MAX"),
				gpio.provisionPwmOutputPin(
					gpioProvider,
					PCA9685Pin.PWM_15,
					"not used") };
		return myOutputs;
	}

	@Override
	public void stop() {
		super.stop();
		if (provider != null) {
			try {
				Thread.sleep(200L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			showColor(Color.BLACK);
		}
	}

	@Override
	protected void start() {
		super.start();
		new Thread(this).start();
	}

	@Override
	public void updateAvailability() {

	}

}
