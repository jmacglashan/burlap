package burlap.oomdp.stochasticgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.Action;


/**
 * This class is used to define Stochastic Games Domains. This class extends the parent {@link burlap.oomdp.core.Domain} class
 * by including an index for possible actions each individual agent can take and the joint action model that defines the
 * "physics" of the domain.
 * @author James MacGlashan
 *
 */
public class SGDomain extends Domain{

	/**
	 * The full set of actions that could be taken by any agent.
	 */
	protected Set <SGAgentAction> agentActions;

	/**
	 * A map from action names to their corresponding {@link SGAgentAction}
	 */
	protected Map <String, SGAgentAction>				singleActionMap;


	/**
	 * The joint action model of the domain
	 */
	protected JointActionModel							jam;
	
	public SGDomain() {
		super();
		
		agentActions = new HashSet<SGAgentAction>();
		singleActionMap = new HashMap<String, SGAgentAction>();
	}


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
		if(!this.singleActionMap.containsKey(sa.actionName)){
			agentActions.add(sa);
			singleActionMap.put(sa.actionName, sa);
		}
	}

	
	
	@Override
	public List <SGAgentAction> getAgentActions(){
		return new ArrayList<SGAgentAction>(agentActions);
	}
	
	
	@Override
	public SGAgentAction getSingleAction(String name){
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



	@Override
	protected Domain newInstance() {
		return new SGDomain();
	}
	
	

}
