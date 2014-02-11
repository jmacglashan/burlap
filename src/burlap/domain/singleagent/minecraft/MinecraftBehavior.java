package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.core.Domain;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SingleGoalPFRF;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.visualizer.Visualizer;
import burlap.oomdp.core.State;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.*;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.TerminalFunction;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.statehashing.DiscreteStateHashFactory;



public class MinecraftBehavior {

	MinecraftDomain				mcdg;
	Domain						domain;
	StateParser					sp;
	RewardFunction				rf;
	TerminalFunction			tf;
	StateConditionTest			goalCondition;
	State						initialState;
	DiscreteStateHashFactory	hashingFactory;
	
	
	public MinecraftBehavior(String mapfile) {
		mcdg = new MinecraftDomain();
		domain = mcdg.generateDomain();
		
		sp = new MinecraftStateParser(domain); 	
		
		//define the task
		rf = new SingleGoalPFRF(domain.getPropFunction(MinecraftDomain.PFATGOAL), 10, -1); 
		tf = new SinglePFTF(domain.getPropFunction(MinecraftDomain.PFATGOAL)); 
		goalCondition = new TFGoalCondition(tf);
		
		// === Build Initial State=== //

		MCStateGenerator mcsg = new MCStateGenerator(mapfile);
		
		State initialState = mcsg.getCleanState(domain);
		
		// -- Initialize Goal Stack --
		// TODO: Is this necessary or should it be only in affordance planner?
		ObjectInstance goalObj = initialState.getObject(MinecraftDomain.CLASSGOAL + "0");
		
		//get the goal coordinates
		int gx = goalObj.getDiscValForAttribute(MinecraftDomain.ATTX);
		int gy = goalObj.getDiscValForAttribute(MinecraftDomain.ATTY);
		int gz = goalObj.getDiscValForAttribute(MinecraftDomain.ATTZ);
		
		String[] goalCoords = {"" + gx,"" + gy,"" + gz};
		
		// Set the initial subGoal params of reaching the goal
		domain.goalStack.peek().setParams(goalCoords);
		
		
		// Set up the state hashing system
		hashingFactory = new DiscreteStateHashFactory();
		hashingFactory.setAttributesForClass(MinecraftDomain.CLASSAGENT, 
					domain.getObjectClass(MinecraftDomain.CLASSAGENT).attributeList); 
		
	}
	
	
	// Older working version if basic bad VI
	public void ValueIterationMC(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 1, Integer.MAX_VALUE);
		
		planner.planFromState(initialState);
//		System.out.println(((ValueFunctionPlanner) planner).value(initialState));
		//create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		//int maxIterations = 150;
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
	}
	
	// Affordances Planner
	
	// Subgoal Planner
	
	// Options Planner
	
	// Macroactions Planner
	
	
	public static void main(String[] args) {
	
		MinecraftBehavior mcb = new MinecraftBehavior("test.txt");
		
		String outputPath = "output/"; //directory to record results
		
		// Call planning and learning algorithms here
		
		//mcb.ValueIterationMC(outputPath);
		
		
		

	}
	
	
}