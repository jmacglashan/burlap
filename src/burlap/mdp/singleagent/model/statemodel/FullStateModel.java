package burlap.mdp.singleagent.model.statemodel;

import burlap.mdp.core.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public interface FullStateModel extends SampleStateModel{
	List<StateTransitionProb> stateTransitions(State s, Action a);
}
