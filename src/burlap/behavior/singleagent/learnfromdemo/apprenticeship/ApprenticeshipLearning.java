package burlap.behavior.singleagent.learnfromdemo.apprenticeship;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learnfromdemo.CustomRewardModel;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.valuefunction.QFunction;
import burlap.debugtools.DPrint;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.SimpleHashableStateFactory;
import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.functions.PSDQuadraticMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;
import com.joptimizer.optimizers.OptimizationResponse;
import com.joptimizer.util.Utils;

import java.util.*;


/** 
 * This algorithm will take expert trajectors and return a policy that models them. It is an implementation of the algorithm described by Abbel and Ng [1].
 * Both the projection method and quadractic programming version are available.
 * 
 * 
 * 
 * 1. Abbeel, Peter and Ng, Andrew. "Apprenticeship Learning via Inverse Reinforcement Learning"
 * 
 * @author Stephen Brawner and Mark Ho; modified by James MacGlashan
 *
 */
public class ApprenticeshipLearning {

	
	public static final int								debugCodeScore = 746329;
	public static final int								debugCodeRFWeights = 636392;
	
	private ApprenticeshipLearning() {
	    // do nothing
	}

	/**
	 * Calculates the Feature Expectations given one demonstration, a feature mapping and a discount factor gamma
	 * @param episodeAnalysis An EpisodeAnalysis object that contains a sequence of state-action pairs
	 * @param featureFunctions Feature Mapping which maps states to features
	 * @param gamma Discount factor gamma
	 * @return The Feature Expectations generated (double array that matches the length of the featureMapping)
	 */
	public static double[] estimateFeatureExpectation(
			EpisodeAnalysis episodeAnalysis, DenseStateFeatures featureFunctions, Double gamma) {
		return ApprenticeshipLearning.estimateFeatureExpectation(
				Arrays.asList(episodeAnalysis), featureFunctions, gamma);
	}

	/**
	 * Calculates the Feature Expectations given a list of demonstrations, a feature mapping and a 
	 * discount factor gamma
	 * @param episodes List of expert demonstrations as EpisodeAnalysis objects
	 * @param featureFunctions Feature Mapping which maps states to features
	 * @param gamma Discount factor for future expected reward
	 * @return The Feature Expectations generated (double array that matches the length of the featureMapping)
	 */
	public static double[] estimateFeatureExpectation(
			List<EpisodeAnalysis> episodes, DenseStateFeatures featureFunctions, Double gamma) {

		double[] featureExpectations = null;

		for (EpisodeAnalysis episodeAnalysis : episodes) {
			for (int i = 0; i < episodeAnalysis.stateSequence.size(); ++i) {
				double [] fvi = featureFunctions.features(episodeAnalysis.stateSequence.get(i));
				if(featureExpectations == null){
					featureExpectations = new double[fvi.length];
				}
				for (int j = 0; j < featureExpectations.length; ++j) {
					if(fvi[j] != 0.){
						featureExpectations[j] += fvi[j] * Math.pow(gamma, i);
					}
				}
			}
		}

		// Normalize the feature expectation values
		for (int i = 0; i < featureExpectations.length; ++i) {
			featureExpectations[i] /= episodes.size();
		}
		return featureExpectations;
	}

	/**
	 * Generates an anonymous instance of a reward function derived from a FeatureMapping 
	 * and associated feature weights
	 * Computes (w^(i))T phi from step 4 in section 3
	 * @param featureFunctions The feature mapping of states to features
	 * @param featureWeights The weights given to each feature
	 * @return An anonymous instance of RewardFunction
	 */
	public static RewardFunction generateRewardFunction(
			DenseStateFeatures featureFunctions, FeatureWeights featureWeights) {
		final DenseStateFeatures newFeatureFunctions = featureFunctions;
		final FeatureWeights newFeatureWeights = new FeatureWeights(featureWeights);
		return new RewardFunction() {
			@Override
			public double reward(State state, Action a, State sprime) {
				double[] featureWeightValues = newFeatureWeights.getWeights();
				double sumReward = 0;
				double [] fv = newFeatureFunctions.features(state);
				for (int i = 0; i < fv.length; ++i) {
					sumReward += featureWeightValues[i] * fv[i];
				}
				return sumReward;
			}

		};
	}

	
	
