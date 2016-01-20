package minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class EmptyCellInFrontOfAgentPF extends PropositionalFunction {
	int rows;
	int cols;
	int height;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param rows
	 * @param cols
	 * @param height
	 */
	public EmptyCellInFrontOfAgentPF(String name, Domain domain, String[] parameterClasses, int rows, int cols, int height) {
		super(name, domain, parameterClasses);
		this.rows = rows;
		this.cols = cols;
		this.height = height;
	}
	
	@Override
	public boolean isTrue(State state, String[] parameterClasses) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);

		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		int[] posinInFront = Helpers.positionInFrontOfAgent(1, state, false);
		
		int trenchX = posinInFront[0];
		int trenchY = posinInFront[1];
		int trenchZ = agentZ-2;
		
		// True if block below agent and nothing in same plane in front of agent
		boolean b = Helpers.blockBelowAgent(state) && 
				Helpers.withinMapAt(trenchX, trenchY, trenchZ, this.cols, this.rows, this.height) && Helpers.emptySpaceAt(trenchX, trenchY, trenchZ, state);
		
		return b;
	}
}
