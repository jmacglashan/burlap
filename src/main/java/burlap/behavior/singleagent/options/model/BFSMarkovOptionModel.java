package burlap.behavior.singleagent.options.model;

import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.options.Option;
import burlap.datastructures.HashedAggregator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.*;

/**
 * A model that can compute a Markov option's transition model, and cache it, from a source {@link SampleModel}. A {@link FullModel} is
 * required for the {@link #transitions(State, Action)} method. Note that the transitions model for an option
 * is a multi-time model, which means the state transition probabilities factor in the discount factor. That is,
 * P(s' | s, o) = \sum_k^\ifnty p(s', k | s, o) \gamma^k, where p(s', k | s, o) is the probability that the
 * agent will terminate in state s' after k steps, given that option o was initiated in state s.
 * <p>
 * The computation of the transition model can be quite
 * expensive (particularly for stochastic domains) and ideally, you should consider a custom implementation of your option model. The computation of
 * the model proceeds by running a BFS-like algorithm from the input state following the option policy
 * to possible option (or environment) termination states. The BFS expansion will stop when a minimum threshold
 * of the probability mass of all possible trajectories following the policy is computed (by default 0.999). However,
 * you can shrink the probability threshold using the method {@link #setMinProb(double)} to decrease computation time.
 * When you decrease the probability threshold,
 * the compute probabilities are normalized by the amount of the trajectory probability mass computed, given
 * an estimated option transition model.
 * <p>
 * If you need a model for non-Markov options (e.g., a {@link burlap.behavior.singleagent.options.MacroAction}), use
 * the {@link BFSNonMarkovOptionModel} model, which using slightly more memory overhead in the computation to maintain
 * the fully trajectory history.
 *
 * @author James MacGlashan.
 */
public class BFSMarkovOptionModel implements FullModel{

	protected SampleModel model;
	protected double discount;
	protected HashableStateFactory hashingFactory;


	protected Map<Option, CachedModel> cachedModels = new HashMap<Option, CachedModel>();
	protected Set<HashableState> srcTerminateStates = new HashSet<HashableState>();

	protected double minProb = 0.999;

	protected boolean requireMarkov = true;


	public BFSMarkovOptionModel(SampleModel model, double discount, HashableStateFactory hashingFactory) {
		this.model = model;
		this.discount = discount;
		this.hashingFactory = hashingFactory;
	}


	public void setMinProb(double minProb) {
		this.minProb = minProb;
	}

	@Override
	public List<TransitionProb> transitions(State s, Action a) {
		if(!(model instanceof FullModel)){
			throw new RuntimeException("Cannot compute option transition function probability distribution, because the underlying state model is" +
					"not a FullModel");
		}

		FullModel fmodel = (FullModel)model;

		if(!(a instanceof Option)){
			return fmodel.transitions(s, a);
		}

		Option o = (Option)a;
		if(!o.markov() && requireMarkov){
			throw new RuntimeException("DerivedOptionMarkovModel can only compute transition function probability distribution for Markov options, but the input Option is not Markov");
		}


		CachedModel cmodel = this.getOrCreateModel(o);
		List<TransitionProb> result = cmodel.cachedExpectations.get(hashingFactory.hashState(s));
		if(result != null){
			return result;
		}

		HashedAggregator <HashableState> possibleTerminations = new HashedAggregator<HashableState>();
		double [] expectedReturn = new double[]{0.};

		double sumProb = this.computeTransitions(s, o, possibleTerminations, expectedReturn);


		double r = expectedReturn[0];

		List<TransitionProb> transitions = new ArrayList<TransitionProb>(possibleTerminations.size());
		for(Map.Entry<HashableState, Double> e : possibleTerminations.entrySet()){
			EnvironmentOutcome eo = new EnvironmentOutcome(s, a, e.getKey().s(), r, srcTerminateStates.contains(e.getKey()));
			double p = e.getValue();
			p /= sumProb;
			TransitionProb tp = new TransitionProb(p, eo);
			transitions.add(tp);
		}

		return transitions;
	}

