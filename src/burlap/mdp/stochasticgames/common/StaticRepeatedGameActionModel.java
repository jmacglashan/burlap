package burlap.mdp.stochasticgames.common;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.state.State;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.JointActionModel;


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
	public List<TransitionProbability> transitionProbsFor(State s, JointAction ja) {
		List <TransitionProbability> res = new ArrayList<TransitionProbability>();
		TransitionProbability tp = new TransitionProbability(s.copy(), 1.);
		res.add(tp);
		
		return res;
	}

	@Override
	protected State actionHelper(State s, JointAction ja) {
		//do nothing, the state simply repeats itself
		return s;
	}

}
