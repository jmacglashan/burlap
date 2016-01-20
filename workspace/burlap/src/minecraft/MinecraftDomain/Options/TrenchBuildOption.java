package minecraft.MinecraftDomain.Options;

import minecraft.NameSpace;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class TrenchBuildOption extends MinecraftOption {
	
	private PropositionalFunction endOfMapPF;
	private PropositionalFunction trenchPF;
	private boolean justPlacedBlock;
	private boolean justMoved;

	/**
	 * 
	 * @param name
	 * @param state
	 * @param domain
	 * @param rf
	 * @param gamma
	 * @param hashFactory
	 */
	public TrenchBuildOption(String name, State state, Domain domain,
			RewardFunction rf, double gamma, StateHashFactory hashFactory) {
		super(name, state, domain, rf, gamma, hashFactory);
		this.endOfMapPF = domain.getPropFunction(NameSpace.PFENDOFMAPINFRONT);
		this.trenchPF = domain.getPropFunction(NameSpace.PFEMPTYCELLINFRONT);
	}

	@Override
	public GroundedAction getGroundedAction(State state) {

		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		int vertDir = agent.getDiscValForAttribute(NameSpace.ATVERTDIR);
		//Place Block
		if (trenchPF.isTrue(state, "") && vertDir == 1 && !this.justPlacedBlock) {
			this.justPlacedBlock = true;
			return this.getGAByActionName(NameSpace.ACTIONPLACEBLOCK);
		}
		//Look down
		else if (trenchPF.isTrue(state, "") && vertDir > 1) {
			return this.getGAByActionName(NameSpace.ACTIONLOOKDOWN);
		}

		//Default is to move (up to  times)
		this.justMoved = true;
		return this.getGAByActionName(NameSpace.ACTIONMOVE);
	}

	@Override
	public boolean shouldTerminate(State state) {
		if ((this.justMoved && this.justPlacedBlock) || this.endOfMapPF.isTrue(state, "")) return true;

		else return false;
	}
	
	@Override
	public boolean shouldInitiate(State state) {
		return trenchPF.isTrue(state, "");
	}

	@Override
	public void initiateOptionVariables() {		

		this.justPlacedBlock = false;
		this.justMoved = false;
	}

	@Override
	public void updateVariablesAfterOneAction() {	
	}



}