	/**
	 * Returns the initial state of a randomly chosen episode analysis
	 * @param episodes the expert demonstrations
	 * @return a random episode's initial state
	 */
	public static State getInitialState(List<EpisodeAnalysis> episodes) {
		Random rando = new Random();
		EpisodeAnalysis randomEpisodeAnalysis = episodes.get(rando.nextInt(episodes.size()));
		return randomEpisodeAnalysis.getState(0);
	}


	/**
	 * Computes a policy that models the expert trajectories included in the request object.
	 * @param request the IRL problem description
	 * @return the computed {@link Policy}
	 */
	public static Policy getLearnedPolicy(ApprenticeshipLearningRequest request) {
		if (!request.isValid()) {
			return null;
		}
		if (request.getUsingMaxMargin()) {
			return ApprenticeshipLearning.maxMarginMethod(request);
		}
		return ApprenticeshipLearning.projectionMethod(request);
	}


	/**
	 * Method which implements high level algorithm provided section 3 of
	 * Abbeel, Peter and Ng, Andrew. "Apprenticeship Learning via Inverse Reinforcement Learning"
	 * @param request contains all of the IRL request informaiton.
	 * @return the computed {@link Policy}
	 */
	private static Policy maxMarginMethod(ApprenticeshipLearningRequest request) {

		// Need to evaluate policies with trajectory lengths equal to that of the demonstrated episodes
		int maximumExpertEpisodeLength = 0;
		List<EpisodeAnalysis> expertEpisodes = request.getExpertEpisodes();
		for (EpisodeAnalysis expertEpisode : expertEpisodes) {
			maximumExpertEpisodeLength = 
					Math.max(maximumExpertEpisodeLength, expertEpisode.numTimeSteps());
		}

		Planner planner = request.getPlanner();
		HashableStateFactory stateHashingFactory = planner.getHashingFactory();

		// (1). Randomly generate policy pi^(0)
		SADomain domain = request.getDomain();
		Policy policy = new StationaryRandomDistributionPolicy(domain);

		DenseStateFeatures featureFunctions = request.getFeatureGenerator();
		List<double[]> featureExpectationsHistory = new ArrayList<double[]>();
		double[] expertExpectations = 
				ApprenticeshipLearning.estimateFeatureExpectation(expertEpisodes, featureFunctions, request.getGamma());

		// (1b) Compute u^(0) = u(pi^(0))
		EpisodeAnalysis episodeAnalysis = 
				policy.evaluateBehavior(request.getStartStateGenerator().generateState(), request.getPlanner().getModel(), maximumExpertEpisodeLength);
		double[] featureExpectations = 
				ApprenticeshipLearning.estimateFeatureExpectation(episodeAnalysis, featureFunctions, request.getGamma());
		featureExpectationsHistory.add(featureExpectations);

		int maxIterations = request.getMaxIterations();
		double[] tHistory = new double[maxIterations];
		int policyCount = request.getPolicyCount();
		for (int i = 0; i < maxIterations; ++i) {
			// (2) Compute t^(i) = max_w min_j (wT (uE - u^(j)))
			FeatureWeights featureWeights = null;

			while(featureWeights == null) {
				 featureWeights = solveFeatureWeights(expertExpectations, featureExpectationsHistory);
			}
			
			for(int z = 0; z < featureWeights.weights.length; z++){
				DPrint.c(debugCodeRFWeights, z + ": " + featureWeights.weights[z] + "; ");
			}
			DPrint.cl(debugCodeRFWeights, "");

			// (3) if t^(i) <= epsilon, terminate
			if (featureWeights == null || Math.abs(featureWeights.getScore()) <= request.getEpsilon()) {
				request.setTHistory(tHistory);
				return policy;
			}
			tHistory[i] = featureWeights.getScore();
			DPrint.cl(debugCodeScore, "Score: "+tHistory[i]);
			// (4a) Calculate R = (w^(i))T * phi 
			RewardFunction rewardFunction = 
					ApprenticeshipLearning.generateRewardFunction(featureFunctions, featureWeights);

			// (4b) Compute optimal policy for pi^(i) give R
			CustomRewardModel crModel = new CustomRewardModel(domain.getModel(), rewardFunction);
			planner.resetSolver();
			planner.solverInit(domain, request.getGamma(), stateHashingFactory);
			planner.setModel(crModel);
			planner.planFromState(request.getStartStateGenerator().generateState());

			if (planner instanceof DeterministicPlanner) {
				policy = new DDPlannerPolicy((DeterministicPlanner)planner);
			}
			else if (planner instanceof QFunction) {
				policy = new GreedyQPolicy((QFunction)planner);
			}

			// (5) Compute u^(i) = u(pi^(i))

			List<EpisodeAnalysis> evaluatedEpisodes = new ArrayList<EpisodeAnalysis>();
			for (int j = 0; j < policyCount; ++j) {
				evaluatedEpisodes.add(
						policy.evaluateBehavior(request.getStartStateGenerator().generateState(), crModel, maximumExpertEpisodeLength));
			}
			featureExpectations = 
					ApprenticeshipLearning.estimateFeatureExpectation(evaluatedEpisodes, featureFunctions, request.getGamma());
			featureExpectationsHistory.add(featureExpectations);

			// (6) i++, go back to (2).
		}
		request.setTHistory(tHistory);

		return policy;
	}

