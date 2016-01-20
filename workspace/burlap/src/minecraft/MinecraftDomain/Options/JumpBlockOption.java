package minecraft.MinecraftDomain.Options;

import minecraft.NameSpace;
import minecraft.MinecraftBehavior.MinecraftBehavior;
import minecraft.MinecraftDomain.Helpers;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class JumpBlockOption extends MinecraftOption{

	private int mapHeight;
	int lastAgentBlocks;
	PropositionalFunction agentInMidAirPF;
	int actionCounter;
	int terminateAfter = 12;
	
	public JumpBlockOption(String name, State state, Domain domain,
			RewardFunction rf, double gamma, StateHashFactory hashFactory, MinecraftBehavior mcBeh) {
		super(name, state, domain, rf, gamma, hashFactory);
		this.mapHeight = mcBeh.MCDomainGenerator.height;
		this.agentInMidAirPF = domain.getPropFunction(NameSpace.PFAGENTINMIDAIR);
		
	}

	@Override
	public GroundedAction getGroundedAction(State state) {
		ObjectInstance agent = state.getFirstObjectOfClass(NameSpace.CLASSAGENT);
		int agentVertDir = agent.getDiscValForAttribute(NameSpace.ATVERTDIR);
		//Look all the way down
		if (agentVertDir > 0) {
			return this.getGAByActionName(NameSpace.ACTIONLOOKDOWN);
		}
		//Place block
		else if (this.agentInMidAirPF.isTrue(state, "")) {
			return this.getGAByActionName(NameSpace.ACTIONPLACEBLOCK);
		}
		//Jump
		return this.getGAByActionName(NameSpace.ACTIONJUMP);
	}

	@Override
	public boolean shouldInitiate(State state) {
		ObjectInstance agent = state.getFirstObjectOfClass(NameSpace.CLASSAGENT);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		int agentBlocks = agent.getDiscValForAttribute(NameSpace.ATPLACEBLOCKS);
		return agentBlocks > 0 && Helpers.blockBelowAgent(state) && agentZ < this.mapHeight-1;
	}

	@Override
	public boolean shouldTerminate(State state) {
		ObjectInstance agent = state.getFirstObjectOfClass(NameSpace.CLASSAGENT);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		int agentBlocks = agent.getDiscValForAttribute(NameSpace.ATPLACEBLOCKS);
		return this.actionCounter > this.terminateAfter ||
				agentBlocks > 0 && Helpers.blockBelowAgent(state) || agentZ == this.mapHeight-1;
	}

	@Override
	public void initiateOptionVariables() {
		this.actionCounter = 0;
	}

	@Override
	public void updateVariablesAfterOneAction() {
		this.actionCounter++;
	}

}
