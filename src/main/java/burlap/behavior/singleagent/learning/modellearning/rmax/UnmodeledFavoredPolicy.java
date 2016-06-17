package burlap.behavior.singleagent.learning.modellearning.rmax;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.learning.modellearning.KWIKModel;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.core.action.ActionType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class UnmodeledFavoredPolicy implements Policy, EnumerablePolicy {

	protected Policy sourcePolicy;
	protected KWIKModel model;
	protected List<ActionType> allActionTypes;


	public UnmodeledFavoredPolicy(Policy sourcePolicy, KWIKModel model, List <ActionType> actionTypes){
		this.sourcePolicy = sourcePolicy;
		this.model = model;
		this.allActionTypes = actionTypes;
	}

	@Override
	public Action action(State s) {

		List<Action> unmodeled = KWIKModel.Helper.unmodeledActions(model, allActionTypes, s);

		if(!unmodeled.isEmpty()){
			return unmodeled.get(RandomFactory.getMapped(0).nextInt(unmodeled.size()));
		}

		return this.sourcePolicy.action(s);
	}

	@Override
	public double actionProb(State s, Action a) {
		List<Action> unmodeled = KWIKModel.Helper.unmodeledActions(model, allActionTypes, s);

		if(!unmodeled.isEmpty()){
			return 1. / unmodeled.size();
		}
		return this.sourcePolicy.actionProb(s, a);
	}

	@Override
	public List<ActionProb> policyDistribution(State s) {

		if(!(this.sourcePolicy instanceof EnumerablePolicy)){
			throw new RuntimeException("Cannot return policy distribution because source policy does not implement EnumerablePolicy");
		}

		List<Action> unmodeled = KWIKModel.Helper.unmodeledActions(model, allActionTypes, s);

		if(!unmodeled.isEmpty()){
			List<ActionProb> aps = new ArrayList<ActionProb>(unmodeled.size());
			double p = 1./(double)unmodeled.size();
			for(Action ga : unmodeled){
				aps.add(new ActionProb(ga, p));
			}
			return aps;
		}

		return ((EnumerablePolicy)this.sourcePolicy).policyDistribution(s);
	}


	@Override
	public boolean definedFor(State s) {
		return true;
	}
}
