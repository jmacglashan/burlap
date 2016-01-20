package minecraft.MinecraftDomain.Actions;

import java.util.List;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;

/**
 * Abstract class extended by all the agent's actions in order to properly account
 * for time-step related things that should happen after an agent's actions like
 * objects falling etc. Requires overriding the doAction method which changes state as
 * appropriate when an action is performed.
 * @author Dhershkowitz
 *
 */
public abstract class AgentAction extends Action {
	
	protected int rows;
	protected int cols;
	protected final int height;
	protected boolean causesAgentToFall;
	
	abstract void doAction(State state);
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param rows
	 * @param cols
	 * @param height
	 * @param causesAgentToFall 
	 */
	public AgentAction(String name, Domain domain, int rows, int cols, int height, boolean causesAgentToFall){
		super(name, domain, "");
		this.rows = rows;
		this.cols = cols;
		this.height = height;
		this.causesAgentToFall = causesAgentToFall;
	}
	
	//By default just returns the action but for StochasicAgentActions it does so stochastically
	protected AgentAction getAction() {
		return this;
	}
	
	@Override
	protected State performActionHelper(State state, String[] params) {
		AgentAction toPerform = getAction();
		toPerform.doAction(state);
		
		performPostActionUpdates(state);
		return state;
	}
	
	private void performPostActionUpdates(State state) {
		fallAllObjects(state, rows, cols, height);
		pickUpItems(state);
	}
	
	private void pickUpItems(State state) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		for (ObjectInstance object: state.getAllObjects()) {
			if (object.getObjectClass().hasAttribute(NameSpace.ATDESTWHENWALKED) && object.getDiscValForAttribute(NameSpace.ATDESTWHENWALKED) == 1 &&
					objectAtAgentLocation(object, agent)) {
				Helpers.removeObjectFromState(object, state, this.domain);
			}
		}
	}
	
	/**
	 * 
	 * @param object
	 * @param agent
	 * @return true if the object is either at the agent's location or the agent's feet's location
	 */
	private boolean objectAtAgentLocation(ObjectInstance object, ObjectInstance agent) {
		if (!object.getObjectClass().hasAttribute(NameSpace.ATX)) {
			return false;
		}
		int objX = object.getDiscValForAttribute(NameSpace.ATX);
		int objY = object.getDiscValForAttribute(NameSpace.ATY);
		int objZ = object.getDiscValForAttribute(NameSpace.ATZ);
		
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		return objX == agentX && objY == agentY && (objZ == agentZ || objZ == agentZ-1);
	}
	
	
	/**
	 * 
	 * @param object
	 * @param state
	 * @param rows
	 * @param cols
	 * @param height
	 * @return true if the object fell and false otherwise
	 */
	private boolean fall(ObjectInstance object, State state, int rows,  int cols, int height) {
		int x = object.getDiscValForAttribute(NameSpace.ATX);
		int y = object.getDiscValForAttribute(NameSpace.ATY);
		int z = object.getDiscValForAttribute(NameSpace.ATZ);
		int newZ = z-1;
		String objectName = object.getObjectClass().name;
		
		//Agent feet falling
		if (objectName.equals(NameSpace.CLASSAGENTFEET)) {
			
			//Break if action doesn't cause falling
			if (!this.causesAgentToFall) {
				return false;
			}
			
			if (Helpers.withinMapAt(x, y, newZ, cols, rows, height) && Helpers.emptySpaceAt(x, y, newZ, state)) {
				object.setValue(NameSpace.ATZ, newZ);
			}
			
		}
		//Other falling
		if (Helpers.withinMapAt(x, y, newZ, cols, rows, height) && Helpers.emptySpaceAt(x, y, newZ, state)) {
			object.setValue(NameSpace.ATZ, newZ);
			return true;
		}
		return false;
		
		
	}
	
	private void fallAllObjects(State state, int rows, int cols, int height) {
		//Make agents feet then agent fall
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		ObjectInstance agentFeet = state.getObjectsOfTrueClass(NameSpace.CLASSAGENTFEET).get(0);
		String agentName = agent.getName();
		String agentFeetName = agentFeet.getName();
		
		
		if (fall(agentFeet, state, rows, cols, height)){
			fall(agent, state, rows, cols, height);
		}
		
		//Make rest fall
		List<ObjectInstance> allObjects = state.getAllObjects();
		for (ObjectInstance object: allObjects) {
			if (!object.getName().equals(agentName)&&!object.getName().equals(agentFeetName)&&
					object.getObjectClass().hasAttribute(NameSpace.ATFLOATS) && object.getDiscValForAttribute(NameSpace.ATFLOATS) == 0) {
				fall(object, state, rows, cols, height);
			}
			
		}
	}
	
	@Override
	public List<TransitionProbability> getTransitions (State s, String [] params) {
		return super.deterministicTransition(s, params);
	}
}