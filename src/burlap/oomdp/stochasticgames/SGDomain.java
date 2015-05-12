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
	protected Set <SingleAction>						singleActions;

	/**
	 * A map from action names to their corresponding {@link burlap.oomdp.stochasticgames.SingleAction}
	 */
	protected Map <String, SingleAction>				singleActionMap;


	/**
	 * The joint action model of the domain
	 */
	protected JointActionModel							jam;
	
	public SGDomain() {
		super();
		
		singleActions = new HashSet<SingleAction>();
		singleActionMap = new HashMap<String, SingleAction>();
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
	public void addSingleAction(SingleAction sa){
		if(!this.singleActionMap.containsKey(sa.actionName)){
			singleActions.add(sa);
			singleActionMap.put(sa.actionName, sa);
		}
	}

	
	
	@Override
	public List <SingleAction> getSingleActions(){
		return new ArrayList<SingleAction>(singleActions);
	}
	
	
	@Override
	public SingleAction getSingleAction(String name){
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
