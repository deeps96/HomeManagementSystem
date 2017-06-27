package de.deeps.modules.pccontroller;

import java.io.IOException;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.IRRemoteEvent;
import de.deeps.event.moduleevents.PCControllerEvent;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.modules.Module;
import de.deeps.modules.lirc.IRRemoteSubwoofer;

/**
 * @author Deeps
 */

public class PCController extends Module implements Runnable {

	private boolean isWindows, isLinux;
	private PCControllerInterface controller;

	public PCController(EventCollector collector, String machineName) {
		super(collector, Name.PC_CONTROLLER, machineName);
	}

	@Override
	public void run() {
		long last = System.currentTimeMillis();
		while (isRunning) {
			if (System.currentTimeMillis() - last > 10000L) {
				System.out.println(
					"Time clock suggests waking up from sleep. \nInitiiating restart in 30 secs...");
				try {
					Thread.sleep(30000L);
					controller.restartHMS();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
			last = System.currentTimeMillis();
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void addEventListener() {
		addShutdownListener();
		addSetVolumeListener();
		addGetVolumeListener();
		addStartProgramListener();
		addSelectAudioOutListener();
		addSleepListener();
		addChangeOutputDeviceListener();
	}

	private void addChangeOutputDeviceListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(PCControllerEvent.Actions.CHANGE_OUTPUT_DEVICE);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				try {
					controller.changeOutputDeviceTo(
						(String) event.getValues().get(0));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void addSelectAudioOutListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(PCControllerEvent.Actions.SELECT_AUDIO_OUT);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				String button = null;
				if (isWindows) {
					button = IRRemoteSubwoofer.Button.INPUT_DIGITAL.toString();
				} else if (isLinux) {
					button = IRRemoteSubwoofer.Button.INPUT_PC.toString();
				}
				collector.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.PC_CONTROLLER,
						"raspberry",
						IRRemoteEvent.createPressKeyEvent(
							Source.PC_CONTROLLER,
							IRRemoteSubwoofer.REMOTE_NAME,
							button)));
			}
		});
	}

	private void addStartProgramListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(
					PCControllerEvent.Actions.START_PROGRAM_REQUEST);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				try {
					controller.runProgram(
						(PCControllerInterface.Program) event.getValues()
								.get(0));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void addGetVolumeListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(PCControllerEvent.Actions.VOLUME_REQUEST);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				collector.addEvent(
					PCControllerEvent.createVolumeInfo(
						Source.PC_CONTROLLER,
						controller.getVolume()));
			}
		});
	}

	private void addSetVolumeListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(PCControllerEvent.Actions.SET_VOLUME_REQUEST);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				controller.setVolume((int) event.getValues().get(0));
			}
		});
	}

	private void addSleepListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(PCControllerEvent.Actions.SLEEP_REQUEST);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				try {
					controller.sleep();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void addShutdownListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(PCControllerEvent.Actions.SHUTDOWN_REQUEST);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				try {
					controller.shutdown();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void initialize() {
		try {
			isWindows = System.getProperty("os.name").contains("Windows");
			isLinux = System.getProperty("os.name").contains("Linux");
			if (isWindows) {
				controller = new WindowsController();
			} else if (isLinux) {
				controller = new LinuxController();
			} else {
				throw new Exception("OS not supported!");
			}
			isAvailable = true;
			start();
		} catch (Exception e) {
			e.printStackTrace();
			isAvailable = false;
		}
	}

	@Override
	protected void start() {
		super.start();
		if (isRunning) {
			new Thread(this).start();
		}
	}

	@Override
	public void updateAvailability() {
	}

}
