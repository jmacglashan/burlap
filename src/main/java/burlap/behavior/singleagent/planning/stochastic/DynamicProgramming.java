package burlap.behavior.singleagent.planning.stochastic;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.stochastic.dpoperator.BellmanOperator;
import burlap.behavior.singleagent.planning.stochastic.dpoperator.DPOperator;
import burlap.behavior.valuefunction.*;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * A class for performing dynamic programming operations: updating the value function using a Bellman backup.
 * @author James MacGlashan
 *
 */
public class DynamicProgramming extends MDPSolver implements ValueFunction, QProvider {


	
	/**
	 * A map for storing the current value function estimate for each state.
	 */
	protected Map <HashableState, Double>							valueFunction;
	
	
	/**
	 * The value function initialization to use; defaulted to an initialization of 0 everywhere.
	 */
	protected ValueFunction valueInitializer = new ConstantValueFunction();


	protected DPOperator operator = new BellmanOperator();
	

	
	
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
	public SampleModel getModel() {
		return this.model;
	}

	@Override
	public void resetSolver(){
		this.valueFunction.clear();
	}
	
	/**
	 * Sets the value function initialization to use.
	 * @param vfInit the object that defines how to initializes the value function.
	 */
	public void setValueFunctionInitialization(ValueFunction vfInit){
		this.valueInitializer = vfInit;
	}
	
	/**
	 * Returns the value initialization function used.
	 * @return the value initialization function used.
	 */
	public ValueFunction getValueFunctionInitialization(){
		return this.valueInitializer;
	}


	/**
	 * Returns the dynamic programming operator used
	 * @return the dynamic programming operator used
	 */
	public DPOperator getOperator() {
		return operator;
	}

	/**
	 * Sets the dynamic programming operator use. Note that default setting is {@link BellmanOperator} (max)
	 * @param operator the dynamic programming operator to use.
	 */
	public void setOperator(DPOperator operator) {
		this.operator = operator;
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
		if(this.model.terminal(s)){
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
		if(this.model.terminal(sh.s())){
			return 0.;
		}
		Double V = valueFunction.get(sh);
		double v = V == null ? this.getDefaultValue(sh.s()) : V;
		return v;
	}
	

	
	
	@Override
	public List <QValue> qValues(State s){
		
		List<Action> gas = this.applicableActions(s);
		List<QValue> qs = new ArrayList<QValue>(gas.size());
		for(Action ga : gas){
			QValue q = new QValue(s, ga, this.qValue(s, ga));
			qs.add(q);
		}

		return qs;
		
	}
	
	
	
	@Override
	public double qValue(State s, Action a){

		double dq = this.computeQ(s, a);
		return dq;
		
	}
	
	
	
	/**
	 * This method will return all states that are stored in this planners value function.
	 * @return all states that are stored in this planners value function.
	 */
	public List <State> getAllStates(){
		List <State> result = new ArrayList<State>(valueFunction.size());
		Set<HashableState> shs = valueFunction.keySet();
		for(HashableState sh : shs){
			result.add(sh.s());
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
	 * Writes the value function table stored in this object to the specified file path.
	 * Uses a standard YAML approach, which means the HashableState and underlying Domain states
	 * must have JavaBean like properties; i.e., have a default constructor and getters and setters (or public data
	 * members) for all relevant fields.
	 * @param path the path to write the value function
	 */
	public void writeValueTable(String path){
		Yaml yaml = new Yaml();
		try {
			yaml.dump(this.valueFunction, new BufferedWriter(new FileWriter(path)));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the value function table located on disk at the specified path. Expects the file to be a Yaml
	 * representation of a Java {@link Map} from {@link HashableState} to {@link Double}.
	 * @param path the path to the save value function table
	 */
	public void loadValueTable(String path){
		Yaml yaml = new Yaml();
		try {
			this.valueFunction = (Map<HashableState, Double>)yaml.load(new FileReader(path));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Performs a Bellman value function update on the provided (hashed) state. Results are stored in the value function map as well as returned.
	 * If this object is set to used cached transition dynamics and the transition dynamics for this state are not cached, then they will be created and cached.
	 * @param sh the hashed state on which to perform the Bellman update.
	 * @return the new value of the state.
	 */
	protected double performBellmanUpdateOn(HashableState sh){
		
		if(model.terminal(sh.s())){
			//terminal states always have a state value of 0
			valueFunction.put(sh, 0.);
			return 0.;
		}


		List<Action> gas = this.applicableActions(sh.s());
		double [] qs = new double[gas.size()];
		int i = 0;
		for(Action ga : gas){
			double q = this.computeQ(sh.s(), ga);
			qs[i] = q;
			i++;
		}

		double nv = operator.apply(qs);
		valueFunction.put(sh, nv);
		
		return nv;
	}
	
	
	
	
	/**
	 * Performs a fixed-policy Bellman value function update (i.e., policy evaluation) on the provided state. Results are stored in the value function map as well as returned.
	 * @param sh the hashed state on which to perform the Bellman update.
	 * @param p the policy that is being evaluated
	 * @return the new value of the state
	 */
	protected double performFixedPolicyBellmanUpdateOn(HashableState sh, Policy p){
		
		
		if(this.model.terminal(sh.s())){
			//terminal states always have a state value of 0
			valueFunction.put(sh, 0.);
			return 0.;
		}
		
		double weightedQ = 0.;
		List<ActionProb> policyDistribution = p.policyDistribution(sh.s());
		

		//List <GroundedAction> gas = sh.s.getAllGroundedActionsFor(this.actions);
		List<Action> gas = this.applicableActions(sh.s());
		for(Action ga : gas){

			double policyProb = PolicyUtils.actionProbGivenDistribution(ga, policyDistribution);
			if(policyProb == 0.){
				continue; //doesn't contribute
			}

			double q = this.computeQ(sh.s(), ga);
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
