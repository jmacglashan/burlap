package burlap.behavior.stochasticgames.agents.naiveq.history;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.Domain;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SimpleGroundedSGAgentAction;


/**
 * An action to int map that takes the list of possible action names in a domain and assigns and int value to them.
 * This method will not manage object identifier independence.
 * @author James MacGlashan
 *
 */
public class ParameterNaiveActionIdMap implements ActionIdMap {

	/**
	 * The map from action names to their corresponding int value
	 */
	protected Map<String, Integer> map;
	
	/**
	 * The domain for which the action values should be created.
	 */
	protected Domain domain;
	
	
	/**
	 * Initializes a mapping from the names of all actions in a given domain to an int value.
	 * @param d the domain containing the actions.
	 */
	public ParameterNaiveActionIdMap(Domain d){
		
		this.domain = d;
		List<SGAgentAction> actions = d.getAgentActions();
		map = new HashMap<String, Integer>(actions.size());
		for(int i = 0; i < actions.size(); i++){
			map.put(actions.get(i).actionName, i);
		}
	}
	
	
	@Override
	public int getActionId(GroundedSGAgentAction gsa) {
		return map.get(gsa.action.actionName);
	}


	@Override
	public int getActionId(String actionName, String[] params) {
		return map.get(actionName);
	}
	
	@Override
	public int maxValue() {
		return map.size();
	}


	@Override
	public GroundedSGAgentAction getActionForId(int id) {
		
		for(String key : map.keySet()){
			int sid = map.get(key);
			if(sid == id){
				//found it
				GroundedSGAgentAction gsa = new SimpleGroundedSGAgentAction("", domain.getSingleAction(key));
				return gsa;
			}
		}
		
		return null;
	}

}
