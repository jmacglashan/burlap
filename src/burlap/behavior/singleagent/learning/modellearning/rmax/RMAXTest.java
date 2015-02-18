package burlap.behavior.singleagent.learning.modellearning.rmax;

import java.util.ArrayList;
import java.util.List;

import minecraft.MinecraftBehavior.MinecraftBehavior;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.behavior.singleagent.learning.modellearning.ModelPlanner.ModelPlannerGenerator;
import burlap.behavior.singleagent.learning.modellearning.modelplanners.VIModelPlanner;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.MultipleConditionEffectsLearner;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.OOMDPModel;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.shaping.potential.PotentialFunction;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.UniformCostRF;





public class RMAXTest {

	
	public static class WallRF implements RewardFunction{

		int gx;
		int gy;

		public WallRF(int gx, int gy){
			this.gx = gx;
			this.gy = gy;
		}


		@Override
		public double reward(State s, GroundedAction a, State sprime) {

			ObjectInstance nagent = sprime.getFirstObjectOfClass(GridWorldDomain.CLASSAGENT);
			int nx = nagent.getDiscValForAttribute(GridWorldDomain.ATTX);
			int ny = nagent.getDiscValForAttribute(GridWorldDomain.ATTY);

			//did agent reach goal location?
			if(nx == this.gx && ny == this.gy){
				return 1000.;
			}

			ObjectInstance pagent = s.getFirstObjectOfClass(GridWorldDomain.CLASSAGENT);
			int px = pagent.getDiscValForAttribute(GridWorldDomain.ATTX);
			int py = pagent.getDiscValForAttribute(GridWorldDomain.ATTY);

			//if agent didn't change position, they must have hit a wall
			if(px == nx && py == ny){
				return -100.;
			}

			return -1.;
		}
	}


	public static void main(String[] args) {

		
		
		
		//GRID WORLD DOMAIN
		//Set up domain and initial state
		GridWorldDomain gwdg = new GridWorldDomain(11, 11);
		gwdg.setMapToFourRooms();

		int endX = 10;
		int endY = 10;

		final double maxReward = 0;

		final Domain d = gwdg.generateDomain();
		State initialState = GridWorldDomain.getOneAgentOneLocationState(d);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, endX, endY, 0);

		//		RewardFunction rf = new WallRF(endX, endY);

		//		GridWorldRewardFunction rf = new GridWorldRewardFunction(d);
		//		rf.setReward(endX, endY, maxReward);

		final RewardFunction rf = new UniformCostRF();

		final TerminalFunction tf = new GridWorldTerminalFunction(10, 10);

		//MINECRAFT DOMAIN
//		String mapName = "src/plane1.map";
//		
//		MinecraftBehavior mcBeh = new MinecraftBehavior(mapName);
//		Domain d = mcBeh.getDomain();
//		double [] results;
//		RewardFunction rf = mcBeh.getRewardFunction();
//		TerminalFunction tf = mcBeh.getTerminalFunction();
//		double maxReward = 0;
//		State initialState = mcBeh.getInitialState();
		
		//BFS
		//		TFGoalCondition goalCondition = new TFGoalCondition(tf);

		//		DeterministicPlanner planner = new BFS(d, goalCondition, new DiscreteStateHashFactory());
		//		
		//		planner.planFromState(initialState);
		//		
		//		Policy p = new SDPlannerPolicy(planner);
		//		
		//		
		//		EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf);
		//		System.out.println(ea.getActionSequenceString());

		//RMAX
		final DiscreteStateHashFactory hf = new DiscreteStateHashFactory();

		final int nConfident = 3;
		final double maxVIDelta = .1;
		final int maxVIPasses = 20;

		int learningIterations = 1;
//		PotentialShapedRMax(d, rf, tf, .9, hf, PotentialFunction potential,
//				oomdpModel, ModelPlannerGenerator plannerGenerator){
		
		
		//RMAX
//		PotentialShapedRMax rmax = new PotentialShapedRMax(d, rf, tf, .9, hf, maxReward, nConfident, maxVIDelta, maxVIPasses);

		//DOORMAX
		List<PropositionalFunction> propFunsToUse = d.getPropFunctions();
		Model oomdpModel = new OOMDPModel(d,rf, tf,propFunsToUse);
		PotentialShapedRMax rmax = new PotentialShapedRMax(d, rf, tf,.9, hf,maxReward, nConfident, maxVIDelta, maxVIPasses,  oomdpModel);
		
