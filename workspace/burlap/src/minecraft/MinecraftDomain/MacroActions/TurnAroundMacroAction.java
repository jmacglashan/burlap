package minecraft.MinecraftDomain.MacroActions;

import java.util.ArrayList;
import java.util.List;

import minecraft.NameSpace;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class TurnAroundMacroAction extends MinecraftMacroAction{
	/**
	 * 
	 * @param name
	 * @param rf
	 * @param gamma
	 * @param hashFactory
	 * @param domain
	 * @param state
	 */
	public TurnAroundMacroAction(String name, RewardFunction rf, double gamma,
			StateHashFactory hashFactory, Domain domain, State state) {
		super(name, rf, gamma, hashFactory, domain, state);
	}

	@Override
	public List<GroundedAction> getGroundedActions() {
		GroundedAction rotateCGroundedAction = this.getGAByActionName(NameSpace.ACTIONROTATEC);

		List<GroundedAction> toReturn = new ArrayList<GroundedAction>();
		toReturn.add(rotateCGroundedAction);
		toReturn.add(rotateCGroundedAction);
		
		return toReturn;
	}

}
