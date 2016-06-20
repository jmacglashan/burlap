package burlap.mdp.stochasticgames.common;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.model.FullJointModel;

import java.util.List;


/**
 * This action model can be used to take a single stage game, and cause it to repeat itself.
 * This is achieved by simply having the same state returned after each joint action.
 * @author James MacGlashan
 *
 */
public class StaticRepeatedGameModel implements FullJointModel {

	public StaticRepeatedGameModel() {

	}

	@Override
	public State sample(State s, JointAction ja) {
		return s.copy();
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, JointAction ja) {
		return FullJointModel.Helper.deterministicTransition(this, s, ja);
	}

}
