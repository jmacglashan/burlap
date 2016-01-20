package burlap.domain.singleagent.minecraft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.PriorityQueue;
import java.util.Queue;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.MultiplePFTF;
import burlap.oomdp.singleagent.common.SingleGoalMultiplePFRF;
import burlap.oomdp.singleagent.common.SingleGoalPFRF;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.visualizer.Visualizer;
import burlap.oomdp.core.State;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.PolicyDefinedSubgoalOption;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.*;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.TerminalFunction;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.domain.singleagent.minecraft.MinecraftDomain.*;



public class MinecraftBehavior {

	boolean						debug = true;
	
	MinecraftDomain				mcdg;
	Domain						domain;
	StateParser					sp;
	RewardFunction				rf;
	static TerminalFunction		tf;
	static StateConditionTest	goalCondition;
	State						initialState;
	DiscreteStateHashFactory	hashingFactory;
	
	static PropositionalFunction		pfAgentAtGoal;
	static PropositionalFunction 		pfAgentHasGoldBlock;
	static PropositionalFunction 		pfAgentNotInWorld;

	PropositionalFunction		pfIsPlane;
	PropositionalFunction		pfIsAdjTrench;
	PropositionalFunction		pfIsAdjDoor;
	PropositionalFunction		pfIsThrQWay;
	PropositionalFunction		pfIsHalfWay;
	PropositionalFunction		pfIsOneQWay;
	PropositionalFunction		pfIsAgentYAt;
	PropositionalFunction		pfIsAtGoal;
	PropositionalFunction		pfIsAtLocation;
	PropositionalFunction		pfIsWalkable;
	PropositionalFunction 		pfIsAdjDstableWall;
	PropositionalFunction		pfIsAdjFurnace;
	PropositionalFunction		pfIsOnGoldOre;
	PropositionalFunction  		pfIsInLava;
	PropositionalFunction		pfIsTrenchF;
	PropositionalFunction		pfIsTrenchB;
	PropositionalFunction		pfIsTrenchL;
	PropositionalFunction		pfIsTrenchR;
	PropositionalFunction		pfIsPlaneF;
	PropositionalFunction		pfIsPlaneB;
	PropositionalFunction		pfIsPlaneL;
	PropositionalFunction		pfIsPlaneR;
	PropositionalFunction		pfIsDstableWallF;
	PropositionalFunction		pfIsDstableWallB;
	PropositionalFunction		pfIsDstableWallL;
	PropositionalFunction		pfIsDstableWallR;

	HashMap<PropositionalFunction, Double> rewardTable;

