package de.deeps.modules.lirc;

import java.util.LinkedList;
import java.util.List;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.IRRemoteEvent;
import de.deeps.event.moduleevents.LircEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class IRRemote extends Module {

	private List<IRRemoteInterface> remotes;

	public IRRemote(EventCollector collector, String machineName) {
		super(collector, Name.IR_REMOTE, machineName);
	}

	@Override
	protected void addEventListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(IRRemoteEvent.Actions.PRESS_KEY);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				IRRemoteInterface remote = getRemoteByName(
					(String) event.getValues().get(0));
				if (remote == null) {
					return;
				}
				String keyString = remote.mapButtonToKeystring(
					(String) event.getValues().get(1));
				collector.addEvent(
					LircEvent.createSendIR(
						Source.IR_REMOTE,
						remote.getRemoteName(),
						keyString,
						1));
			}
		});
	}

	protected IRRemoteInterface getRemoteByName(String string) {
		for (IRRemoteInterface remote : remotes) {
			if (remote.getRemoteName().equals(string)) {
				return remote;
			}
		}
		return null;
	}

	@Override
	protected void initialize() {
		remotes = new LinkedList<>();
		remotes.add(new IRRemoteLightCeiling());
		remotes.add(new IRRemoteSubwoofer());
		isAvailable = true;
	}

	@Override
	public void updateAvailability() {
	}

}
