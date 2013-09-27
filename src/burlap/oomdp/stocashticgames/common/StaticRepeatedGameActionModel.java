package burlap.oomdp.stocashticgames.common;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.stocashticgames.JointAction;
import burlap.oomdp.stocashticgames.JointActionModel;


public class StaticRepeatedGameActionModel extends JointActionModel {

	public StaticRepeatedGameActionModel() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<TransitionProbability> transitionProbsFor(State s, JointAction ja) {
		List <TransitionProbability> res = new ArrayList<TransitionProbability>();
		TransitionProbability tp = new TransitionProbability(s.copy(), 1.);
		res.add(tp);
		
		return res;
	}

	@Override
	protected void actionHelper(State s, JointAction ja) {
		//do nothing, the state simply repeats itself
	}

}
