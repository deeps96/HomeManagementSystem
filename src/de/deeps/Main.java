package de.deeps;

import java.util.LinkedList;
import java.util.List;

import de.deeps.event.EventCollector;
import de.deeps.modules.Module;
import de.deeps.modules.alarm.AlarmManager;
import de.deeps.modules.ambilight.Ambilight;
import de.deeps.modules.bluetooth.BluetoothDeviceDetection;
import de.deeps.modules.clapdetection.ClapDetection;
import de.deeps.modules.diodercontrol.DioderController;
import de.deeps.modules.eventdelayer.EventDelayer;
import de.deeps.modules.ledboard.LEDBoard;
import de.deeps.modules.lirc.IRRemote;
import de.deeps.modules.lirc.Lirc;
import de.deeps.modules.mediaplayer.AudioPlayer;
import de.deeps.modules.modes.ModeSwitcher;
import de.deeps.modules.network.EventForwarder;
import de.deeps.modules.network.NetworkDeviceDetection;
import de.deeps.modules.network.TCPEventTransmitter;
import de.deeps.modules.network.UDPEventReceiver;
import de.deeps.modules.pccontroller.PCController;
import de.deeps.modules.radioremote.RadioRemote;
import de.deeps.modules.rules.RuleManager;
import de.deeps.modules.visualizer.Visualizer;
import de.deeps.modules.voicecontrol.VoiceControl;
import de.deeps.modules.webserver.Website;
import de.deeps.modules.wol.WakeOnLan;

/**
 * @author Deeps
 */

public class Main {

	private EventCollector collector;
	private List<Module> modules;

	public Main(String machineName) {
		initialize(machineName);
	}

	private void initialize(String machineName) {
		collector = new EventCollector();
		loadModules(machineName);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("System halted. Stopping modules...");
				shutdownModules();
			}

		});
		System.out.println("READY");
	}

	private void shutdownModules() {
		for (Module module : modules) {
			module.stop();
		}
	}

	private void loadModules(String machineName) {
		modules = new LinkedList<>();
		for (Module.Name name : Module.Name.values()) {
			modules.add(loadModule(name, machineName));
		}
	}

	private Module loadModule(Module.Name name, String machineName) {
		Module module = null;
		switch (name) {
			case BLUETOOTH_DEVICE_DETECTION:
				module = new BluetoothDeviceDetection(collector, machineName);
				break;
			case CLAP_DETECTION:
				module = new ClapDetection(collector, machineName);
				break;
			case EVENT_DELAYER:
				module = new EventDelayer(collector, machineName);
				break;
			case UDP_EVENT_RECEIVER:
				module = new UDPEventReceiver(collector, machineName);
				break;
			case TCP_EVENT_TRANSMITTER:
				module = new TCPEventTransmitter(collector, machineName);
				break;
			case EVENT_FORWARDER:
				module = new EventForwarder(collector, machineName);
				break;
			case LED_BOARD:
				module = new LEDBoard(collector, machineName);
				break;
			case LIRC:
				module = new Lirc(collector, machineName);
				break;
			case RADIO_REMOTE:
				module = new RadioRemote(collector, machineName);
				break;
			case PC_CONTROLLER:
				module = new PCController(collector, machineName);
				break;
			case VISUALIZER:
				module = new Visualizer(collector, machineName);
				break;
			case IR_REMOTE:
				module = new IRRemote(collector, machineName);
				break;
			case WEBSITE:
				module = new Website(collector, machineName);
				break;
			case MODE_SWITCHER:
				module = new ModeSwitcher(collector, machineName);
				break;
			case WAKE_ON_LAN:
				module = new WakeOnLan(collector, machineName);
				break;
			case RULE_MANAGER:
				module = new RuleManager(collector, machineName);
				break;
			case MEDIA_PLAYER:
				module = new AudioPlayer(collector, machineName);
				break;
			case ALARM_MANAGER:
				module = new AlarmManager(collector, machineName);
				break;
			case VOICE_CONTROL:
				module = new VoiceControl(collector, machineName);
				break;
			case DIODER_CONTROL:
				module = new DioderController(collector, machineName);
				break;
			case AMBILIGHT:
				module = new Ambilight(collector, machineName);
				break;
			case NETWORK_DEVICE_DETECTION:
				module = new NetworkDeviceDetection(collector, machineName);
				break;
		}
		return module;
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Only pass machine name as parameter!");
			System.exit(1);
		}
		new Main(args[0]);
	}
}
