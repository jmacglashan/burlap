package burlap.behavior.affordances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import minecraft.MinecraftStateParser;
import minecraft.NameSpace;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.logicalexpressions.LogicalExpression;
import burlap.oomdp.singleagent.Action;

public class AffordancesController {

	protected List<AffordanceDelegate> affordances;
	public LogicalExpression currentGoal;
	protected HashMap<State,List<AbstractGroundedAction>> stateActionHash = new HashMap<State,List<AbstractGroundedAction>>();
	protected boolean cacheActionSets = false; // True when we only sample action sets each time we enter a state, then cache for later use. 
	
	
	public AffordancesController(List<AffordanceDelegate> affs) {
		this.affordances = affs;
	}
	
	public AffordancesController(List<AffordanceDelegate> affs, boolean cacheActionsInEachState) {
		this.affordances = affs;
		this.cacheActionSets = cacheActionsInEachState;
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
			if(aff.primeAndCheckIfActiveInState(s, currentGoal)){
				aff.resampleActionSet();
				for(AbstractGroundedAction aga : aff.listedActionSet) {
					// If that action wasn't added yet then add it
					if(!actions.contains(aga)) {
						actions.add(aga);
					}
				}
			}
			
		}
		
//		System.out.println("(AffordancesController) Affordance Action Set: ");
//		for(AbstractGroundedAction aga : actions) {
//			System.out.println("\t(AffordancesController) action: " + aga.actionName());
//		}
//		System.out.println("\n");
		
//		if (actions.size() == 0) {
//			 return full action set
//		}
		
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
			if(aff.primeAndCheckIfActiveInState(s, currentGoal)){
				activeAffordances.add(aff);
			}
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
	
	public void addAffordanceDelegate(AffordanceDelegate aff) {
		if(!this.affordances.contains(aff)) {
			this.affordances.add(aff);
		}
	}
	
	public void removeAffordance(AffordanceDelegate aff) {
		this.affordances.remove(aff);
	}
	
}
