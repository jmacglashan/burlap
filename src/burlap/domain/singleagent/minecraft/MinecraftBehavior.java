package burlap.domain.singleagent.minecraft;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.PriorityQueue;
import java.util.Queue;

import burlap.oomdp.singleagent.Action;
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
import burlap.domain.singleagent.minecraft.MinecraftDomain.*;



public class MinecraftBehavior {

	MinecraftDomain				mcdg;
	Domain						domain;
	StateParser					sp;
	RewardFunction				rf;
	TerminalFunction			tf;
	StateConditionTest			goalCondition;
	State						initialState;
	DiscreteStateHashFactory	hashingFactory;
	
	PropositionalFunction		pfAgentAtGoal;
	PropositionalFunction		pfIsPlane;
	PropositionalFunction		pfIsAdjTrench;
	PropositionalFunction		pfIsAdjDoor;
	PropositionalFunction		pfIsThrQWay;
	PropositionalFunction		pfIsHalfWay;
	PropositionalFunction		pfIsOneQWay;
	PropositionalFunction		pfIsAtGoal;
	PropositionalFunction		pfIsAtLocation;
	PropositionalFunction		pfIsWalkable;
	PropositionalFunction 		pfIsAdjDstableWall;
	PropositionalFunction 		pfAgentHasBread;
	PropositionalFunction		pfIsAdjOven;
	PropositionalFunction		pfIsOnGrain;
	
	// Timing stuff
//	private static long			timeStart;
//	private static long			timeEnd;
	private static double			timeDelta;
	
