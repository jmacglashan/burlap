package minecraft.MinecraftDomain.Options;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class DigDownOption extends MinecraftOption {

	public DigDownOption(String name, State state, Domain domain,
			RewardFunction rf, double gamma, StateHashFactory hashFactory) {
		super(name, state, domain, rf, gamma, hashFactory);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GroundedAction getGroundedAction(State state) {
		ObjectInstance agent = state.getFirstObjectOfClass(NameSpace.CLASSAGENT);
		int agentVertDir = agent.getDiscValForAttribute(NameSpace.ATVERTDIR);
		//Look all the way down
		if (agentVertDir > 0) {
			return this.getGAByActionName(NameSpace.ACTIONLOOKDOWN);
		}
		else return this.getGAByActionName(NameSpace.ACTIONDESTBLOCK);
	}

	@Override
	public boolean shouldInitiate(State state) {
		ObjectInstance agent = state.getFirstObjectOfClass(NameSpace.CLASSAGENT);
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		ObjectInstance gold = state.getFirstObjectOfClass(NameSpace.CLASSGOLDBLOCK);
		int goldX = gold.getDiscValForAttribute(NameSpace.ATX);
		int goldY = gold.getDiscValForAttribute(NameSpace.ATY);
		//Initiate if gold block below
		if (agentX == goldX && agentY == goldY) {
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldTerminate(State state) {
		ObjectInstance agent = state.getFirstObjectOfClass(NameSpace.CLASSAGENT);
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		int zBelowAgent = agent.getDiscValForAttribute(NameSpace.ATZ)-2;
		//Terminate if cannot destroy what is below
		if (zBelowAgent < 0) return true;
		for (ObjectInstance object : Helpers.objectsAt(agentX, agentY, zBelowAgent, state)) {
			if (object.getObjectClass().hasAttribute(NameSpace.ATDEST) &&
					object.getDiscValForAttribute(NameSpace.ATDEST) == 0)
				return true;	
		}
		return false;
	}

	@Override
	public void initiateOptionVariables() {
		
	}

	@Override
	public void updateVariablesAfterOneAction() {
		
	}

}
