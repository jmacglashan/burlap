package burlap.behavior.singleagent.planning.stochastic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.Policy.ActionProb;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.options.Option;
import burlap.oomdp.core.*;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.HashableState;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * A class for performing dynamic programming operations: updating the value function using a Bellman backup.
 * It defines data members for storing hashed transition dynamics
 * (so that they can be quickly retrieved without multiple calls to the action transition generation) and a map
 * from states to their values. It also implements {@link burlap.behavior.valuefunction.QFunction} which can return
 * Q-values by using the transition dynamics and the stored value function.
 * <p>
 * Note that by default {@link burlap.behavior.singleagent.planning.stochastic.DynamicProgramming} instances
 * will cache the transition dynamics so that they do not have to be procedurally generated
 * by the {@link burlap.oomdp.singleagent.Action}. Transition dynamic caching can be disable by calling the {@link #toggleUseCachedTransitionDynamics(boolean)}
 * method. This may be desirable if the transition dynamics are expected to change with time, such as when the model is being learned in model-based RL.
 * @author James MacGlashan
 *
 */
public class DynamicProgramming extends MDPSolver implements ValueFunction, QFunction {

	
	
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
	protected Map <HashableState, List<ActionTransitions>>			transitionDynamics;
	
	
	/**
	 * A map for storing the current value function estimate for each state.
	 */
	protected Map <HashableState, Double>							valueFunction;
	
	
	/**
	 * The value function initialization to use; defaulted to an initialization of 0 everywhere.
	 */
	protected ValueFunctionInitialization valueInitializer = new ValueFunctionInitialization.ConstantValueFunctionInitialization();
	

	
	
	/**
	 * Common init method for {@link burlap.behavior.singleagent.planning.stochastic.DynamicProgramming} instances. This will automatically call the
	 * {@link burlap.behavior.singleagent.MDPSolver#solverInit(burlap.oomdp.core.Domain, burlap.oomdp.singleagent.RewardFunction, burlap.oomdp.core.TerminalFunction, double, burlap.oomdp.statehashing.HashableStateFactory)}
	 * method.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory
	 */
	public void DPPInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, HashableStateFactory hashingFactory){
		
		this.solverInit(domain, rf, tf, gamma, hashingFactory);
		
		this.transitionDynamics = new HashMap<HashableState, List<ActionTransitions>>();
		this.valueFunction = new HashMap<HashableState, Double>();
		
		
		
	}
	
	
	@Override
	public void resetSolver(){
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
		HashableState sh = this.hashingFactory.hashState(s);
		return this.valueFunction.containsKey(sh);
	}
	
	
	/**
	 * Returns the value function evaluation of the given state. If the value is not stored, then the default value
	 * specified by the ValueFunctionInitialization object of this class is returned.
	 * @param s the state to evaluate.
	 * @return the value function evaluation of the given state.
	 */
	@Override
	public double value(State s){
		HashableState sh = this.hashingFactory.hashState(s);
		return this.value(sh);
	}
	
	/**
	 * Returns the value function evaluation of the given hashed state. If the value is not stored, then the default value
	 * specified by the ValueFunctionInitialization object of this class is returned.
	 * @param sh the hashed state to evaluate.
	 * @return the value function evaluation of the given state.
	 */
	public double value(HashableState sh){
		if(this.tf.isTerminal(sh.s)){
			return 0.;
		}
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
		
		HashableState sh = this.stateHash(s);
		Map<String,String> matching = null;
		HashableState indexSH = mapToStateIndex.get(sh);
		
		if(indexSH == null){
			//then this is an unexplored state
			indexSH = sh;
			mapToStateIndex.put(indexSH, indexSH);
		}
		
		
//		if(this.containsParameterizedActions && !this.domain.isObjectIdentifierDependent()){
//			matching = sh.s.getObjectMatchingTo(indexSH.s, false);
//		}
		
		
		List <QValue> res = new ArrayList<QValue>();
		for(Action a : actions){
			//List <GroundedAction> applications = s.getAllGroundedActionsFor(a);
			List<GroundedAction> applications = a.getAllApplicableGroundedActions(s);
			for(GroundedAction ga : applications){
				if(matching == null && ga instanceof AbstractObjectParameterizedGroundedAction){
					matching = sh.s.getObjectMatchingTo(indexSH.s, false);
				}
				res.add(this.getQ(sh, ga, matching));
			}
		}
		
		return res;
		
	}
	
	
	
	@Override
	public QValue getQ(State s, AbstractGroundedAction a){
		
		
		if(this.useCachedTransitions){
			HashableState sh = this.stateHash(s);
			Map<String,String> matching = null;
			HashableState indexSH = mapToStateIndex.get(sh);
			
			if(indexSH == null){
				//then this is an unexplored state
				indexSH = sh;
				mapToStateIndex.put(indexSH, indexSH);
			}
			
//			if(this.containsParameterizedActions && !this.domain.isObjectIdentifierDependent() && a.parametersAreObjects()){
//				matching = sh.s.getObjectMatchingTo(indexSH.s, false);
//			}
			if(a instanceof AbstractObjectParameterizedGroundedAction){
				matching = sh.s.getObjectMatchingTo(indexSH.s, false);
			}
			return this.getQ(sh, (GroundedAction)a, matching);
			
		}
		else{
			
			HashableState sh = this.stateHash(s);
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
		Set<HashableState> shs = valueFunction.keySet();
		for(HashableState sh : shs){
			result.add(sh.s);
		}
		return result;
	}


	public DynamicProgramming getCopyOfValueFunction(){

		DynamicProgramming dpCopy = new DynamicProgramming();
		dpCopy.DPPInit(this.domain, this.rf, this.tf, this.gamma, this.hashingFactory);


		//copy the value function
		for(Map.Entry<HashableState, Double> e : this.valueFunction.entrySet()){
			dpCopy.valueFunction.put(e.getKey(), e.getValue());
		}
		return dpCopy;
	}

	
	/**
	 * Gets a Q-Value for a hashed state, grounded action, and object instance matching from the hashed states an internally stored hashed transition dynamics.
	 * If the input state is a terminal state, then the value 0 is returned.
	 * @param sh the input state
	 * @param a the action to get the Q-value for
	 * @param matching the object instance matching from sh to the corresponding state stored in the value function
	 * @return the Q-value
	 */
	protected QValue getQ(HashableState sh, GroundedAction a, Map <String, String> matching){
		
		//translate grounded action if necessary
		GroundedAction ta = a;
		if(a instanceof AbstractObjectParameterizedGroundedAction){
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
		
		double q = 0.;
		if(!this.tf.isTerminal(sh.s)){
			q = this.computeQ(sh.s, matchingAt);
		}
		
		return new QValue(sh.s, a, q);
	}
	
	
	
	/**
	 * Returns the stored action transitions for the given state. If the action transitions
	 * are not already cached and this object is set to use caching, then they will be cached.
	 * @param sh the input state from which to get the transitions
	 * @return the stored action transitions for the given state
	 */
	protected List <ActionTransitions> getActionsTransitions(HashableState sh){
		List <ActionTransitions> allTransitions = transitionDynamics.get(sh);
		
		if(allTransitions == null){
			//need to create them
			
			//indicate how this state is stored
			mapToStateIndex.put(sh, sh);
			
			
			//first get all grounded actions for this state
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
	protected double performBellmanUpdateOn(HashableState sh){
		
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
	protected double performFixedPolicyBellmanUpdateOn(HashableState sh, Policy p){
		
		
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
				
				double policyProb = Policy.getProbOfActionGivenDistribution(at.ga, policyDistribution);
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
				
				double policyProb = Policy.getProbOfActionGivenDistribution(ga, policyDistribution);
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
			double expectedR = o.getExpectedRewards(s, trans.ga);
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
	protected double computeQ(HashableState sh, GroundedAction ga){
		
		double q = 0.;
		
		if(ga.action instanceof Option){
			
			Option o = (Option)ga.action;
			double expectedR = o.getExpectedRewards(sh.s, ga);
			q += expectedR;
			
			List <TransitionProbability> tps = o.getTransitions(sh.s, ga);
			for(TransitionProbability tp : tps){
				double vp = this.value(tp.s);
				
				//note that for options, tp.p will be the *discounted* probability of transition to s',
				//so there is no need for a discount factor to be included
				q += tp.p * vp; 
			}
			
		}
		else{
			
			List <TransitionProbability> tps = ga.getTransitions(sh.s);
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


	/**
	 * This class is used to store tabular value function values that can be manipulated with the {@link DynamicProgramming}
	 * methods. It has no planning method defined and will throw a runtime exception if you try to call it. When you pass it a seed
	 * value function (represented as a {@link java.util.Map}), it copies the values into its internal stored value function so that
	 * changes to the original value function may be made without affecting this objects values.
	 */
	public static class StaticVFPlanner extends DynamicProgramming {


		/**
		 * Initializes. The source value function will be *copied* into this objects value function so that changes to the source
		 * will not affect this object. The action list will also be copied.
		 * @param domain the planning domain
		 * @param rf the reward function
		 * @param gamma the discount factor
		 * @param hashingFactory the state hashing factory used to index states
		 * @param allActions the set of actions for computing Q-values
		 * @param srcValueFunction the source value function to copy.
		 */
		public StaticVFPlanner(Domain domain, RewardFunction rf, double gamma, HashableStateFactory hashingFactory, List<Action> allActions, Map <HashableState, Double> srcValueFunction){
			this.DPPInit(domain, rf, new NullTermination(), gamma, hashingFactory);
			for(Action a : allActions){
				this.addNonDomainReferencedAction(a);
			}

			//copy the value function
			for(Map.Entry<HashableState, Double> e : srcValueFunction.entrySet()){
				this.valueFunction.put(e.getKey(), e.getValue());
			}

		}




	}

	
}
