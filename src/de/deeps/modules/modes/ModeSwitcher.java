package de.deeps.modules.modes;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.IRRemoteEvent;
import de.deeps.event.moduleevents.MediaPlayerEvent;
import de.deeps.event.moduleevents.ModeEvent;
import de.deeps.event.moduleevents.RadioControllerEvent;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.event.moduleevents.VisualizationEvent;
import de.deeps.modules.Module;
import de.deeps.modules.lirc.IRRemoteLightCeiling;
import de.deeps.modules.radioremote.RadioRemote;

/**
 * @author Deeps
 */

public class ModeSwitcher extends Module {

	public ModeSwitcher(EventCollector collector, String machineName) {
		super(collector, Name.MODE_SWITCHER, machineName);
	}

	@Override
	protected void addEventListener() {
		addStandardListener();
		addCinemaListener();
		addWorkingListener();
		addEatingListener();
		addPartyListener();
		addWakeUpAlarmListener();
	}

	private void addWakeUpAlarmListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(ModeEvent.Actions.ENABLE_WAKE_UP_ALARM);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				emableWakeUpAlarmMode();
			}
		});
	}

	protected void emableWakeUpAlarmMode() {
		collector.addEvent(
			IRRemoteEvent.createPressKeyEvent(
				Source.MODE_SWITCHER,
				IRRemoteLightCeiling.REMOTE_NAME,
				IRRemoteLightCeiling.Button.POWER_ON.toString()));
		collector.addEvent(
			IRRemoteEvent.createPressKeyEvent(
				Source.MODE_SWITCHER,
				IRRemoteLightCeiling.REMOTE_NAME,
				IRRemoteLightCeiling.Button.MORNING_MODE.toString()));
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.MODE_SWITCHER,
				"raspberry",
				MediaPlayerEvent.createPlayMP3Request(
					Source.MODE_SWITCHER,
					"alarm.mp3")));
	}

	private void addPartyListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(ModeEvent.Actions.ENABLE_PARTY);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				enablePartyMode();
			}
		});
	}

	private void addEatingListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(ModeEvent.Actions.ENABLE_EATING);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				enableEatingMode();
			}
		});
	}

	private void addWorkingListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(ModeEvent.Actions.ENABLE_WORKING);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				enableWorkingMode();
			}
		});
	}

	private void addCinemaListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(ModeEvent.Actions.ENABLE_CINEMA);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				enableCinemaMode();
			}
		});
	}

	private void addStandardListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(ModeEvent.Actions.ENABLE_STANDARD);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				enableStandardMode();
			}
		});
	}

	private void enablePartyMode() {
		collector.addEvent(
			IRRemoteEvent.createPressKeyEvent(
				Source.MODE_SWITCHER,
				IRRemoteLightCeiling.REMOTE_NAME,
				IRRemoteLightCeiling.Button.PARTY_MODE.toString()));
		collector
				.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"raspberry",
						RadioControllerEvent.createSendSignalEvent(
							Source.MODE_SWITCHER,
							RadioRemote.Button.C,
							false)));
		collector
				.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"raspberry",
						RadioControllerEvent.createSendSignalEvent(
							Source.MODE_SWITCHER,
							RadioRemote.Button.D,
							false)));
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"standpc",
				VisualizationEvent.createStartVisualizationRequest(
					Source.MODE_SWITCHER)));
	}

	private void enableEatingMode() {
		collector.addEvent(
			IRRemoteEvent.createPressKeyEvent(
				Source.MODE_SWITCHER,
				IRRemoteLightCeiling.REMOTE_NAME,
				IRRemoteLightCeiling.Button.LIGHT_BULB_MODE.toString()));
		collector
				.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"raspberry",
						RadioControllerEvent.createSendSignalEvent(
							Source.MODE_SWITCHER,
							RadioRemote.Button.C,
							false)));
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"raspberry",
				RadioControllerEvent.createSendSignalEvent(
					Source.MODE_SWITCHER,
					RadioRemote.Button.D,
					true)));
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"standpc",
				VisualizationEvent
						.createStopVisualizationRequest(Source.MODE_SWITCHER)));
	}

	private void enableWorkingMode() {
		collector.addEvent(
			IRRemoteEvent.createPressKeyEvent(
				Source.MODE_SWITCHER,
				IRRemoteLightCeiling.REMOTE_NAME,
				IRRemoteLightCeiling.Button.SUN_MODE.toString()));
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"raspberry",
				RadioControllerEvent.createSendSignalEvent(
					Source.MODE_SWITCHER,
					RadioRemote.Button.C,
					true)));
		collector
				.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"raspberry",
						RadioControllerEvent.createSendSignalEvent(
							Source.MODE_SWITCHER,
							RadioRemote.Button.D,
							false)));
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"standpc",
				VisualizationEvent
						.createStopVisualizationRequest(Source.MODE_SWITCHER)));
	}

	private void enableCinemaMode() {
		collector.addEvent(
			IRRemoteEvent.createPressKeyEvent(
				Source.MODE_SWITCHER,
				IRRemoteLightCeiling.REMOTE_NAME,
				IRRemoteLightCeiling.Button.EVENING_MODE.toString()));
		collector
				.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"raspberry",
						RadioControllerEvent.createSendSignalEvent(
							Source.MODE_SWITCHER,
							RadioRemote.Button.C,
							false)));
		collector
				.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"raspberry",
						RadioControllerEvent.createSendSignalEvent(
							Source.MODE_SWITCHER,
							RadioRemote.Button.D,
							false)));
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"standpc",
				VisualizationEvent
						.createStopVisualizationRequest(Source.MODE_SWITCHER)));
	}

	private void enableStandardMode() {
		collector.addEvent(
			IRRemoteEvent.createPressKeyEvent(
				Source.MODE_SWITCHER,
				IRRemoteLightCeiling.REMOTE_NAME,
				IRRemoteLightCeiling.Button.LIGHT_BULB_MODE.toString()));
		collector
				.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"raspberry",
						RadioControllerEvent.createSendSignalEvent(
							Source.MODE_SWITCHER,
							RadioRemote.Button.C,
							false)));
		collector
				.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"raspberry",
						RadioControllerEvent.createSendSignalEvent(
							Source.MODE_SWITCHER,
							RadioRemote.Button.D,
							false)));
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"standpc",
				VisualizationEvent
						.createStopVisualizationRequest(Source.MODE_SWITCHER)));
	}

	@Override
	protected void initialize() {
		isAvailable = (System.getProperty("os.name").equals("Linux")
				&& System.getProperty("os.arch").equals("arm"));
	}

	@Override
	public void updateAvailability() {
	}

}
