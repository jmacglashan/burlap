package burlap.mdp.singleagent.model.statemodel;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

/**
 * An interface for a model that can sample a state transition from the state transition function for a given input
 * state and action.
 * @author James MacGlashan.
 */
public interface SampleStateModel {

	/**
	 * Samples and returns a {@link State} from a state transition function.
	 * @param s the source state
	 * @param a the action to be executed in the source state
	 * @return a sample {@link State}
	 */
	State sample(State s, Action a);
}
