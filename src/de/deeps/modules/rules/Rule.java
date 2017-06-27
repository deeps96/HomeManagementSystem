package de.deeps.modules.rules;

import de.deeps.event.Event;
import de.deeps.event.Event.Source;
import de.deeps.event.EventCondition;

/**
 * @author Deeps
 */

public class Rule {

	private EventCondition condition;
	private Event<?> issueOnMatch;

	public Rule(EventCondition condition, Event<?> issueOnMatch) {
		this.condition = condition;
		this.issueOnMatch = issueOnMatch;
		issueOnMatch.setSource(Source.RULE_MANAGER);
	}

	public EventCondition getCondition() {
		return condition;
	}

	public Event<?> getIssueOnMatch() {
		return issueOnMatch;
	}

}
