package burlap.oomdp.singleagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import burlap.domain.singleagent.minecraft.Affordance;
import burlap.domain.singleagent.minecraft.OldAffordance;
import burlap.domain.singleagent.minecraft.MinecraftDomain;
import burlap.domain.singleagent.minecraft.OldAffordanceSubgoal;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;


/**
 * Abstract class for defining what happens when an action is executed in a state. The method getTransitions(State s, String [] params)
 * is what defines the transition dynamics of the MDP for this action. If this method is not overridden by subclasses, then the default
 * behavior is to assume deterministic transition dynamics which are produced by sampling the performAction(State s, String [] params)
 * method and setting its returned state as having a transition probability of 1. If the domain being created is only
 * going to be used planning/learning algorithms that require a generative model, rather than the fully enumerated transition
 * dynamics, then the getTransitions(State s, String [] params) does not need to be defined, but for full robustness it should be.
 * 
 * Action objects may also be defined to require object parameters (which must adhere to a type). Parameters can also have parameter order groups specified if
 * there is effect symmetry when changing the order of the parameters. That is, if you swapped the parameter assignments for parameters in the same order group, the action would have
 * the same effect. However, if you swapped the parameter assignments of two parameters in different order groups, the action would have a different effect. 
 * For more information on parameter order groups, see its discussion
 * in the {@link burlap.oomdp.core.PropositionalFunction} class description.
 * @author James MacGlashan
 *
 */
public abstract class Action {

	/**
	 * The name of the action that can uniquely identify it
	 */
	protected String					name;									
	
	/**
	 * The domain with which this action is associated
	 */
	protected Domain					domain;
	
	/**
	 * The object classes each parameter of this action can accept; empty list for a parameter-less action (which is the default)
	 */
	protected String []					parameterClasses = new String[0];
	
	/**
	 * Specifies the parameter order group each parameter. Parameters in the same order group are order invariant; that is, if you swapped the parameter assignments for for parameters in the same group, the action would have
	 * the same effect. However, if you swapped the parameter assignments of two parameters in different order groups, the action would have a different effect.
	 */
	protected String []					parameterOrderGroup = new String[0];
	
	
	/**
	 * An observer that will be notified of an actions results every time it is executed. By default no observer is specified.
	 */
	protected ActionObserver			observer = null;
	
	
	public Action(){
		//should not be called directly, but may be useful for subclasses of Action
	}
	
	
	/**
	 * Initializes the action with the name of the action, the domain to which it belongs, and the parameters it takes.
	 * The action will also be automatically be added to the domain. The parameter order group is set to be a unique order
	 * group for each parameter.
	 * @param name the name of the action
	 * @param domain the domain to which the action belongs
	 * @param parameterClasses a comma delineated String of the names of the object classes to which bound parameters must belong 
	 */
	public Action(String name, Domain domain, String parameterClasses){
		
		String [] pClassArray;
		if(parameterClasses.equals("")){
			pClassArray = new String[0];
		}
		else{
			pClassArray = parameterClasses.split(",");
		}
		
		//without parameter order group specified, all parameters are assumed to be in a different group
		String [] pog = new String[pClassArray.length];
		for(int i = 0; i < pog.length; i++){
			pog[i] = name + ".P" + i;
		}
		
		this.init(name, domain, pClassArray, pog);
		
	}
	
	
	/**
	 * Initializes the action with the name of the action, the domain to which it belongs, and the parameters it takes.
	 * The action will also be automatically be added to the domain. The parameter order group is set to be a unique order
	 * group for each parameter.
	 * @param name the name of the action
	 * @param domain the domain to which the action belongs
	 * @param parameterClasses a String array of the names of the object classes to which bound parameters must belong 
	 */
	public Action(String name, Domain domain, String [] parameterClasses){
		
		String [] pog = new String[parameterClasses.length];
		//without parameter order group specified, all parameters are assumed to be in a different group
		for(int i = 0; i < pog.length; i++){
			pog[i] = name + ".P" + i;
		}
		this.init(name, domain, parameterClasses, pog);
		
	}
	
	
	/**
	 * Initializes the action with the name of the action, the domain to which it belongs, the parameters it takes, and the parameter order groups.
	 * The action will also be automatically be added to the domain.
	 * @param name the name of the action
	 * @param domain the domain to which the action belongs
	 * @param parameterClasses a String array of the names of the object classes to which bound parameters must belong 
	 * @param parameterOrderGroups the order group assignments for each of the parameters.
	 */
	public Action(String name, Domain domain, String [] parameterClasses, String [] parameterOrderGroups){
		this.init(name, domain, parameterClasses, parameterOrderGroups);
	}
	
	
	protected void init(String name, Domain domain, String [] parameterClasses, String [] parameterOrderGroups){
		
		this.name = name;
		this.domain = domain;
		this.domain.addAction(this);
		this.parameterClasses = parameterClasses;
		this.parameterOrderGroup = parameterOrderGroups;
		
	}
	
	
	/**
	 * Returns the name of the action
	 * @return the name of the action
	 */
	public final String getName(){
		return name;
	}
	
	
	/**
	 * Returns a String array of the names of of the object classes to which bound parameters must belong
	 * @return a String array of the names of of the object classes to which bound parameters must belong. The array is empty if this action does not require parameters.
	 */
	public final String[] getParameterClasses(){
		return parameterClasses;
	}
	
	
	/**
	 * Returns the a String array specifying the parameter order group of each parameter.
	 * @return the a String array specifying the parameter order group of each parameter. The array is empty if this action does not require parameters.
	 */
	public final String[] getParameterOrderGroups(){
		return parameterOrderGroup;
	}
	
