package burlap.mdp.stochasticgames.common;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.JointActionModel;

import java.util.ArrayList;
import java.util.List;


/**
 * This action model can be used to take a single stage game, and cause it to repeat itself.
 * This is achieved by simply having the same state returned after each joint action.
 * @author James MacGlashan
 *
 */
public class StaticRepeatedGameActionModel extends JointActionModel {

	public StaticRepeatedGameActionModel() {
		//nothing to do
	}

	@Override
	public List<StateTransitionProb> transitionProbsFor(State s, JointAction ja) {
		List <StateTransitionProb> res = new ArrayList<StateTransitionProb>();
		StateTransitionProb tp = new StateTransitionProb(s.copy(), 1.);
		res.add(tp);
		
		return res;
	}

	@Override
	protected State actionHelper(State s, JointAction ja) {
		//do nothing, the state simply repeats itself
		return s;
	}

}