	// Params for Planners
	static int 					numRollouts = 5000; // RTDP
	static int 					maxDepth = 50; // RTDP
	static double				vInit = 0;
	static double				goalReward = -1.0;
	static int					maxSteps = 100; // cutoff for VI
	static double				maxDelta = 0.01;
	static double				gamma = 0.99;
	
	
	public MinecraftBehavior(String mapfile) {
		// WOOT HARD CODED
		mcdg = new MinecraftDomain();
		domain = this.mcdg.generateDomain(true, false);
		
		if (mapfile.length() > 0) {
			updateMap(mapfile);			
		}

		// Set up the state hashing system
		hashingFactory = new DiscreteStateHashFactory();
		hashingFactory.setAttributesForClass(MinecraftDomain.CLASSAGENT, 
					domain.getObjectClass(MinecraftDomain.CLASSAGENT).attributeList); 
		
		// Create Propfuncs for use
		
		pfIsPlane = new IsAdjPlane(this.mcdg.ISPLANE, domain,
				new String[]{this.mcdg.CLASSAGENT});
		
		// ===== TRENCH =====
		
		pfIsAdjTrench = new IsAdjTrench(this.mcdg.ISADJTRENCH, domain,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsTrenchF = new IsAdjTrench(this.mcdg.ISADJTRENCH + "F", domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {0,-1,0}, 1);
		
		pfIsTrenchB = new IsAdjTrench(this.mcdg.ISADJTRENCH + "B", domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {0,1,0}, 1);
		
		pfIsTrenchR = new IsAdjTrench(this.mcdg.ISADJTRENCH + "R", domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {1,0,0}, 1);
		
		pfIsTrenchL = new IsAdjTrench(this.mcdg.ISADJTRENCH + "L", domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {-1,0,0}, 1);
		
		// ===== DSTABLE WALL =====
		pfIsDstableWallF = new IsAdjDstableWall(this.mcdg.ISADJTRENCH, domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {0,-1,0});
		
		pfIsDstableWallB = new IsAdjDstableWall(this.mcdg.ISADJTRENCH, domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {0,1,0});
		
		pfIsDstableWallR = new IsAdjDstableWall(this.mcdg.ISADJTRENCH, domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {-1,0,0});
		
		pfIsDstableWallL = new IsAdjDstableWall(this.mcdg.ISADJTRENCH, domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {1,0,0});
		
		
		// ===== PLANE =====
		
		pfIsPlaneF = new IsAdjPlane(this.mcdg.ISPLANE + "F", domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {0,-1,0});
		
		pfIsPlaneB = new IsAdjPlane(this.mcdg.ISPLANE + "B", domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {0,1,0});
		
		pfIsPlaneL = new IsAdjPlane(this.mcdg.ISPLANE + "L", domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {-1,0,0});
		
		pfIsPlaneR = new IsAdjPlane(this.mcdg.ISPLANE + "R", domain,
				new String[]{this.mcdg.CLASSAGENT}, new int[] {1,0,0});
		
		pfIsAdjDoor = new IsAdjDoor(this.mcdg.ISADJDOOR, domain,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsAdjFurnace = new IsAdjFurnace(this.mcdg.ISADJFURNACE, domain,
				new String[]{this.mcdg.CLASSAGENT});
		
		// ===== MISC =====
		
		pfIsOnGoldOre = new IsOnGoldOre(this.mcdg.ISONGOLDORE, domain,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsInLava = new IsInLava(this.mcdg.ISINLAVA, domain,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsAdjDstableWall = new IsAdjDstableWall(this.mcdg.ISADJDWALL, domain,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfIsAtGoal = new AtGoalPF(this.mcdg.PFATGOAL, domain,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL});
		
		pfIsAgentYAt = new IsAgentYAt("IsAgentOnBridge", domain,
				new String[]{this.mcdg.CLASSAGENT}, (this.mcdg.MAXY + 1) / 2 - 1, 0);
		
		
		pfIsAtLocation = new IsAtLocationPF(this.mcdg.ISATLOC, domain,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, 13, 6, 1);
		
		pfIsWalkable = new IsWalkablePF(this.mcdg.ISWALK, domain,
				new String[]{"Integer", "Integer", "Integer"});

		pfAgentHasGoldBlock = new AgentHasGoldBlockPF(this.mcdg.AGENTHASGOLDBLOCK, domain,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfAgentAtGoal = new AtGoalPF(this.mcdg.PFATGOAL, domain,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL});
		
		pfAgentNotInWorld = new NotInWorldPF(this.mcdg.PFNINWORLD, domain,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL});
		
		// Generate Goal Condition
//		rf = new SingleGoalPFRF(pfAgentHasBread, 10, -1); 
//		tf = new SinglePFTF(pfAgentHasBread); 
//		goalCondition = new TFGoalCondition(tf);
		
		rewardTable = new HashMap<PropositionalFunction, Double>();
		rewardTable.put(pfAgentAtGoal, (Double) goalReward);
		rewardTable.put(pfAgentNotInWorld, (Double) 1.0 - 99999999.9);
		rewardTable.put(pfAgentHasGoldBlock, (Double) goalReward);
		Double lavaRew = -100.0;
		rewardTable.put(pfIsInLava, lavaRew);
		
		rf = new SingleGoalMultiplePFRF(rewardTable, -1);
		
//		tf = new SinglePFTF(pfAgentAtGoal);
//		goalCondition = new TFGoalCondition(tf);
		
		
	}
	
	public void updateMap(String mapfile) {
		tf = new MultiplePFTF(new PropositionalFunction[]{pfAgentAtGoal, pfAgentNotInWorld}, pfAgentAtGoal);
		goalCondition = new TFGoalCondition(tf);

		MCStateGenerator mcsg = new MCStateGenerator(mapfile);

		sp = new MinecraftStateParser(domain);

		// === Build Initial State=== //
		initialState = mcsg.getCleanState(domain);
		this.mcdg.MAXX = mcsg.getMaxX();
		this.mcdg.MAXY = mcsg.getMaxY();
	}

	// ---------- PLANNERS ---------- 
	
	public Policy solve(OOMDPPlanner planner) {
		int statePasses = planner.planFromState(initialState, this.mcdg);

		// Create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		// Print out some infos
		EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf, maxSteps);
		
		System.out.println(ea.getActionSequenceString());
		double totalReward = sumReward(ea.rewardSequence);
		
		State finalState = ea.getLastState();
		
		double completed = goalCondition.satisfies(finalState) ? 1.0 : 0.0;
		System.out.println("Passes: " + statePasses);

		return p;
	}
			
	
	// === VI Planner	===
	public double[] ValueIterationPlanner(){
		
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, gamma, hashingFactory, 0.01, Integer.MAX_VALUE);
		
		int statePasses = planner.planFromState(initialState, this.mcdg);

		// Create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		// Record the plan results to a file
		EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf, maxSteps);
		
		System.out.println(ea.getActionSequenceString());
		double totalReward = sumReward(ea.rewardSequence);
		
		State finalState = ea.getLastState();
		
		double completed = goalCondition.satisfies(finalState) ? 1.0 : 0.0;
		
		return new double[]{statePasses, totalReward, completed};	
	}
	
