package de.deeps.modules.radioremote;

import de.deeps.event.Event;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.RadioControllerEvent;
import de.deeps.modules.Module;
import de.deeps.radiosocket.RadioSocket;

/**
 * @author Deeps
 */

public class RadioRemote extends Module {

	public static enum Button {
		A, B, C, D
	}

	private RadioSocket socket;

	public RadioRemote(EventCollector collector, String machineName) {
		super(collector, Name.RADIO_REMOTE, machineName);
	}

	@Override
	protected void addEventListener() {
		if (!isAvailable) {
			return;
		}
		addSendRadioSignalForButtonListener();
		addSendRadioSignalForButtonIDListener();
	}

	private void addSendRadioSignalForButtonIDListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(
					RadioControllerEvent.Actions.SEND_RADIO_SIGNAL_FOR_BUTTON_STRING);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				try {
					socket.send(
						RadioSocket.DEFAULT_REMOTE,
						radioEventButtonToChar(
							Button.valueOf((String) event.getValues().get(0))),
						(Boolean) event.getValues().get(1));
				} catch (Exception e) {
					e.printStackTrace();
					isAvailable = false;
				}
			}
		});
	}

	private void addSendRadioSignalForButtonListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction().equals(
					RadioControllerEvent.Actions.SEND_RADIO_SIGNAL_FOR_BUTTON);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				try {
					socket.send(
						RadioSocket.DEFAULT_REMOTE,
						radioEventButtonToChar(
							(Button) event.getValues().get(0)),
						(Boolean) event.getValues().get(1));
				} catch (Exception e) {
					e.printStackTrace();
					isAvailable = false;
				}
			}
		});
	}

	private char radioEventButtonToChar(Button button) {
		return button.toString().charAt(0);
	}

	@Override
	protected void initialize() {
		try {
			socket = new RadioSocket();
			isAvailable = true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			isAvailable = false;
		}
	}

	@Override
	public void updateAvailability() {
	}

}
