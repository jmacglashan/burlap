package burlap.oomdp.stochasticgames;

import java.util.List;

import burlap.oomdp.core.ObjectClass;


/**
 * This class specifies the type of agent a stochastic games agent can be. Different agent types may have different actions they can execute
 * and may also have different observable properties to other agents, which is indicated by the ObjectClass that represents their world state. 
 * @author James MacGlashan
 *
 */
public class SGAgentType {

	public String						typeName;
	public ObjectClass					oclass;
	public List<SGAgentAction>			actions;
	
	
	/**
	 * Creates a new agent type with a given name, object class describing the agent's world state, and actions available to the agent.
	 * @param typeName the type name
	 * @param oclass the object class that represents the agent's world state information
	 * @param actionsAvailableToType the available actions that this agent can take in the world.
	 */
	public SGAgentType(String typeName, ObjectClass oclass, List<SGAgentAction> actionsAvailableToType){
		this.typeName = typeName;
		this.oclass = oclass;
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
