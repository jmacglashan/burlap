package burlap.behavior.singleagent.vfa;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.singleagent.GroundedAction;


public class ActionFeaturesQuery {

	public GroundedAction		queryAction;
	public List<StateFeature>	features;
	
	
	public ActionFeaturesQuery(GroundedAction queryAction) {
		this.queryAction = queryAction;
		this.features = new ArrayList<StateFeature>();
	}
	
	public ActionFeaturesQuery(GroundedAction queryAction, List<StateFeature> features) {
		this.queryAction = queryAction;
		this.features = features;
	}
	
	public void addFeature(StateFeature sf){
		this.features.add(sf);
	}
	
	public boolean featuresForQuery(GroundedAction ga){
		if(queryAction.equals(ga)){
			return true;
		}
		return false;
	}
	
	
	public static ActionFeaturesQuery getActionFeaturesForQueryFromSet(GroundedAction query, List <ActionFeaturesQuery> actionFeaturesSets){
		for(ActionFeaturesQuery afq : actionFeaturesSets){
			if(afq.featuresForQuery(query)){
				return afq;
			}
		}
		
		return null;
	}

}
