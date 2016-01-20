package minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

/**
 * Outputs true when the agent's yaw is equal to the value specified
 * @author dabel
 *
 */
public class AgentLookingAtAngle extends PropositionalFunction{
	private final int yaw;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param angle : 0 = straightdown, 1 = down (diagonal), 2 = downforward (legs), 3 = forward (eyes) 
	 */
	public AgentLookingAtAngle(String name, Domain domain, String[] parameterClasses, int angle) {
		super(name, domain, parameterClasses);
		this.yaw = angle;
	}
	
	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		int agentYaw = agent.getDiscValForAttribute(NameSpace.ATVERTDIR);
		
		return agentYaw == this.yaw;
		
	}
}
