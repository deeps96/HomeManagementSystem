package de.deeps.modules.alarm;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.deeps.Utils;
import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.AlarmEvent;
import de.deeps.event.moduleevents.IRRemoteEvent;
import de.deeps.event.moduleevents.MediaPlayerEvent;
import de.deeps.event.moduleevents.ModeEvent;
import de.deeps.event.moduleevents.RadioControllerEvent;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.modules.Module;
import de.deeps.modules.lirc.IRRemoteSubwoofer;
import de.deeps.modules.radioremote.RadioRemote;

/**
 * @author Deeps
 */

public class AlarmManager extends Module {

	private static final String ALARMS_FILE = "alarms.txt";
	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(
			"HH:mm");

	private boolean isPCAvailable;
	private List<Timer> timers;

	public AlarmManager(EventCollector collector, String machineName) {
		super(collector, Name.ALARM_MANAGER, machineName);
	}

	@Override
	protected void addEventListener() {
		addAddAlarmEventListener();
		addRemoveAlarmEventListener();
		addSilenceAllAlarmsEventListener();
		addMachineAvailableListener();
	}

	private void addMachineAvailableListener() {
		EventCondition condition = new EventCondition(false) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(TCPEvent.Actions.MACHINE_AVAILABLE_INFO);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				if (((String) event.getValues().get(0))
						.equalsIgnoreCase("standpc")) {
					isPCAvailable = (Boolean) event.getValues().get(1);
				}
			}
		});
	}

	private void addSilenceAllAlarmsEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(AlarmEvent.Actions.SILENCE_ALL);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				silenceAllAlarms();
			}
		});
	}

	protected void silenceAllAlarms() {
		collector.addEvent(
			MediaPlayerEvent.createStopAllRequest(Source.ALARM_MANAGER));
		collector.addEvent(
			TCPEvent.createMachineAvailableRequest(
				Source.MODE_SWITCHER,
				"standpc"));
		try {
			Thread.sleep(300L);
		} catch (InterruptedException e) {
		}
		if (!isPCAvailable) {
			collector
					.addEvent(
						TCPEvent.createSendEventToMachineName(
							Source.MODE_SWITCHER,
							"raspberry",
							RadioControllerEvent.createSendSignalEvent(
								Source.MODE_SWITCHER,
								RadioRemote.Button.B,
								false)));
		}
	}

	private void addRemoveAlarmEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(AlarmEvent.Actions.REMOVE_ALARM);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				removeAlarm((String) event.getValues().get(0));
			}
		});
	}

	private void addAddAlarmEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(AlarmEvent.Actions.ADD_ALARM);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				addAlarm((String) event.getValues().get(0));
			}
		});
	}

	protected void removeAlarm(String time) {
		try {
			String alarms = Utils.readStringFromFile(new File(ALARMS_FILE));
			alarms = alarms.replace(time + "\r\n", "");
			Utils.writeStringToFile(alarms, new File(ALARMS_FILE));
			reloadAlarms();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private void reloadAlarms() {
		for (Timer timer : timers) {
			timer.cancel();
		}
		timers.clear();
		try {
			String alarms = Utils.readStringFromFile(new File(ALARMS_FILE));
			Calendar nowCalendar = Calendar.getInstance();
			Calendar alarmCalendar = Calendar.getInstance();
			alarmCalendar.set(Calendar.SECOND, 0);
			for (String alarmString : alarms.split("\r\n")) {
				if (alarmString.length() == 0) {
					continue;
				}
				try {
					Date hours = DATE_FORMATTER.parse(alarmString);
					Calendar hourCalendar = Calendar.getInstance();
					hourCalendar.setTime(hours);

					alarmCalendar.set(
						Calendar.HOUR_OF_DAY,
						hourCalendar.get(Calendar.HOUR_OF_DAY));
					alarmCalendar.set(
						Calendar.MINUTE,
						hourCalendar.get(Calendar.MINUTE));

					if (nowCalendar.getTimeInMillis() > alarmCalendar
							.getTimeInMillis()) {
						alarmCalendar.add(Calendar.DATE, 1);
					}
					addAlarmTimer(alarmCalendar.getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addAlarmTimer(Date date) {
		Timer alarmTimer = new Timer();
		alarmTimer.schedule(createNewAlarmTask(), date, 86400000); // 24 h
																   // period
		timers.add(alarmTimer);
	}

	private TimerTask createNewAlarmTask() {
		return new TimerTask() {
			@Override
			public void run() {
				collector.addEvent(
					TCPEvent.createMachineAvailableRequest(
						Source.ALARM_MANAGER,
						"standpc"));
				try {
					Thread.sleep(300L);
				} catch (InterruptedException e) {
				}
				if (!isPCAvailable) {
					collector.addEvent(
						TCPEvent.createSendEventToMachineName(
							Source.ALARM_MANAGER,
							"raspberry",
							RadioControllerEvent.createSendSignalEvent(
								Source.ALARM_MANAGER,
								RadioRemote.Button.B,
								true)));
					try {
						Thread.sleep(3000L);
					} catch (InterruptedException e) {
					}
					collector.addEvent(
						IRRemoteEvent.createPressKeyEvent(
							Source.ALARM_MANAGER,
							IRRemoteSubwoofer.REMOTE_NAME,
							IRRemoteSubwoofer.Button.POWER_TOGGLE.toString()));
					try {
						Thread.sleep(300L);
					} catch (InterruptedException e) {
					}
				}
				collector.addEvent(
					ModeEvent.createEnableWakeUpAlarm(Source.ALARM_MANAGER));
			}
		};
	}

	protected void addAlarm(String time) {
		try {
			Utils.writeStringToFile(time + "\r\n", new File(ALARMS_FILE), true);
			reloadAlarms();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initialize() {
		if (!(new File(ALARMS_FILE).exists())) {
			System.err.println("Alarms-file not found!");
			return;
		}
		timers = new LinkedList<>();
		reloadAlarms();
		isAvailable = true;
	}

	@Override
	public void updateAvailability() {
	}

}