	/**
	 * Returns the domain to which this action belongs.
	 * @return the domain to which this action belongs.
	 */
	public final Domain getDomain(){
		return domain;
	}
	
	/**
	 * Sets an action observer for this action. Set to null to specify no observer or to disable observaiton.
	 * @param observer the observer that will be told of each event when this action is executed.
	 */
	public void setActionObserver(ActionObserver observer){
		this.observer = observer;
	}
	
	
	/**
	 * Returns true if this action can be applied in this specified state with the specified parameters.
	 * Default behavior is that an action can be applied in any state, but the {@link applicableInState(State, String [])}
	 * method will need to be override if this is not the case.
	 * @param s the state in which to check if this action can be applied
	 * @param params a comma delineated String specifying the action object parameters
	 * @return true if this action can be applied in this specified state with the specified parameters; false otherwise.
	 */
	public final boolean applicableInAffordanceState(State s, ArrayList<Affordance> kb){
		for(Affordance aff : kb) {
			// The affordance is applicable in this state, given the goal
//			System.out.println(this);
			if (aff.isApplicable(s, this.domain.getPropFunction("AtGoal"))) {
				if (aff.getActions().contains(this)) {
					// This action is applicable based on the affordance
					return true;
				}

			}
		}
		
		// No Affordances indicated that this was a good action in this state given the goal
		return false;

	}
	
	/**
	 * Returns true if this action can be applied in this specified state with the specified parameters.
	 * Default behavior is that an action can be applied in any state, but the {@link applicableInState(State, String [])}
	 * method will need to be override if this is not the case.
	 * @param s the state in which to check if this action can be applied
	 * @param params a comma delineated String specifying the action object parameters
	 * @return true if this action can be applied in this specified state with the specified parameters; false otherwise.
	 */
	public final boolean applicableInState(State s, String params){
		return applicableInState(s, params.split(","));
	}
	
