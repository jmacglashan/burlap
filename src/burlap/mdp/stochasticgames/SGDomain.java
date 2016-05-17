package burlap.mdp.stochasticgames;

import burlap.mdp.core.Domain;
import burlap.mdp.stochasticgames.agentactions.SGAgentActionType;

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
	 * A map from action names to their corresponding {@link SGAgentActionType}
	 */
	protected Map <String, SGAgentActionType>				singleActionMap = new HashMap<String, SGAgentActionType>();


	/**
	 * The joint action model of the domain
	 */
	protected JointActionModel							jam;
	

	/**
	 * Sets the joint action model associated with this domain.
	 * @param jam the joint action model to associate with this domain.
	 */
	public void setJointActionModel(JointActionModel jam){
		this.jam = jam;
	}

	/**
	 * Returns the joint action model associated with this domain.
	 * @return the joint action model associated with this domain.
	 */
	public JointActionModel getJointActionModel(){
		return this.jam;
	}

	public SGDomain addSGAgentAction(SGAgentActionType sa){
		singleActionMap.put(sa.typeName(), sa);
		return this;
	}

	

	public List <SGAgentActionType> getAgentActions(){
		return new ArrayList<SGAgentActionType>(this.singleActionMap.values());
	}


	public SGAgentActionType getSGAgentAction(String name) {
		return singleActionMap.get(name);
	}



	
	

}
