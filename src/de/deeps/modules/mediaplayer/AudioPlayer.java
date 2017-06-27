package de.deeps.modules.mediaplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.IRRemoteEvent;
import de.deeps.event.moduleevents.MediaPlayerEvent;
import de.deeps.event.moduleevents.PCControllerEvent;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.modules.Module;
import de.deeps.modules.lirc.IRRemoteSubwoofer;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * @author Deeps
 */

public class AudioPlayer extends Module implements Runnable {

	private Player player;

	public AudioPlayer(EventCollector collector, String machineName) {
		super(collector, Name.MEDIA_PLAYER, machineName);
	}

	@Override
	protected void addEventListener() {
		addPlayMp3Listener();
		addStopAllListener();
	}

	private void addStopAllListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(MediaPlayerEvent.Actions.STOP_ALL);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				stopCurrentAudio();
			}
		});
	}

	private void addPlayMp3Listener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(MediaPlayerEvent.Actions.PLAY_MP3);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				playAudio((String) event.getValues().get(0));
			}
		});
	}

	@Override
	public void run() {
		try {
			player.play();
			stopCurrentAudio();
		} catch (JavaLayerException e) {
			e.printStackTrace();
		}
	}

	private void stopCurrentAudio() {
		if (player != null) {
			collector.addEvent(
				TCPEvent.createSendEventToMachineName(
					Source.PC_CONTROLLER,
					"raspberry",
					IRRemoteEvent.createPressKeyEvent(
						Source.PC_CONTROLLER,
						IRRemoteSubwoofer.REMOTE_NAME,
						IRRemoteSubwoofer.Button.INPUT_DIGITAL.toString())));
			player.close();
		}
	}

	private void playAudio(String filePath) {
		try {
			stopCurrentAudio();
			collector.addEvent(
				PCControllerEvent.createSelectAudioOut(Source.MEDIA_PLAYER));
			FileInputStream fis = new FileInputStream(new File(filePath));
			player = new Player(fis);
			new Thread(this).start();
		} catch (FileNotFoundException | JavaLayerException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initialize() {
		isAvailable = true;
	}

	@Override
	public void updateAvailability() {
	}

}
