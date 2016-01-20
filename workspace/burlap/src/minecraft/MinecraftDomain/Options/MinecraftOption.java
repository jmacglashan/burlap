package minecraft.MinecraftDomain.Options;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public abstract class MinecraftOption extends Option {
	public abstract GroundedAction getGroundedAction(State state);
	public abstract boolean shouldInitiate(State state);
	public abstract boolean shouldTerminate(State state);
	public abstract void initiateOptionVariables();
	public abstract void updateVariablesAfterOneAction();
	
	protected Domain domain;
	protected State state;
	
	public MinecraftOption(String name, State state, Domain domain, RewardFunction rf, double gamma, StateHashFactory hashFactory) {
		super(name, domain, "");
		this.state = state;
		this.domain = domain;
		this.keepTrackOfRewardWith(rf, gamma);
		this.setExpectationHashingFactory(hashFactory);
	}
	
	@Override
	public List<ActionProb> getActionDistributionForState(State state, String[] params) {
		List<ActionProb> actionProbs = new ArrayList<ActionProb>();
		actionProbs.add(new ActionProb(this.getGroundedAction(state), 1.0));
		updateVariablesAfterOneAction();
		return actionProbs;
	}

	@Override
	public void initiateInStateHelper(State arg0, String[] arg1) {
		initiateOptionVariables();
	}

	@Override
	public boolean isMarkov() {
		return true;
	}

	@Override
	public GroundedAction oneStepActionSelection(State state, String[] arg1) {
		GroundedAction toReturn = getGroundedAction(state);
		updateVariablesAfterOneAction();

		return toReturn;
	}

	@Override
	public double probabilityOfTermination(State state, String[] arg1) {
		if (shouldTerminate(state)) return 1.;
		return 0.;
	}

	@Override
	public boolean usesDeterministicPolicy() {
		return true;
	}

	@Override
	public boolean usesDeterministicTermination() {
		return true;
	}
	
	@Override 
	public boolean applicableInState(State state, String[] params) {
		return shouldInitiate(state);
	}
	
	protected GroundedAction getGAByActionName(String name) {
		GroundedAction toReturn = this.domain.getAction(name).getAllApplicableGroundedActions(state).get(0);
		return toReturn;
	}

}
