package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.PriorityQueue;
import java.util.Queue;

import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SingleGoalPFRF;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.visualizer.Visualizer;
import burlap.oomdp.core.State;
import burlap.behavior.singleagent.EpisodeAnalysis;
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
import burlap.domain.singleagent.minecraft.MinecraftDomain.AtGoalPF;
import burlap.domain.singleagent.minecraft.MinecraftDomain.IsAtLocationPF;
import burlap.domain.singleagent.minecraft.MinecraftDomain.IsWalkablePF;



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
		System.out.println("BEFOREHOWDY");
		initialState = mcsg.getCleanState(domain);
		System.out.println("AFTERHOWDY");
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

	// ---------- PLANNERS ---------- 
	
	// Value Iteration (Basic)
	public void ValueIterationPlanner(String outputPath){
		
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 1, Integer.MAX_VALUE);
		
		planner.planFromState(initialState);

		// Create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		// Record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
	}
	
	// Subgoal Planner	
	public String SubgoalPlanner(ArrayList<Subgoal> kb){
		
		// Initialize action plan
		String actions = new String();
		
		// Build subgoal tree
		Node subgoalTree = generateGraph(kb);

		// Run BFS on subgoal tree (i.e. planning in subgoal space) 
		// returns a Node that is closer to the agent than the goal
		Node propfuncChain = BFS(subgoalTree);
		
		// Run VI between each subgoal in the chain
		State currState = initialState;
		while(propfuncChain.getParent() != null) {
			//define the task
			rf = new SingleGoalPFRF(propfuncChain.getPropFunc(), 10, -1); 
			tf = new SinglePFTF(propfuncChain.getPropFunc()); 
			goalCondition = new TFGoalCondition(tf);
			
			OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 1, Integer.MAX_VALUE);

			planner.planFromState(currState);
			Policy p = new GreedyQPolicy((QComputablePlanner)planner);
			EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf);
			
			// Add low level plan to overall plan and update current state to the end of that subgoal plan
			actions += ea.getActionSequenceString();
			currState = ea.getState(-1);
			
		}
		return actions;
		
	}
	
	private Node generateGraph(ArrayList<Subgoal> kb) {
		HashMap<PropositionalFunction,Node> nodes = new HashMap<PropositionalFunction,Node>();
		
		// Initialize Root of tree (based on final goal)
		Node root = new Node(kb.get(0).getPost(), null);
		nodes.put(kb.get(0).getPost(), root);
		
		// Create a node for each propositional function
		for (int i = 0; i < kb.size(); i++) {
			PropositionalFunction pre = kb.get(i).getPre();
			PropositionalFunction post = kb.get(i).getPost();
			
			Node postNode = new Node(post, null);
			Node preNode = new Node(pre, null);
			
			if (!nodes.containsKey(post)) {
				nodes.put(post, postNode);
			}
			
			if (!nodes.containsKey(preNode)) {
				nodes.put(pre, preNode);
			}
		}
		
		// Add edges between the nodes to form a tree of PropFuncs
		for (int i = 0; i < kb.size(); i++) {
			Subgoal edge = kb.get(i);
			
			PropositionalFunction edgeStart = edge.getPre();
			PropositionalFunction edgeEnd = edge.getPost();
			
			Node startNode = nodes.get(edgeStart);
			Node endNode = nodes.get(edgeEnd);
			
			if (startNode != null) {
				startNode.setParent(endNode);				
				endNode.addChild(startNode);
			}
						
		}
		
		return root;
	}
	
	private Node BFS(Node root) {
		PriorityQueue<Node> nodeQueue = new PriorityQueue<Node>();
		
		nodeQueue.add(root);
		Node curr = null;
		while (!nodeQueue.isEmpty()) {
			curr = nodeQueue.poll();
			if (curr.getPropFunc().isTrue(this.initialState)) {
				return curr;
			}
			if (curr.getChildren() != null) {
				nodeQueue.addAll(curr.getChildren());
			}
		}
		
		return curr;
	}
	
	private ArrayList<Subgoal> generateSubgoalKB() {
		ArrayList<Subgoal> result = new ArrayList<Subgoal>();
		
		// Define desired propositional functions here:
		PropositionalFunction atGoal = new AtGoalPF(this.mcdg.PFATGOAL, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL});
		PropositionalFunction IsAtLocation = new IsAtLocationPF(this.mcdg.ISATLOC, this.mcdg.DOMAIN,
				new String[]{"Integer", "Integer", "Integer"}, new String[]{"5", "5", "1"});
		PropositionalFunction isWalkable = new IsWalkablePF(this.mcdg.ISWALK, this.mcdg.DOMAIN,
				new String[]{"Integer", "Integer", "Integer"});

		// Define desired subgoals here:
		
		// ALWAYS add a subgoal with the FINAL goal first
		Subgoal simpleSubgoal = new Subgoal(IsAtLocation, atGoal);
		result.add(simpleSubgoal);
		
		return result;
	}
	
	
	
	// Affordances Planner
	
	// Options Planner
	
	// Macroactions Planner
	
	
	public static void main(String[] args) {
		
		// Setup Minecraft World
		MinecraftBehavior mcb = new MinecraftBehavior("flatland.map");

		// Define Knowledge Base
		ArrayList<Subgoal> kb = mcb.generateSubgoalKB();
		
		// Call Planning Algorithm
		String actions = mcb.SubgoalPlanner(kb);
		System.out.println(actions);
//		mcb.ValueIterationPlanner("output/");
		
	}
	
	
}