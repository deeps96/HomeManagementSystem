package de.deeps.event;

/**
 * @author Deeps
 */

public abstract class EventCondition {

	private boolean consumeAfterMatch;

	public EventCondition(boolean consumeAfterMatch) {
		this.consumeAfterMatch = consumeAfterMatch;
	}

	public abstract boolean doesApplyOnEvent(Event<?> event);

	public boolean isConsumeAfterMatch() {
		return consumeAfterMatch;
	}

}
