package burlap.behavior.singleagent.planning.stochastic;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.Policy.ActionProb;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.mdp.statehashing.HashableState;
import burlap.mdp.statehashing.HashableStateFactory;

import java.util.*;

/**
 * A class for performing dynamic programming operations: updating the value function using a Bellman backup.
 * @author James MacGlashan
 *
 */
public class DynamicProgramming extends MDPSolver implements ValueFunction, QFunction {


	
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
	 * {@link burlap.behavior.singleagent.MDPSolver#solverInit(SADomain, double, HashableStateFactory)}
	 * method.
	 * @param domain the domain in which to plan
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory
	 */
	public void DPPInit(SADomain domain, double gamma, HashableStateFactory hashingFactory){
		
		this.solverInit(domain, gamma, hashingFactory);

		this.valueFunction = new HashMap<HashableState, Double>();
		
		
		
	}
	
	
	@Override
	public void resetSolver(){
		this.valueFunction.clear();
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
		if(this.model.terminalState(s)){
			return 0.;
		}
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
		if(this.model.terminalState(sh.s)){
			return 0.;
		}
		Double V = valueFunction.get(sh);
		double v = V == null ? this.getDefaultValue(sh.s) : V;
		return v;
	}
	

	
	
	@Override
	public List <QValue> getQs(State s){
		
		List<Action> gas = this.getAllGroundedActions(s);
		List<QValue> qs = new ArrayList<QValue>(gas.size());
		for(Action ga : gas){
			QValue q = this.getQ(s, ga);
			qs.add(q);
		}

		return qs;
		
	}
	
	
	
	@Override
	public QValue getQ(State s, Action a){

		double dq = this.computeQ(s, a);
		QValue q = new QValue(s, a, dq);
		return q;
		
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
		dpCopy.DPPInit(this.domain, this.gamma, this.hashingFactory);


		//copy the value function
		for(Map.Entry<HashableState, Double> e : this.valueFunction.entrySet()){
			dpCopy.valueFunction.put(e.getKey(), e.getValue());
		}
		return dpCopy;
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
		
		if(model.terminalState(sh.s)){
			//terminal states always have a state value of 0
			valueFunction.put(sh, 0.);
			return 0.;
		}
		
		
		double maxQ = Double.NEGATIVE_INFINITY;

		List<Action> gas = this.getAllGroundedActions(sh.s);
		for(Action ga : gas){
			double q = this.computeQ(sh.s, ga);
			if(q > maxQ){
				maxQ = q;
			}
		}
			

		
		valueFunction.put(sh, maxQ);
		
		return maxQ;
	}
	
	
	
	
	/**
	 * Performs a fixed-policy Bellman value function update (i.e., policy evaluation) on the provided state. Results are stored in the value function map as well as returned.
	 * @param sh the hashed state on which to perform the Bellman update.
	 * @param p the policy that is being evaluated
	 * @return the new value of the state
	 */
	protected double performFixedPolicyBellmanUpdateOn(HashableState sh, Policy p){
		
		
		if(this.model.terminalState(sh.s)){
			//terminal states always have a state value of 0
			valueFunction.put(sh, 0.);
			return 0.;
		}
		
		double weightedQ = 0.;
		List<ActionProb> policyDistribution = p.getActionDistributionForState(sh.s);
		

		//List <GroundedAction> gas = sh.s.getAllGroundedActionsFor(this.actions);
		List<Action> gas = this.getAllGroundedActions(sh.s);
		for(Action ga : gas){

			double policyProb = Policy.getProbOfActionGivenDistribution(ga, policyDistribution);
			if(policyProb == 0.){
				continue; //doesn't contribute
			}

			double q = this.computeQ(sh.s, ga);
			weightedQ += policyProb*q;
		}
			

		
		
		
		valueFunction.put(sh, weightedQ);
		
		return weightedQ;
		
	}
	
	


	/**
	 * Computes the Q-value This computation
	 * *is* compatible with {@link burlap.behavior.singleagent.options.Option} objects.
	 * @param s the given state
	 * @param ga the given action
	 * @return the double value of a Q-value for the given state-aciton pair.
	 */
	protected double computeQ(State s, Action ga){
		
		double q = 0.;

		List<TransitionProb> tps = ((FullModel)this.model).transitions(s, ga);

		if(ga instanceof Option){

			//for options, expected reward is on state-action level and always the same, so only add it once
			q += tps.get(0).eo.r;

			for(TransitionProb tp : tps){

				double vp = this.value(tp.eo.op);
				
				//note that for options, tp.p will be the *discounted* probability of transition to s',
				//so there is no need for a discount factor to be included
				q += tp.p * vp; 
			}
			
		}
		else{

			for(TransitionProb tp : tps){
				double vp = this.value(tp.eo.op);
				
				double discount = this.gamma;
				double r = tp.eo.r;
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
	

	
}
