package minecraft.MinecraftDomain.Actions;

import java.util.List;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class DestroyBlockAction extends AgentAction {
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param rows
	 * @param cols
	 * @param height
	 */
	public DestroyBlockAction(String name, Domain domain, int rows, int cols, int height) {
		super(name, domain, rows, cols, height, true);

	}
	

	@Override
	protected void doAction(State state) {
		List<ObjectInstance> objectsInfrontAgent = Helpers.getBlocksInfrontOfAgent(1, state);
		for (ObjectInstance object: objectsInfrontAgent) {
			if (object.getObjectClass().hasAttribute(NameSpace.ATDEST) && object.getDiscValForAttribute(NameSpace.ATDEST) == 1) {
				Helpers.removeObjectFromState(object, state, this.domain);
			}
		}
	}
}