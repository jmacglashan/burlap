package burlap.behavior.singleagent.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * This class extends the OOMDP planner to define a class of planners that compute state value functions
 * using the tabular Bellman update, such as ValueIteraiton. It defines data members for storing hashed transition dynamics
 * (so that they can be quickly retrieved without multiple calls to the action transition generation) and a map
 * from states to their values. It also adds support for the QComputable planner which can return
 * Q-values by using the transition dynamics and the stored value function.
 * @author James MacGlashan
 *
 */
public abstract class ValueFunctionPlanner extends OOMDPPlanner implements QComputablePlanner{

	/**
	 * A data structure for storing the hashed transition dynamics from each state.
	 */
	protected Map <StateHashTuple, List<ActionTransitions>>			transitionDynamics;
	
	/**
	 * A map for storing the current value function estimate for each state.
	 */
	protected Map <StateHashTuple, Double>							valueFunction;
	
	
	/**
	 * The value function initialization to use; defaulted to an initialization of 0 everywhere.
	 */
	protected ValueFunctionInitialization							valueInitializer = new ValueFunctionInitialization.ConstantValueFunctionInitialization();
	

	@Override
	public abstract void planFromState(State initialState);
	
	
	
	
	/**
	 * Common init method for ValueFunction Planners. This will automatically call the OOMDPPLanner init method.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory
	 */
	public void VFPInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory){
		
		this.plannerInit(domain, rf, tf, gamma, hashingFactory);
		
		this.transitionDynamics = new HashMap<StateHashTuple, List<ActionTransitions>>();
		this.valueFunction = new HashMap<StateHashTuple, Double>();
		
		
		
	}
	
	
	/**
	 * Returns the value function evaluation of the given state. If the value is not stored, then the default value
	 * specified by the ValueFunctionInitialization object of this class is returned.
	 * @param s the state to evaluate.
	 * @return the value function evaluation of the given state.
	 */
	public double value(State s){
		StateHashTuple sh = this.hashingFactory.hashState(s);
		return this.value(sh);
	}
	
	/**
	 * Returns the value function evaluation of the given hashed state. If the value is not stored, then the default value
	 * specified by the ValueFunctionInitialization object of this class is returned.
	 * @param s the hashed state to evaluate.
	 * @return the value function evaluation of the given state.
	 */
	public double value(StateHashTuple sh){
		Double V = valueFunction.get(sh);
		double v = V == null ? this.getDefaultValue(sh.s) : V;
		return v;
	}
	
	@Override
	public List <QValue> getQs(State s){
		
		StateHashTuple sh = this.stateHash(s);
		Map<String,String> matching = null;
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		
		if(indexSH == null){
			//then this is an unexplored state
			indexSH = sh;
			mapToStateIndex.put(indexSH, indexSH);
		}
		
		
		if(this.containsParameterizedActions && !this.domain.isNameDependent()){
			matching = sh.s.getObjectMatchingTo(indexSH.s, false);
		}
		
		
		List <QValue> res = new ArrayList<QValue>();
		for(Action a : actions){
			List <GroundedAction> applications = s.getAllGroundedActionsFor(a);
			for(GroundedAction ga : applications){
				res.add(this.getQ(sh, ga, matching));
			}
		}
		
		return res;
		
	}
	
	
	
	@Override
	public QValue getQ(State s, GroundedAction a){
		StateHashTuple sh = this.stateHash(s);
		Map<String,String> matching = null;
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		
		if(indexSH == null){
			//then this is an unexplored state
			indexSH = sh;
			mapToStateIndex.put(indexSH, indexSH);
		}
		
		if(this.containsParameterizedActions && !this.domain.isNameDependent()){
			matching = sh.s.getObjectMatchingTo(indexSH.s, false);
		}
		return this.getQ(sh, a, matching);
	}
	
	
	
	/**
	 * This method will return all states that are stored in this planners value function.
	 * @return all states that are stored in this planners value function.
	 */
	public List <State> getAllStates(){
		List <State> result = new ArrayList<State>(valueFunction.size());
		Set<StateHashTuple> shs = valueFunction.keySet();
		for(StateHashTuple sh : shs){
			result.add(sh.s);
		}
		return result;
	}
	
	
	/**
	 * Gets a Q-Value for a hashed state, grounded action, and object instance matching from the hashed state to states stored in the internal data structure.
	 * @param sh the input state
	 * @param a the action to get the Q-value for
	 * @param matching the object instance matching from sh to the corresponding state stored in the value function
	 * @return the Q-value
	 */
	protected QValue getQ(StateHashTuple sh, GroundedAction a, Map <String, String> matching){
		
		//translate grounded action if necessary
		GroundedAction ta = a;
		if(matching != null){
			ta = this.translateAction(ta, matching);
		}
		
		//find ActionTransition for the designated GA
		List <ActionTransitions> allTransitions = this.getActionsTransitions(sh);
		ActionTransitions matchingAt = null;
		for(ActionTransitions at : allTransitions){
			if(at.matchingTransitions(ta)){
				matchingAt = at;
				break;
			}
		}
		
		double q = this.computeQ(sh.s, matchingAt);
		
		return new QValue(sh.s, a, q);
	}
	
	
	
	/**
	 * Returns the stored action transitions for the given state. If the action transitions
	 * are not already stored, they will be created and stored.
	 * @param sh the input state from which to get the transitions
	 * @return the stored action transitions for the given state
	 */
	protected List <ActionTransitions> getActionsTransitions(StateHashTuple sh){
		List <ActionTransitions> allTransitions = transitionDynamics.get(sh);
		
		if(allTransitions == null){
			//need to create them
			//first get all grounded actions for this state
			List <GroundedAction> gas = new ArrayList<GroundedAction>();
			for(Action a : actions){
				gas.addAll(sh.s.getAllGroundedActionsFor(a));
			}
			
			//now add transitions
			allTransitions = new ArrayList<ActionTransitions>(gas.size());
			for(GroundedAction ga : gas){
				ActionTransitions at = new ActionTransitions(sh.s, ga, hashingFactory);
				allTransitions.add(at);
			}
			
			//set it
			transitionDynamics.put(sh, allTransitions);
			
		}
		
		return allTransitions;
	}
	
	
	/**
	 * Performs a Bellman value function update on the provided (hashed) state. Results are stored in the value function map as well as returned.
	 * @param sh the hashed state on which to perform the Bellman update.
	 * @return the new value of the state.
	 */
	protected double performBellmanUpdateOn(StateHashTuple sh){
		List<ActionTransitions> transitions = transitionDynamics.get(sh);
		double maxQ = Double.NEGATIVE_INFINITY;
		for(ActionTransitions at : transitions){
			double q = this.computeQ(sh.s, at);
			if(q > maxQ){
				maxQ = q;
			}
		}
		
		valueFunction.put(sh, maxQ);
		
		return maxQ;
	}
	
	
	/**
	 * Returns the Q-value for a given set and the possible transitions from it for a given action. This computation
	 * *is* compatible with {@link burlap.behavior.singleagent.options.Option} objects.
	 * @param s the given state
	 * @param trans the given action transitions
	 * @return the double value of a Q-value
	 */
	protected double computeQ(State s, ActionTransitions trans){
		
		double q = this.getDefaultValue(s);
		
		if(trans.ga.action instanceof Option){
			
			Option o = (Option)trans.ga.action;
			double expectedR = o.getExpectedRewards(s, trans.ga.params);
			q += expectedR;
			
			for(HashedTransitionProbability tp : trans.transitions){
				
				double vp = this.value(tp.sh);
				
				//note that for options, tp.p will be the *discounted* probability of transition to s',
				//so there is not need for a discount factor to be included
				q += tp.p * vp; 
				
			}
			
		}
		else{
			
			for(HashedTransitionProbability tp : trans.transitions){
				
				double vp = this.value(tp.sh);
				
				double discount = this.gamma;
				double r = rf.reward(s, trans.ga, tp.sh.s);
				q += tp.p * (r + (discount * vp));
				
			}
			
		}
		
		
		return q;
	}
	
	/**
	 * Returns the default V-value to use for the state
	 * @param s the input state to get the default V-value for
	 * @return the default V-value in double form.
	 */
	protected double getDefaultValue(State s){
		return this.valueInitializer.value(s);
	}
	
	
	
	
	
	/**
	 * Options need to to have transition probabilities computed and keep track of the possible termination states
	 * using as hashed data structure. This method tells each option which state hashing factory to use.
	 */
	protected void initializeOptionsForExpectationComputations(){
		for(Action a : this.actions){
			if(a instanceof Option){
				((Option)a).setExpectationHashingFactory(hashingFactory);
			}
		}
	}
	
	
}
