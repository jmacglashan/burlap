package minecraft.MinecraftDomain.Options;

import minecraft.NameSpace;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class LookAllTheWayDownOption extends MinecraftOption {

	public LookAllTheWayDownOption(String name, State state, Domain domain,
			RewardFunction rf, double gamma, StateHashFactory hashFactory) {
		super(name, state, domain, rf, gamma, hashFactory);
	}

	@Override
	public GroundedAction getGroundedAction(State state) {
		return this.getGAByActionName(NameSpace.ACTIONLOOKDOWN);
	}

	@Override
	public boolean shouldInitiate(State state) {
		ObjectInstance agent = state.getFirstObjectOfClass(NameSpace.CLASSAGENT);
		int agentVert = agent.getDiscValForAttribute(NameSpace.ATVERTDIR);
		return agentVert > 0;
	}

	@Override
	public boolean shouldTerminate(State state) {
		ObjectInstance agent = state.getFirstObjectOfClass(NameSpace.CLASSAGENT);
		int agentVert = agent.getDiscValForAttribute(NameSpace.ATVERTDIR);
		return agentVert == 0;
	}

	@Override
	public void initiateOptionVariables() {		
	}

	@Override
	public void updateVariablesAfterOneAction() {		
	}

}