	/**
	 * Implements the "projection method" for calculating a policy-tilde with a
	 * feature expectation within epsilon of an expert's feature expectation.
	 * As described in:
	 * Abbeel, Peter and Ng, Andrew. "Apprenticeship Learning via Inverse Reinforcement Learning"
	 * 
	 * Takes in an expert's samples in some domain given some features, and returns a list of
	 * policies that can be evaluated algorithmically or manually.
	 * 
	 * Note: I've stored policy histories AND feature expectation histories
	 *
	 * @param request contains all of the IRL request informaiton.
	 * @return the policy that mimics the expert
	 */
	private static Policy projectionMethod(ApprenticeshipLearningRequest request) {

		//Max steps that the apprentice will have to learn
		int maximumExpertEpisodeLength = 0;
		List<EpisodeAnalysis> expertEpisodes = request.getExpertEpisodes();
		for (EpisodeAnalysis expertEpisode : expertEpisodes) {
			maximumExpertEpisodeLength = Math.max(maximumExpertEpisodeLength, expertEpisode.numTimeSteps());
		}

		//Planning objects
		Planner planner = request.getPlanner();
		HashableStateFactory stateHashingFactory = planner.getHashingFactory();

		//(0) set up policy array; exper feature expectation
		List<Policy> policyHistory = new ArrayList<Policy>();
		List<double[]> featureExpectationsHistory = new ArrayList<double[]>();

		DenseStateFeatures featureFunctions = request.getFeatureGenerator();
		double[] expertExpectations = 
				ApprenticeshipLearning.estimateFeatureExpectation(expertEpisodes, featureFunctions, request.getGamma());

		// (1). Randomly generate policy pi^(0)
		SADomain domain = request.getDomain();
		Policy policy = new StationaryRandomDistributionPolicy(domain);
		policyHistory.add(policy);

		// (1b) Set up initial Feature Expectation based on policy
		List<EpisodeAnalysis> sampleEpisodes = new ArrayList<EpisodeAnalysis>();
		for (int j = 0; j < request.getPolicyCount(); ++j) {
			sampleEpisodes.add(
					policy.evaluateBehavior(request.getStartStateGenerator().generateState(), domain.getModel(), maximumExpertEpisodeLength));
		}
		double[] curFE = 
				ApprenticeshipLearning.estimateFeatureExpectation(sampleEpisodes, featureFunctions, request.getGamma());
		featureExpectationsHistory.add(curFE);
		double[] lastProjFE = null;
		double[] newProjFE;

		int maxIterations = request.getMaxIterations();
		double[] tHistory = new double[maxIterations];
		int policyCount = request.getPolicyCount();
		for (int i = 0; i < maxIterations; ++i) {
			// (2) Compute weights and score using projection method
			//THIS IS THE KEY DIFFERENCE BETWEEN THE MAXIMUM MARGIN METHOD AND THE PROJECTION METHOD
			//On the first iteration, the projection is just set as the current feature expectation
			if (lastProjFE == null) { 
				newProjFE = curFE.clone();
			}
			else {
				newProjFE = projectExpertFE(expertExpectations, curFE, lastProjFE);
			}
			FeatureWeights featureWeights = getWeightsProjectionMethod(expertExpectations, newProjFE);
			tHistory[i] = featureWeights.getScore();
			DPrint.cl(debugCodeScore, "Score: "+tHistory[i]);
			lastProjFE = newProjFE; //don't forget to set the old projection to the new one!


			// (3) if t^(i) <= epsilon, terminate
			if (featureWeights.getScore() <= request.getEpsilon()) {
				return policy;
			}

			for(int z = 0; z < featureWeights.weights.length; z++){
				DPrint.c(debugCodeRFWeights, z + ": " + featureWeights.weights[z] + "; ");
			}
			DPrint.cl(debugCodeRFWeights, "");
			
			
			// (4a) Calculate R = (w^(i))T * phi 
			RewardFunction rewardFunction = 
					ApprenticeshipLearning.generateRewardFunction(featureFunctions, featureWeights);

			// (4b) Compute optimal policy for pi^(i) give R
			CustomRewardModel crModel = new CustomRewardModel(domain.getModel(), rewardFunction);
			planner.resetSolver();
			planner.solverInit(domain, request.getGamma(), stateHashingFactory);
			planner.setModel(crModel);
			planner.planFromState(request.getStartStateGenerator().generateState());
			if (planner instanceof DeterministicPlanner) {
				policy = new DDPlannerPolicy((DeterministicPlanner)planner);
			}
			else if (planner instanceof QFunction) {
				policy = new GreedyQPolicy((QFunction)planner);
			}
			policyHistory.add(policy);

			// (5) Compute u^(i) = u(pi^(i))
			List<EpisodeAnalysis> evaluatedEpisodes = new ArrayList<EpisodeAnalysis>();
			for (int j = 0; j < policyCount; ++j) {
				evaluatedEpisodes.add(
						policy.evaluateBehavior(request.getStartStateGenerator().generateState(), crModel, maximumExpertEpisodeLength));
			}
			curFE = ApprenticeshipLearning.estimateFeatureExpectation(evaluatedEpisodes, featureFunctions, request.getGamma());
			featureExpectationsHistory.add(curFE.clone());

			// (6) i++, go back to (2).
		}
		request.setTHistory(tHistory);
		return policy;
	}

	
	