	public MinecraftBehavior(String mapfile) {
		mcdg = new MinecraftDomain();
		domain = mcdg.generateDomain();
		
		sp = new MinecraftStateParser(domain); 	

		// === Build Initial State=== //

		MCStateGenerator mcsg = new MCStateGenerator(mapfile);

		initialState = mcsg.getCleanState(domain);

		// Set up the state hashing system
		hashingFactory = new DiscreteStateHashFactory();
		hashingFactory.setAttributesForClass(MinecraftDomain.CLASSAGENT, 
					domain.getObjectClass(MinecraftDomain.CLASSAGENT).attributeList); 
		
		// Create Propfuncs for use
		
		String ax = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(this.mcdg.ATTX));
		String ay = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(this.mcdg.ATTY));
		String az = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(this.mcdg.ATTZ));
		
		pfIsPlane = new IsAdjPlane(this.mcdg.ISPLANE, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsAdjTrench = new IsAdjTrench(this.mcdg.ISADJTRENCH, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsAdjDoor = new IsAdjDoor(this.mcdg.ISADJDOOR, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsAdjOven = new IsAdjOven(this.mcdg.ISADJOVEN, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsOnGrain = new IsOnGrain(this.mcdg.ISONGRAIN, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsAdjDstableWall = new IsAdjDstableWall(this.mcdg.ISADJDWALL, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsThrQWay = new IsNthOfTheWay("IsThrQWay", this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, new String[] {ax, ay, az}, 0.75);
		
		pfIsHalfWay = new IsNthOfTheWay("IsHalfWay", this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, new String[] {ax, ay, az}, 0.5);
		
		pfIsOneQWay = new IsNthOfTheWay("IsOneQWay", this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, new String[] {ax, ay, az}, 0.25);
		
		pfIsAtGoal = new AtGoalPF(this.mcdg.PFATGOAL, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL});
		
		pfIsAtLocation = new IsAtLocationPF(this.mcdg.ISATLOC, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, new String[]{"13", "6", "1"});
		
		pfIsWalkable = new IsWalkablePF(this.mcdg.ISWALK, this.mcdg.DOMAIN,
				new String[]{"Integer", "Integer", "Integer"});

		pfAgentHasBread = new AgentHasBreadPF(this.mcdg.AGENTHASBREAD, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfAgentAtGoal = new AtGoalPF(this.mcdg.PFATGOAL, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL});
		
		// Generate Goal Condition
		rf = new SingleGoalPFRF(pfAgentHasBread, 10, -1); 
		tf = new SinglePFTF(pfAgentHasBread); 
		goalCondition = new TFGoalCondition(tf);
		
//		rf = new SingleGoalPFRF(pfAgentAtGoal, 10, -1); 
//		tf = new SinglePFTF(pfAgentAtGoal); 
//		goalCondition = new TFGoalCondition(tf);
		
		
	}

	// ---------- PLANNERS ---------- 
	
	// Value Iteration (Basic)
	public String ValueIterationPlanner(){

		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 0.01, Integer.MAX_VALUE);
		
		planner.planFromState(initialState);

		// Create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		// Record the plan results to a file
		String actionSequence = p.evaluateBehavior(initialState, rf, tf).getActionSequenceString();
		
		return actionSequence;
	}
	
	// === Subgoal Planner	===
	public String SubgoalPlanner(ArrayList<Subgoal> kb){
		
		// Initialize action plan
		String actionSequence = new String();
		
		// Build subgoal tree
		Node subgoalTree = generateGraph(kb);

		// Run BFS on subgoal tree (i.e. planning in subgoal space) 
		// returns a Node that is closer to the agent than the goal
		Node propfuncChain = BFS(subgoalTree);
		
		// Run VI between each subgoal in the chain
		State currState = initialState;
		boolean timeReachability = true;
		while(propfuncChain != null) {
			//define the task
			System.out.println("Current goal: " + propfuncChain.getPropFunc().toString());
			rf = new SingleGoalPFRF(propfuncChain.getPropFunc(), 10, -1); 
			tf = new SinglePFTF(propfuncChain.getPropFunc()); 
			goalCondition = new TFGoalCondition(tf);
			
			OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 0.01, Integer.MAX_VALUE);

//			planner.planFromState(currState);
			timeDelta += planner.planFromStateAndTime(currState, timeReachability);
			
			if (timeReachability) {
				timeReachability = false;
			}
			
			Policy p = new GreedyQPolicy((QComputablePlanner)planner);
			EpisodeAnalysis ea = p.evaluateBehavior(currState, rf, tf);
			
			// Add low level plan to overall plan and update current state to the end of that subgoal plan
			actionSequence += ea.getActionSequenceString() + "; ";
			currState = ea.getLastState();
			
			propfuncChain = propfuncChain.getParent();
			
		}
		return actionSequence;
		
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
			System.out.println("Post Node: " + post);
			System.out.println("Pre Node: " + pre);
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
		ArrayDeque<Node> nodeQueue = new ArrayDeque<Node>();
		
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
		// NOTE: ALWAYS add a subgoal with the FINAL goal first
		ArrayList<Subgoal> result = new ArrayList<Subgoal>();
		
		// Get agent starting coordinates
//		String ax = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(MinecraftDomain.ATTX));
//		String ay = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(MinecraftDomain.ATTY));
//		String az = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(MinecraftDomain.ATTZ));
		
		// Define desired subgoals here:
		
		// Flatworld subgoals
//		Subgoal sg3 = new Subgoal(this.pfIsHalfWay, this.pfIsAtGoal);
//		Subgoal sg2 = new Subgoal(this.pfIsHalfWay, this.pfIsThrQWay);
//		Subgoal sg1 = new Subgoal(this.pfIsOneQWay, this.pfIsHalfWay);
//		result.add(sg3);
//		result.add(sg2);
//		result.add(sg1);
		
		// Jumpworld subgoals
//		Subgoal pastTrench = new Subgoal(this.pfIsAtLocation, this.pfIsAtGoal);
//		result.add(pastTrench);
//		
		// Breadworld subgoals
//		PropositionalFunction hasGrainPF = new AgentHasGrainPF(MinecraftDomain.ATTAGHASGRAIN, MinecraftDomain.DOMAIN,
//				new String[]{MinecraftDomain.CLASSAGENT});
//		Subgoal hasGrain = new Subgoal(hasGrainPF, pfAgentHasBread);
//		result.add(hasGrain);
		
		// Doorworld subgoals
//		PropositionalFunction doorOpenPF = new IsDoorOpen(MinecraftDomain.ISDOOROPEN, MinecraftDomain.DOMAIN,
//				new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"2", "9", "1"});
//		Subgoal doorOpen = new Subgoal(doorOpenPF, this.pfIsAtGoal);
//		result.add(doorOpen);
		
		// Mazeworld subgoals
//		PropositionalFunction atEntrancePF = new IsAtLocationPF("ATENTRANCE", this.mcdg.DOMAIN,
//		new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, new String[]{"6", "10", "1"});
//
//		PropositionalFunction midwayPF = new IsAtLocationPF("MIDWAY", this.mcdg.DOMAIN,
//		new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, new String[]{"1", "10", "1"});
//
//		PropositionalFunction almostTherePF = new IsAtLocationPF("ALMOST", this.mcdg.DOMAIN,
//		new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, new String[]{"5", "3", "1"});
//
//		Subgoal almostThere = new Subgoal(almostTherePF, this.pfIsAtGoal);
//		Subgoal halfwahThere = new Subgoal(midwayPF, almostTherePF);
//		Subgoal atEntrance = new Subgoal(atEntrancePF, midwayPF);
//		
//		result.add(almostThere);
//		result.add(halfwahThere);
//		result.add(atEntrance);

		// Hardworld subgoals
		PropositionalFunction firstDoorOpenPF = new IsDoorOpen("FIRSTDOOROPEN", MinecraftDomain.DOMAIN,
				new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"10", "14", "1"});
		PropositionalFunction secondDoorOpenPF = new IsDoorOpen("SECONDDOOROPEN", MinecraftDomain.DOMAIN,
				new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"1", "9", "1"});
		
		Subgoal secondDoor = new Subgoal(secondDoorOpenPF, this.pfIsAtGoal);
		Subgoal firstDoor = new Subgoal(firstDoorOpenPF, secondDoorOpenPF);

		result.add(secondDoor);
		result.add(firstDoor);
		
		return result;
	}
	
	
	
	// Affordances Planner
	public String AffordancePlanner(ArrayList<Affordance> kb){
		
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, .1, Integer.MAX_VALUE);
		
