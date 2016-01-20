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
public class AtGoalPF extends PropositionalFunction{
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 */
	public AtGoalPF(String name, Domain domain, String[] parameterClasses) {
		super(name, domain, parameterClasses);
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		// Get the agent coordinates
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		// Get the goal coordinates
		List<ObjectInstance> allGoals = state.getObjectsOfTrueClass(NameSpace.CLASSGOAL);
		
		for (ObjectInstance goal : allGoals) {
			int goalX = goal.getDiscValForAttribute(NameSpace.ATX);
			int goalY = goal.getDiscValForAttribute(NameSpace.ATY);
			int goalZ = goal.getDiscValForAttribute(NameSpace.ATZ);
			
			// Check if equal
			if(agentX == goalX && agentY == goalY && agentZ == goalZ){
				return true;
			}
		}
		return false;
	}
}