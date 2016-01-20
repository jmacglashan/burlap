package minecraft.MinecraftDomain.MacroActions;

import java.util.ArrayList;
import java.util.List;

import minecraft.NameSpace;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class DigDownMacroAction extends MinecraftMacroAction {
	private int amtDig;
	
	public DigDownMacroAction(String name, RewardFunction rf, double gamma,
			StateHashFactory hashFactory, Domain domain, State state, int amtDig) {
		super(name, rf, gamma, hashFactory, domain, state);
//		this.name = this.name + amtDig; 
		this.amtDig = amtDig;
		if (amtDig < 1) throw new IllegalArgumentException();
	}

	@Override
	protected List<GroundedAction> getGroundedActions() {
		List<GroundedAction> toReturn = new ArrayList<GroundedAction>();
		toReturn.add(this.getGAByActionName(NameSpace.ACTIONLOOKDOWN));
		toReturn.add(this.getGAByActionName(NameSpace.ACTIONLOOKDOWN));
		toReturn.add(this.getGAByActionName(NameSpace.ACTIONLOOKDOWN));
		
		for (int i = 0; i < this.amtDig; i++) {
			toReturn.add(this.getGAByActionName(NameSpace.ACTIONDESTBLOCK));
		}
		
		return toReturn;
	}


}
