package minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing;

import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class EmptySpacePF extends PropositionalFunction{
	private final int x;
	private final int y;
	private final int z;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param x
	 * @param y
	 * @param z
	 */
	public EmptySpacePF(String name, Domain domain, String[] parameterClasses, int x, int y, int z) {
		super(name, domain, parameterClasses);
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public boolean isTrue(State state, String[] params) {
		return Helpers.emptySpaceAt(this.x, this.y, this.z, state);
	}
}
