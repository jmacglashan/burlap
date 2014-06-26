package burlap.behavior.affordances;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.logicalexpressions.LogicalExpression;
import burlap.oomdp.logicalexpressions.PFAtom;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class AffordanceDelegate {

	 protected Affordance								affordance;
	 protected Collection<AbstractGroundedAction>		listedActionSet;
	 
	 public AffordanceDelegate(Affordance affordance){
		 this.affordance = affordance;
		 this.resampleActionSet();
	 }
	 
	 public void resampleActionSet(){
		 this.listedActionSet = affordance.sampleNewLiftedActionSet();
	 }
	 
	 public void setCurrentGoal(LogicalExpression currentGoal){
		 //TODO: fill this in; should set current goal; check if this affordance does not satisfy; and handle variable bindings with lifted affordance goal if it is satisifed.
	 }
	 
	 /**
	  * Primes this affordance to answer if actions are relevant for the given state (using method {@link #actionIsRelevant(AbstractGroundedAction)})
	  * and return whether this affordance is active for the given state and using the currently set goal as the task (sub)goal.
	  * If this affordance is not active,
	  * then any subsequent queries to {@link #isActionRelevant(AbstractGroundedAction)} will return false.
	  * An affordance is determined to be active if its preconditions are satisifed in s and if the current task goal
	  * entails the affordance lifted goal description.
	  * @param s the state in which to prime the affordance
	  * @return true if this affordance is active, false if it is not.
	  */
	 public boolean primeAndCheckIfActiveInState(State s){
		 //TODO: Add goal description here
		 if(this.affordance.preCondition.evaluateIn(s)) {
			 return true;
		 }
		 
		 return false;
	 }
	 
	 public boolean actionIsRelevant(AbstractGroundedAction action){
		 if(this.listedActionSet.contains(action)) {
			 return true;
		 }
		 
		 return false;
	 }
	 
	 public Affordance getAffordance() {
		 return this.affordance;
	 }
	 
	 /**
	 * Assumes that the header is on the first line it reads
	 * @param d: domain
	 * @param scnr: scanner associated with the knowledge base file
	 */
	public static AffordanceDelegate loadSoft(Domain d, Scanner scnr) {
		String line;
		boolean readHeader = true;
		boolean readActCounts = true;
		
		LogicalExpression preCondition = null;
		LogicalExpression goal = null;
		
		int[] actionNumCounts = null;
		HashMap<AbstractGroundedAction,Integer> actionCounts = new HashMap<AbstractGroundedAction,Integer>();
		while (scnr.hasNextLine()) {
			line = scnr.nextLine();
			
			if (line.equals("===")) {
				// Reached the end of an affordance definition
				break;
			}
			
			if (line.equals("---")) {
				// Finished reading action counts -- ready to start reading action set sizes
				readActCounts = false;
				actionNumCounts = new int[actionCounts.size()];
				continue;
			}
			
			String[] info = line.split(",");
			
			if (readHeader) {
				// We haven't read the header yet, so do that
				
				// TODO: Change to parsing a logical expression (instead of assuming a single pf)
				String pfName = info[0];
				String goalName = info[1];
				
				// -- Create Precondition -- 
				PropositionalFunction preCondPF = d.getPropFunction(pfName);
				
				// Get grounded prop free variables
				String[] groundedPropPreCondFreeVars = makeFreeVarListFromObjectClasses(preCondPF.getParameterClasses());
				
				GroundedProp preCondGroundedProp = new GroundedProp(preCondPF, groundedPropPreCondFreeVars);
				preCondition = new PFAtom(preCondGroundedProp);
				
				// -- Create GOAL --
				PropositionalFunction goalPF = d.getPropFunction(pfName);
				
				String[] groundedPropGoalFreeVars = makeFreeVarListFromObjectClasses(preCondPF.getParameterClasses());
				
				GroundedProp goalGroundedProp = new GroundedProp(goalPF, groundedPropGoalFreeVars);
				goal = new PFAtom(goalGroundedProp);
				
				readHeader = false;
				continue;
			}
			
			if (readActCounts) {
				// Read the action counts
				String actName = info[0];
				Integer count = Integer.parseInt(info[1]);
				
				// Get action free variables
				Action act = d.getAction(actName);
				String[] actionParams = makeFreeVarListFromObjectClasses(act.getParameterClasses());
				
				GroundedAction ga = new GroundedAction(act, actionParams);
				actionCounts.put(ga, count);
			} else {
				// Read the action set size counts
				Integer size = Integer.parseInt(info[0]);
				Integer count = Integer.parseInt(info[1]);
				
				actionNumCounts[size] = count;
			}
			
		}
		
		// Create the Affordance
		List<AbstractGroundedAction> allActions = new ArrayList<AbstractGroundedAction>(actionCounts.keySet()); 
		Affordance aff = new SoftAffordance(preCondition, goal, allActions);
		((SoftAffordance)aff).setActionCounts(actionCounts);
		((SoftAffordance)aff).setActionNumCounts(actionNumCounts);

		AffordanceDelegate affDelegate = new AffordanceDelegate(aff);
		
		return affDelegate;
	}
	
	/**
	 * Gets a list of free variables given an OOMDP object's parameter object classes and order groups
	 * @param orderGroups
	 * @param objectClasses
	 * @return: String[] - a list of free variables
	 */
	public static String[] makeFreeVarListFromObjectClasses(String[] objectClasses){
		List<String> groundedPropFreeVariablesList = new ArrayList<String>();
		
		// TODO: improve variable binding stuff
		// Make variables free
		for(String objectClass : objectClasses){
			String freeVar = "?" + objectClass.charAt(0);
			groundedPropFreeVariablesList.add(freeVar);
		}
		String[] groundedPropFreeVars = new String[groundedPropFreeVariablesList.size()];
		groundedPropFreeVars = groundedPropFreeVariablesList.toArray(groundedPropFreeVars);
		
		return groundedPropFreeVars;
	}
	 
}
