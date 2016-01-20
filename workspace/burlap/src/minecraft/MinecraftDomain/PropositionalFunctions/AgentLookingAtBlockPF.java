package minecraft.MinecraftDomain.PropositionalFunctions;

import java.util.List;

import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AgentLookingAtBlockPF extends PropositionalFunction{
	

	
	String objectStringToCheckAgainst; 
	
	/**
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param object
	 */
	public AgentLookingAtBlockPF(String name, Domain domain, String[] parameterClasses, String objectName) {
		super(name, domain, parameterClasses);
		this.objectStringToCheckAgainst = objectName;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		int[]positionInFront = Helpers.positionInFrontOfAgent(1, state, false);
		List<ObjectInstance> objectsInFront = Helpers.objectsAt(positionInFront[0], positionInFront[1], positionInFront[2], state);
		
		for(ObjectInstance block: objectsInFront) {
			String blockClassName = block.getTrueClassName();
			if (this.objectStringToCheckAgainst.equals(blockClassName)) {
				return true;
			}
		}
		return false;
	}
}
