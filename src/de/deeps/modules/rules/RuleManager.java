package de.deeps.modules.rules;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import de.deeps.event.Event;
import de.deeps.event.EventCollector;
import de.deeps.event.EventCondition;
import de.deeps.event.EventListener;
import de.deeps.event.moduleevents.RuleManagerEvent;
import de.deeps.modules.Module;

/**
 * @author Deeps
 */

public class RuleManager extends Module {

	public static final String RULES_DIR = "rules",
			EVENTS_DIR = "rules/action-events";

	private HashMap<Rule, EventListener> listeners;
	private List<Rule> rules;

	public RuleManager(EventCollector collector, String machineName) {
		super(collector, Name.RULE_MANAGER, machineName);
	}

	private void loadRulesAndRegisterListeners() {
		rules = RuleParser.loadAllRulesInDir(RULES_DIR);
		initializeListeners();
		for (EventListener listener : listeners.values()) {
			collector.addListener(listener);
		}
	}

	private void initializeListeners() {
		listeners = new HashMap<>();
		for (Rule rule : rules) {
			if (rule.getCondition() == null || rule.getIssueOnMatch() == null) {
				continue;
			}
			listeners.put(rule, new EventListener(rule.getCondition(), false) {
				@Override
				public void onEventOccured(Event<?> event) {
					if (rule.getIssueOnMatch() != null) {
						rule.getIssueOnMatch().updateTimestamp();
						collector.addEvent(rule.getIssueOnMatch());
					}
				}
			});
		}
	}

	@Override
	protected void addEventListener() {
		addDisableRuleListener();
		addEnableRuleListener();
		addReloadRuleListener();
	}

	private void addReloadRuleListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(RuleManagerEvent.Actions.RELOAD_RULES);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				reloadAllRules();
			}
		});
	}

	protected void reloadAllRules() {
		for (EventListener listener : listeners.values()) {
			collector.removeListener(listener);
		}
		loadRulesAndRegisterListeners();

	}

	private void addEnableRuleListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(RuleManagerEvent.Actions.ENABLE_RULE);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				ruleActivation((String) event.getValues().get(0), true);
			}
		});
	}

	private void ruleActivation(String ruleName, boolean enable) {
		String name;
		String end = ".bak";
		if (enable) {
			end = ".json";
		}
		for (File file : new File(RULES_DIR).listFiles()) {
			name = file.getName();
			if (name.contains(".")) {
				name = name.substring(0, file.getName().lastIndexOf('.'));
			}
			if (name.equals(ruleName)) {
				file.renameTo(new File(RULES_DIR + "/" + name + end));
				break;
			}
		}
	}

	private void addDisableRuleListener() {
		EventCondition condition = new EventCondition(true) {
			@Override
			public boolean doesApplyOnEvent(Event<?> event) {
				return event.getAction()
						.equals(RuleManagerEvent.Actions.DISABLE_RULE);
			}
		};
		collector.addListener(new EventListener(condition, false) {
			@Override
			public void onEventOccured(Event<?> event) {
				ruleActivation((String) event.getValues().get(0), false);
			}
		});
	}

	@Override
	protected void initialize() {
		loadRulesAndRegisterListeners();
		isAvailable = true;
	}

	@Override
	public void updateAvailability() {
	}

}
