package burlap.behavior.affordances;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.logicalexpressions.LogicalExpression;

public class AffordancesController {

	protected List<AffordanceDelegate> affordances;
	protected LogicalExpression currentGoal;
	
	
	
	public void setCurrentGoal(LogicalExpression currentGoal){
		this.currentGoal = currentGoal;
		for(AffordanceDelegate aff : this.affordances){
			aff.setCurrentGoal(currentGoal);
		}
	}
	
	public void resampleActionSets(){
		for(AffordanceDelegate aff : this.affordances){
			aff.resampleActionSet();
		}
	}
	
	public List<AbstractGroundedAction> filterIrrelevantActionsInState(List<AbstractGroundedAction> actions, State s){
		
		List<AffordanceDelegate> activeAffordances = new ArrayList<AffordanceDelegate>(this.affordances.size());
		for(AffordanceDelegate aff : this.affordances){
			if(aff.primeAndCheckIfActiveInState(s)){
				activeAffordances.add(aff);
			}
		}
		
		List<AbstractGroundedAction> filteredList = new ArrayList<AbstractGroundedAction>(actions.size());
		for(AbstractGroundedAction a : actions){
			for(AffordanceDelegate aff : activeAffordances){
				if(aff.actionIsRelevant(a)){
					filteredList.add(a);
					break;
				}
			}
		}
		
		return filteredList;
	}
	
}
