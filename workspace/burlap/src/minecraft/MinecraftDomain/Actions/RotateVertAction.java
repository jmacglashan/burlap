package minecraft.MinecraftDomain.Actions;

import minecraft.NameSpace;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import java.lang.Math;

public class RotateVertAction extends StochasticAgentAction {
	private int rotation;
	/**
	 * @param name
	 * @param domain
	 * @param rotation 1 for looking up and -1 for looking down
	 */
	public RotateVertAction(String name, Domain domain, int rows, int cols, int height, int rotation){
		super(name, domain, rows, cols, height, true);
		this.rotation = rotation;
	}
	
	protected void doAction(State state){
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		int newRotation = this.rotation + agent.getDiscValForAttribute(NameSpace.ATVERTDIR);
		//Looking down
		if (this.rotation < 0) {
			newRotation = Math.max(0,newRotation);
		}
		else if (this.rotation > 0) {
			newRotation = Math.min(NameSpace.VertDirection.size-1, newRotation);
		}
		agent.setValue(NameSpace.ATVERTDIR, newRotation);
	}
}