	@Override
	public EnvironmentOutcome sample(State s, Action a) {
		if(!(a instanceof Option)){
			return model.sample(s, a);
		}

		Option o = (Option)a;

		SimulatedEnvironment env = new SimulatedEnvironment(model, s);
		return o.control(env, discount);
	}

	@Override
	public boolean terminal(State s) {
		return this.model.terminal(s);
	}


	protected CachedModel getOrCreateModel(Option o){
		CachedModel model = this.cachedModels.get(o);
		if(model != null){
			return model;
		}
		model = new CachedModel();
		this.cachedModels.put(o, model);
		return model;
	}


	protected double computeTransitions(State s, Option o, HashedAggregator<HashableState> possibleTerminations, double [] expectedReturn){

		double sumTermProb = 0.;

		LinkedList<OptionScanNode> openList = new LinkedList<OptionScanNode>();
		OptionScanNode inode = new OptionScanNode(s);
		openList.addLast(inode);

		while(openList.size() > 0 && sumTermProb < this.minProb){

			OptionScanNode src = openList.poll();
			double probTerm = 0.0; //can never terminate in initiation state
			if(src.nSteps > 0){
				probTerm = o.probabilityOfTermination(src.s, null);
			}
			if(this.model.terminal(src.s)){
				probTerm = 1.;
			}

			double probContinue = 1.-probTerm;
			double stackedDiscount = Math.pow(this.discount, src.nSteps);

			//handle possible termination
			if(probTerm > 0.){
				double probOfDiscountedTrajectory = src.probability*stackedDiscount*probTerm;
				possibleTerminations.add(hashingFactory.hashState(src.s), probOfDiscountedTrajectory);
				expectedReturn[0] += src.cumulativeDiscountedReward*src.probability*probTerm;
				sumTermProb += src.probability;
			}

			//handle continuation
			if(probContinue > 0.){

				//handle option policy selection
				List <ActionProb> actionSelction = o.policyDistribution(src.s, null);
				for(ActionProb ap : actionSelction){

					//now get possible outcomes of each action
					List <TransitionProb> transitions = ((FullModel)model).transitions(src.s, ap.ga);
					for(TransitionProb tp : transitions){
						double totalTransP = ap.pSelection * tp.p * probContinue;
						double r = stackedDiscount * tp.eo.r;
						if(tp.eo.terminated){
							srcTerminateStates.add(hashingFactory.hashState(tp.eo.op));
						}

						OptionScanNode next = new OptionScanNode(src, tp.eo.op, totalTransP, r);
						openList.addLast(next);

					}

				}

			}

		}


		return sumTermProb;



	}


	public static class CachedModel{

		/**
		 * The cached transition probabilities from each initiation state
		 */
		protected Map<HashableState, List <TransitionProb>> cachedExpectations = new HashMap<HashableState, List<TransitionProb>>();


	}


	public static class OptionScanNode{

		/**
		 * the state this search node wraps
		 */
		public State s;

		/**
		 * the *un*-discounted probability of reaching this search node
		 */
		public double	probability;

		/**
		 * The cumulative discounted reward received reaching this node.
		 */
		public double	cumulativeDiscountedReward;

		/**
		 * The number of steps taken to reach this node.
		 */
		public int		nSteps;

		public OptionScanNode() {
		}

		public OptionScanNode(State s) {
			this.s = s;
			this.probability = 1.;
			this.cumulativeDiscountedReward = 0.;
			this.nSteps = 0;
		}


		/**
		 * Initializes.
		 * @param src a source parent node from which this node was generated
		 * @param s the state this search node wraps
		 * @param transProb the transition probability of reaching this node from the source node
		 * @param discountedR the discounted reward received from reaching this node from the source node.
		 */
		public OptionScanNode(OptionScanNode src, State s, double transProb, double discountedR){

			this.s = s;
			this.probability = src.probability*transProb;
			this.cumulativeDiscountedReward = src.cumulativeDiscountedReward + discountedR;
			this.nSteps = src.nSteps+1;


		}
	}

}
