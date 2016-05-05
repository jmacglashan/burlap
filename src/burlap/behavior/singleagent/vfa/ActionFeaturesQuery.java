package burlap.behavior.singleagent.vfa;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.singleagent.GroundedAction;


/**
 * A class that associates an action with a set of state features.
 * @author James MacGlashan
 *
 */
public class ActionFeaturesQuery {

	/**
	 * The action with which the state features are associated
	 */
	public GroundedAction		queryAction;
	
	/**
	 * The list of state features associated with this action.
	 */
	public List<StateFeature>	features;
	
	
	
	/**
	 * Initializes with an empty list of state features
	 * @param queryAction the action with which state features will be associated
	 */
	public ActionFeaturesQuery(GroundedAction queryAction) {
		this.queryAction = queryAction;
		this.features = new ArrayList<StateFeature>();
	}
	
	
	/**
	 * Initializes
	 * @param queryAction the action with which state features are associated
	 * @param features the list of state features associated with this action.
	 */
	public ActionFeaturesQuery(GroundedAction queryAction, List<StateFeature> features) {
		this.queryAction = queryAction;
		this.features = features;
	}
	
	
	/**
	 * Adds a state feature to be associate with this objects action
	 * @param sf the state feature to add
	 */
	public void addFeature(StateFeature sf){
		this.features.add(sf);
	}
	
	
	/**
	 * Indicates whether this association is for a given action.
	 * @param ga a given action to check against
	 * @return true of this objects action is the same as the input action
	 */
	public boolean featuresForQuery(GroundedAction ga){
		if(queryAction.equals(ga)){
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * Returns the {@link ActionFeaturesQuery} object from a list of {@link ActionFeaturesQuery} objects that is associated with a given action.
	 * @param query the query action to find the {@link ActionFeaturesQuery} for
	 * @param actionFeaturesSets the list of {@link ActionFeaturesQuery} objects to search
	 * @return the {@link ActionFeaturesQuery} object that is associated with a given action. Null if it does not exist in the list.
	 */
	public static ActionFeaturesQuery getActionFeaturesForQueryFromSet(GroundedAction query, List <ActionFeaturesQuery> actionFeaturesSets){
		for(ActionFeaturesQuery afq : actionFeaturesSets){
			if(afq.featuresForQuery(query)){
				return afq;
			}
		}
		
		return null;
	}

}
