package burlap.behavior.policy;

import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.policy.support.PolicyUndefinedException;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.singleagent.SADomain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A uniform random policy for single agent domains. You may set the actions between which it randomly
 * selects by providing a domain (from which the domains primitive actions are copied into an internal list)
 * or from a list of Action objects (from which the action references are copied into an internal list).
 * You may also add additional actions with the {@link #addAction(ActionType)} method
 * or remove or clear the actions.
 * <p>
 * Upon action selection, all applicable grounded actions for the state are generated and an action is selected
 * uniformly randomly from them. The policy is not defined if there are no applicable actions.
 */
public class RandomPolicy implements Policy{


	/**
	 * The actions from which selection is performed
	 */
	protected List<ActionType> actionTypes;

	/**
	 * The random factory used to randomly select actions.
	 */
	protected Random rand = RandomFactory.getMapped(0);


	/**
	 * Initializes by copying all the primitive actions references defined for the domain into an internal action
	 * list for this policy.
	 * @param domain the domain containing all the primitive actions.
	 */
	public RandomPolicy(SADomain domain){
		this.actionTypes = new ArrayList<ActionType>(domain.getActionTypes());
	}

	/**
	 * Initializes by copying all the actions references defined in the provided list into an internal action
	 * list for this policy.
	 * @param acitons the actions to select between.
	 */
	public RandomPolicy(List<ActionType> acitons){
		this.actionTypes = new ArrayList<ActionType>(actionTypes);
	}


	/**
	 * Adds an aciton to consider in selection.
	 * @param actionType an action to consider in selection
	 */
	public void addAction(ActionType actionType){
		this.actionTypes.add(actionType);
	}


	/**
	 * Clears the action list used in action selection. Note that if no actions are added to this policy after
	 * calling this method then the policy will be undefined everywhere.
	 */
	public void clearActions(){
		this.actionTypes.clear();
	}


	/**
	 * Removes an action from consideration.
	 * @param actionName the name of the action to remove.
	 */
	public void removeAction(String actionName){
		ActionType toRemove = null;
		for(ActionType a : this.actionTypes){
			if(a.typeName().equals(actionName)){
				toRemove = a;
				break;
			}
		}
		if(toRemove != null){
			this.actionTypes.remove(toRemove);
		}
	}

	/**
	 * Returns of the list of actions that can be randomly selected.
	 * @return the list of actions that can be randomly selected.
	 */
	public List<ActionType> getSelectionActions(){
		return this.actionTypes;
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
	public Action action(State s) {
		List<Action> gas = ActionUtils.allApplicableActionsForTypes(this.actionTypes, s);
		if(gas.isEmpty()){
			throw new PolicyUndefinedException();
		}
		Action selection = gas.get(this.rand.nextInt(this.actionTypes.size()));
		return selection;
	}

	@Override
	public List<ActionProb> policyDistribution(State s) {
		List<Action> gas = ActionUtils.allApplicableActionsForTypes(this.actionTypes, s);
		if(gas.isEmpty()){
			throw new PolicyUndefinedException();
		}
		double p = 1./gas.size();
		List<ActionProb> aps = new ArrayList<ActionProb>(gas.size());
		for(Action ga : gas){
			ActionProb ap = new ActionProb(ga, p);
			aps.add(ap);
		}
		return aps;
	}

	@Override
	public boolean stochastic() {
		return true;
	}

	@Override
	public boolean definedFor(State s) {
		return ActionUtils.allApplicableActionsForTypes(this.actionTypes, s).size() > 0;
	}
}
