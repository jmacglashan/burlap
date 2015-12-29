package burlap.behavior.singleagent.learning.modellearning.rmax;

import java.util.ArrayList;
import java.util.List;

import minecraft.NameSpace;
import minecraft.MinecraftBehavior.MinecraftBehavior;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.behavior.singleagent.learning.modellearning.ModelPlanner.ModelPlannerGenerator;
import burlap.behavior.singleagent.learning.modellearning.modelplanners.VIModelPlanner;
import burlap.behavior.singleagent.learning.modellearning.models.TabularModel;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionHypothesis;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.PredictionsLearner;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.OOMDPModel;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.EffectHelpers;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.StatePerception;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.shaping.potential.PotentialFunction;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.UniformCostRF;





public class DOORMAXExperiments {

	private static void runExperiments(final Domain d, final RewardFunction rf, final TerminalFunction tf, final double maxReward, final int nConfident,
			final double maxVIDelta, final int maxVIPasses, final State initialState, int numTrials, int numEpisodes, final int k) {
		final StateHashFactory hf = new DiscreteStateHashFactory();
		final Model tabModel = new TabularModel(d,new DiscreteStateHashFactory(),1);

		final List<PropositionalFunction> propFunsToUse = d.getPropFunctions();
		final List<String> effectsToUse = new ArrayList<String>();
		effectsToUse.add(EffectHelpers.arithEffect);
		effectsToUse.add(EffectHelpers.assigEffect);

		//EXPERIMENTS
		LearningAgentFactory RMAXLearningFactory = new LearningAgentFactory() {
			@Override
			public String getAgentName() {
				return "RMAX";
			}

			@Override
			public LearningAgent generateAgent() {
				return new PotentialShapedRMax(d, rf, tf,.9, hf,maxReward, nConfident, maxVIDelta, maxVIPasses, tabModel);
			}
		};


		LearningAgentFactory DOORMaxLearning = new LearningAgentFactory() {

			@Override
			public String getAgentName() {
				return "DOORMAXWithPFs";
			}

			@Override
			public LearningAgent generateAgent() {
				final Model oomdpModel = new OOMDPModel(d,rf, tf,propFunsToUse, effectsToUse, initialState, k, null);
				return new PotentialShapedRMax(d, rf, tf,.9, hf,maxReward, nConfident, maxVIDelta, maxVIPasses, oomdpModel);
			}
		};
		
		LearningAgentFactory DOORMaxTabAttStateLearning = new LearningAgentFactory() {

			@Override
			public String getAgentName() {
				return "DOORMaxTabAttStatePerception-Learning";
			}

			@Override
			public LearningAgent generateAgent() {
				final Model oomdpModel = new OOMDPModel(d,rf, tf,propFunsToUse, effectsToUse, initialState, k, StatePerception.FullAttributeStatePerception);
				return new PotentialShapedRMax(d, rf, tf,.9, hf,maxReward, nConfident, maxVIDelta, maxVIPasses, oomdpModel);
			}
		};
		
		LearningAgentFactory DOORMAXPFStatePerception = new LearningAgentFactory() {

			@Override
			public String getAgentName() {
				return "DOORMAXPFStatePerception-Learning";
			}

			@Override
			public LearningAgent generateAgent() {
				final Model oomdpModel = new OOMDPModel(d,rf, tf,propFunsToUse, effectsToUse, initialState, k, StatePerception.PFStatePerception);
				return new PotentialShapedRMax(d, rf, tf,.9, hf,maxReward, nConfident, maxVIDelta, maxVIPasses, oomdpModel);
			}
		};
		
		LearningAgentFactory DOORMAXRelationalStatePerception = new LearningAgentFactory() {

			@Override
			public String getAgentName() {
				return "DOORMAXRelationalStatePerception-Learning";
			}

			@Override
			public LearningAgent generateAgent() {
				final Model oomdpModel = new OOMDPModel(d,rf, tf,propFunsToUse, effectsToUse, initialState, k, StatePerception.RelationalStatePerception);
				return new PotentialShapedRMax(d, rf, tf,.9, hf,maxReward, nConfident, maxVIDelta, maxVIPasses, oomdpModel);
			}
		};
		
		LearningAgentFactory DOORMAXClassRelationalStatePerception = new LearningAgentFactory() {

			@Override
			public String getAgentName() {
				return "DOORMAXWithoutPFs";
			}

			@Override
			public LearningAgent generateAgent() {
				final Model oomdpModel = new OOMDPModel(d,rf, tf,propFunsToUse, effectsToUse, initialState, k, StatePerception.ClassRelationalStatePerception);
				return new PotentialShapedRMax(d, rf, tf,.9, hf,maxReward, nConfident, maxVIDelta, maxVIPasses, oomdpModel);
			}
		};
		
		StateGenerator sg = new ConstantStateGenerator(initialState);

		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter((SADomain)d, rf, sg, numTrials, numEpisodes,RMAXLearningFactory , DOORMaxLearning,DOORMAXClassRelationalStatePerception);
		exp.setUpPlottingConfiguration(500, 250, 2, 1000, 
				TrialMode.MOSTRECENTANDAVERAGE, 
				PerformanceMetric.CUMULATIVESTEPSPEREPISODE);

		exp.startExperiment();

		exp.writeStepAndEpisodeDataToCSV("expData");
	}

	private static void runTaxiExperiments(final int nConfident, final double maxVIDelta, final int maxVIPasses, int numTrials, int numEpisodes) {
		final DomainGenerator dg = new TaxiDomain();
		final Domain d = dg.generateDomain();
		final State initialState = TaxiDomain.getClassicState(d);
		final TerminalFunction tf = new TaxiDomain.TaxiTF();
		final RewardFunction rf = new UniformCostRF();
		final double maxReward = 0;

		runExperiments(d, rf, tf, maxReward, nConfident, maxVIDelta, maxVIPasses, initialState, numTrials, numEpisodes, 2);
	}

	private static void runGridWorldExperiments(final int nConfident, final double maxVIDelta, final int maxVIPasses, int numTrials, int numEpisodes) {
		GridWorldDomain gwdg = new GridWorldDomain(11, 11);
		gwdg.setMapToFourRooms();

		int endX = 10;
		int endY = 10;

		final Domain d = gwdg.generateDomain();
		final State initialState = GridWorldDomain.getOneAgentOneLocationState(d);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, endX, endY, 0);

		final RewardFunction rf = new UniformCostRF();

		final TerminalFunction tf = new GridWorldTerminalFunction(10, 10);
		final double maxReward = 0;


		runExperiments(d, rf, tf, maxReward, nConfident, maxVIDelta, maxVIPasses, initialState, numTrials, numEpisodes, 1);

	}


	public static void main(String[] args) {

		//PARAMS TO SET

		int numTrials = 1;
		int numEpisodes = 10;
		int nConfident = 1;
		double maxVIDelta = .1;
		int maxVIPasses = 20;

//		runGridWorldExperiments(nConfident, maxVIDelta, maxVIPasses, numTrials, numEpisodes);
		runTaxiExperiments(nConfident, maxVIDelta, maxVIPasses, numTrials, numEpisodes);





	}

}
