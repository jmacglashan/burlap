package minecraft.MinecraftDomain.PropositionalFunctions;

import java.util.List;

import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

/**
 * Returns true if there is a block of a particular type in front of the agent (w.r.t rotation)
 * @author dabel
 *
 */
public class BlockInFrontOfAgentPF extends PropositionalFunction{
	

	
	String objectStringToCheckAgainst; 
	
	/**
	 * Checks to see if the given @param objectName is either at 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param objectName
	 */
	public BlockInFrontOfAgentPF(String name, Domain domain, String[] parameterClasses, String objectName) {
		super(name, domain, parameterClasses);
		this.objectStringToCheckAgainst = objectName;
	}

	
	@Override
	public boolean isTrue(State state, String[] params) {
		int[]positionInFront = Helpers.positionInFrontOfAgent(1, state, true);
		
		// Head
		List<ObjectInstance> objectsInFront = Helpers.objectsAt(positionInFront[0], positionInFront[1], positionInFront[2], state);
		for(ObjectInstance block: objectsInFront) {
			String blockClassName = block.getTrueClassName();
			if (this.objectStringToCheckAgainst.equals(blockClassName)) {
				return true;
			}
		}
		
		// Feet
		List<ObjectInstance> objectsInFrontFeet = Helpers.objectsAt(positionInFront[0], positionInFront[1], positionInFront[2] - 1, state);
		for(ObjectInstance block: objectsInFrontFeet) {
			String blockClassName = block.getTrueClassName();
			if (this.objectStringToCheckAgainst.equals(blockClassName)) {
				return true;
			}
		}
		
		return false;
	}
}
