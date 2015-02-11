package burlap.behavior.singleagent.learning.modellearning.rmax;
import burlap.domain.singleagent.gridworld.*;
import burlap.oomdp.core.*;
import burlap.behavior.singleagent.auxiliary.performance.*;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.behavior.singleagent.learning.*;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.oomdp.singleagent.*;
import burlap.oomdp.singleagent.common.SinglePFTF;


public class PlotTest{

	public static void main(String [] args){

		GridWorldDomain gw = new GridWorldDomain(11,11); //11x11 grid world
		gw.setMapToFourRooms(); //four rooms layout
		gw.setProbSucceedTransitionDynamics(0.8); //stochastic transitions with 0.8 success rate
		final Domain domain = gw.generateDomain(); //generate the grid world domain

		//setup initial state
		State s = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(s, 0, 0);
		GridWorldDomain.setLocation(s, 0, 10, 10);

		//ends when the agent reaches a location
		final TerminalFunction tf = new SinglePFTF(domain.
			getPropFunction(GridWorldDomain.PFATLOCATION)); 

		//reward function definition
		final RewardFunction rf = new GoalBasedRF(new TFGoalCondition(tf), 5., -0.1);

		//initial state generator
		final ConstantStateGenerator sg = new ConstantStateGenerator(s);


		//set up the state hashing system for looking up states
		final DiscreteStateHashFactory hashingFactory = new DiscreteStateHashFactory();


		/**
		 * Create factory for Q-learning agent
		 */
		LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
			
			@Override
			public String getAgentName() {
				return "Q-learning";
			}
			
			@Override
			public LearningAgent generateAgent() {
				return new QLearning(domain, rf, tf, 0.99, hashingFactory, 0.3, 0.1);
			}
		};

		//define experiment
		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter((SADomain)domain, 
			rf, sg, 10, 100, qLearningFactory);
		
		exp.setUpPlottingConfiguration(500, 250, 2, 1000, TrialMode.MOSTRECENTANDAVERAGE, 
			PerformanceMetric.CUMULATIVESTEPSPEREPISODE, PerformanceMetric.AVERAGEEPISODEREWARD);
		

		//start experiment
		exp.startExperiment();


	}




}