package minecraft.MinecraftDomain.PropositionalFunctions;

import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AgentCanJumpPF extends PropositionalFunction {
	private int rows;
	private int cols;
	private int height;
	
	
	public AgentCanJumpPF(String name, Domain domain, String[] parameterClasses, int rows, int cols, int height) {
		super(name, domain, parameterClasses);
		this.rows = rows;
		this.height = height;
		this.cols = cols;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		return Helpers.agentCanJump(state, rows, cols, height);
	}

}
