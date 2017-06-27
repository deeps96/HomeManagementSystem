package de.deeps.modules.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.deeps.Utils;
import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.modules.Module;
import de.deeps.network.TCPClient;
import de.deeps.network.TCPServer;
import de.deeps.network.UDPSender;

/**
 * @author Deeps
 */

public class TCPEventTransmitter extends Module {

	private static final int TCP_EVENT_RECEIVER_PORT = 7777;

	private HashMap<String, InetAddress> clientDictionary;
	private List<TCPClient> clients; // does also contain server-tcp-clients
	private TCPServer server;

	public TCPEventTransmitter(EventCollector collector, String machineName) {
		super(collector, Name.TCP_EVENT_TRANSMITTER, machineName);
	}

	@Override
	protected void addEventListener() {
		addEstablishConnectionListener();
		addTransmitToIPEventListener();
		addTransmitBroadcastEventListener();
		addTransmitToMachineNameEventListener();
		addConnectedClientEventListener();
		addMachineAvailableEventListener();
		addMachineNameEventListener();
	}

	private void addMachineNameEventListener() {
		EventCondition transmitEventCondition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return (event.getAction()
						.equals(TCPEvent.Actions.MACHINE_NAME_INFO));
			}
		};
		collector.addListener(new EventListener(transmitEventCondition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				registerClient(
					(String) event.getValues().get(0),
					(String) event.getValues().get(1));
			}
		});
	}

	private void addMachineAvailableEventListener() {
		EventCondition transmitEventCondition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return (event.getAction()
						.equals(TCPEvent.Actions.REQUEST_MACHINE_AVAILABLE));
			}
		};
		collector.addListener(new EventListener(transmitEventCondition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				String requestedName = (String) event.getValues().get(0);
				boolean machineAvailable = getMachineName()
						.equals(requestedName)
						|| clientDictionary.containsKey(requestedName);
				addEvent(
					TCPEvent.createMachineAvailableInfo(
						Source.TCP_EVENT_TRANSMITTER,
						requestedName,
						machineAvailable));
			}
		});
	}

	private void addConnectedClientEventListener() {
		EventCondition transmitEventCondition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return (event.getAction()
						.equals(TCPEvent.Actions.REQUEST_CONNECTED_CLIENTS));
			}
		};
		collector.addListener(new EventListener(transmitEventCondition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				addEvent(
					TCPEvent.createConnectedClientInfo(
						Source.TCP_EVENT_TRANSMITTER,
						clientDictionary.entrySet()));
			}
		});
	}

	private void addTransmitBroadcastEventListener() {
		EventCondition transmitEventCondition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return (event.getAction()
						.equals(TCPEvent.Actions.SEND_EVENT_BROADCAST));
			}
		};
		collector.addListener(new EventListener(transmitEventCondition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				sendEventToAllClients((Event<?>) event.getValues().get(0));
			}
		});
	}

	private void addTransmitToMachineNameEventListener() {
		EventCondition transmitEventCondition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return (event.getAction().equals(
					TCPEvent.Actions.SEND_EVENT_TO_TARGET_MACHINE_NAME));
			}
		};
		collector.addListener(new EventListener(transmitEventCondition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				String targetMachineName = (String) event.getValues().get(0);
				if (targetMachineName.equals(getMachineName())) {
					addEvent((Event<?>) event.getValues().get(1));
				} else if (clientDictionary.containsKey(targetMachineName)) {
					sendEvent(
						clientDictionary.get(targetMachineName),
						(Event<?>) event.getValues().get(1));
				}
			}
		});
	}

	private void addTransmitToIPEventListener() {
		EventCondition transmitEventCondition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return (event.getAction()
						.equals(TCPEvent.Actions.SEND_EVENT_TO_TARGET_IP));
			}
		};
		collector.addListener(new EventListener(transmitEventCondition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				try {
					sendEvent(
						InetAddress
								.getByName((String) event.getValues().get(0)),
						(Event<?>) event.getValues().get(1));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void addEstablishConnectionListener() {
		EventCondition connectionEstablishConnection = new EventCondition(
				true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return (event.getAction()
						.equals(TCPEvent.Actions.ESTABLISH_TCP_CONNECTION)
						&& !((String) event.getValues().get(0)).equals(
							Utils.getLocalAddress().getHostAddress()));
			}
		};
		collector.addListener(
			new EventListener(connectionEstablishConnection, false) {
				@Override
				public void onEventOccured(Event<?> event) {
					createNewClient(
						(String) event.getValues().get(0),
						(int) event.getValues().get(1));
				}
			});
	}

	@Override
	protected void initialize() {
		clientDictionary = new HashMap<>();
		TCPEventTransmitter instance = this;
		clients = new LinkedList<>();
		try {
			server = new TCPServer(TCP_EVENT_RECEIVER_PORT) {
				@Override
				protected void handleReceivedObject(Object receivedObject) {
					instance.handleReceivedObject(receivedObject);
				}

				@Override
				protected void onNewTCPClientConnect(TCPClient client) {
					clients.add(client);
					sendEvent(
						client.getConnectedAddress(),
						TCPEvent.createMachineNameInfo(
							Source.TCP_EVENT_TRANSMITTER,
							Utils.getLocalAddress().getHostAddress(),
							getMachineName()));
				}

				@Override
				protected void onTCPClientDisconnect(TCPClient client) {
					removeClient(client);
				}
			};
			isAvailable = true;
			start();
		} catch (IOException e) {
			e.printStackTrace();
			isAvailable = false;
		}
	}

	private void handleReceivedObject(Object receivedObject) {
		if (receivedObject instanceof Event) {
			Event<?> event = (Event<?>) receivedObject;
			event.setSource(Source.TCP_EVENT_TRANSMITTER);
			collector.addEvent(event);
		}
	}

	private void sendConnectionRequests() {
		try {
			new UDPSender(UDPEventReceiver.UDP_EVENT_RECEIVER_PORT).sendObject(
				TCPEvent.createConnectionRequest(
					Source.TCP_EVENT_TRANSMITTER,
					Utils.getLocalAddress().getHostAddress(),
					TCP_EVENT_RECEIVER_PORT));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void registerClient(String clientIP, String machineName) {
		try {
			InetAddress address = InetAddress.getByName(clientIP);
			clientDictionary.put(machineName, address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void createNewClient(String targetIP, int targetPort) {
		try {
			TCPEventTransmitter instance = this;
			InetAddress address = InetAddress.getByName(targetIP);
			TCPClient client = new TCPClient(address, targetPort) {
				@Override
				protected void handleReceivedObject(Object receivedObject) {
					instance.handleReceivedObject(receivedObject);
				}

				@Override
				protected void handleShutdown() {
					removeClient(this);
				}
			};
			clients.add(client);
			sendEvent(
				client.getConnectedAddress(),
				TCPEvent.createMachineNameInfo(
					Source.TCP_EVENT_TRANSMITTER,
					Utils.getLocalAddress().getHostAddress(),
					getMachineName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removeClient(TCPClient client) {
		for (Iterator<Entry<String, InetAddress>> iterator = clientDictionary
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, InetAddress> entry = iterator.next();
			if (entry.getValue().equals(client.getConnectedAddress())) {
				iterator.remove();
				break;
			}
		}
		clients.remove(client);
	}

	private void sendEvent(InetAddress targetAddress, Event<?> event) {
		TCPClient client = getClientByInetAddress(targetAddress);
		if (client == null) {
			return;
		}
		client.sendPackage(event);
	}

	private void sendEventToAllClients(Event<?> event) {
		for (Iterator<TCPClient> iterator = clients.iterator(); iterator
				.hasNext();) {
			iterator.next().sendPackage(event);
		}
	}

	@Override
	public void updateAvailability() {
		if (server != null) {
			server.shutdown();
		}
		initialize();
	}

	@Override
	protected void start() {
		super.start();
		if (!isRunning) {
			return;
		}
		sendConnectionRequests();
	}

	@Override
	public void stop() {
		super.stop();
		if (isRunning) {
			return;
		}
		server.shutdown();
		for (TCPClient client : clients) {
			client.shutdown();
		}
	}

	private TCPClient getClientByInetAddress(InetAddress address) {
		for (TCPClient client : clients) {
			if (client.getConnectedAddress().equals(address)) {
				return client;
			}
		}
		return null;
	}

}
