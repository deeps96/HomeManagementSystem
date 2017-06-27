package de.deeps.modules.clapdetection;

import javax.sound.sampled.LineUnavailableException;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import de.deeps.event.Event;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.ClapEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class ClapDetection extends Module implements Runnable {

	private final int DOUBLE_CLAP_DELAY = 1000;

	private AudioDispatcher audioDispatcher;
	private int claps;

	public ClapDetection(EventCollector collector, String machineName) {
		super(collector, Name.CLAP_DETECTION, machineName);
	}

	@Override
	protected void initialize() {
		try {
			audioDispatcher = AudioDispatcherFactory
					.fromDefaultMicrophone(22050, 1024, 0);
			audioDispatcher
					.addAudioProcessor(createDefaultPercussionOnsetDetector());
			if (!isAvailable) {
				addEvent(ClapEvent.microConnected());
				isAvailable = true;
			}
			start();
		} catch (LineUnavailableException | IllegalArgumentException e) {
			if (isAvailable) {
				e.printStackTrace();
				isAvailable = false;
			}
		}
	}

	private PercussionOnsetDetector createDefaultPercussionOnsetDetector() {
		ClapDetection instance = this;
		return new PercussionOnsetDetector(22050, 1024, new OnsetHandler() {
			@Override
			public void handleOnset(double time, double salience) {
				claps++;
				new Thread(instance).start();
			}
		}, 40, 8);
	}

	@Override
	public void run() {
		try {
			Thread.sleep(DOUBLE_CLAP_DELAY);
			if (claps > 0) {
				addEvent(ClapEvent.clapDetected(claps));
				claps = 0;
			}
		} catch (InterruptedException e) {
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
		claps = 0;
		if (audioDispatcher != null) {
			isRunning = true;
			new Thread(audioDispatcher).start();
		}
	}

	@Override
	public void stop() {
		super.stop();
		if (!isRunning && audioDispatcher != null) {
			audioDispatcher.stop();
		}
	}

	@Override
	protected void addEventListener() {
		addStopListener();
		addStartListener();
	}

	private void addStopListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(ClapEvent.Actions.STOP);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				stop();
			}
		});
	}

	private void addStartListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(ClapEvent.Actions.START);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				initialize();
			}
		});
	}

}
