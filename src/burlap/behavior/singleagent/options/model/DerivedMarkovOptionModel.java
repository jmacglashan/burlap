package burlap.behavior.singleagent.options.model;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.options.Option;
import burlap.datastructures.HashedAggregator;
import burlap.mdp.core.Action;
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
 * @author James MacGlashan.
 */
public class DerivedMarkovOptionModel implements FullModel{

	protected SampleModel model;
	protected double discount;
	protected HashableStateFactory hashingFactory;


	protected Map<Option, CachedModel> cachedModels = new HashMap<Option, CachedModel>();
	protected Set<HashableState> srcTerminateStates = new HashSet<HashableState>();


	/**
	 * The minimum probability a possible terminal state being reached to be included in the computed transition dynamics
	 */
	protected double expectationSearchCutoffProb = 0.001;

	public DerivedMarkovOptionModel(SampleModel model, double discount, HashableStateFactory hashingFactory) {
		this.model = model;
		this.discount = discount;
		this.hashingFactory = hashingFactory;
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
		if(!o.markov()){
			throw new RuntimeException("DerivedOptionMarkovModel can only compute transition function probability distribution for Markov options, but the input Option is not Markov");
		}


		CachedModel cmodel = this.getOrCreateModel(o);
		List<TransitionProb> result = cmodel.cachedExpectations.get(hashingFactory.hashState(s));
		if(result != null){
			return result;
		}

		ExpectationSearchNode node = new ExpectationSearchNode(s);
		HashedAggregator <HashableState> possibleTerminations = new HashedAggregator<HashableState>();
		double [] expectedReturn = new double[]{0.};

		this.iterateExpectationScan(o, node, 1., possibleTerminations, expectedReturn);

		double r = expectedReturn[0];

		List<TransitionProb> transitions = new ArrayList<TransitionProb>(possibleTerminations.size());
		for(Map.Entry<HashableState, Double> e : possibleTerminations.entrySet()){
			EnvironmentOutcome eo = new EnvironmentOutcome(s, a, e.getKey().s(), r, srcTerminateStates.contains(e.getKey()));
			TransitionProb tp = new TransitionProb(e.getValue(), eo);
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

	/**
	 * This method will recursively determine all possible paths that could occur from execution of the option as well
	 * as the expected return. This method will stop expanding the possible paths when the probability of a state
	 * being reached is less than {@link #expectationSearchCutoffProb}
	 * @param o the option for which the transitions are being computed
	 * @param src the source node from which to expand possible paths
	 * @param stackedDiscount the discount amount up to this point
	 * @param possibleTerminations a map of possible termination states and their probability
	 * @param expectedReturn the expected discounted cumulative reward up to node src (this is an array of length 1 that is used to be a mutable double)
	 */
	protected void iterateExpectationScan(Option o, ExpectationSearchNode src, double stackedDiscount,
										  HashedAggregator<HashableState> possibleTerminations, double [] expectedReturn){


		double probTerm = 0.0; //can never terminate in initiation state
		if(src.nSteps > 0){
			probTerm = o.probabilityOfTermination(src.s);
		}

		double probContinue = 1.-probTerm;


		//handle possible termination
		if(probTerm > 0.){
			double probOfDiscountedTrajectory = src.probability*stackedDiscount;
			possibleTerminations.add(hashingFactory.hashState(src.s), probOfDiscountedTrajectory);
			expectedReturn[0] += src.cumulativeDiscountedReward;
		}

		//handle continuation
		if(probContinue > 0.){

			//handle option policy selection
			List <Policy.ActionProb> actionSelction = o.oneStepProbabilities(src.s);
			for(Policy.ActionProb ap : actionSelction){

				//now get possible outcomes of each action
				List <TransitionProb> transitions = ((FullModel)model).transitions(src.s, o);
				for(TransitionProb tp : transitions){
					double totalTransP = ap.pSelection * tp.p;
					double r = stackedDiscount * tp.eo.r;
					if(tp.eo.terminated){
						srcTerminateStates.add(hashingFactory.hashState(tp.eo.op));
					}

					ExpectationSearchNode next = new ExpectationSearchNode(src, tp.eo.op, totalTransP, r);
					if(next.probability > this.expectationSearchCutoffProb && !tp.eo.terminated){
						this.iterateExpectationScan(o, next, stackedDiscount*discount, possibleTerminations, expectedReturn);
					}
				}

			}

		}

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


	public static class CachedModel{

		/**
		 * The cached transition probabilities from each initiation state
		 */
		protected Map<HashableState, List <TransitionProb>> cachedExpectations = new HashMap<HashableState, List<TransitionProb>>();


	}


	/**
	 * A search node class used for finding all possible paths of execution an option could take in the world from each initiation state.
	 * @author James MacGlashan
	 *
	 */
	public static class ExpectationSearchNode{

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



		/**
		 * Initializes with the probability set to 1, the number of steps 0, and the cumulative discounted reward 0. This constructor
		 * is useful for the initial root node of the option path expansion
		 * @param s the state this search node wraps
		 */
		public ExpectationSearchNode(State s){
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
		public ExpectationSearchNode(ExpectationSearchNode src, State s, double transProb, double discountedR){

			this.s = s;
			this.probability = src.probability*transProb;
			this.cumulativeDiscountedReward = src.cumulativeDiscountedReward + discountedR;
			this.nSteps = src.nSteps+1;


		}

	}


}
