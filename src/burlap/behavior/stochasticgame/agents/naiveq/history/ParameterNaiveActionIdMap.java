package burlap.behavior.stochasticgame.agents.naiveq.history;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.Domain;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.SingleAction;

public class ParameterNaiveActionIdMap implements ActionIdMap {

	protected Map<String, Integer> map;
	protected Domain domain;
	
	public ParameterNaiveActionIdMap(Domain d){
		
		this.domain = d;
		List<SingleAction> actions = d.getSingleActions();
		map = new HashMap<String, Integer>(actions.size());
		for(int i = 0; i < actions.size(); i++){
			map.put(actions.get(i).actionName, i);
		}
	}
	
	
	@Override
	public int getActionId(GroundedSingleAction gsa) {
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
	public GroundedSingleAction getActionForId(int id) {
		
		for(String key : map.keySet()){
			int sid = map.get(key);
			if(sid == id){
				//found it
				GroundedSingleAction gsa = new GroundedSingleAction("", domain.getSingleAction(key), "");
				return gsa;
			}
		}
		
		return null;
	}

}