	/*
	 * Static methods for estimating weights and tolerance in feature expectation space
	 */
	
	
	/**
	 * FeatureWeight factory which solves the best weights given Feature Expectations calculated from
	 * the expert demonstrations and a history of Feature Expectations.
	 * @param expertExpectations Feature Expectations calculated from the expert demonstrations
	 * @param featureExpectations Feature History of feature expectations generated from past policies
	 * @return the best feature weights
	 */
	private static FeatureWeights solveFeatureWeights(
			double[] expertExpectations, List<double[]> featureExpectations) {
		// We are solving a Quadratic Programming Problem here, yay!
		// Solve equation of form xT * P * x + qT * x + r
		// Let x = {w0, w1, ... , wn, t}
		int weightsSize = expertExpectations.length;

		// The objective is to maximize t, or minimize -t
		double[] qObjective = new double[weightsSize + 1];
		qObjective[weightsSize] = -1;
		LinearMultivariateRealFunction objectiveFunction = 
				new LinearMultivariateRealFunction( qObjective, 0);

		// We set the requirement that all feature expectations generated have to be less than the expert
		List<ConvexMultivariateRealFunction> expertBasedWeightConstraints = 
				new ArrayList<ConvexMultivariateRealFunction>();

		// (1/2)xT * Pi * x + qiT + ri <= 0
		// Equation (11) wT * uE >= wT * u(j) + t ===>  (u(j) - uE)T * w + t <= 0
		// Because x = {w0, w1, ... , wn, t}, we can set
		// qi = {u(j)_1 - uE_1, ... , u(j)_n - uE_n, 1}
		for (double[] expectations : featureExpectations) {
			double[] difference = new double[weightsSize + 1];
			for (int i = 0; i < expectations.length; ++i) {
				difference[i] = expectations[i] - expertExpectations[i];
			}
			difference[weightsSize] = 1;
			expertBasedWeightConstraints.add(new LinearMultivariateRealFunction(difference, 1));
		}

		// L2 norm of weights must be less than or equal to 1. So 
		// P = Identity, except for the last entry (which cancels t).
		double[][] identityMatrix = Utils.createConstantDiagonalMatrix(weightsSize + 1, 1);
		identityMatrix[weightsSize][weightsSize] = 0;
		expertBasedWeightConstraints.add(new PSDQuadraticMultivariateRealFunction(identityMatrix, null, -0.5));

		OptimizationRequest optimizationRequest = new OptimizationRequest();
		optimizationRequest.setF0(objectiveFunction);
		optimizationRequest.setFi(expertBasedWeightConstraints.toArray(
				new ConvexMultivariateRealFunction[expertBasedWeightConstraints.size()]));
		optimizationRequest.setCheckKKTSolutionAccuracy(false);
		optimizationRequest.setTolerance(1.E-12);
		optimizationRequest.setToleranceFeas(1.E-12);

		JOptimizer optimizer = new JOptimizer();
		optimizer.setOptimizationRequest(optimizationRequest);
		try {
			optimizer.optimize();
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		OptimizationResponse optimizationResponse = optimizer.getOptimizationResponse();

		double[] solution = optimizationResponse.getSolution();
		double[] weights = Arrays.copyOfRange(solution, 0, weightsSize);
		double score = solution[weightsSize];
		return new FeatureWeights(weights, score);
	}

	/**
	 * 
	 * This projects the expert's feature expectation onto a line connecting the previous
	 * estimate of the optimal feature expectation to the previous projection. It is step 2a
	 * of the projection method.
	 * 
	 * @param expertFE - The Expert's Feature Expectations (or estimate of)
	 * @param lastFE - The last (i-1)th estimate of the optimal feature expectations
	 * @param lastProjFE - The last (i-2)th projection of the expert's Feature Expectations
	 * @return A new projection of the Expert's feature Expectation
	 */
	private static double[] projectExpertFE(double[] expertFE,
											double[] lastFE,
											double[] lastProjFE) {

		double[] newProjExp = new double[lastProjFE.length];

		double newProjExpCoefficient_num = 0.0;
		double newProjExpCoefficient_den = 0.0;
		//mu_bar^(i-2) + (mu^(i-1)-mu_bar^(i-2))*
			//((mu^(i-1)-mu_bar^(i-2))*(mu_E-mu_bar^(i-2)))/
			//((mu^(i-1)-mu_bar^(i-2))*(mu(i-1)-mu_bar^(i-2)))

		for (int i = 0; i < newProjExp.length; i++) {
			newProjExpCoefficient_num += ((lastFE[i]-lastProjFE[i])*(expertFE[i]-lastProjFE[i]));
			newProjExpCoefficient_den += ((lastFE[i]-lastProjFE[i])*(lastFE[i]-lastProjFE[i]));
		}

		double newProjExpCoefficient = newProjExpCoefficient_num/newProjExpCoefficient_den;

		for (int i = 0; i < newProjExp.length; i++) {
			newProjExp[i] = lastProjFE[i] + (lastFE[i]-lastProjFE[i])*newProjExpCoefficient;
		}

		return newProjExp;
	}



	

	/**
	 * This takes the Expert's feature expectation and a projection, and calculates the weight
	 * and score. This is step 2b of the projection method.
	 * 
	 * @param expertFE expert feature expectation
	 * @param newProjFE a projection
	 * @return the feature weights
	 */
	private static FeatureWeights getWeightsProjectionMethod(double[] expertFE, double[] newProjFE){

		//set the weight as the expert's feature expectation minus the new projection
		double[] weights = new double[newProjFE.length];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = expertFE[i] - newProjFE[i];
		}

		//set the score (t) as the L2 norm of the weight
		double score = 0;
		for (double w : weights) {
			score += w*w;
			}

		score = Math.sqrt(score);
		return new FeatureWeights(weights, score);
	}

