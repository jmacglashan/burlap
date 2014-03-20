package burlap.oomdp.singleagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.Domain;
import burlap.oomdp.stochasticgames.SingleAction;



/**
 * A domain subclass for single agent domains. This class adds data structures to index the actions that can be taken
 * by the agent in the domain.
 * @author James MacGlashan
 *
 */
public class SADomain extends Domain {

	protected List <Action>							actions;				//list of actions
	protected Map <String, Action>					actionMap;				//lookup actions by name
	
	public SADomain() {
		super();
		actions = new ArrayList <Action>();
		actionMap = new HashMap <String, Action>();
	}

	
	/**
	 * Adss the action observer to all actions associated with this domain. Actions added to this domain after this method is called
	 * will have to have the observer set for them independently or by a subsequent call to this method.
	 * @param observer the observer to set all actions to use.
	 */
	public void addActionObserverForAllAction(ActionObserver observer){
		for(Action a : this.actions){
			a.addActionObserver(observer);
		}
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


	@Override
	protected Domain newInstance() {
		return new SADomain();
	}

	
}
