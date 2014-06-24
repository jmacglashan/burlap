package burlap.behavior.singleagent.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * This class extends the OOMDP planner to define a class of planners that compute state value functions
 * using the tabular Bellman update, such as ValueIteraiton. It defines data members for storing hashed transition dynamics
 * (so that they can be quickly retrieved without multiple calls to the action transition generation) and a map
 * from states to their values. It also adds support for the QComputable planner which can return
 * Q-values by using the transition dynamics and the stored value function.
 * <p/>
 * Note that by default ValueFunction planners will cache the transition dynamics so that they do not have to be procedurally generated
 * by the {@link burlap.oomdp.singleagent.Action}. Transition dynamic caching can be disable by calling the {@link #toggleUseCachedTransitionDynamics(boolean)}
 * method. This may be desirable if the transition dynamics are expected to change with time, such as when the model is being learned in model-based RL.
 * @author James MacGlashan
 *
 */
public abstract class ValueFunctionPlanner extends OOMDPPlanner implements QComputablePlanner{

	
	
	/**
	 * A boolean toggle to indicate whether the transition dynamics should cached in a hashed data structure for quicker access,
	 * or computed as needed by the Action methods. The default is true, to cache the transition dynamics. However, this value
	 * should be set to false if it is expected that the transition dynamics can change over time which might be the case
	 * in model learning scenarios.
	 */
	protected boolean												useCachedTransitions = true;
	
	
	/**
	 * A data structure for storing the hashed transition dynamics from each state, if this algorithm is set to use them.
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
	
	
	@Override
	public void resetPlannerResults(){
		this.mapToStateIndex.clear();
		this.valueFunction.clear();
		this.transitionDynamics.clear();
	}
	
	/**
	 * Sets the value function initialization to use.
	 * @param vfInit the object that defines how to initializes the value function.
	 */
	public void setValueFunctionInitialization(ValueFunctionInitialization vfInit){
		this.valueInitializer = vfInit;
	}
	
	/**
	 * Returns the value initialization function used.
	 * @return the value initialization function used.
	 */
	public ValueFunctionInitialization getValueFunctionInitialization(){
		return this.valueInitializer;
	}
	
	
	/**
	 * Returns whether a value for the given state has been computed previously.
	 * @param s the state to check
	 * @return true if the the value for the given state has already been computed; false otherwise.
	 */
	public boolean hasComputedValueFor(State s){
		StateHashTuple sh = this.hashingFactory.hashState(s);
		return this.valueFunction.containsKey(sh);
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
	 * @param sh the hashed state to evaluate.
	 * @return the value function evaluation of the given state.
	 */
	public double value(StateHashTuple sh){
		Double V = valueFunction.get(sh);
		double v = V == null ? this.getDefaultValue(sh.s) : V;
		return v;
	}
	
	
	/**
	 * Sets whether this object should cache hashed transition dynamics for each for faster look up, or whether
	 * to procedurally generate the transition dynamics as needed from the {@link burlap.oomdp.singleagent.Action} objects.
	 * Letting the transition dynamics be procedurally generated may be useful if the transition dynamics can change over the time
	 * such as when using a learned model.
	 * @param useCachedTransitions true if the transition dynamics should be cached and stored; false if they should always be procedurally generated from the {@link burlap.oomdp.singleagent.Action} objects.
	 */
	public void toggleUseCachedTransitionDynamics(boolean useCachedTransitions){
		this.useCachedTransitions = useCachedTransitions;
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
		
		
		if(this.containsParameterizedActions && !this.domain.isObjectIdentifierDependent()){
			matching = sh.s.getObjectMatchingTo(indexSH.s, false);
		}
		
		
		List <QValue> res = new ArrayList<QValue>();
		for(Action a : actions){
			//List <GroundedAction> applications = s.getAllGroundedActionsFor(a);
			List<GroundedAction> applications = a.getAllApplicableGroundedActions(s);
			for(GroundedAction ga : applications){
				res.add(this.getQ(sh, ga, matching));
			}
		}
		
		return res;
		
	}
	
	
	
	@Override
	public QValue getQ(State s, AbstractGroundedAction a){
		
		
		if(this.useCachedTransitions){
			StateHashTuple sh = this.stateHash(s);
			Map<String,String> matching = null;
			StateHashTuple indexSH = mapToStateIndex.get(sh);
			
			if(indexSH == null){
				//then this is an unexplored state
				indexSH = sh;
				mapToStateIndex.put(indexSH, indexSH);
			}
			
			if(this.containsParameterizedActions && !this.domain.isObjectIdentifierDependent()){
				matching = sh.s.getObjectMatchingTo(indexSH.s, false);
			}
			return this.getQ(sh, (GroundedAction)a, matching);
			
		}
		else{
			
			StateHashTuple sh = this.stateHash(s);
			double dq = this.computeQ(sh, (GroundedAction)a);
			
			QValue q = new QValue(s, a, dq);
			
			return q;
			
		}
		
		
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
	 * Gets a Q-Value for a hashed state, grounded action, and object instance matching from the hashed states an internally stored hashed transition dynamics.
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
	 * are not already cached and this object is set to use caching, then they will be cached.
	 * @param sh the input state from which to get the transitions
	 * @return the stored action transitions for the given state
	 */
	protected List <ActionTransitions> getActionsTransitions(StateHashTuple sh){
		List <ActionTransitions> allTransitions = transitionDynamics.get(sh);
		
		if(allTransitions == null){
			//need to create them
			
			//indicate how this state is stored
			mapToStateIndex.put(sh, sh);
			
			
			//first get all grounded actions for this state
			/*
			List <GroundedAction> gas = new ArrayList<GroundedAction>();
			for(Action a : actions){
				gas.addAll(sh.s.getAllGroundedActionsFor(a));
			}*/
			List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, sh.s);
			
			//now add transitions
			allTransitions = new ArrayList<ActionTransitions>(gas.size());
			for(GroundedAction ga : gas){
				ActionTransitions at = new ActionTransitions(sh.s, ga, hashingFactory);
				allTransitions.add(at);
			}
			
			//set it if we're caching
			if(this.useCachedTransitions){
				transitionDynamics.put(sh, allTransitions);
			}
			
		}
		
		return allTransitions;
	}
	
	
	
	
	/**
	 * Performs a Bellman value function update on the provided state. Results are stored in the value function map as well as returned.
	 * If this object is set to used cached transition dynamics and the transition dynamics for this state are not cached, then they will be created and cached.
	 * @param s the state on which to perform the Bellman update.
	 * @return the new value of the state.
	 */
	public double performBellmanUpdateOn(State s){
		return this.performBellmanUpdateOn(this.stateHash(s));
	}
	
	
	/**
	 * Performs a fixed-policy Bellman value function update (i.e., policy evaluation) on the provided state. Results are stored in the value function map as well as returned.
	 * If this object is set to used cached transition dynamics and the transition dynamics for this state are not cached, then they will be created and cached.
	 * @param s the state on which to perform the Bellman update.
	 * @param p the policy that is being evaluated
	 * @return the new value of the state
	 */
	public double performFixedPolicyBellmanUpdateOn(State s, Policy p){
		return this.performFixedPolicyBellmanUpdateOn(this.stateHash(s), p);
	}
	
	
	
	/**
	 * Performs a Bellman value function update on the provided (hashed) state. Results are stored in the value function map as well as returned.
	 * If this object is set to used cached transition dynamics and the transition dynamics for this state are not cached, then they will be created and cached.
	 * @param sh the hashed state on which to perform the Bellman update.
	 * @return the new value of the state.
	 */
	protected double performBellmanUpdateOn(StateHashTuple sh){
		
		if(this.tf.isTerminal(sh.s)){
			//terminal states always have a state value of 0
			valueFunction.put(sh, 0.);
			return 0.;
		}
		
		
		double maxQ = Double.NEGATIVE_INFINITY;
		
		if(this.useCachedTransitions){
		
			List<ActionTransitions> transitions = this.getActionsTransitions(sh);
			for(ActionTransitions at : transitions){
				double q = this.computeQ(sh.s, at);
				if(q > maxQ){
					maxQ = q;
				}
			}
			
		}
		else{
			
			//List <GroundedAction> gas = sh.s.getAllGroundedActionsFor(this.actions);
			List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, sh.s);
			for(GroundedAction ga : gas){
				double q = this.computeQ(sh, ga);
				if(q > maxQ){
					maxQ = q;
				}
			}
			
		}
		
		valueFunction.put(sh, maxQ);
		
		return maxQ;
	}
	
	
	
	
	/**
	 * Performs a fixed-policy Bellman value function update (i.e., policy evaluation) on the provided (hashed) state. Results are stored in the value function map as well as returned.
	 * If this object is set to used cached transition dynamics and the transition dynamics for this state are not cached, then they will be created and cached.
	 * @param sh the hashed state on which to perform the Bellman update.
	 * @param p the policy that is being evaluated
	 * @return the new value of the state
	 */
	protected double performFixedPolicyBellmanUpdateOn(StateHashTuple sh, Policy p){
		
		
		if(this.tf.isTerminal(sh.s)){
			//terminal states always have a state value of 0
			valueFunction.put(sh, 0.);
			return 0.;
		}
		
		double weightedQ = 0.;
		List<ActionProb> policyDistribution = p.getActionDistributionForState(sh.s);
		
		if(this.useCachedTransitions){
			
			List<ActionTransitions> transitions = this.getActionsTransitions(sh);
			for(ActionTransitions at : transitions){
				
				double policyProb = Policy.getProbOfActionGivenDistribution(sh.s, at.ga, policyDistribution);
				if(policyProb == 0.){
					continue; //doesn't contribute
				}
				
				double q = this.computeQ(sh.s, at);
				weightedQ += policyProb*q;
				
			}
			
		}
		else{
			
			//List <GroundedAction> gas = sh.s.getAllGroundedActionsFor(this.actions);
			List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, sh.s);
			for(GroundedAction ga : gas){
				
				double policyProb = Policy.getProbOfActionGivenDistribution(sh.s, ga, policyDistribution);
				if(policyProb == 0.){
					continue; //doesn't contribute
				}
				
				double q = this.computeQ(sh, ga);
				weightedQ += policyProb*q;
			}
			
		}
		
		
		
		valueFunction.put(sh, weightedQ);
		
		return weightedQ;
		
	}
	
	
	/**
	 * Returns the Q-value for a given set and the possible transitions from it for a given action. This computation
	 * *is* compatible with {@link burlap.behavior.singleagent.options.Option} objects.
	 * @param s the given state
	 * @param trans the given action transitions
	 * @return the double value of a Q-value
	 */
	protected double computeQ(State s, ActionTransitions trans){
		
		double q = 0.;
		
		if(trans.ga.action instanceof Option){
			
			Option o = (Option)trans.ga.action;
			double expectedR = o.getExpectedRewards(s, trans.ga.params);
			q += expectedR;
			
			for(HashedTransitionProbability tp : trans.transitions){
				
				double vp = this.value(tp.sh);
				
				//note that for options, tp.p will be the *discounted* probability of transition to s',
				//so there is no need for a discount factor to be included
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
	 * Computes the Q-value using the uncached transition dynamics produced by the Action object methods. This computation
	 * *is* compatible with {@link burlap.behavior.singleagent.options.Option} objects.
	 * @param sh the given state
	 * @param ga the given action
	 * @return the double value of a Q-value for the given state-aciton pair.
	 */
	protected double computeQ(StateHashTuple sh, GroundedAction ga){
		
		double q = 0.;
		
		if(ga.action instanceof Option){
			
			Option o = (Option)ga.action;
			double expectedR = o.getExpectedRewards(sh.s, ga.params);
			q += expectedR;
			
			List <TransitionProbability> tps = o.getTransitions(sh.s, ga.params);
			for(TransitionProbability tp : tps){
				double vp = this.value(tp.s);
				
				//note that for options, tp.p will be the *discounted* probability of transition to s',
				//so there is no need for a discount factor to be included
				q += tp.p * vp; 
			}
			
		}
		else{
			
			List <TransitionProbability> tps = ga.action.getTransitions(sh.s, ga.params);
			for(TransitionProbability tp : tps){
				double vp = this.value(tp.s);
				
				double discount = this.gamma;
				double r = rf.reward(sh.s, ga, tp.s);
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
