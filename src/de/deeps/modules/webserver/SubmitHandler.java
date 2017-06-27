package de.deeps.modules.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.sun.net.httpserver.HttpExchange;

import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.moduleevents.AlarmEvent;
import de.deeps.event.moduleevents.AmbilightEvent;
import de.deeps.event.moduleevents.IRRemoteEvent;
import de.deeps.event.moduleevents.ModeEvent;
import de.deeps.event.moduleevents.PCControllerEvent;
import de.deeps.event.moduleevents.RadioControllerEvent;
import de.deeps.event.moduleevents.RuleManagerEvent;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.event.moduleevents.VisualizationEvent;
import de.deeps.event.moduleevents.WakeOnLanEvent;
import de.deeps.modules.lirc.IRRemoteLightCeiling;
import de.deeps.modules.lirc.IRRemoteSubwoofer;
import de.deeps.modules.pccontroller.PCControllerInterface;
import de.deeps.modules.pccontroller.PCControllerInterface.Program;
import de.deeps.modules.radioremote.RadioRemote;
import de.deeps.modules.wol.WakeOnLan;

/**
 * @author Deeps
 */

public class SubmitHandler extends MyHttpHandler {

	private EventCollector collector;

	public SubmitHandler(EventCollector eventCollector) {
		this.collector = eventCollector;
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		byte[] response;
		List<Entry<String, String>> params = getPostParameters(httpExchange);
		switch (params.size()) {
			case 1:
				response = parseSingle(params);
				break;
			case 2:
				response = parseDouble(params);
				break;
			default:
				response = new byte[0];
		}
		if (response.length > 0) {
			sendResponseWithContent(httpExchange, response);
		} else {
			sendResponseWithoutContent(httpExchange);
		}
	}

	private byte[] parseDouble(List<Entry<String, String>> params) {
		switch (params.get(0).getKey().toLowerCase()) {
			case "startprogram":
				if (params.get(1).getKey().equalsIgnoreCase("on")) {
					return handleStartProgram(
						params.get(0).getValue(),
						params.get(1).getValue());
				}
				break;
			case "presskey":
				if (params.get(1).getKey().equalsIgnoreCase("ondevice")) {
					return handlePressKey(
						params.get(0).getValue(),
						params.get(1).getValue());
				}
				break;
			case "radiochannel":
				if (params.get(1).getKey().equalsIgnoreCase("powerstate")) {
					return handleRadioKey(
						params.get(0).getValue(),
						params.get(1).getValue());
				}
				break;
		}
		return new byte[0];
	}

