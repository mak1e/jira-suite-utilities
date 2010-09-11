package com.googlecode.jsu.workflow.condition;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.workflow.condition.AbstractJiraCondition;
import static com.atlassian.plugin.util.Assertions.notNull;
import com.googlecode.jsu.helpers.ComparisonType;
import com.googlecode.jsu.helpers.ConditionCheckerFactory;
import com.googlecode.jsu.helpers.ConditionType;
import com.googlecode.jsu.util.WorkflowUtils;
import com.opensymphony.module.propertyset.PropertySet;

/**
 * This condition evaluates if a given field fulfills the condition.
 * 
 * @author Gustavo Martin
 */
public class ValueFieldCondition extends AbstractJiraCondition {
	private final Logger log = Logger.getLogger(ValueFieldCondition.class);
	
	private final ConditionCheckerFactory conditionCheckerFactory;
	
	public ValueFieldCondition() {
		// This code is specific for 4.0 version only. 
		// At 4.1 must be constructor injection 
		this.conditionCheckerFactory = ComponentManager.getOSGiComponentInstanceOfType(
				ConditionCheckerFactory.class
		);

		notNull("conditionCheckerFactory", conditionCheckerFactory);
	}

	/* (non-Javadoc)
	 * @see com.opensymphony.workflow.Condition#passesCondition(java.util.Map, java.util.Map, com.opensymphony.module.propertyset.PropertySet)
	 */
	public boolean passesCondition(Map transientVars, Map args, PropertySet ps) {
		Issue issue = getIssue(transientVars);

		String fieldId = (String) args.get("fieldsList");
		String valueForCompare = (String) args.get("fieldValue");

		ComparisonType comparison = conditionCheckerFactory.findComparisonById((String) args.get("comparisonType"));
		ConditionType condition = conditionCheckerFactory.findConditionById((String) args.get("conditionList"));

		boolean result = false;
		
		try { 
			Field field = WorkflowUtils.getFieldFromKey(fieldId);
			Object fieldValue = WorkflowUtils.getFieldValueFromIssue(issue, field);
			
			result = conditionCheckerFactory.
					getChecker(comparison, condition).
					checkValues(fieldValue, valueForCompare);
			
			if (log.isDebugEnabled()) {
				log.debug(
						"Comparing field '" + fieldId + 
						"': [" + fieldValue + "]" + 
						condition.getValue() + 
						"[" + valueForCompare + "] as " + 
						comparison.getValue() + " = " + result
				);
			}
		} catch (Exception e) {
			log.error("Unable to compare values for field '" + fieldId + "'", e);
		}
		
		return result;
	}
}
