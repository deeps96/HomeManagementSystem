package de.deeps.event;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Deeps
 */

public class Event<E> implements Comparable<Event<?>>, Serializable {

	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(
			"dd-MM-yyyy hh:mm:ss");

	public static enum Source {
		ALARM_MANAGER,
		AMBILIGHT,
		BLUETOOTH,
		CLAPDETECTOR,
		EVENT_FORWARDER,
		EVENT_DELAYER,
		IR_REMOTE,
		LED_BOARD,
		MEDIA_PLAYER,
		MODE_SWITCHER,
		NETWORK_DEVICE_DETECTION,
		PC_CONTROLLER,
		RULE_MANAGER,
		SUBMIT_HANDLER,
		TCP_EVENT_TRANSMITTER,
		UNKNOWN,
		VISUALIZER,
		VOICE_COMMAND,
		WEBSITE
	};

	public static enum Type {
		ERROR, REQUEST, DETECT, INFO
	};

	private E action;
	private List<Object> values;
	private long timestamp;
	private Source source;
	private Type type;

	public Event(Source source) {
		this.source = source;
		values = new LinkedList<>();
		updateTimestamp();
	}

	public void updateTimestamp() {
		timestamp = System.currentTimeMillis();
	}

	@Override
	public int compareTo(Event<?> o) {
		return (int) Math.signum(getTimestamp() - o.getTimestamp());
	}

	@Override
	public String toString() {
		return "Event src: " + source + " type: " + type + " action: " + action
				+ " values: " + values + " at: "
				+ DATE_FORMATTER.format(new Date(timestamp));
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public E getAction() {
		return action;
	}

	public Type getType() {
		return type;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setAction(E action) {
		this.action = action;
	}

	public Event<E> withAction(E action) {
		this.action = action;
		return this;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	public void setValue(Object object) {
		this.values = new LinkedList<>(Arrays.asList(object));
	}

	public void setType(Type type) {
		this.type = type;
	}

}
