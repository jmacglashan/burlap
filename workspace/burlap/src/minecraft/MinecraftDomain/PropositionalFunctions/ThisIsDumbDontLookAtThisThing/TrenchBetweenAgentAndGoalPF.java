package minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class TrenchBetweenAgentAndGoalPF extends PropositionalFunction {
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
	public TrenchBetweenAgentAndGoalPF(String name, Domain domain, String[] parameterClasses, int rows, int cols, int height) {
		super(name, domain, parameterClasses);
		this.rows = rows;
		this.cols = cols;
		this.height = height;
	}
	
	@Override
	public boolean isTrue(State state, String[] parameterClasses) {
		
		String agentString = parameterClasses[0];
		String goalString = parameterClasses[0];
		String trenchString = parameterClasses[0];
		
		ObjectInstance agent = state.getObject(agentString);
		ObjectInstance trench = state.getObject(goalString);
		ObjectInstance goal = state.getObject(trenchString);

		int ax = agent.getDiscValForAttribute(NameSpace.ATX);
		int ay = agent.getDiscValForAttribute(NameSpace.ATY);
		int az = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		int tx = trench.getDiscValForAttribute(NameSpace.ATX);
		int ty = trench.getDiscValForAttribute(NameSpace.ATY);
		int tz = trench.getDiscValForAttribute(NameSpace.ATZ);
		
		int[] trenchVector = trench.getIntArrayValue(NameSpace.ATTRENCHVECTOR);
		
		int gx = goal.getDiscValForAttribute(NameSpace.ATX);
		int gy = goal.getDiscValForAttribute(NameSpace.ATY);
		int gz = goal.getDiscValForAttribute(NameSpace.ATZ);
		
		boolean b = Helpers.isPlaneBetweenTwoPoints(new int[] {ax, ay, az}, new int[] {gx,gy,gz}, new int[] {tx,ty,tz}, trenchVector);
		
		return b;
	}
}
