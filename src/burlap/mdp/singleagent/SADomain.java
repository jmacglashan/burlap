package burlap.mdp.singleagent;

import burlap.mdp.core.Domain;
import burlap.mdp.singleagent.model.SampleModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * A domain subclass for single agent domains. This class adds data structures to index the actions that can be taken
 * by the agent in the domain.
 * @author James MacGlashan
 *
 */
public class SADomain implements Domain {

	protected List <ActionType> actionTypes = new ArrayList<ActionType>();
	protected Map <String, ActionType>					actionMap = new HashMap<String, ActionType>();
	protected SampleModel model;



	public SADomain addActionType(ActionType act){
		if(!actionMap.containsKey(act.typeName())){
			actionTypes.add(act);
			actionMap.put(act.typeName(), act);
		}
		return this;
	}

	public SADomain addActionTypes(ActionType...actions){
		for(ActionType action : actions){
			this.addActionType(action);
		}
		return this;
	}
	
	
	public List <ActionType> getActionTypes(){
		return new ArrayList <ActionType>(actionTypes);
	}
	

	public ActionType getAction(String name){
		return actionMap.get(name);
	}


	public SampleModel getModel() {
		return model;
	}

	public void setModel(SampleModel model) {
		this.model = model;
	}
}
