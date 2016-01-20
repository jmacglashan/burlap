package minecraft.MinecraftDomain.PropositionalFunctions;

import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AgentWalkIntoEndOfMapPF extends PropositionalFunction {
	
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
	public AgentWalkIntoEndOfMapPF(String name, Domain domain, String[] parameterClasses, int rows, int cols, int height) {
		super(name, domain, parameterClasses);
		this.rows = rows;
		this.cols = cols;
		this.height = height;
	}

	@Override
	public boolean isTrue(State state, String[] parameterClasses) {
		int[] positionInFront = Helpers.positionInFrontOfAgent(1, state, true);
		int x = positionInFront[0];
		int y = positionInFront[1];
		int z = positionInFront[2];
		
		return !Helpers.withinMapAt(x, y, z, this.cols, this.rows, this.height);
	}

}
