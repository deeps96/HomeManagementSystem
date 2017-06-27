package de.deeps.event.moduleevents;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class TCPEvent extends Event<TCPEvent.Actions> {

	public static enum Actions {
		ESTABLISH_TCP_CONNECTION,
		SEND_EVENT_TO_TARGET_IP,
		SEND_EVENT_BROADCAST,
		SEND_EVENT_TO_TARGET_MACHINE_NAME,
		MACHINE_NAME_INFO,
		REQUEST_CONNECTED_CLIENTS,
		CONNECTED_CLIENTS_INFO,
		REQUEST_MACHINE_AVAILABLE,
		MACHINE_AVAILABLE_INFO
	};

	public TCPEvent() {
		this(Source.UNKNOWN);
	}

	public TCPEvent(Source source) {
		super(source);
	}

	public static TCPEvent createMachineNameInfo(Source source, String localIP,
			String machineName) {
		TCPEvent event = new TCPEvent(source);
		event.setAction(Actions.MACHINE_NAME_INFO);
		event.setType(Type.INFO);
		List<Object> values = new LinkedList<>();
		values.add(localIP);
		values.add(machineName);
		event.setValues(values);
		return event;
	}

	public static TCPEvent createConnectionRequest(Source source,
			String localIP, int receiverPort) {
		TCPEvent event = new TCPEvent(source);
		event.setAction(Actions.ESTABLISH_TCP_CONNECTION);
		event.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(localIP);
		values.add(receiverPort);
		event.setValues(values);
		return event;
	}

	public static TCPEvent createSendEventRequest(Source source,
			String targetIP, Event<?> event) {
		TCPEvent tcpEvent = new TCPEvent(source);
		tcpEvent.setAction(Actions.SEND_EVENT_TO_TARGET_IP);
		tcpEvent.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(targetIP);
		values.add(event);
		tcpEvent.setValues(values);
		return tcpEvent;
	}

	public static TCPEvent createSendBroadcastEvent(Source source,
			Event<?> event) {
		TCPEvent tcpEvent = new TCPEvent(source);
		tcpEvent.setAction(Actions.SEND_EVENT_BROADCAST);
		tcpEvent.setType(Type.REQUEST);
		tcpEvent.setValue(event);
		return tcpEvent;
	}

	public static TCPEvent createSendEventToMachineName(Source source,
			String targetMachineName, Event<?> event) {
		TCPEvent tcpEvent = new TCPEvent(source);
		tcpEvent.setAction(Actions.SEND_EVENT_TO_TARGET_MACHINE_NAME);
		tcpEvent.setType(Type.REQUEST);
		List<Object> values = new LinkedList<>();
		values.add(targetMachineName);
		values.add(event);
		tcpEvent.setValues(values);
		return tcpEvent;
	}

	public static TCPEvent createConnectedClientRequest(Source source) {
		TCPEvent tcpEvent = new TCPEvent(source);
		tcpEvent.setAction(Actions.REQUEST_CONNECTED_CLIENTS);
		tcpEvent.setType(Type.REQUEST);
		return tcpEvent;
	}

	public static TCPEvent createConnectedClientInfo(Source source,
			Set<Entry<String, InetAddress>> clients) {
		TCPEvent tcpEvent = new TCPEvent(source);
		tcpEvent.setAction(Actions.CONNECTED_CLIENTS_INFO);
		tcpEvent.setType(Type.INFO);
		tcpEvent.setValue(clients);
		return tcpEvent;
	}

	public static TCPEvent createMachineAvailableRequest(Source source,
			String machineName) {
		TCPEvent event = new TCPEvent(source);
		event.setAction(Actions.REQUEST_MACHINE_AVAILABLE);
		event.setType(Type.REQUEST);
		event.setValue(machineName);
		return event;
	}

	public static TCPEvent createMachineAvailableInfo(Source source,
			String machineName, boolean isAvailable) {
		TCPEvent event = new TCPEvent(source);
		event.setAction(Actions.MACHINE_AVAILABLE_INFO);
		event.setType(Type.INFO);
		List<Object> values = new LinkedList<>();
		values.add(machineName);
		values.add(isAvailable);
		event.setValues(values);
		return event;
	}
}