	// === RTDP Planner	===
	public double[] RTDPPlanner(int numRollouts, int maxDepth){

		RTDP planner = new RTDP(domain, rf, tf, 0.99, hashingFactory, vInit, numRollouts, maxDelta, maxDepth);
		
		int statePasses = planner.planFromStateAndCount(initialState);

		// Create a Q-greedy policy from the planner
		 Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		// Record the plan results to a file
		EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf, maxSteps);
		
		System.out.println(ea.getActionSequenceString());

		double totalReward = sumReward(ea.rewardSequence);
		
		State finalState = ea.getLastState();
		
		double completed = goalCondition.satisfies(finalState) ? 1.0 : 0.0;
		
		return new double[]{statePasses, totalReward, completed};	
	}
	
	// === Subgoal Planner	===
	public double[] SubgoalPlanner(ArrayList<Subgoal> subgoals, int numRollouts, int maxDepth){
		
		// Initialize action plan
		String actionSequence = new String();
		
		// Build subgoal tree
		Node subgoalTree = generateGraph(subgoals);

		// Run BFS on subgoal tree (i.e. planning in subgoal space) 
		// returns a Node that is closer to the agent than the goal
		Node propfuncChain = BFS(subgoalTree);
		
		// Run VI between each subgoal in the chain
		State currState = initialState;
		int statePasses = 0;
		int totalReward = 0;
		rewardTable.remove(pfIsAtGoal);
		while(propfuncChain != null) {
			System.out.println("Current goal: " + propfuncChain.getPropFunc().toString());
			rewardTable.put(propfuncChain.getPropFunc(), (Double) goalReward);
			rf = new SingleGoalMultiplePFRF(rewardTable, -1);
			tf = new SinglePFTF(propfuncChain.getPropFunc()); 
			goalCondition = new TFGoalCondition(tf);
			
//			OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 0.01, Integer.MAX_VALUE);
//			statePasses += planner.planFromState(currState, this.mcdg);
			
			RTDP planner = new RTDP(domain, rf, tf, 0.99, hashingFactory, vInit, numRollouts, maxDelta, maxDepth);
			statePasses += planner.planFromStateAndCount(currState);

			Policy p = new GreedyQPolicy((QComputablePlanner)planner);

			EpisodeAnalysis ea = p.evaluateBehavior(currState, rf, tf, maxSteps);
			
			// Add low level plan to overall plan and update current state to the end of that subgoal plan
//			actionSequence += ea.getActionSequenceString() + "; ";
			currState = ea.getLastState();

			totalReward += sumReward(ea.rewardSequence);
			
			
			rewardTable.remove(propfuncChain.getPropFunc());
			propfuncChain = propfuncChain.getParent();
			
		}
		rewardTable.put(pfIsAtGoal, (Double) goalReward);
		
		// Record the plan results to a file

		double completed = goalCondition.satisfies(currState) ? 1.0 : 0.0;
		
		return new double[]{statePasses, totalReward, completed};		

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
	
	private ArrayList<Subgoal> generateSubgoalKB(String worldName) {
		// NOTE: ALWAYS add a subgoal with the FINAL goal first
		ArrayList<Subgoal> result = new ArrayList<Subgoal>();
		
		// Get agent starting coordinates
//		String ax = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(MinecraftDomain.ATTX));
//		String ay = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(MinecraftDomain.ATTY));
//		String az = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(MinecraftDomain.ATTZ));
		
		// Define desired subgoals here:
		
		// Flatworld subgoals
		if (worldName.equals("lavaworld.map") || worldName.equals("10world.map") || worldName.equals("15world.map") || worldName.equals("20world.map")) {
		Subgoal sg3 = new Subgoal(this.pfIsHalfWay, this.pfIsAtGoal);
//		Subgoal sg2 = new Subgoal(this.pfIsHalfWay, this.pfIsThrQWay);
//		Subgoal sg1 = new Subgoal(this.pfIsOneQWay, this.pfIsHalfWay);
		result.add(sg3);
//		result.add(sg2);
//		result.add(sg1);
		}
		
		if (worldName.equals("jumpworld.map")) {
			Subgoal sg = new Subgoal(this.pfIsOneQWay, this.pfIsAtGoal);
			result.add(sg);
		}
		
		if (worldName.contains("bridgeworld") && !worldName.contains("door")) {
			PropositionalFunction pfIsAgentBeforeBridge= new IsAtLocationPF("IsBeforeBridge", domain,
					new String[]{this.mcdg.CLASSAGENT}, (int)Math.floor((this.mcdg.MAXX) / 2), (int)Math.floor((this.mcdg.MAXX) / 2) + 1, 1, 1);
			PropositionalFunction pfIsAgentOnBridge= new IsAtLocationPF("IsOnBridge", domain,
					new String[]{this.mcdg.CLASSAGENT}, (int)Math.floor((this.mcdg.MAXX) / 2), (int)Math.floor((this.mcdg.MAXX) / 2), 1, 0);
			
			Subgoal bridge_sg = new Subgoal(pfIsAgentOnBridge, this.pfIsAtGoal);
			Subgoal before_bridge = new Subgoal(pfIsAgentBeforeBridge, pfIsAgentOnBridge);
			result.add(bridge_sg);
			result.add(before_bridge);
		}
		
		if (worldName.equals("doorworld.map")) {
			PropositionalFunction doorOpenPF = new IsDoorOpen("IsDoorOpen", domain,
					new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"2", "9", "1"});
			PropositionalFunction agentBeyondDoor = new IsAtLocationPF("IsBeyondDoor", domain,
					new String[]{this.mcdg.CLASSAGENT}, 2, 8, 1);
			
			Subgoal beyondDoor = new Subgoal(agentBeyondDoor, this.pfIsAtGoal);
			Subgoal doorOpen = new Subgoal(doorOpenPF, agentBeyondDoor);
			
			result.add(beyondDoor);
			result.add(doorOpen);
		}
	
		// Goldworld subgoals
		if (worldName.contains("gold")) {
			PropositionalFunction hasGoldOrePF = new AgentHasGoldOrePF(MinecraftDomain.ATTAGHASGOLDORE, domain,
					new String[]{MinecraftDomain.CLASSAGENT});
			Subgoal hasGoldOre = new Subgoal(hasGoldOrePF, pfAgentHasGoldBlock);
			result.add(hasGoldOre);
		}
		
		if (worldName.contains("tunnel")) {
			PropositionalFunction agentInTunnel= new IsAtLocationPF("IsInTunnel", domain,
					new String[]{this.mcdg.CLASSAGENT}, 1, 1, 1);
			Subgoal inTunnel = new Subgoal(agentInTunnel, this.pfIsAtGoal);
			result.add(inTunnel);
		}
		
		// Doorworld subgoals
		if (worldName.equals("doorbridgeworld.map")) {
			PropositionalFunction doorOpenPF2 = new IsDoorOpen("IsDoorOpen2", domain,
					new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"0", "4", "1"});
			PropositionalFunction pfIsAgentBeforeBridge= new IsAtLocationPF("IsBeforeBridge", domain,
					new String[]{this.mcdg.CLASSAGENT}, 5, 2, 1, 1);
			PropositionalFunction pfIsAgentOnBridge= new IsAtLocationPF("IsOnBridge", domain,
					new String[]{this.mcdg.CLASSAGENT}, 5, 1, 1, 0);
			PropositionalFunction doorOpenPF1 = new IsDoorOpen("IsDoorOpen1", domain,
					new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"2", "0", "1"});
			
			Subgoal doorOpen2 = new Subgoal(doorOpenPF2, this.pfIsAtGoal);
			Subgoal beforeBridge = new Subgoal(pfIsAgentOnBridge, doorOpenPF2);
			Subgoal onBridge = new Subgoal(pfIsAgentBeforeBridge, pfIsAgentOnBridge);
			Subgoal doorOpen1 = new Subgoal(doorOpenPF1, pfIsAgentBeforeBridge);
			
			result.add(doorOpen2);
			result.add(beforeBridge);
			result.add(onBridge);