	/**
	 * Class of feature weights which contain the weight values and the associated score given to them
	 * @author Stephen Brawner
	 *
	 */
	private static class FeatureWeights {
		private double[] weights;
		private double score;

		private FeatureWeights(double[] weights, double score) {
			this.weights = weights.clone();
			this.score = score;
		}

		public FeatureWeights(FeatureWeights featureWeights) {
			this.weights = featureWeights.getWeights();
			this.score = featureWeights.getScore();
		}

		public double[] getWeights() {
			return this.weights.clone();
		}

		public Double getScore() {
			return this.score;
		}
	}

	/**
	 * This class extends Policy. It creates a random policy distribution lazily for each state and keeps that distribution
	 * forever.
	 * @author Stephen Brawner
	 *
	 */
	public static class StationaryRandomDistributionPolicy extends Policy {
		Map<HashableState, Action> stateActionMapping;
		List<ActionType> actionTypes;
		Map<HashableState, List<ActionProb>> stateActionDistributionMapping;
		HashableStateFactory hashFactory;
		Random rando;

		/**
		 * Constructor initializes the policy, doesn't compute anything here.
		 * @param domain Domain object for which we need to plan
		 */
		private StationaryRandomDistributionPolicy(SADomain domain) {
			this.stateActionMapping = new HashMap<HashableState, Action>();
			this.stateActionDistributionMapping = new HashMap<HashableState, List<ActionProb>>();
			this.actionTypes = domain.getActionTypes();
			this.rando = new Random();
			this.hashFactory = new SimpleHashableStateFactory(true);
		}

