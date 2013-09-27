package burlap.oomdp.singleagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.Domain;
import burlap.oomdp.stocashticgames.SingleAction;


public class SADomain extends Domain {

	protected List <Action>							actions;				//list of actions
	protected Map <String, Action>					actionMap;				//lookup actions by name
	
	public SADomain() {
		super();
		actions = new ArrayList <Action>();
		actionMap = new HashMap <String, Action>();
	}

	
	@Override
	public void addAction(Action act){
		if(!actionMap.containsKey(act.getName())){
			actions.add(act);
			actionMap.put(act.getName(), act);
		}
	}
	
	
	@Override
	public List <Action> getActions(){
		return new ArrayList <Action>(actions);
	}
	
	
	@Override
	public Action getAction(String name){
		return actionMap.get(name);
	}


	@Override
	public void addSingleAction(SingleAction sa) {
		throw new UnsupportedOperationException("Single Agent domain cannot add actions designed for stochastic game formalisms");
	}


	@Override
	public List<SingleAction> getSingleActions() {
		throw new UnsupportedOperationException("Single Agent domain does not contain any action for stocashtic game formalisms");
	}


	@Override
	public SingleAction getSingleAction(String name) {
		throw new UnsupportedOperationException("Single Agent domain does not contain any action for stocashtic game formalisms");
	}
	
}
