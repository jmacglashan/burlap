package burlap.behavior.affordances;

import java.util.Collection;
import java.util.List;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.logicalexpressions.LogicalExpression;

public class HardAffordance extends Affordance {

	private List<AbstractGroundedAction> prunedActions;

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
