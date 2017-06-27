package de.deeps.event;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Deeps
 */

public class EventCollector {

	private static final int MAX_EVENTS = 25000;

	private Queue<Event<?>> events;
	private Queue<EventListener> listeners;

	public EventCollector() {
		events = new LinkedBlockingQueue<>(MAX_EVENTS);
		listeners = new LinkedBlockingQueue<>();
	}

	public synchronized void addEvent(Event<?> event) {
		System.out.println(event); // debug
		if (event == null) {
			return;
		}
		for (EventListener listener : listeners) {
			if (checkEventWithListener(listener, event)) {
				return;
			}
		}
		if (events.size() == MAX_EVENTS) {
			events.remove();
		}
		events.add(event);
	}

	private synchronized boolean checkEventWithListener(EventListener listener,
			Event<?> event) {
		if (listener.getFilter().doesApplyOnEvent(event)) {
			listener.onEventOccured(event);
			if (listener.getFilter().isConsumeAfterMatch()) {
				return true;
			}
		}
		return false;
	}

	public Iterator<Event<?>> retriveElementsMatchingCondition(
			EventCondition eventCondition) {
		PriorityQueue<Event<?>> retrievedElements = new PriorityQueue<>();
		Event<?> currentEvent;
		for (Iterator<Event<?>> iterator = events.iterator(); iterator
				.hasNext();) {
			currentEvent = iterator.next();
			if (eventCondition.doesApplyOnEvent(currentEvent)) {
				retrievedElements.add(currentEvent);
				if (eventCondition.isConsumeAfterMatch()) {
					iterator.remove();
				}
			}
		}
		return retrievedElements.iterator();
	}

	public synchronized void addListener(EventListener listener) {
		listeners.add(listener);
		if (listener.isRequestPreviousEvents()) {
			for (Iterator<Event<?>> iterator = retriveElementsMatchingCondition(
				listener.getFilter()); iterator.hasNext();) {
				listener.onEventOccured(iterator.next());
			}
		}
	}

	public void removeListener(EventListener listener) {
		listeners.remove(listener);
	}

}
