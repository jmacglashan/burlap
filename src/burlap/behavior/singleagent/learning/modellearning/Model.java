package burlap.behavior.singleagent.learning.modellearning;

import java.util.List;
import java.util.Random;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

/**
 * This abstract class is used by model learning algorithms to learn the transition dynamics, reward function, and terminal function, of the world through experience.
 * The interface is used to return the modeled reward function, terminal function, and transition dynamics (or a sample from the transition dynamics)
 * which can be used with any planning algorithm to generate a policy for how to act in the real world.
 * It also specifies {@link #transitionIsModeled(State, GroundedAction)} method, which can be used to determine when the model is confident in its predictions from a given input state and action. This is useful for RMax
 * algorithms which treat "unknown" states-action (or under sampled state-actions) specially.
 * 
 * @author James MacGlashan
 *
 */
public abstract class Model {

	/**
	 * Random number generator
	 */
	protected Random rand = RandomFactory.getMapped(0);
	
	/**
	 * Returns the learned reward function in this model
	 * @return the learned reward function in this model
	 */
	public abstract RewardFunction getModelRF();
	
	/**
	 * Returns the learned terminal function in this model
	 * @return the learned terminal function in this model
	 */
	public abstract TerminalFunction getModelTF();
	
	/**
	 * Indicates whether this model "knows" how the transition dynamics from the given input state and action work.
	 * @param s the state that is checked
	 * @param ga the action to take in state s
	 * @return true if the transition dynamics from the input state and action are "known;" false otherwise.
	 */
	public abstract boolean transitionIsModeled(State s, GroundedAction ga);


	/**
	 * Indicates whether this model "knows" the transition dynamics from the given input state for all applicable actions.
	 * @param s the state that is checked.
	 * @return true if the transition dynamics for all actions are "known;" false otherwise.
	 */
	public abstract boolean stateTransitionsAreModeled(State s);


	/**
	 * Returns a list specifying the actions for which the transition dynamics are not yet "known."
	 * @param s the state for which the un-modeled actions should be returned.
	 * @return a {@link java.util.List} of {@link AbstractGroundedAction} objects
	 */
	public abstract List<AbstractGroundedAction> getUnmodeledActionsForState(State s);

	/**
	 * A method to sample this model's transition dynamics for the given state and action.
	 * @param s The source state
	 * @param ga the action taken in the source state
	 * @return a new State object instance, which is a sampled result of taking the given action in the given state.
	 */
	public final State sampleModel(State s, GroundedAction ga){
		State sc = s.copy();
		return this.sampleModelHelper(sc, ga);
	}
	
	
	/**
	 * A helper method to sample this model's transition dynamics for the given state and action.
	 * @param s a copied source state which can be directly modified.
	 * @param ga the action taken in the source state
	 * @return a State which is a sampled result of taking the given action in the given state. This may be the same object as State s.
	 */
	public abstract State sampleModelHelper(State s, GroundedAction ga);
	
	
	/**
	 * Returns this model's transition probabilities for the given source state and action
	 * @param s the source state
	 * @param ga an action taken in the source state
	 * @return the list of the possible transition probabilities; should sum to 1 and states will probability zero do *not* have to be included.
	 */
	public abstract List<TransitionProbability> getTransitionProbabilities(State s, GroundedAction ga);


	/**
	 * Updates this model with respect to the observed {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome}.
	 * @param eo The {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} specifying the observed interaction with an {@link burlap.oomdp.singleagent.environment.Environment}.
	 */
	public void updateModel(EnvironmentOutcome eo){
		this.updateModel(eo.o, eo.a, eo.op, eo.r, eo.terminated);
	}

	
	/**
	 * Causes this model to be updated with a new interaction with the world.
	 * @param s a source state
	 * @param ga an action that was taken in the source state
	 * @param sprime the state to which the agent transitioned for taking action 
	 * @param r a reward that was received for taking the given action in the given source state and transitioning to the given next state
	 * @param sprimeIsTerminal whether the next state is a terminal state or not
	 */
	public abstract void updateModel(State s, GroundedAction ga, State sprime, double r, boolean sprimeIsTerminal);
	
	
	/**
	 * Resets the model data so that learning can begin anew.
	 */
	public abstract void resetModel();

	
	/**
	 * Will return a sampled outcome state by calling the {@link #getTransitionProbabilities(State, GroundedAction)} method and randomly drawing a state
	 * according to its distribution
	 * @param s a source state
	 * @param ga the action taken in the source state
	 * @return a sampled outcome state
	 */
	protected State sampleTransitionFromTransitionProbabilities(State s, GroundedAction ga){
		
		List <TransitionProbability> tps = this.getTransitionProbabilities(s, ga);
		double sum = 0.;
		double r = rand.nextDouble();
		for(TransitionProbability tp : tps){
			sum += tp.p;
			if(r < sum){
				return tp.s;
			}
		}
		
		throw new RuntimeException("Transition probabilities did not sum to 1; they summed to: " + sum);
	}
	
}
