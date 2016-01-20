package minecraft.MinecraftDomain.Actions;

import minecraft.NameSpace;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class RotateAction extends StochasticAgentAction{
	
	private int rotation;
	
	/**
	 * @param name
	 * @param domain
	 * @param rotation
	 * @param rows
	 * @param cols
	 * @param height
	 */
	public RotateAction(String name, Domain domain, int rotation, int rows, int cols, int height){
		super(name, domain, rows, cols, height, true);
		this.rotation = rotation;
	}
	
	protected void doAction(State state){
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		int newRotation = (this.rotation + agent.getDiscValForAttribute(NameSpace.ATROTDIR))  % 4;
		agent.setValue(NameSpace.ATROTDIR, newRotation);
	}
	
}