		//RUN ALGORITHM
		//run agent for 40 learning episodes
		for(int i = 0; i < learningIterations; i++){
			EpisodeAnalysis ea = rmax.runLearningEpisodeFrom(initialState, 100);
			//average reward is undiscounted cumulative reward divided by number of actions (num time steps -1)
			double avgReward = ea.getDiscountedReturn(1.) / (ea.numTimeSteps() -1);
			System.out.println(avgReward + " average reward for episode " + (i+1));
//			System.out.println(ea.actionSequence);
		}
//		List<TransitionProbability> test = rmax.model.getTransitionProbabilities(initialState, d.getActions().get(0).getAllApplicableGroundedActions(initialState).get(0));

		//		PRINT STATE DETAILS FOR DOORMAX
//				State stateToTest = initialState.copy();		
//				GridWorldDomain.setAgent(stateToTest, 4, 1);
//
				MultipleConditionEffectsLearner MCELearner = ((OOMDPModel)rmax.getModel()).MCELearner;
//			
//				String actionToTest = "east";
//				
//				System.out.println(stateToTest.toString());
//				System.out.println("Unmodeled actions:"  + rmax.model.getUnmodeledActionsForState(stateToTest));
//				
//				System.out.println(MCELearner.stateOfEffectsOnState(stateToTest, d.getAction(actionToTest)));

		System.out.println(MCELearner.predictionsStillInEffect());


		//VISUALIZER
		//get reachable states
		//		ValueFunctionPlanner planner = ((VIModelPlanner)rmax.getModelPlanner()).getValueIterationPlanner().getCopyOfValueFunction();

		//		VIModelPlanner planner = (VIModelPlanner)rmax.getModelPlanner();
		ValueFunctionPlanner planner = ((VIModelPlanner)rmax.getModelPlanner()).getValueIterationPlanner().getCopyOfValueFunction();

		List<State> allStates = StateReachability.getReachableStates(initialState, (SADomain)d, new DiscreteStateHashFactory());

		List<State> modeledStates = new ArrayList<State>();
		for (State state : allStates) {
			if ((rmax.getModel()).stateTransitionsAreModeled(state)) {
				modeledStates.add(state);
			}
		}
		ValueFunctionVisualizerGUI gui = GridWorldDomain.getGridWorldValueFunctionVisualization(modeledStates, planner, new GreedyQPolicy(planner));
		gui.initGUI();


		//EXPERIMENTS
//		LearningAgentFactory RMAXLearningFactory = new LearningAgentFactory() {
//			@Override
//			public String getAgentName() {
//				return "RMAX-Learning";
//			}
//
//			@Override
//			public LearningAgent generateAgent() {
//				return new PotentialShapedRMax(d, rf, tf, .9, hf, maxReward, nConfident, maxVIDelta, maxVIPasses);
//			}
//		};
//
//
//		LearningAgentFactory DOORMaxLearning = new LearningAgentFactory() {
//
//			@Override
//			public String getAgentName() {
//				return "DOORMAX-Learning";
//			}
//
//			@Override
//			public LearningAgent generateAgent() {
//				return new PotentialShapedRMax(d, rf, tf, .9, hf, maxReward, maxVIDelta, maxVIPasses);
//			}
//		};
//
//		StateGenerator sg = new ConstantStateGenerator(initialState);
//
//		//		LearningAgentFactory [] arr = {RMAXLearningFactory, DOORMaxLearning};
//
//
//		int numTrials = 1;
//		int stepsPerEpisode = 20;
//		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter((SADomain)d, rf, sg, numTrials, stepsPerEpisode, RMAXLearningFactory, DOORMaxLearning);
//		exp.setUpPlottingConfiguration(500, 250, 2, 1000, 
//				TrialMode.MOSTRECENTANDAVERAGE, 
//				PerformanceMetric.CUMULATIVESTEPSPEREPISODE);
//
//		exp.startExperiment();
//
//		exp.writeStepAndEpisodeDataToCSV("expData");

	}


	//TODO:
	/**
	 * Parametrized actions
	 * Make efficient
	 * Comment/clean
	 * Don't count on state equality -- use hashing factories
	 * Implement effect contradictions
	 */


}
