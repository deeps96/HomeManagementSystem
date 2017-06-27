package de.deeps.modules.voicecontrol;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.recognizer.GoogleResponse;
import com.darkprograms.speech.recognizer.Recognizer;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.ClapEvent;
import de.deeps.event.moduleevents.IRRemoteEvent;
import de.deeps.event.moduleevents.LEDBoardEvent;
import de.deeps.event.moduleevents.ModeEvent;
import de.deeps.event.moduleevents.VoiceControlEvent;
import de.deeps.modules.Module;
import de.deeps.modules.lirc.IRRemoteLightCeiling;
import net.sourceforge.javaflacencoder.FLACFileWriter;

/**
 * @author Deeps
 */

public class VoiceControl extends Module {

	private final int COUNTDOWN = 3, RECORD_TIME = 5, NUMBER_OF_RESPONSES = 4;
	private final String API_KEY = "AIzaSyDly1bae-KgxzT644OmkeilEryZ3sCKBtU";

	private boolean isRecording;
	private File tmpAudioFile;
	private Microphone microphone;
	private Recognizer recognizer;

	public VoiceControl(EventCollector collector, String machineName) {
		super(collector, Name.VOICE_CONTROL, machineName);
	}

	@Override
	protected void addEventListener() {
		addStartSequenceListener();
	}

	private void addStartSequenceListener() {
		EventCondition condition = new EventCondition(false) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(VoiceControlEvent.Actions.CAPTURE_SEQUENCE);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				showCountdown();
			}
		});
	}

	private void showCountdown() {
		if (isRecording) {
			return;
		}
		isRecording = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int iCountdown = COUNTDOWN; iCountdown > 0; iCountdown--) {
					collector.addEvent(
						LEDBoardEvent.createShowStaticString(
							Source.VOICE_COMMAND,
							"  " + iCountdown));
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				collector.addEvent(
					LEDBoardEvent.createTurnOff(Source.VOICE_COMMAND));
				record();
			}
		}).start();
	}

	private void record() {
		try {
			collector.addEvent(ClapEvent.stop());
			microphone = new Microphone(FLACFileWriter.FLAC);
			microphone.captureAudioToFile(tmpAudioFile);
			for (int iCountdown = RECORD_TIME; iCountdown > 0; iCountdown--) {
				collector.addEvent(
					LEDBoardEvent.createShowStaticString(
						Source.VOICE_COMMAND,
						"  " + iCountdown));
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			microphone.close();
			letGoogleDetect();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	private void letGoogleDetect() {
		collector.addEvent(
			LEDBoardEvent.createShowAnimatedStringEvent(
				Source.VOICE_COMMAND,
				"- - -",
				Integer.MAX_VALUE));
		try {
			GoogleResponse googleResponse = recognizer.getRecognizedDataForFlac(
				tmpAudioFile,
				NUMBER_OF_RESPONSES,
				(int) microphone.getAudioFormat().getSampleRate());
			String concatedResponses = googleResponse.getResponse();
			for (String response : googleResponse.getOtherPossibleResponses()) {
				concatedResponses += " " + response;
			}
			parseResponse(concatedResponses);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tmpAudioFile.delete();
		collector.addEvent(LEDBoardEvent.createTurnOff(Source.VOICE_COMMAND));
		isRecording = false;
		collector.addEvent(ClapEvent.start());
	}

	private void parseResponse(String response) {
		if (response != null) {
			if (response.contains("light")) {
				if (response.contains("on")) {
					collector.addEvent(
						IRRemoteEvent.createPressKeyEvent(
							Source.VOICE_COMMAND,
							IRRemoteLightCeiling.REMOTE_NAME,
							IRRemoteLightCeiling.Button.POWER_ON.toString()));
					return;
				} else if (response.contains("off")) {
					collector.addEvent(
						IRRemoteEvent.createPressKeyEvent(
							Source.VOICE_COMMAND,
							IRRemoteLightCeiling.REMOTE_NAME,
							IRRemoteLightCeiling.Button.POWER_OFF.toString()));
					return;
				}
			}

			if (response.contains("cinema")) {
				collector.addEvent(
					ModeEvent.createEnableCinema(Source.VOICE_COMMAND));
				return;
			}
			if (response.contains("work")) {
				collector.addEvent(
					ModeEvent.createEnableWorking(Source.VOICE_COMMAND));
				return;
			}
		}
		collector.addEvent(
			LEDBoardEvent
					.createShowStaticString(Source.VOICE_COMMAND, "NONE", 5));
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initialize() {
		tmpAudioFile = new File("voicerecord.flac");
		recognizer = new Recognizer(Recognizer.Languages.ENGLISH_US, API_KEY);
		isRecording = false;
		isAvailable = true;
	}

	@Override
	public void updateAvailability() {
	}

}