	private byte[] handleRadioKey(String key, String powerstateBoolean) {
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"raspberry",
				RadioControllerEvent.createSendSignalEvent(
					Source.SUBMIT_HANDLER,
					RadioRemote.Button.valueOf(key.toUpperCase()),
					Boolean.parseBoolean(powerstateBoolean))));
		return new byte[0];
	}

	private byte[] handlePressKey(String key, String device) {
		switch (device.toLowerCase()) {
			case "light":
				collector.addEvent(
					IRRemoteEvent.createPressKeyEvent(
						Source.SUBMIT_HANDLER,
						IRRemoteLightCeiling.REMOTE_NAME,
						key));
				break;
			case "sub":
				collector.addEvent(
					IRRemoteEvent.createPressKeyEvent(
						Source.SUBMIT_HANDLER,
						IRRemoteSubwoofer.REMOTE_NAME,
						key));
				break;
		}
		return new byte[0];
	}

	private byte[] handleStartProgram(String name, String device) {
		PCControllerInterface.Program program;
		switch (name.toLowerCase()) {
			case "spotify":
				program = Program.SPOTIFY;
				break;
			default:
				return new byte[0];
		}
		switch (device.toLowerCase()) {
			case "pc":
				collector.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"standpc",
						PCControllerEvent.createStartProgramRequest(
							Source.SUBMIT_HANDLER,
							program)));
				break;
		}
		return new byte[0];
	}

	private byte[] parseSingle(List<Entry<String, String>> params) {
		switch (params.get(0).getKey().toLowerCase()) {
			case "mode":
				return handleModeEvent(params.get(0).getValue());
			case "shutdown":
				return handleShutdownEvent(params.get(0).getValue());
			case "powerup":
				return handlePowerupEvent(params.get(0).getValue());
			case "addalarmat":
				return addAlarm(params.get(0).getValue());
			case "removealarmat":
				return removeAlarm(params.get(0).getValue());
			case "enablerule":
				return enableRule(params.get(0).getValue());
			case "disablerule":
				return disableRule(params.get(0).getValue());
			case "setpcvolume":
				return setPCVolumeTo(params.get(0).getValue());
			case "loadlog":
				return loadLog();
			case "silencealarms":
				return silenceAllAlarms();
			case "startvisualizer":
				return startVisualizer();
			case "startambilight":
				return startAmbilight();
			case "stopfeatures":
				return stopFeatures();
		}
		return new byte[0];
	}

	private byte[] stopFeatures() {
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"standpc",
				VisualizationEvent.createStopVisualizationRequest(
					Source.SUBMIT_HANDLER)));
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"standpc",
				AmbilightEvent
						.createStopAmbilightRequest(Source.SUBMIT_HANDLER)));
		return new byte[0];
	}

	private byte[] startAmbilight() {
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"standpc",
				AmbilightEvent
						.createStartAmbilightRequest(Source.SUBMIT_HANDLER)));
		return new byte[0];
	}

	private byte[] startVisualizer() {
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.SUBMIT_HANDLER,
				"standpc",
				VisualizationEvent.createStartVisualizationRequest(
					Source.SUBMIT_HANDLER)));
		return new byte[0];
	}

	private byte[] silenceAllAlarms() {
		collector.addEvent(AlarmEvent.createSilenceAll(Source.SUBMIT_HANDLER));
		return new byte[0];
	}

	private byte[] disableRule(String value) {
		collector.addEvent(
			RuleManagerEvent
					.createDisableRuleRequest(Source.SUBMIT_HANDLER, value));
		collector.addEvent(
			RuleManagerEvent.createReloadRulesRequest(Source.SUBMIT_HANDLER));
		return new byte[0];
	}

	private byte[] enableRule(String value) {
		collector.addEvent(
			RuleManagerEvent
					.createEnableRuleRequest(Source.SUBMIT_HANDLER, value));
		collector.addEvent(
			RuleManagerEvent.createReloadRulesRequest(Source.SUBMIT_HANDLER));
		return new byte[0];
	}

	private byte[] loadLog() {
		try {
			File logFile = new File("log.log");
			String line;
			StringBuffer content = new StringBuffer();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(logFile)));
			while ((line = reader.readLine()) != null) {
				content.append(line + "<br />");
			}
			reader.close();
			return content.toString().getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	private byte[] setPCVolumeTo(String valueString) {
		try {
			int value = Integer.parseInt(valueString);
			collector.addEvent(
				TCPEvent.createSendEventToMachineName(
					Source.SUBMIT_HANDLER,
					"standpc",
					PCControllerEvent.createSetVolumeRequest(
						Source.SUBMIT_HANDLER,
						value)));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	private byte[] removeAlarm(String value) {
		collector.addEvent(
			AlarmEvent.createRemoveAlarm(Source.SUBMIT_HANDLER, value));
		return new byte[0];
	}

	private byte[] addAlarm(String value) {
		collector.addEvent(
			AlarmEvent.createAddAlarm(Source.SUBMIT_HANDLER, value));
		return new byte[0];
	}

	private byte[] handlePowerupEvent(String device) {
		WakeOnLanEvent event;
		switch (device.toLowerCase()) {
			case "pc":
				event = WakeOnLanEvent.createWakeDevice(
					Source.SUBMIT_HANDLER,
					WakeOnLan.Device.STAND_PC);
				break;
			default:
				event = null;
		}
		if (event != null) {
			collector.addEvent(event);
		}
		return new byte[0];
	}

	private byte[] handleShutdownEvent(String device) {
		PCControllerEvent event = PCControllerEvent
				.createShutdownRequest(Source.SUBMIT_HANDLER);
		switch (device.toLowerCase()) {
			case "pi":
				collector.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"raspberry",
						event));
				break;
			case "pc":
				collector.addEvent(
					TCPEvent.createSendEventToMachineName(
						Source.SUBMIT_HANDLER,
						"standpc",
						event));
				break;
		}
		return new byte[0];
	}

	private byte[] handleModeEvent(String modeName) {
		ModeEvent modeEvent;
		switch (modeName.toLowerCase()) {
			case "standard":
				modeEvent = ModeEvent
						.createEnableStandard(Source.SUBMIT_HANDLER);
				break;
			case "cinema":
				modeEvent = ModeEvent.createEnableCinema(Source.SUBMIT_HANDLER);
				break;
			case "working":
				modeEvent = ModeEvent
						.createEnableWorking(Source.SUBMIT_HANDLER);
				break;
			case "eating":
				modeEvent = ModeEvent.createEnableEating(Source.SUBMIT_HANDLER);
				break;
			case "party":
				modeEvent = ModeEvent.createEnableParty(Source.SUBMIT_HANDLER);
				break;
			default:
				modeEvent = null;
		}
		if (modeEvent != null) {
			collector.addEvent(modeEvent);
		}
		return new byte[0];
	}

	private List<Entry<String, String>> getPostParameters(
			HttpExchange httpExchange) throws IOException {
		List<Entry<String, String>> parameters = new LinkedList<>();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(httpExchange.getRequestBody()));
		String line;
		while ((line = reader.readLine()) != null) {
			for (String param : line.split("&")) {
				String[] part = param.split("=");
				if (part.length != 2) {
					continue;
				}
				parameters
						.add(new SimpleEntry<String, String>(part[0], part[1]));
			}
		}
		return parameters;
	}

}