//		System.out.println((initialState.getStateDescription()));
		
		planner.planFromStateAffordance(initialState, kb);

		// Create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		String actionSequence = p.evaluateAffordanceBehavior(initialState, rf, tf, kb).getActionSequenceString();
		
		return actionSequence;	
	}
	
	public ArrayList<Affordance> generateAffordanceKB() {
		ArrayList<Affordance> affordances = new ArrayList<Affordance>();
		
		String ax = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(this.mcdg.ATTX));
		String ay = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(this.mcdg.ATTY));
		String az = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(this.mcdg.ATTZ));
		
		ArrayList<Action> isPlaneActions = new ArrayList<Action>();
		isPlaneActions.add(this.mcdg.forward);
		isPlaneActions.add(this.mcdg.backward);
		isPlaneActions.add(this.mcdg.left);
		isPlaneActions.add(this.mcdg.right);
//		
		ArrayList<Action> isTrenchActions = new ArrayList<Action>();
		isTrenchActions.add(this.mcdg.jumpF);
		isTrenchActions.add(this.mcdg.jumpB);
		isTrenchActions.add(this.mcdg.jumpR);
		isTrenchActions.add(this.mcdg.jumpL);
		
//		isTrenchActions.add(this.mcdg.forward);
//		isTrenchActions.add(this.mcdg.backward);
//		isTrenchActions.add(this.mcdg.left);
//		isTrenchActions.add(this.mcdg.right);
//		isTrenchActions.add(this.mcdg.placeF);
		
		ArrayList<Action> isDoorActions = new ArrayList<Action>();
		isDoorActions.add(this.mcdg.forward);
		isDoorActions.add(this.mcdg.backward);
		isDoorActions.add(this.mcdg.left);
		isDoorActions.add(this.mcdg.right);
		isDoorActions.add(this.mcdg.openF);
		isDoorActions.add(this.mcdg.openB);
		isDoorActions.add(this.mcdg.openR);
		isDoorActions.add(this.mcdg.openL);
		
//		ArrayList<Action> isDstableWallActions = new ArrayList<Action>();
//		isDstableWallActions.add(this.mcdg.forward);
//		isDstableWallActions.add(this.mcdg.backward);
//		isDstableWallActions.add(this.mcdg.left);
//		isDstableWallActions.add(this.mcdg.right);
//		isDstableWallActions.add(this.mcdg.destF);
		
		ArrayList<Action> isOnGrainActions = new ArrayList<Action>();
		isOnGrainActions.add(this.mcdg.pickUpGrain);
		
		ArrayList<Action> isAdjOvenActions = new ArrayList<Action>();
		isAdjOvenActions.add(this.mcdg.useOvenF);
		isAdjOvenActions.add(this.mcdg.useOvenB);
		isAdjOvenActions.add(this.mcdg.useOvenR);
		isAdjOvenActions.add(this.mcdg.useOvenL);
		
		Affordance affIsPlane = new Affordance(this.pfIsPlane, this.pfIsAtGoal, isPlaneActions);
		Affordance affIsAdjTrench = new Affordance(this.pfIsAdjTrench, this.pfIsAtGoal, isTrenchActions);
		Affordance affIsAdjDoor = new Affordance(this.pfIsAdjDoor, this.pfIsAtGoal, isDoorActions);
		Affordance affIsAdjOven = new Affordance(this.pfIsAdjOven, this.pfIsAtGoal, isAdjOvenActions);
		Affordance affIsOnGrain = new Affordance(this.pfIsOnGrain, this.pfIsAtGoal, isOnGrainActions);
//		Affordance affIsDstableWall = new Affordance(this.pfIsAdjDstableWall, this.pfIsAtGoal, isDstableWallActions);
		
		affordances.add(affIsPlane);
		affordances.add(affIsAdjDoor);
		affordances.add(affIsAdjTrench);
		affordances.add(affIsAdjOven);
		affordances.add(affIsOnGrain);
//		affordances.add(affIsDstableWall);
		
		
		return affordances;
	}
	
	
	// Options Planner
	
	// Macroactions Planner
	
	
	public static void main(String[] args) {
		
		// Setup Minecraft World
		MinecraftBehavior mcb = new MinecraftBehavior("breadworld.map");
		
		// START TIMER
		long timeStart = System.nanoTime();
		
		// VANILLA OOMDP/VI
//		String actionSequence = mcb.ValueIterationPlanner();
		
		// SUBGOALS
//		ArrayList<Subgoal> kb = mcb.generateSubgoalKB();
//		String actionSequence = mcb.SubgoalPlanner(kb);
		
		
		// AFFORDANCES
		 ArrayList<Affordance> kb = mcb.generateAffordanceKB();
		 String actionSequence = mcb.AffordancePlanner(kb);
		
		// END TIMER
//		timeEnd = System.nanoTime();
		timeDelta = (double) (System.nanoTime()- timeStart) / 1000000000;
		System.out.println("Took "+ timeDelta + " s"); 

		System.out.println(actionSequence);

		
	}
	
	
}