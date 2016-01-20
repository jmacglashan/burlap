package minecraft.MinecraftDomain.PropositionalFunctions;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AgentInMidAirPF extends PropositionalFunction{
	private int rows;
	private int cols;
	private int height;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 */
	public AgentInMidAirPF(String name, Domain domain, String[] parameterClasses, int rows, int cols, int height) {
		super(name, domain, parameterClasses);
		this.rows = rows;
		this.cols = cols;
		this.height = height;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		
		
		return Helpers.withinMapAt(agentX, agentY, agentZ-2, cols, rows, height) && Helpers.emptySpaceAt(agentX, agentY, agentZ-2, state);
	}
}
