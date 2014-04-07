package burlap.behavior.affordances;

import java.util.Collection;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.logicalexpressions.LogicalExpression;

public abstract class Affordance {

	protected LogicalExpression preCondition;
	protected LogicalExpression	goalDesription;
	
	
	public abstract Collection<AbstractGroundedAction> sampleNewLiftedActionSet();
		
	
}
