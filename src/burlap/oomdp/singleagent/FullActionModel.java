package burlap.oomdp.singleagent;

import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;

import java.util.List;

/**
 * An interface to be used with {@link burlap.oomdp.singleagent.Action} objects that support returning the full
 * Action transition dynamics. Many planning algorithms, such as Dynamic programming methods, require the full transition dynamics,
 * so if you wish to use such an algorithm and it is possible to fully enumerate the transition dynamics, your {@link burlap.oomdp.singleagent.Action}
 * implementation should implement this interface.
 * The required {@link #getTransitions(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)} method provides the full transition dynamics of
 * an {@link burlap.oomdp.singleagent.Action}.  This method should return a list of all transitions from the input {@link burlap.oomdp.core.states.State}
 * that have non-zero probability of occurring. These transitions are specified with a {@link burlap.oomdp.core.TransitionProbability}
 * object that is a pair consisting of the next {@link burlap.oomdp.core.states.State} and a double specifying the probability
 * of transitioning to that state.
 * @author James MacGlashan.
 */
public interface FullActionModel {

	/**
	 * Returns the transition probabilities for applying this action in the given state with the given set of parameters.
	 * Transition probabilities are specified as list of {@link burlap.oomdp.core.TransitionProbability} objects. The list
	 * is only required to contain transitions with non-zero probability.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param groundedAction the {@link burlap.oomdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return a List of transition probabilities for applying this action in the given state with the given set of parameters
	 */
	List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction);
}
