package burlap.mdp.singleagent;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * An interface to be used with {@link burlap.mdp.singleagent.Action} objects that support returning the full
 * Action transition dynamics. Many planning algorithms, such as Dynamic programming methods, require the full transition dynamics,
 * so if you wish to use such an algorithm and it is possible to fully enumerate the transition dynamics, your {@link burlap.mdp.singleagent.Action}
 * implementation should implement this interface.
 * The required {@link #getTransitions(State, burlap.mdp.singleagent.GroundedAction)} method provides the full transition dynamics of
 * an {@link burlap.mdp.singleagent.Action}.  This method should return a list of all transitions from the input {@link State}
 * that have non-zero probability of occurring. These transitions are specified with a {@link burlap.mdp.core.TransitionProbability}
 * object that is a pair consisting of the next {@link State} and a double specifying the probability
 * of transitioning to that state.
 * <p>
 * Also defined is a an inner static helper class called {@link burlap.mdp.singleagent.FullActionModel.FullActionModelHelper}
 * that has static helper methods that may be useful for working with an {@link burlap.mdp.singleagent.Action} that implements
 * {@link burlap.mdp.singleagent.FullActionModel}. Specifically, it includes a method for sampling a state
 * from the transition distribution defined with {@link #getTransitions(State, GroundedAction)}
 * and it includes a method for removing {@link burlap.mdp.core.TransitionProbability} elements from a list
 * that are assigned zero probability.
 * @author James MacGlashan.
 */
public interface FullActionModel {

	/**
	 * Returns the transition probabilities for applying this action in the given state with the given set of parameters.
	 * Transition probabilities are specified as list of {@link burlap.mdp.core.TransitionProbability} objects. The list
	 * is only required to contain transitions with non-zero probability.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param groundedAction the {@link burlap.mdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return a List of transition probabilities for applying this action in the given state with the given set of parameters
	 */
	List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction);


	/**
	 * A class with helper methods for working with actions that implement {@link burlap.mdp.singleagent.FullActionModel}.
	 */
	public static class FullActionModelHelper{
	    
	    private FullActionModelHelper() {
	        // do nothing
	    }


		/**
		 * Samples a state from fully enumerated transition dynamics that are defined with the {@link burlap.mdp.singleagent.FullActionModel}
		 * {@link burlap.mdp.singleagent.FullActionModel#getTransitions(State, GroundedAction)} method.
		 * This method is implement by first getting the enumerated transitions, rolling a random number, and selecting a next
		 * state transition from the enumerated transitions according to their assigned probability.
		 * @param previousState the {@link State} in which an action is to be performed
		 * @param action the {@link burlap.mdp.singleagent.GroundedAction} to apply to previousState
		 * @return a sampled {@link State}
		 */
		public static State sampleTransitionFromEnumeratedDistribution(State previousState, GroundedAction action){

			if(!(action.action instanceof FullActionModel)){
				throw new RuntimeException("Cannot sample a transition from the enumerated transition distribution, because the input action does not implement FullActionModel.");
			}

			List<TransitionProbability> tps = action.getTransitions(previousState);
			double roll = RandomFactory.getMapped(0).nextDouble();
			double sum = 0.;
			for(TransitionProbability tp : tps){
				sum += tp.p;
				if(roll < sum){
					return tp.s;
				}
			}

			throw new RuntimeException("Cannot sample a transition because the transition probabilities did not sum to 1; they summed to " + sum);

		}


		/**
		 * Takes a list of fully enumerated {@link State} transitions specified with {@link burlap.mdp.core.TransitionProbability}
		 * objects, and returns a new list of {@link burlap.mdp.core.TransitionProbability} objects that excludes any objects assigned probability 0.
		 * @param sourceTransitions the original list of state transitions
		 * @return a new list of {@link burlap.mdp.core.TransitionProbability} objects without any with probability 0.
		 */
		public static List<TransitionProbability> removeZeroProbTransitions(List<TransitionProbability> sourceTransitions){

			List<TransitionProbability> nonZeroes = new ArrayList<TransitionProbability>(sourceTransitions.size());
			for(TransitionProbability tp : sourceTransitions){
				if(tp.p > 0){
					nonZeroes.add(tp);
				}
			}

			return nonZeroes;

		}

	}

}
