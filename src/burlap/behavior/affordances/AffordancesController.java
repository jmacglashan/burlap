package burlap.behavior.affordances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.logicalexpressions.LogicalExpression;

public class AffordancesController {

	protected List<AffordanceDelegate> affordances;
	protected LogicalExpression currentGoal;
	protected HashMap<State,List<AbstractGroundedAction>> stateActionHash = new HashMap<State,List<AbstractGroundedAction>>();
	protected boolean cacheActionSets = true;
	
	public AffordancesController(List<AffordanceDelegate> affs) {
		this.affordances = affs;
	}
	
	/**
	 * Update the current goal, and change the state of each affordance accordingly.
	 * @param currentGoal
	 */
	public void setCurrentGoal(LogicalExpression currentGoal){
		this.currentGoal = currentGoal;
		for(AffordanceDelegate aff : this.affordances){
			aff.setCurrentGoal(currentGoal);
		}
	}
	
	/**
	 * Resets all of the action sets for each affordance (resamples)
	 */
	public void resampleActionSets(){
		for(AffordanceDelegate aff : this.affordances){
			aff.resampleActionSet();
		}
	}
	
	/**
	 * Takes the union of each affordance's
	 * @return
	 */
	public List<AbstractGroundedAction> getPrunedActionSetForState(State s) {
		
		// If we're caching actions and we've already seen this state
		if (cacheActionSets && stateActionHash.containsKey(s)) {
			return stateActionHash.get(s);
		}
		
		List<AbstractGroundedAction> actions = new ArrayList<AbstractGroundedAction>();
		for(AffordanceDelegate aff : this.affordances){
			// If affordance is active
			if(aff.primeAndCheckIfActiveInState(s)){
				for(AbstractGroundedAction aga : aff.listedActionSet) {
					// If that action wasn't added yet then we add all of them.
					if(!actions.contains(aga)) {
						actions.add(aga);
					}
				}
			}
			
		}
		
		// If we're caching, add the action set we just computed
		if(cacheActionSets) {
			stateActionHash.put(s, actions);
		}
		
		return actions;
	}
	
	/**
	 * Retrieves the list of relevant actions for a particular state, as pruned by affordances.
	 * @param actions: The set of actions to consider
	 * @param s: The current world state
	 * @return: A list of AbstractGroundedActions, the pruned action set.
	 */
	public List<AbstractGroundedAction> filterIrrelevantActionsInState(List<AbstractGroundedAction> actions, State s){
		
		// If we're caching actions and we've already seen this state
		if (cacheActionSets && stateActionHash.containsKey(s)) {
			return stateActionHash.get(s);
		}
		
		// Build active affordance list
		List<AffordanceDelegate> activeAffordances = new ArrayList<AffordanceDelegate>(this.affordances.size());
		for(AffordanceDelegate aff : this.affordances){
			if(aff.primeAndCheckIfActiveInState(s)){
				activeAffordances.add(aff);
			}
			else {
				System.out.println("(affController)aff not active in state: " + aff.getAffordance().toString());
			}
		}
		if(activeAffordances.size() == 0) {
			System.out.println("(affController)No affordances Active");
		}
		
		// Prune actions according to affordances
		List<AbstractGroundedAction> filteredList = new ArrayList<AbstractGroundedAction>(actions.size());
		for(AbstractGroundedAction a : actions){
			for(AffordanceDelegate aff : activeAffordances){
				if(aff.actionIsRelevant(a)){
					filteredList.add(a);
					break;
				}
			}
		}
		
		// If we're caching, add the action set we just computed
		if(cacheActionSets) {
			stateActionHash.put(s, filteredList);
		}
		
		return filteredList;
	}
	
}
