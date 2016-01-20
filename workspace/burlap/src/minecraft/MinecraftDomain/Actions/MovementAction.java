package minecraft.MinecraftDomain.Actions;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class MovementAction extends StochasticAgentAction{
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param rows
	 * @param cols
	 * @param height
	 */
	public MovementAction(String name, Domain domain, int rows, int cols, int height){
		super(name, domain, rows, cols, height, true);		
	}

	private Boolean emptySpaceForAgentAt(int x, int y, int z, State state) {
		return Helpers.emptySpaceAt(x, y, z, state) && Helpers.emptySpaceAt(x, y, z-1, state);
		
	}
	
	@Override
	protected void doAction(State state){
		
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		ObjectInstance agentFeet = state.getObjectsOfTrueClass(NameSpace.CLASSAGENTFEET).get(0);

		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		int[] inFrontAgent = Helpers.positionInFrontOfAgent(1, state, true);
		
		int newX = inFrontAgent[0];
		int newY = inFrontAgent[1];
		
		//Update position if nothing in agent's way and new position is within map
		if (Helpers.withinMapAt(newX, newY, agentZ, cols, rows, this.height) && Helpers.withinMapAt(newX, newY, agentZ-1, cols, rows, height) &&
				emptySpaceForAgentAt(newX, newY, agentZ, state)) {
			agent.setValue(NameSpace.ATX, newX);
			agent.setValue(NameSpace.ATY, newY);
			
			agentFeet.setValue(NameSpace.ATX, newX);
			agentFeet.setValue(NameSpace.ATY, newY);
		}
	}
	
}