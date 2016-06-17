package burlap.mdp.stochasticgames.agent;

import burlap.mdp.singleagent.action.ActionType;

import java.util.List;


/**
 * This class specifies the type of agent a stochastic games agent can be. Different agent types may have different actions they can execute
 * and may also have different observable properties to other agents, which is indicated by the ObjectClass that represents their world state. 
 * @author James MacGlashan
 *
 */
public class SGAgentType {

	public String						typeName;
	public List<ActionType>			actions;
	
	
	/**
	 * Creates a new agent type with a given name, and actions available to the agent.
	 * @param typeName the type name
	 * @param actionsAvailableToType the available actions that this agent can take in the world.
	 */
	public SGAgentType(String typeName, List<ActionType> actionsAvailableToType){
		this.typeName = typeName;
		this.actions = actionsAvailableToType;
	}
	
	
	@Override
	public int hashCode(){
		return typeName.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof SGAgentType)){
			return false;
		}
		
		return ((SGAgentType)o).typeName.equals(typeName);
		
	}

}
