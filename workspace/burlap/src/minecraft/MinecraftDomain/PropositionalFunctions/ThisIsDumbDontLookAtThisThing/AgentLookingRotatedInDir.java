package minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

/**
 * Outputs true when the agent is rotated according to the rotation specified
 * @author dabel
 *
 */
public class AgentLookingRotatedInDir extends PropositionalFunction{
	private final int dir;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param dir : 0 = forward, 1 = right, 2 = back, 3 = left 
	 */
	public AgentLookingRotatedInDir(String name, Domain domain, String[] parameterClasses, int direction) {
		super(name, domain, parameterClasses);
		this.dir = direction;
	}
	
	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		int agentDir = agent.getDiscValForAttribute(NameSpace.ATROTDIR);
		
		return agentDir == this.dir;
		
	}
}
