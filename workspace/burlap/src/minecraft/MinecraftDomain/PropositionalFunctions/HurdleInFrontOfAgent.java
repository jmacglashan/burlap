package minecraft.MinecraftDomain.PropositionalFunctions;

import java.util.List;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class HurdleInFrontOfAgent extends PropositionalFunction{
	
	String objectStringToCheckAgainst; 
	private int rows;
	private int cols;
	private int height;
	
	
	/**
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param object
	 */
	public HurdleInFrontOfAgent(String name, Domain domain, String[] parameterClasses, int rows, int cols, int height) {
		super(name, domain, parameterClasses);
		this.rows = rows;
		this.cols = cols;
		this.height = height;
	}

	@Override
	public boolean isTrue(State state, String[] params) {

		ObjectInstance agent = state.getFirstObjectOfClass(NameSpace.CLASSAGENT);
		
		int ax = agent.getDiscValForAttribute(NameSpace.ATX);
		int ay = agent.getDiscValForAttribute(NameSpace.ATY);
		int az = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		int agentDir = agent.getDiscValForAttribute(NameSpace.ATROTDIR);
		
		int dx, dy;
		switch(agentDir) {
			case(0):
				dx = 0;
				dy = -1;
				break;
			case(1):
				dx = 1;
				dy = 0;
				break;
			case(2):
				dx = 0;
				dy = 1;
				break;
			default:
				dx = -1;
				dy = 0;
		}
		
		List<ObjectInstance> objectsInFrontHead = Helpers.objectsAt(ax + dx, ay + dy, az, state);
		List<ObjectInstance> objectsInFrontFeet = Helpers.objectsAt(ax + dx, ay + dy, az - 1, state);
		
		// Is agent's head clear?
		for(ObjectInstance block: objectsInFrontHead) {
			if (!block.getBooleanValue(NameSpace.ATCOLLIDES)) {
				return false;
			}
		}
		// Is a block blocking at the feet?
		for(ObjectInstance block: objectsInFrontFeet) {
			if (block.getBooleanValue(NameSpace.ATCOLLIDES)) {
				return true && Helpers.agentCanJump(state, this.rows, this.cols, this.height);
			}
		}
		
		return false;
	}
}
