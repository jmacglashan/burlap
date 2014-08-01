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
	 protected boolean									active = false;
	 protected boolean									goalActive = false;
	 private final static double						actCountPercentThreshold = 0.01;
	 
	 public AffordanceDelegate(Affordance affordance){
		 this.affordance = affordance;
		 this.resampleActionSet();
	 }
	 
	 public void resampleActionSet(){
		 this.listedActionSet = affordance.sampleNewLiftedActionSet();
//		 System.out.println("ACTION SET: ");
//		 for(AbstractGroundedAction a : this.listedActionSet) {
//			 System.out.println(a.actionName());
//		 }
//		 System.out.println("\n");
	 }
	 
	 public void setCurrentGoal(LogicalExpression currentGoal){
		 //TODO: fill this in; should set current goal; check if this affordance does not satisfy; and handle variable bindings with lifted affordance goal if it is satisifed.
		 if(this.affordance.goalDescription.toString().equals(currentGoal.toString())) {
			 this.goalActive = true;
		 }
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
	 public boolean primeAndCheckIfActiveInState(State s, LogicalExpression lgd){
//		 if(!this.goalActive) {
//			 this.active = false;
//			 return false;
//		 }
		 if(this.affordance.goalDescription.toString().equals(lgd.toString()) && this.affordance.preCondition.evaluateIn(s)) {
			 this.active = true;
			 return true;
		 }
		 this.active = false;
		 return false;
	 }
	 
	 public boolean actionIsRelevant(AbstractGroundedAction action){
		 if(this.active && this.listedActionSet.contains(action)) {
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
				String preCondLE= info[0];
				String goalName = info[1];
				
				// -- Create PRECONDITION -- 
				PropositionalFunction preCondPF = d.getPropFunction(preCondLE);
				
				// Get grounded prop free variables
				String[] groundedPropPreCondFreeVars = makeFreeVarListFromObjectClasses(preCondPF.getParameterClasses());
				GroundedProp preCondGroundedProp = new GroundedProp(preCondPF, groundedPropPreCondFreeVars);
				preCondition = new PFAtom(preCondGroundedProp);
				
				// -- Create GOAL --
				PropositionalFunction goalPF = d.getPropFunction(goalName);
				
				// Get grounded prop free variables
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
//				System.out.println("(affDelegate)actionName: " + actName);
				String[] actionParams = makeFreeVarListFromObjectClasses(act.getParameterClasses());
				
				GroundedAction ga = new GroundedAction(act, actionParams);
				actionCounts.put(ga, count);
			} 
			else {
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
		((SoftAffordance)aff).postProcess();

		AffordanceDelegate affDelegate = new AffordanceDelegate(aff);
		
		return affDelegate;
	}
	
	/**
	 * Reads in a hard affordance from a knowledge base file.
	 * Assumes that the header is on the first line it reads
	 * @param d: domain
	 * @param scnr: scanner associated with the knowledge base file
	 */
	public static AffordanceDelegate loadHard(Domain d, Scanner scnr) {
		String line;
		boolean readHeader = true;
		boolean readActCounts = true;
		
		LogicalExpression preCondition = null;
		LogicalExpression goal = null;
		
		List<AbstractGroundedAction> actions = new ArrayList<AbstractGroundedAction>();
		Map<String,Integer> actionCounts = new HashMap<String,Integer>();
		while (scnr.hasNextLine()) {
			line = scnr.nextLine();
			
			if (line.equals("===")) {
				// Reached the end of an affordance definition
				break;
			}
			
			if (line.equals("---")) {
				// Finished reading action counts -- skip reading action set sizes (scanner jumps over rest)
				readActCounts = false;
				
				// Calculate total number of action counts
				int totalCount = 0;
				for(Integer count : actionCounts.values()) {
					totalCount += count;
				}
				
				// Add actions that have more than 15% of the counts
				for(String actName : actionCounts.keySet()) {
//					System.out.println("(AffordanceDelegate) percentage, count: " + ((double)actionCounts.get(actName)) / ((double) totalCount) + "," + actionCounts.get(actName));
					if(((double)actionCounts.get(actName)) / ((double) totalCount) >= actCountPercentThreshold) {
						// Get action free variables
						Action act = d.getAction(actName);
	//					System.out.println("(affDelegate)actionName: " + actName);
						String[] actionParams = makeFreeVarListFromObjectClasses(act.getParameterClasses());
						
						GroundedAction ga = new GroundedAction(act, actionParams);
						actions.add(ga);
					}
				}
//				System.out.println("(AffordanceDelegate) actionSet: \n");
//				for(AbstractGroundedAction ga : actions){
//					System.out.println("\t(AffordanceDelegate) action: " + ga.actionName());
//				}
//				System.out.println("\n");
				
				continue;
			}
			
			String[] info = line.split(",");
			
			if (readHeader) {
				// We haven't read the header yet, so do that
				
				// TODO: Change to parsing a logical expression (instead of assuming a single pf)
				String preCondLE= info[0];
				String goalName = info[1];
				
				// -- Create Precondition -- 
				PropositionalFunction preCondPF = d.getPropFunction(preCondLE);
				
				// Get grounded prop free variables
				String[] groundedPropPreCondFreeVars = makeFreeVarListFromObjectClasses(preCondPF.getParameterClasses());
				GroundedProp preCondGroundedProp = new GroundedProp(preCondPF, groundedPropPreCondFreeVars);
				preCondition = new PFAtom(preCondGroundedProp);
				
				// -- Create GOAL --
				PropositionalFunction goalPF = d.getPropFunction(goalName);
//				System.out.println("(AffordanceDelegate) goalPf: " + goalPF.getName());
				// Get grounded prop free variables
				String[] groundedPropGoalFreeVars = makeFreeVarListFromObjectClasses(goalPF.getParameterClasses());
				GroundedProp goalGroundedProp = new GroundedProp(goalPF, groundedPropGoalFreeVars);
				goal = new PFAtom(goalGroundedProp);
				
				readHeader = false;
				continue;
			}
			
			if (readActCounts) {
				// Read the action counts
				String actName = info[0];
				actionCounts.put(actName, Integer.parseInt(info[1]));
//				if(count >= actCountThreshold) {
//					// Get action free variables
//					Action act = d.getAction(actName);
////					System.out.println("(affDelegate)actionName: " + actName);
//					String[] actionParams = makeFreeVarListFromObjectClasses(act.getParameterClasses());
//					
//					GroundedAction ga = new GroundedAction(act, actionParams);
//					actions.add(ga);
//				}
			}
			
		}
		
		// Create the Hard Affordance
		Affordance aff = new HardAffordance(preCondition, goal, actions);
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
	
	public String toString() {
		return "[" + this.affordance.preCondition + "," + this.affordance.goalDescription + "]";
	}
	 
}
