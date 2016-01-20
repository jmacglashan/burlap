package minecraft.MinecraftDomain.PropositionalFunctions;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class TrenchInFrontOfAgent extends PropositionalFunction {

	private int rows;
	private int cols;
	private int height;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param rows
	 * @param cols
	 * @param height
	 */
	public TrenchInFrontOfAgent(String name, Domain domain,
			String[] parameterClasses, int rows, int cols, int height) {
		super(name, domain, parameterClasses);
		this.rows = rows;
		this.height = height;
		this.cols = cols;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		int[] posInFront = Helpers.positionInFrontOfAgent(1, state, true);
		
		boolean allInBounds = Helpers.withinMapAt(posInFront[0], posInFront[1], agentZ, this.cols, this.rows, this.height) &&
				Helpers.withinMapAt(posInFront[0], posInFront[1], agentZ-1, this.cols, this.rows, this.height) &&
				Helpers.withinMapAt(posInFront[0], posInFront[1], agentZ-2, this.cols, this.rows, this.height);
		
		boolean trenchSpaceInFront = Helpers.emptySpaceAt(posInFront[0], posInFront[1], agentZ, state) &&
				Helpers.emptySpaceAt(posInFront[0], posInFront[1], agentZ-1, state) && 
				Helpers.emptySpaceAt(posInFront[0], posInFront[1], agentZ-2, state);
		
		return allInBounds && trenchSpaceInFront && Helpers.blockBelowAgent(state);
		
		
	}

}