//			result.add(doorOpen1);
		}
		// Mazeworld subgoals
		if (worldName.equals("mazeworld.map")) {
			PropositionalFunction atEntrancePF = new IsAtLocationPF("ATENTRANCE", domain,
			new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, 6, 10, 1);
	
			PropositionalFunction midwayPF = new IsAtLocationPF("MIDWAY", domain,
			new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL},1, 10, 1);
	
			PropositionalFunction almostTherePF = new IsAtLocationPF("ALMOST", domain,
			new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, 5, 3, 1);
	
			Subgoal almostThere = new Subgoal(almostTherePF, this.pfIsAtGoal);
			Subgoal halfwahThere = new Subgoal(midwayPF, almostTherePF);
			Subgoal atEntrance = new Subgoal(atEntrancePF, midwayPF);
			
			result.add(almostThere);
			result.add(halfwahThere);
			result.add(atEntrance);
		}
		
		// Hardworld subgoals
		if (worldName.equals("hardworld.map")) {
			PropositionalFunction firstDoorOpenPF = new IsDoorOpen("FIRSTDOOROPEN", domain,
					new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"10", "14", "1"});
			PropositionalFunction secondDoorOpenPF = new IsDoorOpen("SECONDDOOROPEN", domain,
					new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"1", "9", "1"});
			
			Subgoal secondDoor = new Subgoal(secondDoorOpenPF, this.pfIsAtGoal);
			Subgoal firstDoor = new Subgoal(firstDoorOpenPF, secondDoorOpenPF);
	
			result.add(secondDoor);
			result.add(firstDoor);
		}
		
		if (worldName.equals("epicworld.map")) {
			
			PropositionalFunction agentXMore = new IsAgentXMore("IsAgentXMore", domain,
					new String[]{MinecraftDomain.CLASSAGENT}, 5);

			PropositionalFunction agentYMore = new IsAgentYMore("IsAgentYMore", domain,
					new String[]{MinecraftDomain.CLASSAGENT}, 8);
			
			PropositionalFunction hasGoldOrePF = new AgentHasGoldOrePF(MinecraftDomain.ATTAGHASGOLDORE, domain,
					new String[]{MinecraftDomain.CLASSAGENT});
			
			Subgoal atWall = new Subgoal(agentXMore, agentYMore);
			Subgoal inRoom = new Subgoal(agentYMore, hasGoldOrePF);
			Subgoal hasGoldOre = new Subgoal(hasGoldOrePF, pfAgentHasGoldBlock);
			
			result.add(hasGoldOre);
			result.add(atWall);
			result.add(inRoom);
		}
		
		return result;
	}
	
	// ====== AFFORDANCE VERSIONS ======
	
	// === Affordance RTDP Planner	===
	public double[] AffordanceRTDPPlanner(int numRollouts, int maxDepth, KnowledgeBase<Affordance> kb){
		
		RTDP planner = new RTDP(domain, rf, tf, 0.99, hashingFactory, vInit, numRollouts, maxDelta, maxDepth);
		
		// Done for NEML results. Quick fix.
		
		List<Affordance> realKB;
		if (kb == null) {
			realKB = generateAffordanceKB("bridge");
		}
		else {
			realKB = kb.getAll();
		}
		
		int statePasses = planner.planFromStateAffordance(initialState, (ArrayList<Affordance>)realKB);

		// Create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy(planner);
		
		System.out.println("Finished Planning");
		

		
		EpisodeAnalysis ea = p.evaluateAffordanceBehavior(initialState, rf, tf, (ArrayList<Affordance>)realKB, maxSteps);

		double totalReward = sumReward(ea.rewardSequence);
		
		State finalState = ea.getLastState();
		
		double completed = goalCondition.satisfies(finalState) ? 1.0 : 0.0;
		
		return new double[]{statePasses, totalReward, completed};	
	}
	
	// === Affordance VI Planner	===
	public double[] AffordanceVIPlanner(KnowledgeBase<Affordance> kb){
		
		ValueIteration planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, .1, Integer.MAX_VALUE);
		
