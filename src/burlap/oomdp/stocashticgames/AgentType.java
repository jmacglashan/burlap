package burlap.oomdp.stocashticgames;

import java.util.List;

import burlap.oomdp.core.ObjectClass;


/**
 * This class specifies the type of agent an agent can be. Different agent types may have different actions they can execute
 * and may also have different observable properties to other agents, which is indicated by the ObjectClass that represents their world state. 
 * @author James MacGlashan
 *
 */
public class AgentType {

	public String						typeName;
	public ObjectClass					oclass;
	public List<SingleAction>			actions;
	
	
	/**
	 * Creates a new agent type with a given name, object class describing the agent's world state, and actions available to the agent.
	 * @param typeName the type name
	 * @param oclass the object class that represents the agent's world state information
	 * @param actionsAvailableToType the available actions that this agent can take in the world.
	 */
	public AgentType(String typeName, ObjectClass oclass, List <SingleAction> actionsAvailableToType){
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
		if(!(o instanceof AgentType)){
			return false;
		}
		
		return ((AgentType)o).typeName.equals(typeName);
		
	}

}
