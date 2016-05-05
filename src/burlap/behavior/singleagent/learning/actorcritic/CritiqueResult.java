package burlap.behavior.singleagent.learning.actorcritic;

import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;


/**
 * The CritiqueResult class stores the relevant information regarding a critique of behavior. Specifically, it contains
 * the value of the critique, the and the state-action-state tuple that is being critiqued.
 * 
 * @author James MacGlashan
 *
 */
public class CritiqueResult {

	
	/**
	 * The source state
	 */
	protected State					s;
	
	/**
	 * The action taken in state s
	 */
	protected GroundedAction		a;
	
	/**
	 * The state to which the agent transitioned for when it took action a in state s.
	 */
	protected State					sprime;
	
	/**
	 * The critique of this behavior.
	 */
	protected double				critique;
	
	
	/**
	 * Initializes with a state-action-state behavior tuple and the value of the critique for this behavior.
	 * @param s a source state
	 * @param a the action taken in state s
	 * @param sprime the state to which the agent transitioned for when it took action a in state s
	 * @param critique the critique of this behavior.
	 */
	public CritiqueResult(State s, GroundedAction a, State sprime, double critique) {
		this.s = s;
		this.a = a;
		this.sprime = sprime;
		this.critique = critique;
	}

	/**
	 * Returns the source state of this behavior.
	 * @return the source state of this behavior.
	 */
	public State getS() {
		return s;
	}

	
	/**
	 * Returns the action of this behavior.
	 * @return the action of this behavior.
	 */
	public GroundedAction getA() {
		return a;
	}

	
	/**
	 * Returns the resulting state of this behavior.
	 * @return the resulting state of this behavior.
	 */
	public State getSprime() {
		return sprime;
	}

	
	/**
	 * Returns the critique of this behavior.
	 * @return the critique of this behavior.
	 */
	public double getCritique() {
		return critique;
	}

	
	
}
