package minecraft.MinecraftDomain.MacroActions;

import java.util.ArrayList;
import java.util.List;

import minecraft.NameSpace;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class BuildTrenchMacroAction extends MinecraftMacroAction{

	public BuildTrenchMacroAction(String name, RewardFunction rf, double gamma,
			StateHashFactory hashFactory, Domain domain, State state) {
		super(name, rf, gamma, hashFactory, domain, state);
	}

	@Override
	protected List<GroundedAction> getGroundedActions() {

		List<GroundedAction> toReturn = new ArrayList<GroundedAction>();
		toReturn.add(this.getGAByActionName(NameSpace.ACTIONLOOKDOWN));
		toReturn.add(this.getGAByActionName(NameSpace.ACTIONLOOKDOWN));
		toReturn.add(this.getGAByActionName(NameSpace.ACTIONPLACEBLOCK));
		toReturn.add(this.getGAByActionName(NameSpace.ACTIONMOVE));
		return toReturn;
	}

}