	/**
	 * Returns true if this action can be applied in this specified state with the specified parameters.
	 * Default behavior is that an action can be applied in any state,
	 * but this will need be overridden if that is not the case.
	 * @param s the state to perform the action on
	 * @param params a String array specifying the action object parameters
	 * @return whether the action can be performed on the given state
	 */
	public boolean applicableInState(State s, String [] params){

		return true; 
	}
	
	
	public final boolean applicableInState(State st, Domain domain){
		if(!domain.affordanceMode) {
			return true;
		}
//		System.out.println(st.toString() + " -- " + this.name);
		
		// Get relevant Affordance based on subgoal.
		OldAffordance curAfford = getRelevAffordance(st, domain);
		List<OldAffordanceSubgoal> subgoals = curAfford.getSubgoals();
		
		// Breadth first search through affordance space
		
		LinkedList<OldAffordanceSubgoal> bfsQ = new LinkedList<OldAffordanceSubgoal>();
		bfsQ.addAll(subgoals);
		
		while(!bfsQ.isEmpty()) {
			OldAffordanceSubgoal sg = bfsQ.remove();
//			System.out.println(sg.getName());
			if (sg.isTrue(st)) {
				if (sg.inActions(this.name)) {
					// This action is associated with a relevant subgoal, return true.
					return true;
				}
				else if (sg.hasAffordance()) {
					// Subgoal's action isn't correct but it has an affordance
					// so let's try to follow it (later)
					OldAffordance af = sg.getAffordance();
					for (OldAffordanceSubgoal afSG: af.getSubgoals()) {
						if (afSG.isTrue(st) || !afSG.shouldSatisfy()) {
							// Either Subgoal is true or isn't a big deal so we take care of it now
							// Consider adding: if subGoal.inActions(this.name), return true
//							if (afSG.inActions(this.name)) {
//								return true;
//							}
//							else {
							bfsQ.add(afSG);
//							}
						}
						else if (afSG.shouldSatisfy()) {

							// Can't walk right, so we want to find a new y coord that lets us walk right
							Integer dx = Integer.parseInt(afSG.getParams()[0]);
							Integer dy = Integer.parseInt(afSG.getParams()[1]);
							Integer dz = Integer.parseInt(afSG.getParams()[2]);
							
							String[] oldParams = afSG.getParams();
							char dir = afSG.getName().charAt(afSG.getName().length() - 1);
							
							String[][] possibleParams = new String[2][3];
							int sgToSet = 0;
							// Change Y positively
							while(dy < MinecraftDomain.MAXY && dx < MinecraftDomain.MAXX) {
								if (dir == 'X') {
									dy++;
								}
								else if (dir == 'Y') {
									dx++;
								}
								String[] newParams = {dx.toString(), dy.toString(), dz.toString()};
								afSG.setParams(newParams);	
								
								if (afSG.isTrue(st)) {
									possibleParams[sgToSet] = newParams;
									sgToSet++;
									break;
								}
							}

							while (dy > -MinecraftDomain.MAXY && dx > -MinecraftDomain.MAXX) {
								if (dir == 'X') {
									dy--;
								}
								else if (dir == 'Y') {
									dx--;
								}
								String[] newParams = {dx.toString(), dy.toString(), dz.toString()};
								afSG.setParams(newParams);
								
								if (afSG.isTrue(st)) {
									possibleParams[sgToSet] = newParams;
									sgToSet++;
									break;
								}
							}
							
							//reset afSG params to one of the subgoals
							// GLOBAL
							String[] localParams;
							
							if (sgToSet == 2) {
								String[] globalPossibleParams1 = MinecraftDomain.locCoordsToGlobal(st, possibleParams[0]);
								String[] globalPossibleParams2 = MinecraftDomain.locCoordsToGlobal(st, possibleParams[1]);
								
								if (domain.prevSatSubgoal != null) {
									localParams = domain.prevSatSubgoal.chooseGoodSubgoal(globalPossibleParams1, globalPossibleParams2);
								}
								else {
									localParams = domain.goalStack.peek().chooseGoodSubgoal(globalPossibleParams1, globalPossibleParams2);
								}
								// Now these are local
								localParams = MinecraftDomain.globCoordsToLocal(st, localParams);
								
							}
							else {
								// Only found one possible subgoal, use its global parameters for afSG
								// Local
								localParams = possibleParams[0];
							}
							afSG.setParams(localParams);
							
							// isTrue requires relative coordinates
							if (afSG.isTrue(st) && afSG.hasSubGoal()) {
								System.out.println("SUBGOAL TIME");
								String[] globalParams = MinecraftDomain.locCoordsToGlobal(st, localParams);
								
								int constraintDir = (int)dir - 88;
								boolean isConstraintLessThan = (Integer.parseInt(oldParams[constraintDir]) < 0);
								
								afSG.getSubgoal().setParams(globalParams, constraintDir, isConstraintLessThan);  // (int)'X' == 88
								
								// For now only isWalkablePX - should loop and find the place were X is 
								// walkable and make isAtLocation of that walkable X the new subgoal
								if (!domain.goalStack.peek().getName().equals(afSG.getSubgoal().getName()) || !domain.goalStack.peek().getParams().equals(afSG.getSubgoal().getParams())) {
									domain.goalStack.add(afSG.getSubgoal());									
									System.out.println("I am trying out a new subgoal!");
								}
								else {
//									domain.goalStack.add(afSG.getSubgoal());
								}
								
								curAfford = getRelevAffordance(st, domain);
//								curAfford.setSubGoalParams(globParams);
								subgoals = curAfford.getSubgoals();
								bfsQ.clear();
								
								for (OldAffordanceSubgoal newSG: subgoals) {
									if (!newSG.getName().equals(sg.getName())) {
										bfsQ.add(newSG);		
									}
								}
							}
							afSG.setParams(oldParams);
						}
					}
				}
				else if (sg.hasSubGoal()) {
					if (sg.getSubgoal().inActions(this.name)) {
						return true;
					}
				}
				
			}
			else {
//				System.out.println(curAfford.getName());
			}
		}

		// Action was not found in relevant affordances/subgoals
		return false;
	}
	
	
	/**
	 * Default behavior is that an action can be applied in any state
	 * , but this might need be overridden if that is not the case.
	 * @param st the state to perform the action on
	 * @param params list of parameters to be passed into the action
	 * @return whether the action can be performed on the given state
	 */
	public OldAffordance getRelevAffordance(State st, Domain domain){

		// pop stack, search affordance list for string of thing popped, perform that action.
		
		OldAffordanceSubgoal goal = domain.goalStack.peek();

		while (goal.isTrue(st)) {
			domain.prevSatSubgoal = domain.goalStack.pop();
			domain.prevSatSubgoal.switchConstraint();
			goal = domain.goalStack.peek();
		}
		
		HashMap<String,OldAffordance> affordances = domain.affordances;
		String goalName = goal.getName();
		OldAffordance curAfford = affordances.get("d" + goalName);
		String[] globParams = MinecraftDomain.locCoordsToGlobal(st, goal.getParams());
		curAfford.setSubGoalParams(globParams);
//		int[] delta = goal.delta(st);
//
//		String[] locGoalCoords = {"" + delta[0], "" + delta[1], "" + delta[2]};
//		
//		String[] globalCoords = MinecraftDomain.locCoordsToGlobal(st, locGoalCoords);
		
//		curAfford.setSubGoalParams(globalCoords);
		
		curAfford.setSubGoalParams(goal.getParams());
		
		return curAfford;
	}
	
	
	/**
	 * Performs this action in the specified state using the specified parameters and returns the resulting state. The input state
	 * will not be modified. The method will return a copy of the input state if the action is not applicable in state s with parameters params.
	 * @param s the state in which the action is to be performed.
	 * @param params a comma delineated String specifying the action object parameters
	 * @return the state that resulted from applying this action
	 */
	public final State performAction(State s, String params){
		return performAction(s, params.split(","));
		
	}
	
	
	/**This is a wrapper for performActionHelper that first performs a check to see whether the action is applicable to the current state.
	 * @param st the state to perform the action on
	 * @param params list of parameters to be passed into the action
	 * @return the modified State st
	 */
	public final State performAction(State st, String [] params){
		
		State resultState = st.copy();
		/*if(params.length == 0) {
			// Affordance case
			if(!this.applicableInState(st, domain)){
				return resultState; //can't do anything if it's not applicable in the state so return the current state
			}
		}
		else if(!this.applicableInState(st, params)){
			return resultState; //can't do anything if it's not applicable in the state so return the current state
		}*/
		if(!this.applicableInState(st, params)){
			return resultState; //can't do anything if it's not applicable in the state so return the current state
		}
		return performActionHelper(resultState, params);
		
	}
	
	
	/**
	 * Returns whether this action is a primitive action of the domain or not. A primitive action
	 * is defined to be an action that always takes one time step.
	 * @return true if the action is primitive; false otherwise.
	 */
	public boolean isPrimitive(){
		return true;
	}
	
	
	/**
	 * Returns the transition probabilities for applying this action in the given state with the given set of parameters.
	 * Transition probabilities are specified as list of {@link burlap.oomdp.core.TransitionProbability} objects. The list
	 * is only required to contain transitions with non-zero probability. By default, this method assumes that transition
	 * dynamics are deterministic and it returns a list with a single TransitionProbability with probability 1 whose
	 * state is determined by querying the {@link performAction(State, String [])} method. If the transition dynamics
	 * are stochastic, then the analogous method {@link getTransitions(State, String [])} needs to be overridden.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param params a comma delineated String specifying the action object parameters
	 * @return a List of transition probabilities for applying this action in the given state with the given set of parameters
	 */
	public final List<TransitionProbability> getTransitions(State s, String params){
		return this.getTransitions(s, params.split(","));
	}
	
	
	
