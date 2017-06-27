package de.deeps.modules.ambilight;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import de.androidpit.colorthief.ColorThief;
import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.AmbilightEvent;
import de.deeps.event.moduleevents.DioderEvent;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class Ambilight extends Module implements Runnable {

	private BufferedImage currentScreenshot;
	private Rectangle screenSize;
	private Robot robot;

	public Ambilight(EventCollector collector, String machineName) {
		super(collector, Name.AMBILIGHT, machineName);
	}

	@Override
	public void run() {
		BufferedImage currentImage;
		while (isRunning) {
			currentImage = copyImage(currentScreenshot);
			if (currentImage == null) {
				try {
					Thread.sleep(10L);
				} catch (InterruptedException e) {
				}
				continue;
			}
			Color dominantColor = getDominantColor(currentImage);
			if (dominantColor == null) {
				continue;
			}
			collector.addEvent(
				TCPEvent.createSendEventToMachineName(
					Source.AMBILIGHT,
					"raspberry",
					DioderEvent.createShowColorRequest(
						Source.AMBILIGHT,
						dominantColor,
						true)));
			try {
				Thread.sleep(0L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized BufferedImage copyImage(BufferedImage image) {
		if (image == null) {
			return null;
		}
		ColorModel cm = image.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	private Color getDominantColor(BufferedImage image) {
		int[] colorValues = ColorThief.getColor(image, 10, false);
		if (colorValues == null) {
			return null;
		}
		return new Color(colorValues[0], colorValues[1], colorValues[2]);
	}

	@Override
	protected void start() {
		super.start();
		if (!isRunning) {
			return;
		}
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.AMBILIGHT,
				"raspberry",
				DioderEvent.createOnRequest(Source.AMBILIGHT)));
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isRunning) {
					currentScreenshot = robot.createScreenCapture(screenSize);
					try {
						Thread.sleep(0L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		new Thread(this).start();
	}

	@Override
	public void stop() {
		super.stop();
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.AMBILIGHT,
				"raspberry",
				DioderEvent.createOffRequest(Source.VISUALIZER)));
	}

	@Override
	protected void addEventListener() {
		addStartVisualizationListener();
		addStopVisualizationListener();
	}

	private void addStopVisualizationListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(AmbilightEvent.Actions.STOP_AMBILIGHT_REQUEST);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				stop();
			}
		});
	}

	private void addStartVisualizationListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(AmbilightEvent.Actions.START_AMBILIGHT_REQUEST);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				start();
			}
		});
	}

	@Override
	protected void initialize() {
		try {
			robot = new Robot();
			Dimension screenDimension = Toolkit.getDefaultToolkit()
					.getScreenSize();
			screenSize = new Rectangle((int) screenDimension.getWidth() / 6,
					(int) screenDimension.getHeight() / 6,
					(int) screenDimension.getWidth() / 3 * 2,
					(int) screenDimension.getHeight() / 3 * 2);
			isAvailable = true;
		} catch (AWTException e) {
			System.err.println(e.getMessage());
			isAvailable = false;
		}
	}

	@Override
	public void updateAvailability() {
	}

}
