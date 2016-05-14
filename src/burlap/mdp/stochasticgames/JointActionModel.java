package burlap.mdp.stochasticgames;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.agentactions.SGAgentAction;

import java.util.ArrayList;
import java.util.List;

/**
 * This abstract class provides the interface and comment mechanisms for defining
 * a stochastic game's transition dynamics. The {@link #performJointAction(State, JointAction)} method
 * first makes a copy of a state and passes it to the {@link #actionHelper(State, JointAction)} method,
 * which is what should be overridden by subclasses. The {@link #transitionProbsFor(State, JointAction)}
 * method defines the transition probabilities and should also be overridden by subclasses if
 * this model is to be used with planning algorithms that require it.
 * 
 * @author James MacGlashan
 *
 */
public abstract class JointActionModel {

	
	/**
	 * Performs {@link JointAction} ja in {@link State} s and returns the result.
	 * The input state is not modified by this operation.
	 * @param s the state in which the joint action is performed.
	 * @param ja the joint action to be performed
	 * @return the resutling state.
	 */
	public State performJointAction(State s, JointAction ja){
		
		//first make sure that every action satisfies the necessary preconditions
		for(SGAgentAction gsa : ja){
			if(!gsa.applicableInState(s)){
				throw new RuntimeException("The action " + gsa.toString() + " is not applicable in this state.");
			}
		}
		
		State sp = s.copy();
		sp = this.actionHelper(sp, ja);
		return sp;
	}

	
	/**
	 * Returns the transition probabilities for applying the provided {@link JointAction} action in the given state.
	 * Transition probabilities are specified as list of {@link StateTransitionProb} objects. The list
	 * is only required to contain transitions with non-zero probability.
	 * @param s the state in which the joint action is performed
	 * @param ja the joint action performed
	 * @return a list of state {@link StateTransitionProb} objects.
	 */
	public abstract List<StateTransitionProb> transitionProbsFor(State s, JointAction ja);
	
	
	
	/**
	 * This method is what determines the state when {@link JointAction} ja is executed in {@link State} s.
	 * The input state should be directly modified.
	 * @param s the state in which the joint action is performed.
	 * @param ja the joint action to be performed.
	 * @return the resulting state of applying this action.
	 */
	protected abstract State actionHelper(State s, JointAction ja);
	

	
	/**
	 * A helper method for deterministic transition dynamics. This method will return a list containing
	 * one {@link StateTransitionProb} object which is assigned probability 1
	 * and whose state is determined by querying the {@link #performJointAction(State, JointAction)}
	 * method.
	 * @param s the state in which the joint action would be executed
	 * @param ja the joint action to be performed in the state.
	 * @return a list containing one {@link StateTransitionProb} object which is assigned probability 1
	 */
	protected List<StateTransitionProb> deterministicTransitionProbsFor(State s, JointAction ja){
		List <StateTransitionProb> res = new ArrayList<StateTransitionProb>();
		State sp = performJointAction(s, ja);
		StateTransitionProb tp = new StateTransitionProb(sp, 1.);
		res.add(tp);
		return res;
	}

}
