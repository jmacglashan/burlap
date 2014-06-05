package burlap.behavior.affordances;

import java.util.Collection;
import java.util.List;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.logicalexpressions.LogicalExpression;

public class HardAffordance extends Affordance {

	private List<AbstractGroundedAction> prunedActions;

	/**
	 * A class for Hard Affordances, where a <Predicate,GoalDescription> pair maps to a specific set of actions
	 * @param preCondition
	 * @param goalDescription
	 * @param actions
	 */
	public HardAffordance(LogicalExpression preCondition, LogicalExpression goalDescription, List<AbstractGroundedAction> actions) {
		this.preCondition = preCondition;
		this.goalDescription = goalDescription;
		this.prunedActions = actions;
	}
	
	@Override
	public Collection<AbstractGroundedAction> sampleNewLiftedActionSet() {
		return this.prunedActions;
	}

}
