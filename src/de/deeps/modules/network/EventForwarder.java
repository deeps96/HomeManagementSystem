package de.deeps.modules.network;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.Event.Type;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.TCPEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class EventForwarder extends Module {

	public EventForwarder(EventCollector collector, String machineName) {
		super(collector, Name.EVENT_FORWARDER, machineName);
	}

	@Override
	protected void addEventListener() {
		EventCondition condition = new EventCondition(false) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return (event.getType().equals(Type.INFO)
						|| event.getType().equals(Type.DETECT))
						&& !event.getSource()
								.equals(Source.TCP_EVENT_TRANSMITTER)
						&& !event.getSource().equals(Source.EVENT_FORWARDER);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				collector.addEvent(
					TCPEvent.createSendBroadcastEvent(
						Source.EVENT_FORWARDER,
						event));
			}
		});
	}

	@Override
	protected void initialize() {
		isAvailable = true;
	}

	@Override
	public void updateAvailability() {
	}

}
