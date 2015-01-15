package burlap.behavior.singleagent.learning.modellearning.rmax;

import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.behavior.singleagent.learning.modellearning.modelplanners.VIModelPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldRewardFunction;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;

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

		GridWorldDomain gwdg = new GridWorldDomain(11, 11);
		gwdg.setMapToFourRooms();
		gwdg.setProbSucceedTransitionDynamics(0.75);//make stochastic

		
		int endX = 10;
		int endY = 10;
		
		Domain d = gwdg.generateDomain();
		State initialState = GridWorldDomain.getOneAgentOneLocationState(d);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, endX, endY, 0);
		
		RewardFunction rf = new WallRF(endX, endY);
//		GridWorldRewardFunction rf = new GridWorldRewardFunction(d);
//		rf.setReward(endX, endY, 1000);
		
		TerminalFunction tf = new GridWorldTerminalFunction(10, 10);
		
		
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
		DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
		double maxReward = 5;
		int nConfident = 3;
		double maxVIDelta = .1;
		int maxVIPasses = 10;
		
		int learningIterations = 1000;
		
		PotentialShapedRMax rmax = new PotentialShapedRMax(d, rf, tf, .9, hf, maxReward, nConfident, maxVIDelta, maxVIPasses);
		
		//run agent for 40 learning episodes
		for(int i = 0; i < learningIterations; i++){
			EpisodeAnalysis ea = rmax.runLearningEpisodeFrom(initialState);
			//average reward is undiscounted cumulative reward divided by number of actions (num time steps -1)
			double avgReward = ea.getDiscountedReturn(1.) / (ea.numTimeSteps() -1);
			System.out.println(avgReward + " average reward for episode " + (i+1));
		}

		//only use the below after updating BURLAP to visualize modeled value function

		//get the model planner. You can use the commented out one below, but if you plan on visualizing the value function
		//at multiple stages, you should make a copy of the value function (using the second line)
		//VIModelPlanner planner = (VIModelPlanner)rmax.getModelPlanner();
		ValueFunctionPlanner planner = ((VIModelPlanner)rmax.getModelPlanner()).getValueIterationPlanner().getCopyOfValueFunction();

		//create a value function visualizer
		List<State> allStates = StateReachability.getReachableStates(initialState, (SADomain)d, new DiscreteStateHashFactory());
		ValueFunctionVisualizerGUI gui = GridWorldDomain.getGridWorldValueFunctionVisualization(allStates, planner, new GreedyQPolicy(planner));
		gui.initGUI();
	}
	
	//TEST
	//Test2
	

}