		public static Policy generateRandomPolicy(SADomain domain) {
			return new burlap.behavior.policy.RandomPolicy(domain);
		}

		/**
		 * For states which we have not yet visited, this policy needs a randomly generated distribution
		 * of actions. It queries all the grounded actions possible from this state and assigns a random
		 * probability to it. The probabilities are all normalized for happiness.
		 * @param state State for which to generate actions.
		 */
		private void addNewDistributionForState(State state) {
			HashableState hashableState = this.hashFactory.hashState(state);

			// Get all possible actions from this state
			//List<GroundedAction> groundedActions = state.getAllGroundedActionsFor(this.actions);
			List<Action> groundedActions = ActionUtils.allApplicableActionsForTypes(this.actionTypes, state);
			Double[] probabilities = new Double[groundedActions.size()];
			Double sum = 0.0;

			// Create a random distribution of doubles
			for (int i = 0; i < probabilities.length; ++i) {
				probabilities[i] = this.rando.nextDouble();
				sum += probabilities[i];
			}

			List<ActionProb> newActionDistribution = new ArrayList<ActionProb>(groundedActions.size());
			// Normalize distribution and add a new ActionProb to our list.
			for (int i = 0; i < probabilities.length; ++i) {
				ActionProb actionProb = new  ActionProb(groundedActions.get(i), probabilities[i] / sum);
				newActionDistribution.add(actionProb);
			}

			this.stateActionDistributionMapping.put(hashableState, newActionDistribution);
		}

		@Override
		public Action getAction(State s) {
			HashableState hashableState = this.hashFactory.hashState(s);

			// If this state has not yet been visited, we need to compute a new distribution of actions
			if (!this.stateActionDistributionMapping.containsKey(hashableState)) {
				this.addNewDistributionForState(s);
			}

			// Get the action probability distribution for this state
			List<ActionProb> actionDistribution = this.stateActionDistributionMapping.get(hashableState);
			Double roll = this.rando.nextDouble();
			Double probabilitySum = 0.0;

			// Choose an action randomly from this distribution
			for (ActionProb actionProb : actionDistribution) {
				probabilitySum += actionProb.pSelection;
				if (probabilitySum >= roll) {
					return actionProb.ga;
				}
			}
			return null;
		}

		@Override
		public List<ActionProb> getActionDistributionForState(State s) {
			HashableState hashableState = this.hashFactory.hashState(s);

			// If this state has not yet been visited, we need to compute a new distribution of actions
			if (!this.stateActionDistributionMapping.containsKey(hashableState)) {
				this.addNewDistributionForState(s);
			}
			return new ArrayList<ActionProb>(this.stateActionDistributionMapping.get(hashableState));
		}

		@Override
		public boolean isStochastic() {
			return true;
		}

		@Override
		public boolean isDefinedFor(State s) {
			return true;
		}
	}
	
}
