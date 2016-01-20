package minecraft.MinecraftDomain.MacroActions;

import java.util.ArrayList;
import java.util.List;

import minecraft.NameSpace;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class SprintMacroAction extends MinecraftMacroAction {

	private int numSprints;
	
	/**
	 * 
	 * @param name
	 * @param rf
	 * @param gamma
	 * @param hashFactory
	 * @param domain
	 * @param state
	 * @param numSprints
	 */
	public SprintMacroAction(String name, RewardFunction rf, double gamma, StateHashFactory hashFactory, Domain domain, State state, int numSprints) {
		super(name, rf, gamma, hashFactory, domain, state);
		this.numSprints = numSprints;
		if (this.numSprints < 1) throw new IllegalArgumentException();
//		this.name = this.name;// + numSprints;
		
	}
	
	@Override
	public List<GroundedAction> getGroundedActions() {
		List<GroundedAction> toReturn = new ArrayList<GroundedAction>();
				
		for (int i = 0; i < this.numSprints; i++) {
			toReturn.add(this.getGAByActionName(NameSpace.ACTIONMOVE));
		}
		
		return toReturn;
	}

	

}
