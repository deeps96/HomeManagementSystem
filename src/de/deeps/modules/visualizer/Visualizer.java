package de.deeps.modules.visualizer;

import java.awt.Color;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import ca.uol.aig.fftpack.RealDoubleFFT;
import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.DioderEvent;
import de.deeps.event.moduleevents.PCControllerEvent;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.event.moduleevents.VisualizationEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class Visualizer extends Module implements Runnable {

	private final Color[] colorTable = new Color[] { Color.decode("#000000"),
			Color.decode("#f44336"), Color.decode("#e91e63"),
			Color.decode("#9c27b0"), Color.decode("#673ab7"),
			Color.decode("#3f51b5"), Color.decode("#2196f3"),
			Color.decode("#03a9f4"), Color.decode("#00bcd4"),
			Color.decode("#009688"), Color.decode("#4caf50"),
			Color.decode("#8bc34a"), Color.decode("#cddc39"),
			Color.decode("#ffeb3b"), Color.decode("#ffc107"),
			Color.decode("#ff9800"), Color.decode("#ffffff"),
			Color.decode("#ffffff"), Color.decode("#ffffff") };
	private final float SAMPLE_RATE = 44100.0f;
	private final int BUFFER_SIZE = 512, CHANNEL = 1;
	private final String VIRTUAL_MIXER_NAME = "Line 1 (Virtual Audio Cable)";

	private byte[] buffer;
	private int blackCounter;
	private RealDoubleFFT fft;
	private StaticQueue lastMaxMagnitudes;
	private TargetDataLine line;

	public Visualizer(EventCollector collector, String machineName) {
		super(collector, Name.VISUALIZER, machineName);
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
				return event.getAction().equals(
					VisualizationEvent.Actions.STOP_VISUALIZATION_REQUEST);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				collector.addEvent(
					PCControllerEvent.createChangeOutputDeviceRequest(
						Source.VISUALIZER,
						"Realtek Digital Output(Optical)"));
				stop();
			}
		});
	}

	private void addStartVisualizationListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(
					VisualizationEvent.Actions.START_VISUALIZATION_REQUEST);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				collector.addEvent(
					PCControllerEvent.createChangeOutputDeviceRequest(
						Source.VISUALIZER,
						"Line 1"));
				start();
			}
		});
	}

	@Override
	protected void initialize() {
		try {
			line = getVirtualLine();
			if (line != null) {
				line.open();
			}
			fft = new RealDoubleFFT(BUFFER_SIZE / 2);
			isAvailable = true;
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			isAvailable = false;
		}
	}

	@Override
	public void updateAvailability() {
		initialize();
	}

	@Override
	protected void start() {
		super.start();
		if (!isRunning) {
			return;
		}
		blackCounter = 0;
		lastMaxMagnitudes = new StaticQueue();
		buffer = new byte[BUFFER_SIZE];
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.VISUALIZER,
				"raspberry",
				DioderEvent.createOnRequest(Source.VISUALIZER)));
		new Thread(this).start();
	}

	@Override
	public void stop() {
		super.stop();
		if (!isRunning && line != null && line.isOpen()) {
			// line.close(); //not supported on all lines
			line.stop();
		}
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.VISUALIZER,
				"raspberry",
				DioderEvent.createOffRequest(Source.VISUALIZER)));
	}

	@Override
	public void run() {
		// line.open(); reopening not supported on all lines
		line.flush();
		line.start();
		while (line.isOpen() && isRunning) {
			line.read(buffer, 0, buffer.length);
			preProcess(buffer);
			try {
				Thread.sleep(0L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void preProcess(byte[] raw) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(raw);
		double[] buffer = new double[raw.length / 2];
		int counter = 0;
		while (byteBuffer.hasRemaining()) {
			buffer[counter] = byteBuffer.getShort() / 32768.0d;
			counter++;
		}
		fft.ft(buffer);

		double r, i;
		double[] magnitudes = new double[buffer.length / 2];
		for (int iMagnitude = 0; iMagnitude < magnitudes.length; iMagnitude++) {
			r = buffer[2 * iMagnitude];
			i = buffer[2 * iMagnitude + 1];

			magnitudes[iMagnitude] = Math.sqrt(Math.pow(i, 2) + Math.pow(r, 2));
		}
		visualize(magnitudes);
	}

	private void visualize(double[] magnitudes) {
		int maxMagnitude = (int) magnitudes[0];
		for (double magnitude : magnitudes) {
			maxMagnitude = (int) Math.max(maxMagnitude, magnitude);
		}
		lastMaxMagnitudes.add(maxMagnitude);
		Color nextColor = Color.BLACK;
		int max = lastMaxMagnitudes.getMax();
		int min = lastMaxMagnitudes.getMin();
		int colorIndex = (int) ((maxMagnitude - min)
				/ ((double) (max - min) / (colorTable.length)));
		colorIndex = Math.max(colorIndex, 0);
		colorIndex = Math.min(colorIndex, colorTable.length - 1);
		nextColor = colorTable[colorIndex];

		if (colorIndex == 0) {
			blackCounter++;
			if (blackCounter < 10) {
				return;
			}
		} else {
			blackCounter = 0;
		}

		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.VISUALIZER,
				"raspberry",
				DioderEvent.createShowColorRequest(
					Source.VISUALIZER,
					nextColor,
					false)));
	}

	private TargetDataLine getVirtualLine() throws LineUnavailableException {
		Mixer mixer = null;
		for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
			if (mixerInfo.getName().equals(VIRTUAL_MIXER_NAME)) {
				mixer = AudioSystem.getMixer(mixerInfo);
			}
		}
		if (mixer == null) {
			return null;
		}
		AudioFormat audioFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 16, CHANNEL,
				2 * CHANNEL, SAMPLE_RATE, true);
		return (TargetDataLine) mixer
				.getLine(new DataLine.Info(TargetDataLine.class, audioFormat));
	}

	private class StaticQueue {
		private final int MAGNITUDE_HISTORY_LENGTH = 1500;

		private int[] data;
		private int nextIndex;

		public StaticQueue() {
			data = new int[MAGNITUDE_HISTORY_LENGTH];
			nextIndex = 0;
		}

		public void add(int value) {
			data[nextIndex % MAGNITUDE_HISTORY_LENGTH] = value;
			nextIndex++;
		}

		public int getMax() {
			int max = data[0];
			for (int iValue = 0; iValue < Math
					.min(nextIndex, MAGNITUDE_HISTORY_LENGTH); iValue++) {
				max = Math.max(max, data[iValue]);
			}
			return max;
		}

		public int getMin() {
			int min = data[0];
			for (int iValue = 0; iValue < Math
					.min(nextIndex, MAGNITUDE_HISTORY_LENGTH); iValue++) {
				min = Math.min(min, data[iValue]);
			}
			return min;
		}

	}
}
