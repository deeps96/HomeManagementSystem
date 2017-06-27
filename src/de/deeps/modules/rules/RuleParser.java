package de.deeps.modules.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCondition;
import de.deeps.event.moduleevents.AlarmEvent;
import de.deeps.event.moduleevents.AmbilightEvent;
import de.deeps.event.moduleevents.BluetoothEvent;
import de.deeps.event.moduleevents.ClapEvent;
import de.deeps.event.moduleevents.DelayEvent;
import de.deeps.event.moduleevents.DioderEvent;
import de.deeps.event.moduleevents.IRRemoteEvent;
import de.deeps.event.moduleevents.LEDBoardEvent;
import de.deeps.event.moduleevents.LircEvent;
import de.deeps.event.moduleevents.MediaPlayerEvent;
import de.deeps.event.moduleevents.ModeEvent;
import de.deeps.event.moduleevents.NetworkDeviceDetectionEvent;
import de.deeps.event.moduleevents.PCControllerEvent;
import de.deeps.event.moduleevents.RadioControllerEvent;
import de.deeps.event.moduleevents.RuleManagerEvent;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.event.moduleevents.VisualizationEvent;
import de.deeps.event.moduleevents.VoiceControlEvent;
import de.deeps.event.moduleevents.WakeOnLanEvent;

/**
 * @author Deeps
 */

public class RuleParser {

	public static List<Rule> loadAllRulesInDir(String dirPath) {
		List<Rule> rules = new LinkedList<>();
		File dirFile;
		dirFile = new File(dirPath);
		if (dirFile.isDirectory()) {
			for (File file : dirFile.listFiles()) {
				if (file.isFile() && file.getAbsolutePath().endsWith(".json")) {
					rules.addAll(parseRuleFromJSONFile(file.getAbsolutePath()));
				}
			}
		}
		return rules;
	}

