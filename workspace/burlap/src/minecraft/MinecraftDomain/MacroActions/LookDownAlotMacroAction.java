package minecraft.MinecraftDomain.MacroActions;

import java.util.ArrayList;
import java.util.List;

import minecraft.NameSpace;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class LookDownAlotMacroAction extends MinecraftMacroAction {

	private int numLookDowns;

	public LookDownAlotMacroAction(String name, RewardFunction rf,
			double gamma, StateHashFactory hashFactory, Domain domain,
			State state, int numLookDowns) {
		
		super(name, rf, gamma, hashFactory, domain, state);
		this.numLookDowns = numLookDowns;
		if (this.numLookDowns < 1) throw new IllegalArgumentException();
	}
	
	@Override
	public List<GroundedAction> getGroundedActions() {
		List<GroundedAction> toReturn = new ArrayList<GroundedAction>();
		
		GroundedAction lookDownGroundedAction = this.getGAByActionName(NameSpace.ACTIONLOOKDOWN);
		
		for (int i = 0; i < this.numLookDowns; i++) {
			toReturn.add(lookDownGroundedAction);
		}
		
		return toReturn;
	}

	

}
