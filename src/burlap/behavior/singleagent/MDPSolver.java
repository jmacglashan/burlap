package burlap.behavior.singleagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.support.OptionEvaluatingRF;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

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
	protected Domain												domain;
	
	/**
	 * The hashing factory to use for hashing states in tabular solvers
	 */
	protected StateHashFactory										hashingFactory;
	
	/**
	 * The task reward function
	 */
	protected RewardFunction										rf;
	
	/**
	 * The terminal function for identifying terminal states
	 */
	protected TerminalFunction										tf;
	
	/**
	 * The MDP discount factor
	 */
	protected double												gamma;
	
	
	/**
	 * The list of actions this solver can use. May include non-domain specified actions like {@link burlap.behavior.singleagent.options.Option}s.
	 */
	protected List <Action>											actions;
	
	/**
	 * A mapping to internal stored hashed states ({@link burlap.behavior.statehashing.StateHashTuple}) that are stored.
	 * Useful since two identical states may have different object instance name identifiers
	 * that can affect the parameters in GroundedActions.
	 */
	protected Map <StateHashTuple, StateHashTuple>					mapToStateIndex;
	
	/**
	 * Indicates whether the action set for this planner includes parametrized actions
	 */
	protected boolean												containsParameterizedActions;
	
	
	/**
	 * The debug code use for calls to {@link burlap.debugtools.DPrint}
	 */
	protected int													debugCode;

	
	

	@Override
	public abstract void resetSolver();
	
	@Override
	public void solverInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory){
		
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
		this.gamma = gamma;
		this.hashingFactory = hashingFactory;
		
		mapToStateIndex = new HashMap<StateHashTuple, StateHashTuple>();
		
		containsParameterizedActions = false;
		List <Action> actions = domain.getActions();
		this.actions = new ArrayList<Action>(actions.size());
		for(Action a : actions){
			this.actions.add(a);
			if(a instanceof Option){
				Option o = (Option)a;
				o.keepTrackOfRewardWith(rf, gamma);
				o.setExernalTermination(tf);
				o.setExpectationHashingFactory(hashingFactory);
				if(!(this.rf instanceof OptionEvaluatingRF)){
					this.rf = new OptionEvaluatingRF(this.rf);
				}
			}
			if(a.getParameterClasses().length > 0){
				containsParameterizedActions = true;
			}
		}
		
	}
	
	
	@Override
	public void addNonDomainReferencedAction(Action a){
		//make sure it doesn't already exist in the list
		if(!actions.contains(a)){
			actions.add(a);
			if(a instanceof Option){
				Option o = (Option)a;
				o.keepTrackOfRewardWith(rf, gamma);
				o.setExernalTermination(tf);
				if(!(this.rf instanceof OptionEvaluatingRF)){
					this.rf = new OptionEvaluatingRF(this.rf);
				}
			}
			if(a.getParameterClasses().length > 0){
				this.containsParameterizedActions = true;
			}
		}
		
	}
	
	
	@Override
	public void setActions(List<Action> actions){
		this.actions = actions;
	}


	@Override
	public List<Action> getActions(){
		return new ArrayList<Action>(this.actions);
	}
	
	@Override
	public TerminalFunction getTF(){
		return tf;
	}
	
	
	@Override
	public RewardFunction getRF(){
		return rf;
	}


	@Override
	public void setHashingFactory(StateHashFactory hashingFactory) {
		this.hashingFactory = hashingFactory;
	}

	@Override
	public StateHashFactory getHashingFactory(){
		return this.hashingFactory;
	}
	
	
	
	@Override
	public void setRf(RewardFunction rf) {
		this.rf = rf;
	}

	@Override
	public void setTf(TerminalFunction tf) {
		this.tf = tf;
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
	public void setDomain(Domain domain) {
		this.domain = domain;
		this.actions.clear();
		for(Action a : domain.getActions()){
			this.actions.add(a);
		}
	}

	@Override
	public Domain getDomain() {
		return domain;
	}

	@Override
	public RewardFunction getRf() {
		return rf;
	}

	@Override
	public TerminalFunction getTf() {
		return tf;
	}

	/**
	 * Takes a source parametrized GroundedAction and a matching between object instances of two different states and returns a GroudnedAction
	 * with parameters using the matched parameters. This method is useful a stored state and action pair in the planner data structure has different
	 * object name identifiers than a query state that is otherwise identical. The matching is from the state in which the source action is applied
	 * to some target state that is not provided to this method.
	 * @param a the source action that needs to be translated
	 * @param matching a map from object instance names to other object instance names.
	 * @return and new GroundedAction with object parametrization that follow from the matching
	 */
	protected GroundedAction translateAction(GroundedAction a, Map <String,String> matching){
		String [] newParams = new String[a.params.length];
		for(int i = 0; i < a.params.length; i++){
			newParams[i] = matching.get(a.params[i]);
		}
		return new GroundedAction(a.action, newParams);
	}
	
	/**
	 * A shorthand method for hashing a state.
	 * @param s the state to hash
	 * @return a StateHashTuple produce from this planners StateHashFactory.
	 */
	public StateHashTuple stateHash(State s){
		return hashingFactory.hashState(s);
	}
	
	
	/**
	 * Returns all grounded actions in the provided state for all the actions that this planner can use.
	 * @param s the source state for which to get all GroundedActions.
	 * @return all GroundedActions.
	 */
	protected List <GroundedAction> getAllGroundedActions(State s){

		return Action.getAllApplicableGroundedActionsFromActionList(this.actions, s);
		
	}
	
}
