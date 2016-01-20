package minecraft.MinecraftDomain.Options;

import java.util.List;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class DestroyWallOption extends MinecraftOption {
	
	public DestroyWallOption(String name, State state, Domain domain,
			RewardFunction rf, double gamma, StateHashFactory hashFactory) {
		super(name, state, domain, rf, gamma, hashFactory);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GroundedAction getGroundedAction(State state) {
		ObjectInstance agent = state.getFirstObjectOfClass(NameSpace.CLASSAGENT);
		int vertDir = agent.getDiscValForAttribute(NameSpace.ATVERTDIR);
		//Center gaze to relevant place
		if (!(vertDir == 3 || vertDir == 2)) {
			if (vertDir < 2) return this.getGAByActionName(NameSpace.ACTIONLOOKUP);
			else return this.getGAByActionName(NameSpace.ACTIONLOOKDOWN);
		}
		//Destroy block in front if necessary
		int[] positionInFront = Helpers.positionInFrontOfAgent(1, state, false);
		for (ObjectInstance object: Helpers.objectsAt(positionInFront[0], positionInFront[1], positionInFront[2], state)) {
			if (objectIsDestructibleAndCollides(object)){
				return this.getGAByActionName(NameSpace.ACTIONDESTBLOCK);
			}
		}
		
		//Look for new block to destroy
		if (vertDir == 2) return this.getGAByActionName(NameSpace.ACTIONLOOKUP);
		return this.getGAByActionName(NameSpace.ACTIONLOOKDOWN);
	}

	@Override
	public boolean shouldInitiate(State state) {
		int [] positionInFrontOfAgent = Helpers.positionInFrontOfAgent(1, state, true);
		
		List<ObjectInstance> allPossibleObstructions = Helpers.objectsAt(positionInFrontOfAgent[0], positionInFrontOfAgent[1], positionInFrontOfAgent[2], state);
		allPossibleObstructions.addAll(Helpers.objectsAt(positionInFrontOfAgent[0], positionInFrontOfAgent[1], positionInFrontOfAgent[2]-1, state));
		//True if something that collides and is destructible
		for (ObjectInstance object : allPossibleObstructions) {
			if (objectIsDestructibleAndCollides(object)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean shouldTerminate(State state) {
	int [] positionInFrontOfAgent = Helpers.positionInFrontOfAgent(1, state, true);
		
		List<ObjectInstance> allPossibleObstructions = Helpers.objectsAt(positionInFrontOfAgent[0], positionInFrontOfAgent[1], positionInFrontOfAgent[2], state);
		allPossibleObstructions.addAll(Helpers.objectsAt(positionInFrontOfAgent[0], positionInFrontOfAgent[1], positionInFrontOfAgent[2]-1, state));
		//True if nothing that collides and is destructible
		for (ObjectInstance object : allPossibleObstructions) {
			if (objectIsDestructibleAndCollides(object)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void initiateOptionVariables() {
	}

	@Override
	public void updateVariablesAfterOneAction() {
	}
	
	private boolean objectIsDestructibleAndCollides(ObjectInstance object) {
		return object.getObjectClass().hasAttribute(NameSpace.ATDEST) && object.getObjectClass().hasAttribute(NameSpace.ATCOLLIDES) &&
				object.getDiscValForAttribute(NameSpace.ATDEST) == 1 && object.getDiscValForAttribute(NameSpace.ATCOLLIDES) == 1;
	}

}