//		System.out.println((initialState.getStateDescription()));
		
		double statePasses = planner.planFromStateAffordance(initialState, (ArrayList<Affordance>)kb.getAll(), this.mcdg);
		
		// Create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		EpisodeAnalysis ea = p.evaluateAffordanceBehavior(initialState, rf, tf, (ArrayList<Affordance>)kb.getAll(), maxSteps);
//		EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf, maxSteps);

		double totalReward = sumReward(ea.rewardSequence);
		
		State finalState = ea.getLastState();
		
		if (debug) {
			System.out.println(ea.getActionSequenceString());
		}
		
		double completed = goalCondition.satisfies(finalState) ? 1.0 : 0.0;
		
		return new double[]{statePasses, totalReward, completed};	
	}
	
	private double sumReward(List<Double> rewardSeq) {
		double total = 0;
		for (double d : rewardSeq) {
			total += d;
		}
		return total;
	}
	
	public ArrayList<Affordance> generateAffordanceKB(String worldName) {
		ArrayList<Affordance> affordances = new ArrayList<Affordance>();
		
		String ax = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(this.mcdg.ATTX));
		String ay = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(this.mcdg.ATTY));
		String az = Integer.toString(this.initialState.getObject("agent0").getDiscValForAttribute(this.mcdg.ATTZ));
		
		// ----- DEFINE ACTION SETS -----
		

		ArrayList<Action> isPlaneFActions = new ArrayList<Action>();
		isPlaneFActions.add(this.mcdg.forward);
		
		
