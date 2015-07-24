package burlap.behavior.singleagent.planning.stochastic.sparsesampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QFunction;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * An implementation of the Sparse Sampling (SS) [1] planning algorithm. SS's computational complexity is indepdent of the state space size, which makes it appealing
 * for exponentially large or infinite state space MDPs and it guarantees epsilon optimal planning under certain conditions (use the {@link #setHAndCByMDPError(double, double, int)}
 * method to ensure this, however, the required horizon and C will probably be intractably large). SS must replan for every new state it sees, so an agent following it in general must replan after every step it takes in the real world. Using a
 * Q-based {@link Policy} object, will ensure this behavior because this algorithm will call the planner whenever it's quried for the Q-value for a state it has not seen.
 * <p/>
 * The algorithm operates by building a tree frome the source initial state. The tree is built by sampling C outcome states for each possible state-action pair, thereby generating new
 * state nodes in the tree. The tree is built out to a fixed height H and then in a tail recursive way, the Q-value and state value is estimated using a Bellman update as if the C samples perfectly defined
 * the transition dyanamics. Because the values are are based on fixed horizon and computed in a tail recusive way, only one bellman update is required per node.
 * <p/>
 * Although the complexity of the algorithm is indepdent of the state space size, it is exponential in the height of the tree, so if a large tree height is required
 * to make good value function estimates, this algorithm may not be appropriate. Therefore, when rewards are sparse or uniform except at a distant horizon, this may not be an appropriate
 * algorithm choice.
 * <p/>
 * By default, this classs will remember the estimated Q-value for every state from which the {@link #planFromState(State)} method was called (which will be indirectly called
 * by the Q-value query methods if it does not have the Q-value for it) and it will also remember the value of state tree nodes it computed so that they may be reused in
 * subsequent tree creations, thereby limiting the amount of additional computation required. However, if memory is scarce, the class can be told to forget all prior planning
 * results, except the Q-value estimate for the most recently planned for state, by using the {@link #forgetPreviousPlanResults} method.
 * <p/>
 * By default, the C parameter (number of state transition samples) is fixed for all nodes; however, it may also be set to use a variable C that reduces the number
 * of sampled states the further down in the tree it is according to C_i = C_0 * gamma^(2i), where i is the depth of the node from the root and gamma is the discount
 * factor.
 * <p/>
 * By default, the state value of leafs will be set to 0, but this value can be changed by providing a {@link ValueFunctionInitialization} object via the
 * {@link #setValueForLeafNodes(ValueFunctionInitialization)} method. Using a non-zero heuristic value may reduce the need for a large tree height.
 * <p/>
 * This class will work with {@link Option}s, but including options will necessarily *increase* the computational complexity, so they are not reccommeneded.
 * <p/>
 * This class requires a {@link StateHashFactory}; if the domain is continuous, just use a {@link NameDependentStateHashFactory} instance.
 * <p/>
 * This class can optionally be set to not use sampling and instead use the full Bellman update, which results in the exact finite horizon Q-value being computed.
 * However, this should only be done when the number of possible state transitions is small and when the full model for the domain is defined (that is, the
 * {@link Action#getTransitions(State, String[])} method is defined). To set this class to comptue the exact finite horizon value function, use the
 * {@link #setComputeExactValueFunction(boolean)} method. Note that you cannot use {@link Option}s when using the fully Bellman update, because that would
 * required factored access to the probability of each length of each transition, which is not available from Options (it's aggregated into the transition function
 * itself). An exception will be thrown if {@link Option}s are used with the full Bellman transitions.
 * <p/>
 * 
 * 
 * 1. Kearns, Michael, Yishay Mansour, and Andrew Y. Ng. "A sparse sampling algorithm for near-optimal planning in large Markov decision processes." 
 * Machine Learning 49.2-3 (2002): 193-208.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class SparseSampling extends OOMDPPlanner implements QFunction {

	/**
	 * The height of the tree
	 */
	protected int h;
	
	/**
	 * The number of transition dynamics samples (for the root if depth-variable C is used)
	 */
	protected int c;
	
	/**
	 * Whether the number of transition dyanmic samples should scale with the depth of the node. Default is false.
	 */
	protected boolean useVariableC = false;
	
	/**
	 * Whether previous planning results should be forgetten or reused; default is reused (false).
	 */
	protected boolean forgetPreviousPlanResults = false;
	
	/**
	 * The state value used for leaf nodes; default is zero.
	 */
	protected ValueFunctionInitialization vinit = new ValueFunctionInitialization.ConstantValueFunctionInitialization();
	
	
	/**
	 * This parameter indicates whether the exact finite horizon value function is computed or whether sparse sampling
	 * to estimate should be used. The default is false: to use sparse sampling.
	 */
	protected boolean computeExactValueFunction = false;
	
	
	/**
	 * The tree nodes indexed by state and height.
	 */
	protected Map<HashedHeightState, StateNode> nodesByHeight;
	
	/**
	 * The root state node Q-values that have been estimated by previous planning calls.
	 */
	protected Map<StateHashTuple, List<QValue>> rootLevelQValues;
	
	
	/**
	 * The total number of pseudo-Bellman updates
	 */
	protected int numUpdates = 0;

	
	
	/**
	 * Initializes. Note that you can have h and c set to values that ensure epsilon optimality by using the {@link #setHAndCByMDPError(double, double, int)} method, but in
	 * general this will result in very large values that will be intractable. If you set c = -1, then the full transition dynamics will be used. You should
	 * only use the full transition dynanics if the number of possible transitions from each state is small and if the domain Action object's {@link Action#getTransitions(State, String[])}
	 * method is defined.
	 * @param domain the planning domain
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory for matching generated states with their state nodes. If the domain is continuous, use a {@link NameDependentStateHashFactory}
	 * @param h the height of the tree
	 * @param c the number of transition dynamics samples used. If set to -1, then the full transition dynamics are used.
	 */
	public SparseSampling(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, int h, int c){
		this.plannerInit(domain, rf, tf, gamma, hashingFactory);
		this.h = h;
		this.c = c;
		this.nodesByHeight = new HashMap<SparseSampling.HashedHeightState, SparseSampling.StateNode>();
		this.rootLevelQValues = new HashMap<StateHashTuple, List<QValue>>();
		if(this.c < 0){
			this.computeExactValueFunction = true;
		}

		this.debugCode = 7369430;
	}
	
	
	/**
	 * Sets the height and number of transition dynamics samples in a way that ensure epsilon optimality.
	 * @param rmax the maximum reward value of the MDP
	 * @param epsilon the epsilon optimality (amount that the estimated value funciton may diverge from the true optimal)
	 * @param numActions the maximum number of actions that could be applied from a state
	 */
	public void setHAndCByMDPError(double rmax, double epsilon, int numActions){
		double lambda = epsilon * (1. - this.gamma) * (1. - this.gamma) / 4.;
		double vmax = rmax / (1. - this.gamma);
		
		this.h = (int)logbase(this.gamma, lambda / vmax) + 1;
		this.c = (int)( (vmax*vmax / (lambda*lambda)) * (2 * this.h * Math.log(numActions*this.h * vmax * vmax / (lambda * lambda) + Math.log(rmax/lambda))) );
		
		DPrint.cl(this.debugCode, "H = " + this.h);
		DPrint.cl(this.debugCode, "C = " + this.c);
	}
	
	
	/**
	 * Sets whether the number of state transition samples (C) should be variable with respect to the depth of the node. If set
	 * to true, then the samples will be defined using C_i = C_0 * gamma^(2i), where i is the depth of the node from the root, gamma is the discount factor
	 * and C_0 is the normal C value set for this object.
	 * @param useVariableC if true, then depth-variable C will be used; if false, all state nodes use the same number of samples.
	 */
	public void setUseVariableCSize(boolean useVariableC){
		this.useVariableC = useVariableC;
	}
	
	
	/**
	 * Sets the number of state transition samples used.
	 * @param c the number of state transition samples used. If -1, then the full transition dynamics are used.
	 */
	public void setC(int c){
		this.c = c;
		if(this.c < 0){
			this.computeExactValueFunction = true;
		}
		else{
			this.computeExactValueFunction = false;
		}
	}
	
	/**
	 * Sets the height of the tree.
	 * @param h the height of the tree.
	 */
	public void setH(int h){
		this.h = h;
	}
	
	/**
	 * Returns the number of state transition samples
	 * @return teh number of state transition samples
	 */
	public int getC(){
		return this.c;
	}
	
	/**
	 * Returns the height of the tree
	 * @return the height of the tree
	 */
	public int getH(){
		return this.h;
	}
	
	
	/**
	 * Sets whether this planner will compute the exact finite horizon value funciton (using the full transition dynamics) or if sampling
	 * to estimate the value function will be used. The default of this class is to use sampling.
	 * @param computeExactValueFunction if true, the exact finite horizon value function is computed; if false, then sampling is used.
	 */
	public void setComputeExactValueFunction(boolean computeExactValueFunction){
		this.computeExactValueFunction = computeExactValueFunction;
	}
	
	
	/**
	 * Returns whether this planner comptues the exact finite horizon value function (by using the full transition dynamics) or whether
	 * it estimates the value funciton with sampling.
	 * @return true if the exact finite horizon value function is estimate; false if sampling is used.
	 */
	public boolean computesExactValueFunction(){
		return this.computeExactValueFunction;
	}
	
	
	/**
	 * Sets whether previous planning results should be forgetten or resued in subsequent planning. Forgetting results is more memory efficient, but less
	 * CPU efficient.
	 * @param forgetPreviousPlanResults if true, then previous planning results will be forgotten; if true, they will be remembered and reused in susbequent planning.
	 */
	public void setForgetPreviousPlanResults(boolean forgetPreviousPlanResults){
		this.forgetPreviousPlanResults = forgetPreviousPlanResults;
		if(this.forgetPreviousPlanResults){
			this.nodesByHeight.clear();
		}
	}
	
	/**
	 * Sets the {@link ValueFunctionInitialization} object to use for settting the value of leaf nodes.
	 * @param vinit the {@link ValueFunctionInitialization} object to use for settting the value of leaf nodes.
	 */
	public void setValueForLeafNodes(ValueFunctionInitialization vinit){
		this.vinit = vinit;
	}
	
	/**
	 * Returns the debug code used for logging plan results with {@link DPrint}.
	 * @return the debug code used for logging plan results with {@link DPrint}.
	 */
	public int getDebugCode(){
		return this.debugCode;
	}
	

	/**
	 * Sets the debug code used for logging plan results with {@link DPrint}.
	 * @param debugCode the debugCode to use.
	 */
	public void setDebugCode(int debugCode){
		this.debugCode = debugCode;
	}
	
	/**
	 * Returns the total number of state value estimates performed since the {@link #resetPlannerResults()} call.
	 * @return the total number of state value estimates performed since the {@link #resetPlannerResults()} call.
	 */
	public int getNumberOfValueEsitmates(){
		return this.numUpdates;
	}
	
	/**
	 * Returns the total number of state nodes that have been created.
	 * @return the total number of state nodes that have been created.
	 */
	public int getNumberOfStateNodesCreated(){
		return this.nodesByHeight.size() + this.rootLevelQValues.size();
	}
	
	
	@Override
	public void planFromState(State initialState) {
		
		if(this.forgetPreviousPlanResults){
			this.rootLevelQValues.clear();
		}
		
		StateHashTuple sh = this.hashingFactory.hashState(initialState);
		if(this.rootLevelQValues.containsKey(sh)){
			return; //already planned for this state
		}
		
		DPrint.cl(this.debugCode, "Beginning Planning.");
		int oldUpdates = this.numUpdates;
		
		StateNode sn = this.getStateNode(initialState, this.h);
		rootLevelQValues.put(sh, sn.estimateQs());
		
		DPrint.cl(this.debugCode, "Finished Planning with " + (this.numUpdates - oldUpdates) + " value esitmates; for a cumulative total of: " + this.numUpdates);
		
		if(this.forgetPreviousPlanResults){
			this.nodesByHeight.clear();
		}
		
		this.mapToStateIndex.put(sh, sh);

	}

	@Override
	public void resetPlannerResults() {
		this.nodesByHeight.clear();
		this.rootLevelQValues.clear();
		this.numUpdates = 0;
	}
	
	
	@Override
	public List<QValue> getQs(State s) {
		
		StateHashTuple sh = this.hashingFactory.hashState(s);
		List<QValue> qs = this.rootLevelQValues.get(sh);
		if(qs == null){
			this.planFromState(s);
			qs = this.rootLevelQValues.get(sh);
		}
		
		return qs;
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		
		StateHashTuple sh = this.hashingFactory.hashState(s);
		List<QValue> qs = this.rootLevelQValues.get(sh);
		if(qs == null){
			this.planFromState(s);
			qs = this.rootLevelQValues.get(sh);
		}
		
		if(a.params.length > 0 && !this.domain.isObjectIdentifierDependent() && a.parametersAreObjects()){
			StateHashTuple storedSh = this.mapToStateIndex.get(sh);
			a = a.translateParameters(s, storedSh.s);
		}
		
		for(QValue qv : qs){
			if(qv.a.equals(a)){
				return qv;
			}
		}
		
		
		return null;
	}
	
	
	/**
	 * Returns the value of C for a node at the given height (height from a leaf node).
	 * @param height the height from a leaf node.
	 * @return the value of C to use.
	 */
	protected int getCAtHeight(int height){
		if(!this.useVariableC){
			return this.c;
		}
		
		//convert height from bottom to depth from root
		int d = this.h = height;
		int vc = (int) (this.c * Math.pow(this.gamma, 2*d));
		if(vc == 0){
			vc = 1;
		}
		return vc;
	}
	
	
	/**
	 * Either returns, or creates, indexes, and returns, the state node for the given state at the given height in the tree
	 * @param s the state
	 * @param height the height (distance from leaf node) of the node.
	 * @return the state node for the given state at the given height in the tree
	 */
	protected StateNode getStateNode(State s, int height){
		StateHashTuple sh = this.hashingFactory.hashState(s);
		HashedHeightState hhs = new HashedHeightState(sh, height);
		StateNode sn = this.nodesByHeight.get(hhs);
		if(sn == null){
			sn = new StateNode(sh, height);
			this.nodesByHeight.put(hhs, sn);
		}
		
		return sn;
	}
	
	
	/**
	 * A class for state nodes. Includes the state, a value estimate, whether the node has been closed and methods for estimating the Q and V values.
	 * @author James MacGlashan
	 *
	 */
	public class StateNode{
		
		/**
		 * The hashed state this node represents
		 */
		StateHashTuple sh;
		
		/**
		 * The height of the node (distance from a leaf)
		 */
		int height;
		
		/**
		 * The estimated value of the state at this height
		 */
		double v;
		
		/**
		 * Whether this node has been closed.
		 */
		boolean closed = false;
		
		
		/**
		 * Creates a node for the given hased state at the given height
		 * @param sh the hashed state
		 * @param height the height of the node
		 */
		public StateNode(StateHashTuple sh, int height){
			this.sh = sh;
			this.height = height;
		}
		
		
		/**
		 * Estimates and returns the Q-values for this node. Q-values and used state samples are forgotten after this call completes.
		 * @return a {@link List} of the estiamted Q-values for each action.
		 */
		public List<QValue> estimateQs(){
			List<GroundedAction> gas = SparseSampling.this.getAllGroundedActions(this.sh.s);
			List<QValue> qs = new ArrayList<QValue>(gas.size());
			for(GroundedAction ga : gas){
				if(this.height <= 0){
					qs.add(new QValue(this.sh.s, ga, SparseSampling.this.vinit.value(this.sh.s)));
				}
				else{
					double q;
					if(!SparseSampling.this.computeExactValueFunction){
						q = this.sampledBellmanQEstimate(ga);
					}
					else{
						q = this.fullBelmmanQValue(ga);
					}
					
					qs.add(new QValue(this.sh.s, ga, q));
				}
			}
			
			return qs;
		}
		
		/**
		 * Estimates the Q-value using sampling from the transition dynamics. This is the standard Sparse Sampling procedure.
		 * @param ga the action for which the Q-value estimate is to be returned
		 * @return the Q-value estimate
		 */
		protected double sampledBellmanQEstimate(GroundedAction ga){
			
			double sum = 0.;
			
			//generate C samples
			int c = SparseSampling.this.getCAtHeight(this.height);
			for(int i = 0; i < c; i++){
				
				//execute
				State ns = ga.executeIn(this.sh.s);
				
				//manage option stepsize modifications
				int k = 1;
				if(ga.action instanceof Option){
					k = ((Option)ga.action).getLastNumSteps();
				}
				
				//get reward; our rf will automatically do cumumative discounted if it's an option
				double r = SparseSampling.this.rf.reward(this.sh.s, ga, ns);
				
				StateNode nsn = SparseSampling.this.getStateNode(ns, this.height-k);
				
				sum += r + Math.pow(SparseSampling.this.gamma, k)*nsn.estimateV();
			}
			sum /= (double)c;
			
			return sum;
		}
		
		
		/**
		 * Computes the exact Q-value using full Bellman update with the actual transition dynamics. This procedure will cause Sparse Sampling
		 * to compute the exact Q-values and optimal policy for a finite horizon problem. It is reccommened when the number of transitions from
		 * any given state is small tractable to compute.
		 * @param ga the action for which the Q-value estimate is to be returned
		 * @return the exact finite horizon Q-value
		 */
		protected double fullBelmmanQValue(GroundedAction ga){
			
			double sum = 0.;
			List<TransitionProbability> tps = ga.action.getTransitions(this.sh.s, ga.params);
			
			if(!(ga.action instanceof Option)){
				
				for(TransitionProbability tp : tps){
					
					double r = SparseSampling.this.rf.reward(this.sh.s, ga, tp.s);
					StateNode nsn = SparseSampling.this.getStateNode(tp.s, this.height-1);
					sum += tp.p * (r + SparseSampling.this.gamma * nsn.estimateV());
					
				}
				
			}
			else{
				throw new RuntimeException("Sparse Sampling Planner with Full Bellman updates turned on cannot work with options because it needs factored access to the depth for each option transition. Use the standard sampling mode instead.");
			}
			
			
			return sum;
			
		}
		
		
		
		/**
		 * Returns the estimated Q-value if this node is closed, or estimates it and closes it otherwise.
		 * @return the estimated Q-value for this node.
		 */
		public double estimateV(){
			if(this.closed){
				return this.v;
			}
			
			if(SparseSampling.this.tf.isTerminal(this.sh.s)){
				this.v = 0.;
				this.closed = true;
				return this.v;
			}
			
			
			List<QValue> qs = this.estimateQs();
			double max = Double.NEGATIVE_INFINITY;
			for(QValue q : qs){
				max = Math.max(max, q.q);
			}
			SparseSampling.this.numUpdates++;
			this.v = max;
			this.closed = true;
			return max;
		}
		
	}
	
	
	/**
	 * Tuple for a state and its height in a tree that can be hashed for quick retrieval.
	 * @author James MacGlashan
	 *
	 */
	public static class HashedHeightState{
		
		/**
		 * The hashed state
		 */
		public StateHashTuple sh;
		
		/**
		 * The height of the state
		 */
		public int height;
		
		
		/**
		 * Initializes.
		 * @param sh the hashed state
		 * @param height the height of the state.
		 */
		public HashedHeightState(StateHashTuple sh, int height){
			this.sh = sh;
			this.height = height;
		}
		
		@Override
		public boolean equals(Object other){
			HashedHeightState o = (HashedHeightState)other;
			return this.height == o.height && this.sh.equals(o.sh);
		}
		
		@Override
		public int hashCode(){
			return this.height*31 + this.sh.hashCode();
		}
		
	}

	
	/**
	 * Retuns the log value at the given bases. That is: log_base(x)
	 * @param base the log base
	 * @param x the input of the log
	 * @return log_base(x)
	 */
	protected static double logbase(double base, double x){
		return Math.log(x) / Math.log(base);
	}

}
