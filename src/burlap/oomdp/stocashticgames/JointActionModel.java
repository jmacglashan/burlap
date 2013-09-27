package burlap.oomdp.stocashticgames;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;


public abstract class JointActionModel {

	public State performJointAction(State s, JointAction ja){
		State sp = s.copy();
		this.actionHelper(sp, ja);
		return sp;
	}

	
	public abstract List<TransitionProbability> transitionProbsFor(State s, JointAction ja);
	
	//this is what will define the state generator
	protected abstract void actionHelper(State s, JointAction ja);
	

	
	protected List<TransitionProbability> deterministicTransitionProbsFor(State s, JointAction ja){
		List <TransitionProbability> res = new ArrayList<TransitionProbability>();
		State sp = performJointAction(s, ja);
		TransitionProbability tp = new TransitionProbability(sp, 1.);
		res.add(tp);
		return res;
	}

}
