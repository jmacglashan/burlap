package burlap.mdp.stochasticgames;

import burlap.mdp.core.Domain;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.stochasticgames.model.JointModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is used to define Stochastic Games Domains. This class extends the parent {@link burlap.mdp.core.Domain} class
 * by including an index for possible actions each individual agent can take and the joint action model that defines the
 * "physics" of the domain.
 * @author James MacGlashan
 *
 */
public class SGDomain implements Domain{


	/**
	 * A map from action type names to their corresponding {@link ActionType}
	 */
	protected Map <String, ActionType> actionMap = new HashMap<String, ActionType>();


	/**
	 * The joint action model of the domain
	 */
	protected JointModel jam;
	

	/**
	 * Sets the joint action model associated with this domain.
	 * @param jam the joint action model to associate with this domain.
	 */
	public void setJointActionModel(JointModel jam){
		this.jam = jam;
	}

	/**
	 * Returns the joint action model associated with this domain.
	 * @return the joint action model associated with this domain.
	 */
	public JointModel getJointActionModel(){
		return this.jam;
	}

	public SGDomain addActionType(ActionType actionType){
		actionMap.put(actionType.typeName(), actionType);
		return this;
	}

	

	public List <ActionType> getActionTypes(){
		return new ArrayList<ActionType>(this.actionMap.values());
	}


	public ActionType getActionType(String name) {
		return actionMap.get(name);
	}



	
	

}