//		Option myFirstOption = new PolicyDefinedSubgoalOption("Sprint Forward", new StubbornPolicy(this.mcdg.forward), new OptionConditionTest(this.pfIsPlaneF, true), this.domain);
//		myFirstOption.keepTrackOfRewardWith(rf, gamma);
//		isPlaneFActions.add(myFirstOption);
//		
//		PropositionalFunction nBridgePF = new IsAdjTrench(this.mcdg.ISADJTRENCH, domain,
//				new String[]{this.mcdg.CLASSAGENT}, new int[] {0,-1,0}, 2);
//		
//		Option bridgeBuilder = new PolicyDefinedSubgoalOption("Bridge Builder", new BridgeBuilderPolicy(this.mcdg.forward, this.mcdg.placeF, this.pfIsPlaneF, this.pfIsTrenchF), new OptionConditionTest(nBridgePF, true), this.domain);
//		bridgeBuilder.keepTrackOfRewardWith(rf, gamma);

		ArrayList<Action> isPlaneBActions = new ArrayList<Action>();
		isPlaneBActions.add(this.mcdg.backward);
		
		ArrayList<Action> isPlaneRActions = new ArrayList<Action>();
		isPlaneRActions.add(this.mcdg.right);
		
		ArrayList<Action> isPlaneLActions = new ArrayList<Action>();
		isPlaneLActions.add(this.mcdg.left);
		
		
		ArrayList<Action> isTrenchFActions = new ArrayList<Action>();
		ArrayList<Action> isTrenchBActions = new ArrayList<Action>();
		ArrayList<Action> isTrenchLActions = new ArrayList<Action>();
		ArrayList<Action> isTrenchRActions = new ArrayList<Action>();
		
		if (worldName.contains("bridge") || worldName.contains("epic")) {
			// Add option
			isTrenchFActions.add(this.mcdg.placeF);
			isTrenchBActions.add(this.mcdg.placeB);
			isTrenchLActions.add(this.mcdg.placeL);
			isTrenchRActions.add(this.mcdg.placeR);

//			isTrenchFActions.add(this.mcdg.jumpF);
//			isTrenchFActions.add(bridgeBuilder); // bridgeBuilder Option
//			isTrenchBActions.add(this.mcdg.jumpB);
//			isTrenchLActions.add(this.mcdg.jumpL);
//			isTrenchRActions.add(this.mcdg.jumpR);
		}
		
			
//		ArrayList<Action> isDoorActions = new ArrayList<Action>();
//		isDoorActions.add(this.mcdg.forward);
//		isDoorActions.add(this.mcdg.backward);
//		isDoorActions.add(this.mcdg.left);
//		isDoorActions.add(this.mcdg.right);
//		isDoorActions.add(this.mcdg.openF);
//		isDoorActions.add(this.mcdg.openB);
//		isDoorActions.add(this.mcdg.openR);
//		isDoorActions.add(this.mcdg.openL);
//		
//		ArrayList<Action> isDstableWallFActions = new ArrayList<Action>();
//		isDstableWallFActions.add(this.mcdg.destF);
//		ArrayList<Action> isDstableWallBActions = new ArrayList<Action>();
//		isDstableWallBActions.add(this.mcdg.destB);
//		ArrayList<Action> isDstableWallLActions = new ArrayList<Action>();
//		isDstableWallLActions.add(this.mcdg.destL);
//		ArrayList<Action> isDstableWallRActions = new ArrayList<Action>();
//		isDstableWallRActions.add(this.mcdg.destR);
//		
//		ArrayList<Action> isOnGoldOreActions = new ArrayList<Action>();
//		isOnGoldOreActions.add(this.mcdg.pickUpGoldOre);
//		
//		ArrayList<Action> isAdjFurnaceActions = new ArrayList<Action>();
//		isAdjFurnaceActions.add(this.mcdg.placeGoldOre);
		
		
		// ----- DEFINE AFFORDANCES -----

		Affordance affIsPlaneF = new Affordance(this.pfIsPlaneF, this.pfIsAtGoal, isPlaneFActions);
		affIsPlaneF.setHardFlag(true);
		Affordance affIsPlaneB = new Affordance(this.pfIsPlaneB, this.pfIsAtGoal, isPlaneBActions);
		affIsPlaneB.setHardFlag(true);
		Affordance affIsPlaneR = new Affordance(this.pfIsPlaneR, this.pfIsAtGoal, isPlaneRActions);
		affIsPlaneR.setHardFlag(true);
		Affordance affIsPlaneL = new Affordance(this.pfIsPlaneL, this.pfIsAtGoal, isPlaneLActions);
		affIsPlaneL.setHardFlag(true);
		
		Affordance affIsAdjTrenchF = new Affordance(this.pfIsTrenchF, this.pfIsAtGoal, isTrenchFActions);
		affIsAdjTrenchF.setHardFlag(true);
		Affordance affIsAdjTrenchB = new Affordance(this.pfIsTrenchB, this.pfIsAtGoal, isTrenchBActions);
		affIsAdjTrenchB.setHardFlag(true);
		Affordance affIsAdjTrenchR = new Affordance(this.pfIsTrenchR, this.pfIsAtGoal, isTrenchRActions);
		affIsAdjTrenchR.setHardFlag(true);
		Affordance affIsAdjTrenchL = new Affordance(this.pfIsTrenchL, this.pfIsAtGoal, isTrenchLActions);
		affIsAdjTrenchL.setHardFlag(true);
		
