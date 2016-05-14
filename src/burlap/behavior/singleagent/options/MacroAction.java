package burlap.behavior.singleagent.options;

import burlap.behavior.policy.Policy.ActionProb;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A macro action is a non-Markov option that always executes a fixed sequence of actions
 * @author James MacGlashan
 *
 */
public class MacroAction implements Option {


	/**
	 * The name of the action
	 */
	protected String name;

	/**
	 * The list of actions that will be executed in order when this macro-action is called.
	 */
	protected List<Action> actionSequence;
	
	/**
	 * the current execution index of the macro-action sequence. Every time this action is executed,
	 * it will start at index 0.
	 */
	protected int curIndex;


	/**
	 * Instantiates a macro action with a given name and action sequence. The name of the macro action
	 * should be unique from any other action name.
	 * @param name the name of the macro action.
	 * @param actionSequence the sequence of actions the macro action will execute.
	 */
	public MacroAction(String name, List<Action> actionSequence){
		this.name = name;
		this.actionSequence = new ArrayList<Action>(actionSequence);
	}



	@Override
	public double probabilityOfTermination(State s) {
		if(curIndex >= actionSequence.size()){
			return 1.;
		}
		return 0.;
	}

	@Override
	public void initiateInState(State s) {
		curIndex = 0;
	}

	@Override
	public boolean inInitiationSet(State s) {
		return true;
	}

	@Override
	public Action oneStep(State s) {
		
		Action a = actionSequence.get(curIndex++);
		return a;
	}

	@Override
	public List<ActionProb> oneStepProbabilities(State s) {
		return Arrays.asList(new ActionProb(actionSequence.get(curIndex), 1.));
	}

	@Override
	public EnvironmentOptionOutcome control(Environment env, double discount) {
		return Option.Helper.control(this, env, discount);
	}

	@Override
	public boolean markov() {
		return false;
	}

	@Override
	public String actionName() {
		return this.name;
	}

	@Override
	public Action copy() {
		return new MacroAction(name, this.actionSequence);
	}

	public int actionSequenceSize(){
		return this.actionSequence.size();
	}

	public void setCurActionSequenceIndex(int ind){
		this.curIndex = ind;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		MacroAction that = (MacroAction) o;

		return name.equals(that.name);

	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
