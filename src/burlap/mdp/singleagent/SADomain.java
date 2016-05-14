package burlap.mdp.singleagent;

import burlap.mdp.core.Domain;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.stochasticgames.agentactions.SGAgentActionType;

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


	
	@Override
	public void addAction(ActionType act){
		if(!actionMap.containsKey(act.typeName())){
			actionTypes.add(act);
			actionMap.put(act.typeName(), act);
		}
	}
	
	
	public List <ActionType> getActionTypes(){
		return new ArrayList <ActionType>(actionTypes);
	}
	
	
	@Override
	public ActionType getAction(String name){
		return actionMap.get(name);
	}


	@Override
	public void addSGAgentAction(SGAgentActionType sa) {
		throw new UnsupportedOperationException("Single Agent domain cannot add actions designed for stochastic game formalisms");
	}


	@Override
	public List<SGAgentActionType> getAgentActions() {
		throw new UnsupportedOperationException("Single Agent domain does not contain any action for stochastic game formalisms");
	}


	@Override
	public SGAgentActionType getSGAgentAction(String name) {
		throw new UnsupportedOperationException("Single Agent domain does not contain any action for stochastic game formalisms");
	}

	public SampleModel getModel() {
		return model;
	}

	public void setModel(SampleModel model) {
		this.model = model;
	}
}
