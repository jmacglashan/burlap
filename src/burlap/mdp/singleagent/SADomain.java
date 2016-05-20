package burlap.mdp.singleagent;

import burlap.mdp.core.Domain;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.model.SampleModel;

import java.util.*;


/**
 * A domain subclass for single agent domains. This class maintains the set of {@link ActionType}s associated with
 * a problem and also stores an optional {@link SampleModel}, if one exists, for planning problems or simulated domains.
 * @author James MacGlashan
 *
 */
public class SADomain implements Domain {

	protected List <ActionType> actionTypes = new ArrayList<ActionType>();
	protected Map <String, ActionType>					actionMap = new HashMap<String, ActionType>();
	protected SampleModel model;


	/**
	 * Adds an {@link ActionType} to this domain.
	 * @param act the {@link ActionType} to add
	 * @return this object for method chaining.
	 */
	public SADomain addActionType(ActionType act){
		if(!actionMap.containsKey(act.typeName())){
			actionTypes.add(act);
			actionMap.put(act.typeName(), act);
		}
		return this;
	}


	/**
	 * Adds a set of {@link ActionType} objects to this domain
	 * @param actions the {@link ActionType} objects to add
	 * @return this object for method chaining.
	 */
	public SADomain addActionTypes(ActionType...actions){
		for(ActionType action : actions){
			this.addActionType(action);
		}
		return this;
	}

	/**
	 * Sets the {@link ActionType}s for this domain. Any previously added {@link ActionType}s to this domain
	 * will first be removed.
	 * @param actions the {@link ActionType}s to use.
	 * @return this object for method chaining.
	 */
	public SADomain setActionTypes(ActionType...actions){
		return this.setActionTypes(Arrays.asList(actions));
	}


	/**
	 * Sets the {@link ActionType}s for this domain. Any previously added {@link ActionType}s to this domain
	 * will first be removed.
	 * @param actions the {@link ActionType}s to use.
	 * @return this object for method chaining.
	 */
	public SADomain setActionTypes(List<ActionType> actions){
		this.actionTypes.clear();
		this.actionMap.clear();
		for(ActionType at : actions){
			this.addActionType(at);
		}
		return this;
	}


	/**
	 * Clears all {@link ActionType}s specified for this domain.
	 * @return this object for method chaining
	 */
	public SADomain clearActionTypes(){
		this.actionMap.clear();
		this.actionTypes.clear();
		return this;
	}


	/**
	 * Returns the {@link ActionType}s associated with this domain.
	 * @return the {@link ActionType}s associated with this domain.
	 */
	public List <ActionType> getActionTypes(){
		return new ArrayList <ActionType>(actionTypes);
	}


	/**
	 * Returns the {@link ActionType} in this domain with the given type name, or null if one does not exist.
	 * @param name the type name
	 * @return the {@link ActionType}
	 */
	public ActionType getAction(String name){
		return actionMap.get(name);
	}


	/**
	 * Returns the {@link SampleModel} associated with this domain, or null if one is not defined.
	 * @return the {@link SampleModel} associated with this domain, or null if one is not defined.
	 */
	public SampleModel getModel() {
		return model;
	}


	/**
	 * Sets the {@link SampleModel} associated with this domain.
	 * @param model the {@link SampleModel} to associate with this domain.
	 */
	public void setModel(SampleModel model) {
		this.model = model;
	}
}
