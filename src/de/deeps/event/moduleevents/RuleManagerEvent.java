package de.deeps.event.moduleevents;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class RuleManagerEvent extends Event<RuleManagerEvent.Actions> {

	public static enum Actions {
		ENABLE_RULE, DISABLE_RULE, RELOAD_RULES
	}

	public RuleManagerEvent() {
		this(Source.UNKNOWN);
	}

	public RuleManagerEvent(Source source) {
		super(source);
	}

	public static RuleManagerEvent createEnableRuleRequest(Source source,
			String ruleName) {
		RuleManagerEvent event = new RuleManagerEvent(source);
		event.setAction(Actions.ENABLE_RULE);
		event.setType(Type.REQUEST);
		event.setValue(ruleName);
		return event;
	}

	public static RuleManagerEvent createDisableRuleRequest(Source source,
			String ruleName) {
		RuleManagerEvent event = new RuleManagerEvent(source);
		event.setAction(Actions.DISABLE_RULE);
		event.setType(Type.REQUEST);
		event.setValue(ruleName);
		return event;
	}

	public static RuleManagerEvent createReloadRulesRequest(Source source) {
		RuleManagerEvent event = new RuleManagerEvent(source);
		event.setAction(Actions.RELOAD_RULES);
		event.setType(Type.REQUEST);
		return event;
	}

}
