package minecraft.MinecraftDomain.PropositionalFunctions;

import java.util.List;

import minecraft.NameSpace;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

/**
 * Propositional function to determine if the agent is at the goal
 */
public class AtLocationPF extends PropositionalFunction{
	
	private int x;
	private int y;
	private int z;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 */
	public AtLocationPF(String name, Domain domain, String[] parameterClasses, int lx, int ly, int lz) {
		super(name, domain, parameterClasses);
		this.x = lx;
		this.y = ly;
		this.z = lz;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		// Get the agent's coordinates
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		// Check if equal to the specified location
		if(agentX == this.x && agentY == this.y && agentZ == this.z){
			return true;
		}

		return false;
	}
}