package burlap.behavior.singleagent;

import burlap.debugtools.DPrint;
import burlap.mdp.core.Action;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The abstract super class to use for various MDP solving algorithms, including both planning and learning algorithms.
 * It implements the {@link burlap.behavior.singleagent.MDPSolverInterface} and provides
 * the common data members and method implementations that most all algorithms will need to use
 * and provides methods for manipulating them that are common.
 * @author James MacGlashan
 *
 */
public abstract class MDPSolver implements MDPSolverInterface{

	/**
	 * The domain to solve
	 */
	protected SADomain domain;

	protected SampleModel model;
	
	/**
	 * The hashing factory to use for hashing states in tabular solvers
	 */
	protected HashableStateFactory hashingFactory;

	
	/**
	 * The MDP discount factor
	 */
	protected double												gamma;
	
	
	/**
	 * The list of actions this solver can use. May include non-domain specified actions like {@link burlap.behavior.singleagent.options.Option}s.
	 */
	protected List <ActionType> actionTypes;
	
	/**
	 * A mapping to internal stored hashed states ({@link burlap.statehashing.HashableState}) that are stored.
	 * Useful since two identical states may have different object instance name identifiers
	 * that can affect the parameters in GroundedActions.
	 */
	//protected Map <HashableState, HashableState>					mapToStateIndex;

	
	/**
	 * The debug code use for calls to {@link burlap.debugtools.DPrint}
	 */
	protected int													debugCode;

	
	protected boolean usingOptionModel = false;

	@Override
	public abstract void resetSolver();
	
	@Override
	public void solverInit(SADomain domain, double gamma, HashableStateFactory hashingFactory){

		this.gamma = gamma;
		this.hashingFactory = hashingFactory;
		this.setDomain(domain);
		
	}
	
	
	@Override
	public void addNonDomainReferencedAction(ActionType a){
		//make sure it doesn't already exist in the list
		if(!actionTypes.contains(a)){
			actionTypes.add(a);
		}
		
	}

	@Override
	public void setModel(SampleModel model) {
		this.model = model;
	}

	@Override
	public SampleModel getModel() {
		return this.model;
	}

	public void setActionTypes(List<ActionType> actionTypes){
		this.actionTypes = actionTypes;
	}


	public List<ActionType> getActionTypes(){
		return new ArrayList<ActionType>(this.actionTypes);
	}

	@Override
	public void setHashingFactory(HashableStateFactory hashingFactory) {
		this.hashingFactory = hashingFactory;
	}

	@Override
	public HashableStateFactory getHashingFactory(){
		return this.hashingFactory;
	}
	
	


	@Override
	public double getGamma(){
		return this.gamma;
	}
	
	@Override
	public void setGamma(double gamma){
		this.gamma = gamma;
	}

	@Override
	public void setDebugCode(int code){
		this.debugCode = code;
	}
	
	
	@Override
	public int getDebugCode(){
		return debugCode;
	}
	
	
	@Override
	public void toggleDebugPrinting(boolean toggle){
		DPrint.toggleCode(debugCode, toggle);
	}


	@Override
	public void setDomain(SADomain domain) {
		this.domain = domain;
		if(this.domain != null) {

			this.model = domain.getModel();

			if(this.actionTypes != null) {
				this.actionTypes.clear();
			}
			else{
				this.actionTypes = new ArrayList<ActionType>(domain.getActionTypes().size());
			}

			List<ActionType> actionTypes = domain.getActionTypes();
			this.actionTypes = new ArrayList<ActionType>(actionTypes.size());
			for(ActionType a : actionTypes) {
				this.actionTypes.add(a);
			}
		}
	}

	@Override
	public Domain getDomain() {
		return domain;
	}



	
	/**
	 * A shorthand method for hashing a state.
	 * @param s the state to hash
	 * @return a StateHashTuple produce from this planners StateHashFactory.
	 */
	public HashableState stateHash(State s){
		return hashingFactory.hashState(s);
	}
	
	
	/**
	 * Returns all grounded actions in the provided state for all the actions that this valueFunction can use.
	 * @param s the source state for which to get all GroundedActions.
	 * @return all GroundedActions.
	 */
	protected List <Action> getAllGroundedActions(State s){

		return ActionUtils.allApplicableActionsForTypes(this.actionTypes, s);
		
	}
	
}
