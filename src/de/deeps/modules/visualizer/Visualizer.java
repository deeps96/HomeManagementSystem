package de.deeps.modules.visualizer;

import java.awt.Color;
import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.util.fft.FFT;
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

public class Visualizer extends Module implements AudioProcessor {

	private final double SILENCE_THRESHOLD_IN_DB = -80.0d;
	private final int BUFFER_SIZE = 512, OVERLAP = BUFFER_SIZE / 2;
	private final String VIRTUAL_MIXER_NAME = "Line 1 (Virtual Audio Cable)";

	private AudioFormat audioFormat;
	private FFT fft = new FFT(BUFFER_SIZE);
	private FixedList[] colorLists;
	private float[] amplitudes = new float[BUFFER_SIZE / 2];
	private NonClosingDispatcher audioDispatcher;
	private SilenceDetector silenceDetector;
	private StaticQueue[] colorQueues;
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
				line.open(getAudioFormat(), BUFFER_SIZE);
				initializeAudioDispatcher();
			}
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
		line.flush();
		line.start();
		initializeColorLists();
		initializeColorQueues();
		new Thread(audioDispatcher).start();
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.VISUALIZER,
				"raspberry",
				DioderEvent.createOnRequest(Source.VISUALIZER)));
	}

	private void initializeColorQueues() {
		colorQueues = new StaticQueue[3]; // RGB
		for (int iColor = 0; iColor < colorLists.length; iColor++) {
			colorQueues[iColor] = new StaticQueue();
		}
	}

	private void initializeColorLists() {
		colorLists = new FixedList[3]; // RGB
		for (int iColor = 0; iColor < colorLists.length; iColor++) {
			colorLists[iColor] = new FixedList();
		}
	}

	private void initializeAudioDispatcher() {
		AudioInputStream inputStream = new AudioInputStream(line);
		JVMAudioInputStream jvmInputStream = new JVMAudioInputStream(
				inputStream);
		audioDispatcher = new NonClosingDispatcher(jvmInputStream, BUFFER_SIZE,
				OVERLAP);
		silenceDetector = new SilenceDetector();
		audioDispatcher.addAudioProcessor(silenceDetector);
		audioDispatcher.addAudioProcessor(this);
	}

	@Override
	public boolean process(AudioEvent audioEvent) {
		Color nextColor = Color.BLACK;
		if (silenceDetector.currentSPL() > SILENCE_THRESHOLD_IN_DB) {
			doAudioFFTWith(audioEvent);
			nextColor = getColorFromAmplitudes();
		}
		sendColorToPi(nextColor);
		return true;
	}

	private Color getColorFromAmplitudes() {
		int[] colorCode = new int[3];
		float[] maxAmplitudes = getMaxAmplitudes();

		for (int iColorCode = 0; iColorCode < colorCode.length; iColorCode++) {
			colorQueues[iColorCode].add(maxAmplitudes[iColorCode]);

			colorLists[iColorCode]
					.addFirstRemoveLast(maxAmplitudes[iColorCode]);
			float avg = colorLists[iColorCode].getAverage();
			if (maxAmplitudes[iColorCode] < avg) {
				maxAmplitudes[iColorCode] = avg;
			} else {
				colorLists[iColorCode].setAllTo(maxAmplitudes[iColorCode]);
			}
			maxAmplitudes[iColorCode] /= colorQueues[iColorCode].getMax();
			maxAmplitudes[iColorCode] *= 255.0f * 1.1f; // 10% boost (brighter)
			colorCode[iColorCode] = (int) Math
					.max(maxAmplitudes[iColorCode], 0.0f);
			colorCode[iColorCode] = (int) Math
					.min(maxAmplitudes[iColorCode], 255.0f);
		}
		// bass = green middle = blue high = red
		return new Color(colorCode[1], colorCode[2], colorCode[0]);
	}

	private float[] getMaxAmplitudes() {
		float[] maxAmplitudes = new float[3];
		for (int iAmplitude = 0; iAmplitude < (amplitudes.length / 3)
				* 3; iAmplitude++) {
			maxAmplitudes[iAmplitude / (amplitudes.length / 3)] = Math.max(
				maxAmplitudes[iAmplitude / (amplitudes.length / 3)],
				amplitudes[iAmplitude]);
		}
		return maxAmplitudes;
	}

	private void doAudioFFTWith(AudioEvent audioEvent) {
		float[] audioFloatBuffer = audioEvent.getFloatBuffer();
		float[] transformbuffer = new float[BUFFER_SIZE];
		System.arraycopy(
			audioFloatBuffer,
			0,
			transformbuffer,
			0,
			audioFloatBuffer.length);
		fft.forwardTransform(transformbuffer);
		fft.modulus(transformbuffer, amplitudes);
	}

	@Override
	public void processingFinished() {
	}

	private void sendColorToPi(Color nextColor) {
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.VISUALIZER,
				"raspberry",
				DioderEvent.createShowColorRequest(
					Source.VISUALIZER,
					nextColor,
					false)));
	}

	@Override
	public void stop() {
		super.stop();
		if (!isRunning && line != null && line.isOpen()) {
			line.stop();
		}
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.VISUALIZER,
				"raspberry",
				DioderEvent.createOffRequest(Source.VISUALIZER)));
	}

	private AudioFormat getAudioFormat() {
		if (audioFormat == null) {
			audioFormat = new AudioFormat(44100.0f, 16, 1, true, false);
		}
		return audioFormat;
	}

	private TargetDataLine getVirtualLine() throws LineUnavailableException {
		Mixer mixer = getMixer();
		if (mixer == null) {
			return null;
		}
		return (TargetDataLine) mixer.getLine(
			new DataLine.Info(TargetDataLine.class, getAudioFormat()));
	}

	private Mixer getMixer() {
		Mixer mixer = null;
		for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
			if (mixerInfo.getName().equals(VIRTUAL_MIXER_NAME)) {
				mixer = AudioSystem.getMixer(mixerInfo);
			}
		}
		return mixer;
	}

	private class FixedList {
		private final int MAX_LENGTH = 10;

		private LinkedList<Float> data;

		public FixedList() {
			data = new LinkedList<Float>();
			setAllTo(0.0f);
		}

		public void addFirstRemoveLast(float newValue) {
			data.removeLast();
			data.addFirst(newValue);
		}

		public float getAverage() {
			if (data.isEmpty()) {
				return 0.0f;
			}
			float avg = 0.0f;
			for (float f : data) {
				avg += f;
			}
			return avg /= data.size();
		}

		public void setAllTo(float value) {
			data.clear();
			for (int i = 0; i < MAX_LENGTH; i++) {
				data.add(value);
			}
		}
	}

	private class StaticQueue {
		private final int MAGNITUDE_HISTORY_LENGTH = 200;

		private float[] data;
		private int nextIndex;

		public StaticQueue() {
			data = new float[MAGNITUDE_HISTORY_LENGTH];
			nextIndex = 0;
		}

		public void add(float value) {
			data[nextIndex % MAGNITUDE_HISTORY_LENGTH] = value;
			nextIndex++;
		}

		public float getMax() {
			float max = data[0];
			for (int iValue = 0; iValue < Math
					.min(nextIndex, MAGNITUDE_HISTORY_LENGTH); iValue++) {
				max = Math.max(max, data[iValue]);
			}
			return max;
		}

		public float getMin() {
			float min = data[0];
			for (int iValue = 0; iValue < Math
					.min(nextIndex, MAGNITUDE_HISTORY_LENGTH); iValue++) {
				min = Math.min(min, data[iValue]);
			}
			return min;
		}

	}

}