	/**
	 * Returns the transition probabilities for applying this action in the given state with the given set of parameters.
	 * Transition probabilities are specified as list of {@link burlap.oomdp.core.TransitionProbability} objects. The list
	 * is only required to contain transitions with non-zero probability. By default, this method assumes that transition
	 * dynamics are deterministic and it returns a list with a single TransitionProbability with probability 1 whose
	 * state is determined by querying the {@link performAction(State, String [])} method. If the transition dynamics
	 * are stochastic, then this method needs to be overridden.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param params a String array specifying the action object parameters
	 * @return a List of transition probabilities for applying this action in the given state with the given set of parameters
	 */
	public List<TransitionProbability> getTransitions(State s, String [] params){
		
		List <TransitionProbability> transition = new ArrayList<TransitionProbability>();
		State res = this.performAction(s, params);
		transition.add(new TransitionProbability(res, 1.0));
		
		return transition;
	}
	
	/**
	 * This method determines what happens when an action is applied in the given state with the given parameters. The State
	 * object s may be directly modified in this method since the parent method first copies the input state to pass
	 * to this helper method. The resulting state (which may be s) should then be returned.
	 * @param s the state to perform the action on
	 * @param params a String array specifying the action object parameters
	 * @return the resulting State from performing this action
	 */
	protected abstract State performActionHelper(State s, String [] params);
	
	
	
	
	public boolean equals(Object obj){
		Action op = (Action)obj;
		if(op.name.equals(name))
			return true;
		return false;
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	
}
