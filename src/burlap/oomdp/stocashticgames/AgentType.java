package burlap.oomdp.stocashticgames;

import java.util.List;

import burlap.oomdp.core.ObjectClass;


public class AgentType {

	public String						typeName;
	public ObjectClass					oclass;
	public List<SingleAction>			actions;
	
	
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
