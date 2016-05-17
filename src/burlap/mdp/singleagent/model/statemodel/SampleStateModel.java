package burlap.mdp.singleagent.model.statemodel;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

/**
 * @author James MacGlashan.
 */
public interface SampleStateModel {
	State sample(State s, Action a);
}