//		Affordance affIsDstableWallF = new Affordance(this.pfIsDstableWallF, this.pfIsAtGoal, isDstableWallFActions);
//		Affordance affIsDstableWallB = new Affordance(this.pfIsDstableWallB, this.pfIsAtGoal, isDstableWallBActions);
//		Affordance affIsDstableWallR = new Affordance(this.pfIsDstableWallR, this.pfIsAtGoal, isDstableWallRActions);
//		Affordance affIsDstableWallL = new Affordance(this.pfIsDstableWallL, this.pfIsAtGoal, isDstableWallLActions);
//
//		Affordance affIsAdjDoor = new Affordance(this.pfIsAdjDoor, this.pfIsAtGoal, isDoorActions);
//		Affordance affIsAdjFurnace = new Affordance(this.pfIsAdjFurnace, this.pfIsAtGoal, isAdjFurnaceActions);
//		Affordance affIsOnGoldOre = new Affordance(this.pfIsOnGoldOre, this.pfIsAtGoal, isOnGoldOreActions);
		
		// ----- ADD AFFORDANCES -----
		affordances.add(affIsPlaneF);
		affordances.add(affIsPlaneB);
		affordances.add(affIsPlaneR);
		affordances.add(affIsPlaneL);
		
		affordances.add(affIsAdjTrenchF);
		affordances.add(affIsAdjTrenchB);
		affordances.add(affIsAdjTrenchR);
		affordances.add(affIsAdjTrenchL);
		
