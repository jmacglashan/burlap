package burlap.behavior.policy;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.AbstractGroundedAction;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.Action;
import burlap.mdp.singleagent.GroundedAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A uniform random policy for single agent domains. You may set the actions between which it randomly
 * selects by providing a domain (from which the domains primitive actions are copied into an internal list)
 * or from a list of Action objects (from which the action references are copied into an internal list).
 * You may also add additional actions with the {@link #addAction(burlap.mdp.singleagent.Action)} method
 * or remove or clear the actions.
 * <p>
 * Upon action selection, all applicable grounded actions for the state are generated and an action is selected
 * uniformly randomly from them. The policy is not defined if there are no applicable actions.
 */
public class RandomPolicy extends Policy{


	/**
	 * The actions from which selection is performed
	 */
	protected List<Action> actions;

	/**
	 * The random factory used to randomly select actions.
	 */
	protected Random rand = RandomFactory.getMapped(0);


	/**
	 * Initializes by copying all the primitive actions references defined for the domain into an internal action
	 * list for this policy.
	 * @param domain the domain containing all the primitive actions.
	 */
	public RandomPolicy(Domain domain){
		this.actions = new ArrayList<Action>(domain.getActions());
	}

	/**
	 * Initializes by copying all the actions references defined in the provided list into an internal action
	 * list for this policy.
	 * @param acitons the actions to select between.
	 */
	public RandomPolicy(List<Action> acitons){
		this.actions = new ArrayList<Action>(actions);
	}


	/**
	 * Adds an aciton to consider in selection.
	 * @param action an action to consider in selection
	 */
	public void addAction(Action action){
		this.actions.add(action);
	}


	/**
	 * Clears the action list used in action selection. Note that if no actions are added to this policy after
	 * calling this method then the policy will be undefined everywhere.
	 */
	public void clearActions(){
		this.actions.clear();
	}


	/**
	 * Removes an action from consideration.
	 * @param actionName the name of the action to remove.
	 */
	public void removeAction(String actionName){
		Action toRemove = null;
		for(Action a : this.actions){
			if(a.getName().equals(actionName)){
				toRemove = a;
				break;
			}
		}
		if(toRemove != null){
			this.actions.remove(toRemove);
		}
	}

	/**
	 * Returns of the list of actions that can be randomly selected.
	 * @return the list of actions that can be randomly selected.
	 */
	public List<Action> getSelectionActions(){
		return this.actions;
	}


	/**
	 * Returns the random generator used for action selection.
	 * @return the random generator used for action selection.
	 */
	public Random getRandomGenerator(){
		return this.rand;
	}


	/**
	 * Sets the random generator used for action selection.
	 * @param rand the random generator used for action selection.
	 */
	public void setRandomGenerator(Random rand){
		this.rand = rand;
	}


	@Override
	public AbstractGroundedAction getAction(State s) {
		List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, s);
		if(gas.isEmpty()){
			throw new PolicyUndefinedException();
		}
		GroundedAction selection = gas.get(this.rand.nextInt(this.actions.size()));
		return selection;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, s);
		if(gas.isEmpty()){
			throw new PolicyUndefinedException();
		}
		double p = 1./gas.size();
		List<ActionProb> aps = new ArrayList<ActionProb>(gas.size());
		for(GroundedAction ga : gas){
			ActionProb ap = new ActionProb(ga, p);
			aps.add(ap);
		}
		return aps;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}

	@Override
	public boolean isDefinedFor(State s) {
		return Action.getAllApplicableGroundedActionsFromActionList(this.actions, s).size() > 0;
	}
}
