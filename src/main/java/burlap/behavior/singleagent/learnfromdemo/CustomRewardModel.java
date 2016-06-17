package burlap.behavior.singleagent.learnfromdemo;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.singleagent.model.TransitionProb;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public class CustomRewardModel implements FullModel {

	protected SampleModel model;
	protected RewardFunction rewardFunction;

	public CustomRewardModel(SampleModel model, RewardFunction rewardFunction) {
		this.model = model;
		this.rewardFunction = rewardFunction;
	}

	@Override
	public List<TransitionProb> transitions(State s, Action a) {
		List<TransitionProb> tps = ((FullModel)model).transitions(s, a);
		for(TransitionProb tp : tps){
			modifyOutcome(tp.eo);
		}
		return tps;
	}

	@Override
	public EnvironmentOutcome sample(State s, Action a) {
		return modifyOutcome(model.sample(s, a));
	}

	@Override
	public boolean terminal(State s) {
		return model.terminal(s);
	}

	protected EnvironmentOutcome modifyOutcome(EnvironmentOutcome eo){
		double nr = rewardFunction.reward(eo.o, eo.a, eo.op);
		eo.r = nr;
		return eo;
	}
}
