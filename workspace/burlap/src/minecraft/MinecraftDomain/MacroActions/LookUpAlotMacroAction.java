package minecraft.MinecraftDomain.MacroActions;

import java.util.ArrayList;
import java.util.List;

import minecraft.NameSpace;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class LookUpAlotMacroAction extends MinecraftMacroAction {

	private int numLookUps;

	/**
	 * 
	 * @param name
	 * @param rf
	 * @param gamma
	 * @param hashFactory
	 * @param domain
	 * @param state
	 * @param numLookUps
	 */
	public LookUpAlotMacroAction(String name, RewardFunction rf,
			double gamma, StateHashFactory hashFactory, Domain domain,
			State state, int numLookDowns) {
		
		super(name, rf, gamma, hashFactory, domain, state);
		this.numLookUps = numLookDowns;
		if (this.numLookUps < 1) throw new IllegalArgumentException();
	}
	
	@Override
	public List<GroundedAction> getGroundedActions() {
		List<GroundedAction> toReturn = new ArrayList<GroundedAction>();
		
		GroundedAction lookUpGA = this.getGAByActionName(NameSpace.ACTIONLOOKUP);
		
		for (int i = 0; i < this.numLookUps; i++) {
			toReturn.add(lookUpGA);
		}
		
		return toReturn;
	}

	

}
