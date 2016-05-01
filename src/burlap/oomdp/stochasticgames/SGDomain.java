package burlap.oomdp.stochasticgames;

import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

import java.util.*;


/**
 * This class is used to define Stochastic Games Domains. This class extends the parent {@link burlap.oomdp.core.Domain} class
 * by including an index for possible actions each individual agent can take and the joint action model that defines the
 * "physics" of the domain.
 * @author James MacGlashan
 *
 */
public class SGDomain implements Domain{


	/**
	 * A map from action names to their corresponding {@link SGAgentAction}
	 */
	protected Map <String, SGAgentAction>				singleActionMap = new HashMap<String, SGAgentAction>();


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

	@Override
	public void addSGAgentAction(SGAgentAction sa){
		singleActionMap.put(sa.actionName, sa);
	}

	
	
	@Override
	public List <SGAgentAction> getAgentActions(){
		return new ArrayList<SGAgentAction>(this.singleActionMap.values());
	}


	@Override
	public SGAgentAction getSGAgentAction(String name) {
		return singleActionMap.get(name);
	}

	@Override
	public void addAction(Action act) {
		throw new UnsupportedOperationException("Stochastic Games domain cannot add actions designed for single agent formalisms");
	}



	@Override
	public List<Action> getActions() {
		throw new UnsupportedOperationException("Stochastic Games domain does not contain any action for single agent formalisms");
	}



	@Override
	public Action getAction(String name) {
		throw new UnsupportedOperationException("Stochastic Games domain does not contain any action for single agent formalisms");
	}

	
	

}