//		affordances.add(affIsAdjDoor);
//		affordances.add(affIsAdjFurnace);
//		affordances.add(affIsOnGoldOre);
//		
//		if (worldName.contains("tunnel") || worldName.contains("epic")) {
//			affordances.add(affIsDstableWallF);
//			affordances.add(affIsDstableWallB);
//			affordances.add(affIsDstableWallR);
//			affordances.add(affIsDstableWallL);
//		}
		
		
		return affordances;
	}
	
	// === Affordance SG Planner (RTDP) ===
	public double[] AffordanceSubgoalPlanner(ArrayList<Affordance> kb, ArrayList<Subgoal> subgoals, int numRollouts, int maxDepth){
		
		// Initialize action plan
		String actionSequence = new String();
		
		// Build subgoal tree
		Node subgoalTree = generateGraph(subgoals);

		// Run BFS on subgoal tree (i.e. planning in subgoal space) 
		// returns a Node that is closer to the agent than the goal
		Node propfuncChain = BFS(subgoalTree);
		
		// Run VI between each subgoal in the chain
		State currState = initialState;
		int statePasses = 0;
		int totalReward = 0;
		rewardTable.remove(pfIsAtGoal);
		while(propfuncChain != null) {
			System.out.println("Current goal: " + propfuncChain.getPropFunc().toString());
//			rf = new SingleGoalPFRF(propfuncChain.getPropFunc(), 10, -1);
			
			rewardTable.put(propfuncChain.getPropFunc(), (Double) goalReward);
			rf = new SingleGoalMultiplePFRF(rewardTable, -1);
//			rf = new SingleGoalPFRF(propfuncChain.getPropFunc(), 10, -1); 
			tf = new SinglePFTF(propfuncChain.getPropFunc()); 
//			
//			rf = new SingleGoalMultiplePFRF(rewardTable, -1);
			tf = new SinglePFTF(propfuncChain.getPropFunc()); 
			goalCondition = new TFGoalCondition(tf);
			
			
//			OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 0.01, Integer.MAX_VALUE);
//			statePasses += planner.planFromStateAffordance(currState, kb);
			
			RTDP planner = new RTDP(domain, rf, tf, 0.99, hashingFactory, vInit, numRollouts, maxDelta, maxDepth);
			statePasses += planner.planFromStateAffordance(currState, kb);
			
			Policy p = new GreedyQPolicy((QComputablePlanner)planner);
//			boolean oldDetMode = this.mcdg.deterministicMode;
//			this.mcdg.deterministicMode = true;
			EpisodeAnalysis ea = p.evaluateAffordanceBehavior(currState, rf, tf, kb, maxSteps);
//			this.mcdg.deterministicMode = oldDetMode;
			
			// Add low level plan to overall plan and update current state to the end of that subgoal plan
			actionSequence += ea.getActionSequenceString() + "; ";
			currState = ea.getLastState();
			totalReward += sumReward(ea.rewardSequence);
			
			rewardTable.remove(propfuncChain.getPropFunc());
			propfuncChain = propfuncChain.getParent();
			
		}
		rewardTable.put(pfIsAtGoal, (Double) goalReward);
		
		double completed = goalCondition.satisfies(currState) ? 1.0 : 0.0;
		
		System.out.println(actionSequence);
		return new double[]{statePasses, totalReward, completed};	
		
	}
	
	public static void getResults(String dir, String[] planners) throws IOException {
		
//		String dir = "static/";
		File fout = new File("results/" + dir.substring(0, dir.length() - 1) + "_nondet_results.txt");
		FileWriter fw = new FileWriter(fout.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		File[] files = new File("maps/" + dir).listFiles();

	
		double[] info = null;
		
		for (File f: files) {
			System.out.println("Testing with map: " + f.getName());
			bw.write("Testing with map: " + f.getName() + "\n");
			
			// Minecraft world and knowledge base setup
			MinecraftBehavior mcb = new MinecraftBehavior(dir + f.getName());
			ArrayList<Affordance> kb = mcb.generateAffordanceKB(f.getName());
			ArrayList<Subgoal> subgoals = mcb.generateSubgoalKB(f.getName());
			
			// Change terminal functions depending on map
			if (f.getName().contains("gold") || f.getName().contains("epic")) {
				tf = new SinglePFTF(pfAgentHasGoldBlock); 
			} else {
				tf = new SinglePFTF(pfAgentAtGoal);
			}
			goalCondition = new TFGoalCondition(tf);
			
			for(String planner : planners) {
				System.out.println("Using planner: " + planner);
							
				// VANILLA OOMDP/VI
				if(planner.equals("VI")) {
					info = mcb.ValueIterationPlanner();
				}
			
				// RTDP
				if(planner.equals("RTDP")) {
					info = mcb.RTDPPlanner(numRollouts, maxDepth);
				}
				
				// SUBGOAL
				if(planner.equals("SG")) {
					info = mcb.SubgoalPlanner(subgoals, numRollouts, maxDepth);
				}
				
				// AFFORDANCE - VI
//				if(planner.equals("AFFVI")) {
//					info = mcb.AffordanceVIPlanner(kb);
//				}
				
				// AFFORDANCE - RTDP
//				if(planner.equals("AFFRTDP")) {
//					info = mcb.AffordanceRTDPPlanner(numRollouts, maxDepth, kb);
//				}
				
				// AFFORDANCE - SUBGOAL
				if(planner.equals("AFFSG")) {
					info = mcb.AffordanceSubgoalPlanner(kb, subgoals, numRollouts, maxDepth);
				}
				
				double statePasses = info[0];
				double totalReward = info[1];
				boolean completed = info[2] == 1.0 ? true : false;
				
				bw.write("\t" + planner + "," + statePasses + "," + totalReward + "," + completed + "\n");
				bw.flush();
			}
			bw.write("\n");
		}
		
		bw.close();

	}
	
	public static void getLearningResults(KnowledgeBase<Affordance> affLearnKnowlBase, MinecraftBehavior mcb) throws IOException {
		
		File[] maps = new File("maps/learn_result_maps").listFiles();

		File fout = new File("results/learn_results/results.txt");
		FileWriter fw = new FileWriter(fout.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		for(File map : maps) {
			String mapname = map.getName();
			bw.write(mapname + "\n");
	
			mcb.updateMap("learn_result_maps/" + mapname);

			tf = new MultiplePFTF(new PropositionalFunction[]{pfAgentAtGoal, pfAgentNotInWorld});
			goalCondition = new TFGoalCondition(tf);
	
//			// Setup learned knowledge base
//			KnowledgeBase<Affordance> affLearnKnowlBase = new KnowledgeBase<Affordance>(Affordance.class);
//			affLearnKnowlBase.load(mcb.domain, "trenches100-fullpolicy.kb");
//			affLearnKnowlBase.process();
			
			
//			double[] vanillaResults = mcb.RTDPPlanner(numRollouts, maxDepth);
//			
//			double statePasses = vanillaResults[0];
//			double totalReward = vanillaResults[1];
//			boolean completed = vanillaResults[2] == 1.0 ? true : false;
//			
//			bw.write("\tVanilla" + "," + statePasses + "," + totalReward + "," + completed + "\n");
//			bw.flush();
			
			double[] affLearnResults = mcb.AffordanceRTDPPlanner(numRollouts, maxDepth, affLearnKnowlBase);

			double statePasses = affLearnResults[0];
			double totalReward = affLearnResults[1];
			boolean completed = affLearnResults[2] == 1.0 ? true : false;
			
			bw.write("\tLearnedAffs" + "," + statePasses + "," + totalReward + "," + completed + "\n");
			bw.flush();
			
			KnowledgeBase<Affordance> affKnowlBase = null;
			
			double[] affResults = mcb.AffordanceRTDPPlanner(numRollouts, maxDepth, affKnowlBase);
			
			statePasses = affResults[0];
			totalReward = affResults[1];
			completed = affResults[2] == 1.0 ? true : false;
			
			bw.write("\tExpertAffs" + "," + statePasses + "," + totalReward + "," + completed + "\n\n");
			bw.flush();
			
		}
		System.out.println(" ==== DONE WITH MAP ====");
	}
	
	public static void main(String[] args) throws IOException {

		// Setup Minecraft World
		MinecraftBehavior mcb = new MinecraftBehavior("");
		
		// Setup learned knowledge base
		KnowledgeBase<Affordance> affLearnKnowlBase = new KnowledgeBase<Affordance>(Affordance.class);
		affLearnKnowlBase.load(mcb.domain, "trenches5000.kb");
		affLearnKnowlBase.process();

		getLearningResults(affLearnKnowlBase, mcb);
		
		
	}
	
	
}
