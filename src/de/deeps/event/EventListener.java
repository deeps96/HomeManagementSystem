package de.deeps.event;

/**
 * @author Deeps
 */

public abstract class EventListener {

	private boolean requestPreviousEvents;
	private EventCondition filter;

	public EventListener(EventCondition filter, boolean requestPreviousEvents) {
		this.filter = filter;
		this.requestPreviousEvents = requestPreviousEvents;
	}

	public abstract void onEventOccured(Event<?> event);

	public EventCondition getFilter() {
		return filter;
	}

	public boolean isRequestPreviousEvents() {
		return requestPreviousEvents;
	}
}
