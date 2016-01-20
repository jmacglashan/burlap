package minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AgentAdjacentToTrenchPF extends PropositionalFunction {
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 */
	public AgentAdjacentToTrenchPF(String name, Domain domain, String[] parameterClasses) {
		super(name, domain, parameterClasses);
	}
	
	@Override
	public boolean isTrue(State state, String[] parameterClasses) {
		
		String agentString = parameterClasses[0];
		String trenchString = parameterClasses[1];
		
		ObjectInstance agent = state.getObject(agentString);
		ObjectInstance trench = state.getObject(trenchString);

		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		int belowAgentZ = agent.getDiscValForAttribute(NameSpace.ATZ) - 2; // Subtract 1 for feet, 1 for below
		
		int trenchX = trench.getDiscValForAttribute(NameSpace.ATX);
		int trenchY = trench.getDiscValForAttribute(NameSpace.ATX);
		int trenchZ = trench.getDiscValForAttribute(NameSpace.ATX);
		
		int[] trenchVector = trench.getIntArrayValue(NameSpace.ATTRENCHVECTOR);
		
		for(int dx = -1; dx < 2; dx = dx + 2) {
			for(int dy = -1; dy < 2; dy = dy + 2) {
				if(Helpers.isPointInTrench(new int[] {agentX + dx,  agentY + dy, belowAgentZ}, new int[]{trenchX, trenchY, trenchZ}, trenchVector)) {
					return true;
				}
			}
		}
		return false;
	}
}
