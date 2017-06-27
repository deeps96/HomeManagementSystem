package de.deeps.modules.webserver;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.PCControllerEvent;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class Website extends Module {

	private final int SERVER_PORT = 5555;
	public static final String SUBMIT_URI = "/submit",
			OVERVIEW_URI = "/overview", WEBSERVER_ROOT_DIR = "website";

	private FileHandler fileHandler;
	private HttpServer server;
	private OverviewHandler overviewHandler;
	private SubmitHandler submitHandler;

	public Website(EventCollector collector, String machineName) {
		super(collector, Name.WEBSITE, machineName);
	}

	private void createMultipleContext(HttpServer server) {
		server.createContext(SUBMIT_URI, submitHandler);
		server.createContext(OVERVIEW_URI, overviewHandler);
		createFileContext(server, new File(WEBSERVER_ROOT_DIR));
	}

	private void createFileContext(HttpServer server, File file) {
		if (file.getName().equals("overview.html")) {
			return;
		}
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				createFileContext(server, child);
			}
			return;
		}
		String filePath = file.getAbsolutePath().replace("\\", "/");
		server.createContext(
			filePath.substring(
				filePath.indexOf(WEBSERVER_ROOT_DIR)
						+ WEBSERVER_ROOT_DIR.length()),
			fileHandler);
	}

	@Override
	protected void initialize() {
		try {
			server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
			submitHandler = new SubmitHandler(collector);
			fileHandler = new FileHandler();
			overviewHandler = new OverviewHandler(collector, this);
			createMultipleContext(server);
			isAvailable = true;
			start();
		} catch (IOException e) {
			e.printStackTrace();
			isAvailable = false;
		}

	}

	@Override
	protected void start() {
		super.start();
		if (isRunning && server != null) {
			server.start();
		}
	}

	@Override
	public void stop() {
		super.stop();
		if (!isRunning && server != null) {
			server.stop(0);
		}
	}

	@Override
	public void updateAvailability() {
		initialize();
	}

	@Override
	protected void addEventListener() {
		addMachineAvailableListener();
		addPCVolumeListener();
	}

	private void addPCVolumeListener() {
		EventCondition condition = new EventCondition(false) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(PCControllerEvent.Actions.VOLUME_INFO);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				overviewHandler.setPcVolume((int) event.getValues().get(0));
			}
		});
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
				overviewHandler.getAvailableMachines().put(
					(String) event.getValues().get(0),
					(Boolean) event.getValues().get(1));
			}
		});
	}

	public void updateAll() {
		collector.addEvent(
			TCPEvent.createMachineAvailableRequest(
				Source.WEBSITE,
				"raspberry"));
		collector.addEvent(
			TCPEvent.createMachineAvailableRequest(Source.WEBSITE, "standpc"));
		collector.addEvent(
			TCPEvent.createSendEventToMachineName(
				Source.WEBSITE,
				"standpc",
				PCControllerEvent.createVolumeRequest(Source.WEBSITE)));
		try {
			Thread.sleep(300L);
		} catch (InterruptedException e) {
		}
	}
}