	public static List<Rule> parseRuleFromJSONFile(String filePath) {
		try {
			JsonReader reader = Json
					.createReader(new FileInputStream(filePath));
			JsonObject rule = reader.readObject();
			reader.close();
			return parseRule(rule);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static Event<?> parseActionEventFromJSONFile(String filePath) {
		try {
			JsonReader reader = Json
					.createReader(new FileInputStream(filePath));
			JsonObject rule = reader.readObject();
			reader.close();
			return parseActionEvent(rule);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static List<Rule> parseRule(JsonObject ruleObject) {
		List<Rule> rules = new LinkedList<>();
		EventCondition condition = parseEventCondition(
			ruleObject.getJsonObject("condition"));
		for (JsonObject element : ruleObject.getJsonArray("actionevents")
				.getValuesAs(JsonObject.class)) {
			String actionEventPath = RuleManager.EVENTS_DIR + "/"
					+ element.getString("name") + ".json";
			Event<?> issueOnMatchEvent = parseActionEventFromJSONFile(
				actionEventPath);
			Event<TCPEvent.Actions> tcpEvent = TCPEvent
					.createSendEventToMachineName(
						Source.UNKNOWN,
						element.getString("target"),
						issueOnMatchEvent);
			rules.add(new Rule(condition, tcpEvent));
		}
		return rules;
	}

	private static Event<?> parseActionEvent(JsonObject jsonObject) {
		Event<?> event = getEventForActionString(
			jsonObject.getString("action"));
		if (event == null) {
			throw new IllegalArgumentException("Unknown action.");
		}
		JsonArray values = jsonObject.getJsonArray("values");
		if (values != null) {
			event.setValues(parseValues(values));
		}
		event.setType(Event.Type.valueOf(jsonObject.getString("type")));
		return event;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Event<?> getEventForActionString(String enumAsString) {
		Class[] eventClasses = new Class[] { AlarmEvent.class,
				AmbilightEvent.class, BluetoothEvent.class, ClapEvent.class,
				DelayEvent.class, DioderEvent.class, IRRemoteEvent.class,
				LEDBoardEvent.class, LircEvent.class, MediaPlayerEvent.class,
				ModeEvent.class, NetworkDeviceDetectionEvent.class,
				PCControllerEvent.class, RadioControllerEvent.class,
				RuleManagerEvent.class, TCPEvent.class,
				VisualizationEvent.class, VoiceControlEvent.class,
				WakeOnLanEvent.class };

		Class[] enumClasses = new Class[] { AlarmEvent.Actions.class,
				AmbilightEvent.Actions.class, BluetoothEvent.Actions.class,
				ClapEvent.Actions.class, DelayEvent.Actions.class,
				DioderEvent.Actions.class, IRRemoteEvent.Actions.class,
				LEDBoardEvent.Actions.class, LircEvent.Actions.class,
				MediaPlayerEvent.Actions.class, ModeEvent.Actions.class,
				NetworkDeviceDetectionEvent.Actions.class,
				PCControllerEvent.Actions.class,
				RadioControllerEvent.Actions.class,
				RuleManagerEvent.Actions.class, TCPEvent.Actions.class,
				VisualizationEvent.Actions.class,
				VoiceControlEvent.Actions.class, WakeOnLanEvent.Actions.class };

		Event<?> event = null;
		for (int iClass = 0; iClass < eventClasses.length; iClass++) {
			event = getEventForActionString(
				eventClasses[iClass],
				enumClasses[iClass],
				enumAsString);
			if (event != null) {
				break;
			}
		}
		return event;
	}

	private static <E extends Enum<E>, T extends Event<E>> Event<E> getEventForActionString(
			Class<T> eventClass, Class<E> enumClass, String enumAsString) {
		try {
			E detectedEnum = Enum.valueOf(enumClass, enumAsString);
			Event<E> event = eventClass.newInstance();
			event.setAction(detectedEnum);
			return event;
		} catch (IllegalArgumentException | InstantiationException
				| IllegalAccessException e) {
			return null;
		}
	}

	private static List<Object> parseValues(JsonArray jsonArray) {
		List<Object> values = new LinkedList<>();
		Comparator.SupportedDataType dataType;
		String value;
		for (JsonObject element : jsonArray.getValuesAs(JsonObject.class)) {
			value = element.getString("value");
			dataType = Comparator.SupportedDataType
					.valueOf(element.getString("format"));
			values.add(Comparator.convert(dataType, value));
		}
		return values;
	}

	private static EventCondition parseEventCondition(
			JsonObject jsonCondition) {
		boolean consumeAfterMatch = jsonCondition
				.getBoolean("consumeAfterMatch", false);
		String sourceString = jsonCondition.getString("source", null);
		Event.Source source;
		if (sourceString != null) {
			source = Event.Source.valueOf(sourceString);
		} else {
			source = null;
		}
		Enum<?> action = getAction(jsonCondition.getString("action", null));
		List<ValueCondition> valueConditions = parseValueConditions(
			jsonCondition.getJsonArray("values"));

		EventCondition condition = new EventCondition(consumeAfterMatch) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				if (source != null && !event.getSource().equals(source)) {
					return false;
				}
				if (action != null && !event.getAction().equals(action)) {
					return false;
				}
				for (ValueCondition condition : valueConditions) {
					if (!Comparator.compare(
						condition,
						event.getValues().get(condition.getIndex()))) {
						return false;
					}
				}
				return true;
			}
		};
		return condition;
	}

	private static List<ValueCondition> parseValueConditions(
			JsonArray jsonArray) {
		List<ValueCondition> conditions = new LinkedList<>();
		if (jsonArray != null) {
			for (JsonObject element : jsonArray.getValuesAs(JsonObject.class)) {
				conditions.add(parseValueCondition(element));
			}
		}
		return conditions;
	}

	private static ValueCondition parseValueCondition(JsonObject element) {
		Comparator.Type comparator = Comparator.Type
				.valueOf(element.getString("comparator"));
		int index = element.getInt("index");
		String expectedValue = element.getString("value");
		Comparator.SupportedDataType dataType = Comparator.SupportedDataType
				.valueOf(element.getString("format"));
		return new ValueCondition(comparator, index, expectedValue, dataType);
	}

	private static Enum<?> getAction(String actionString) {
		if (actionString == null) {
			return null;
		}
		Event<?> event = getEventForActionString(actionString);
		if (event == null) {
			throw new IllegalArgumentException("Unknown action.");
		}
		return (Enum<?>) event.getAction();
	}

}
