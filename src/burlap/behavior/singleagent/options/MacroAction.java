package burlap.behavior.singleagent.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.behavior.policy.Policy.ActionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.common.SimpleGroundedAction;


/**
 * A macro action is an action that always executes a sequence of actions.
 * @author James MacGlashan
 *
 */
public class MacroAction extends Option {

	
	/**
	 * The list of actions that will be executed in order when this macro-action is called.
	 */
	protected List<GroundedAction>				actionSequence;
	
	/**
	 * the current execution index of the macro-action sequence. Every time this action is executed,
	 * it will start at index 0.
	 */
	protected int								curIndex;

	@Override
	public boolean applicableInState(State s, GroundedAction groundedAction) {
		return this.actionSequence.get(0).applicableInState(s);
	}

	@Override
	public boolean isParameterized() {
		return false;
	}

	@Override
	public GroundedAction getAssociatedGroundedAction() {
		return new SimpleGroundedAction(this);
	}

	@Override
	public List<GroundedAction> getAllApplicableGroundedActions(State s) {
		GroundedAction ga = new SimpleGroundedAction(this);
		return this.applicableInState(s, ga) ? Arrays.asList(ga) : new ArrayList<GroundedAction>(0);
	}

	/**
	 * Instantiates a macro action with a given name and action sequence. The name of the macro action
	 * should be unique from any other action name.
	 * @param name the name of the macro action.
	 * @param actionSequence the sequence of actions the macro action will execute.
	 */
	public MacroAction(String name, List<GroundedAction> actionSequence){
		this.name = name;
		this.actionSequence = new ArrayList<GroundedAction>(actionSequence);
	}
	
	@Override
	public boolean isMarkov() {
		return false;
	}

	@Override
	public boolean usesDeterministicTermination() {
		return true;
	}

	@Override
	public boolean usesDeterministicPolicy() {
		return true;
	}

	@Override
	public double probabilityOfTermination(State s, GroundedAction groundedAction) {
		if(curIndex >= actionSequence.size()){
			return 1.;
		}
		return 0.;
	}

	@Override
	public void initiateInStateHelper(State s, GroundedAction groundedAction) {
		curIndex = 0;
	}

	@Override
	public GroundedAction oneStepActionSelection(State s, GroundedAction groundedAction) {
		
		GroundedAction a = actionSequence.get(curIndex);
		curIndex++;
		
		return a;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s, GroundedAction groundedAction) {
		return this.getDeterministicPolicy(s, groundedAction);
	}

	

}
