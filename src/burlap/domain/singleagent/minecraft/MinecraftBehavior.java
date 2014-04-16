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
import java.util.Stack;
import java.util.PriorityQueue;
import java.util.Queue;

import sun.font.EAttribute;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SingleGoalMultiplePFRF;
import burlap.oomdp.singleagent.common.SingleGoalPFRF;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.visualizer.Visualizer;
import burlap.oomdp.core.State;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.Policy;
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

	MinecraftDomain				mcdg;
	Domain						domain;
	StateParser					sp;
	RewardFunction				rf;
	static TerminalFunction			tf;
	static StateConditionTest			goalCondition;
	State						initialState;
	DiscreteStateHashFactory	hashingFactory;
	
	static PropositionalFunction		pfAgentAtGoal;
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
	static PropositionalFunction 		pfAgentHasBread;
	PropositionalFunction		pfIsAdjOven;
	PropositionalFunction		pfIsOnGrain;
	PropositionalFunction  		pfIsInLava;
	
	HashMap<PropositionalFunction, Double> rewardTable;
	static int 						numRollouts = 2000;
	static int 						maxDepth = 50;
//	static double				vInit = (10 / (1 - .99));
	static double				vInit = 0;
	static double				goalReward = -1.0;
	static int					maxSteps = 200;
	
	
	public MinecraftBehavior(String mapfile) {
		MCStateGenerator mcsg = new MCStateGenerator(mapfile);
		mcdg = new MinecraftDomain();
		
//		Gets the maximum dimensions for the map. The first entry is the number of columns
//		and the second entry is the number of rows.
		int[] maxDims = mcsg.getDimensions();
		
		boolean placeMode = false;
		boolean destMode = false;


		if (mcsg.getBNum() > 0) {
			placeMode = true;
		}
		if (mapfile.contains("tunnel")) {
			destMode = true;
		}
		
		domain = mcdg.generateDomain(maxDims[0], maxDims[1], placeMode, destMode);
		
		sp = new MinecraftStateParser(domain);

		// === Build Initial State=== //
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
		
		pfIsInLava = new IsInLava(this.mcdg.ISINLAVA, this.mcdg.DOMAIN,
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
		
		pfIsAgentYAt = new IsAgentYAt("IsAgentOnBridge", this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT}, (this.mcdg.MAXY + 1) / 2 - 1, 0);
		
		
		pfIsAtLocation = new IsAtLocationPF(this.mcdg.ISATLOC, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, 13, 6, 1);
		
		pfIsWalkable = new IsWalkablePF(this.mcdg.ISWALK, this.mcdg.DOMAIN,
				new String[]{"Integer", "Integer", "Integer"});

		pfAgentHasBread = new AgentHasBreadPF(this.mcdg.AGENTHASBREAD, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT});
		
		pfAgentAtGoal = new AtGoalPF(this.mcdg.PFATGOAL, this.mcdg.DOMAIN,
				new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL});
		
		// Generate Goal Condition
//		rf = new SingleGoalPFRF(pfAgentHasBread, 10, -1); 
//		tf = new SinglePFTF(pfAgentHasBread); 
//		goalCondition = new TFGoalCondition(tf);
		
		rewardTable = new HashMap<PropositionalFunction, Double>();
		rewardTable.put(pfAgentAtGoal, (Double) goalReward);
		rewardTable.put(pfAgentHasBread, (Double) goalReward);
		Double lavaRew = -100.0;
		rewardTable.put(pfIsInLava, lavaRew);
		
		rf = new SingleGoalMultiplePFRF(rewardTable, -1);
		
		tf = new SinglePFTF(pfAgentAtGoal); 
		goalCondition = new TFGoalCondition(tf);
		
		
	}

	// ---------- PLANNERS ---------- 
	
	// === VI Planner	===
	public double[] ValueIterationPlanner(){
		
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 0.01, Integer.MAX_VALUE);
		
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

		RTDP planner = new RTDP(domain, rf, tf, 0.99, hashingFactory, vInit, numRollouts, 0.01, maxDepth);
		
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
			
			RTDP planner = new RTDP(domain, rf, tf, 0.99, hashingFactory, vInit, numRollouts, 0.01, maxDepth);
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
			PropositionalFunction pfIsAgentBeforeBridge= new IsAtLocationPF("IsBeforeBridge", this.mcdg.DOMAIN,
					new String[]{this.mcdg.CLASSAGENT}, (int)Math.floor((this.mcdg.MAXX) / 2), (int)Math.floor((this.mcdg.MAXX) / 2) + 1, 1, 1);
			PropositionalFunction pfIsAgentOnBridge= new IsAtLocationPF("IsOnBridge", this.mcdg.DOMAIN,
					new String[]{this.mcdg.CLASSAGENT}, (int)Math.floor((this.mcdg.MAXX) / 2), (int)Math.floor((this.mcdg.MAXX) / 2), 1, 0);
			
			Subgoal bridge_sg = new Subgoal(pfIsAgentOnBridge, this.pfIsAtGoal);
			Subgoal before_bridge = new Subgoal(pfIsAgentBeforeBridge, pfIsAgentOnBridge);
			result.add(bridge_sg);
			result.add(before_bridge);
		}
		
		if (worldName.equals("doorworld.map")) {
			PropositionalFunction doorOpenPF = new IsDoorOpen("IsDoorOpen", this.mcdg.DOMAIN,
					new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"2", "9", "1"});
			PropositionalFunction agentBeyondDoor = new IsAtLocationPF("IsBeyondDoor", this.mcdg.DOMAIN,
					new String[]{this.mcdg.CLASSAGENT}, 2, 8, 1);
			
			Subgoal beyondDoor = new Subgoal(agentBeyondDoor, this.pfIsAtGoal);
			Subgoal doorOpen = new Subgoal(doorOpenPF, agentBeyondDoor);
			
			result.add(beyondDoor);
			result.add(doorOpen);
		}
	
		// Breadworld subgoals
		if (worldName.contains("bread")) {
			PropositionalFunction hasGrainPF = new AgentHasGrainPF(MinecraftDomain.ATTAGHASGRAIN, this.mcdg.DOMAIN,
					new String[]{MinecraftDomain.CLASSAGENT});
			Subgoal hasGrain = new Subgoal(hasGrainPF, pfAgentHasBread);
			result.add(hasGrain);
		}
		
		if (worldName.contains("tunnel")) {
			PropositionalFunction agentInTunnel= new IsAtLocationPF("IsInTunnel", this.mcdg.DOMAIN,
					new String[]{this.mcdg.CLASSAGENT}, 1, 1, 1);
			Subgoal inTunnel = new Subgoal(agentInTunnel, this.pfIsAtGoal);
			result.add(inTunnel);
		}
		
		// Doorworld subgoals
		if (worldName.equals("doorbridgeworld.map")) {
			PropositionalFunction doorOpenPF2 = new IsDoorOpen("IsDoorOpen2", this.mcdg.DOMAIN,
					new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"0", "4", "1"});
			PropositionalFunction pfIsAgentBeforeBridge= new IsAtLocationPF("IsBeforeBridge", this.mcdg.DOMAIN,
					new String[]{this.mcdg.CLASSAGENT}, 5, 2, 1, 1);
			PropositionalFunction pfIsAgentOnBridge= new IsAtLocationPF("IsOnBridge", this.mcdg.DOMAIN,
					new String[]{this.mcdg.CLASSAGENT}, 5, 1, 1, 0);
			PropositionalFunction doorOpenPF1 = new IsDoorOpen("IsDoorOpen1", this.mcdg.DOMAIN,
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
			PropositionalFunction atEntrancePF = new IsAtLocationPF("ATENTRANCE", this.mcdg.DOMAIN,
			new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL}, 6, 10, 1);
	
			PropositionalFunction midwayPF = new IsAtLocationPF("MIDWAY", this.mcdg.DOMAIN,
			new String[]{this.mcdg.CLASSAGENT, this.mcdg.CLASSGOAL},1, 10, 1);
	
			PropositionalFunction almostTherePF = new IsAtLocationPF("ALMOST", this.mcdg.DOMAIN,
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
			PropositionalFunction firstDoorOpenPF = new IsDoorOpen("FIRSTDOOROPEN", this.mcdg.DOMAIN,
					new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"10", "14", "1"});
			PropositionalFunction secondDoorOpenPF = new IsDoorOpen("SECONDDOOROPEN", this.mcdg.DOMAIN,
					new String[]{MinecraftDomain.CLASSAGENT}, new String[]{"1", "9", "1"});
			
			Subgoal secondDoor = new Subgoal(secondDoorOpenPF, this.pfIsAtGoal);
			Subgoal firstDoor = new Subgoal(firstDoorOpenPF, secondDoorOpenPF);
	
			result.add(secondDoor);
			result.add(firstDoor);
		}
		return result;
	}
	
	// ====== AFFORDANCE VERSIONS ======
	
	// === Affordance RTDP Planner	===
	public double[] AffordanceRTDPPlanner(int numRollouts, int maxDepth, ArrayList<Affordance> kb){
		
		RTDP planner = new RTDP(domain, rf, tf, 0.99, hashingFactory, vInit, numRollouts, 0.01, maxDepth);
		
		int statePasses = planner.planFromStateAffordance(initialState, kb);

		// Create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy(planner);
		
		System.out.println("Finished Planning");
		EpisodeAnalysis ea = p.evaluateAffordanceBehavior(initialState, rf, tf, kb, maxSteps);

		double totalReward = sumReward(ea.rewardSequence);
		
		State finalState = ea.getLastState();
		
		double completed = goalCondition.satisfies(finalState) ? 1.0 : 0.0;
		
		return new double[]{statePasses, totalReward, completed};	
	}
	
	// === Affordance VI Planner	===
	public double[] AffordanceVIPlanner(ArrayList<Affordance> kb){
		
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, .1, Integer.MAX_VALUE);
		
//		System.out.println((initialState.getStateDescription()));
		
		double statePasses = planner.planFromStateAffordance(initialState, kb);
		
		// Create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		EpisodeAnalysis ea = p.evaluateAffordanceBehavior(initialState, rf, tf, kb, maxSteps);

		double totalReward = sumReward(ea.rewardSequence);
		
		State finalState = ea.getLastState();
		
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
		
		ArrayList<Action> isPlaneActions = new ArrayList<Action>();
		isPlaneActions.add(this.mcdg.forward);
		isPlaneActions.add(this.mcdg.backward);
		isPlaneActions.add(this.mcdg.left);
		isPlaneActions.add(this.mcdg.right);
		isPlaneActions.add(this.mcdg.jumpF);
		isPlaneActions.add(this.mcdg.jumpB);
		isPlaneActions.add(this.mcdg.jumpL);
		isPlaneActions.add(this.mcdg.jumpR);
		
		ArrayList<Action> isTrenchActions = new ArrayList<Action>();
		isTrenchActions.add(this.mcdg.placeF);
		isTrenchActions.add(this.mcdg.placeB);
		isTrenchActions.add(this.mcdg.placeL);
		isTrenchActions.add(this.mcdg.placeR);
		isTrenchActions.add(this.mcdg.forward);
		isTrenchActions.add(this.mcdg.backward);
		isTrenchActions.add(this.mcdg.left);
		isTrenchActions.add(this.mcdg.right);
		isTrenchActions.add(this.mcdg.jumpF);
		isTrenchActions.add(this.mcdg.jumpB);
		isTrenchActions.add(this.mcdg.jumpL);
		isTrenchActions.add(this.mcdg.jumpR);
		
		// Add option
		isTrenchActions.add(new PolicyDefinedSubgoalOption("Build Bridge", new ForwardPolicy(this.mcdg.forward), new SubgoalTests.BridgeSubgoal(this.pfIsAdjTrench)));

		ArrayList<Action> isDoorActions = new ArrayList<Action>();
		isDoorActions.add(this.mcdg.forward);
		isDoorActions.add(this.mcdg.backward);
		isDoorActions.add(this.mcdg.left);
		isDoorActions.add(this.mcdg.right);
		isDoorActions.add(this.mcdg.openF);
		isDoorActions.add(this.mcdg.openB);
		isDoorActions.add(this.mcdg.openR);
		isDoorActions.add(this.mcdg.openL);
		
		ArrayList<Action> isDstableWallActions = new ArrayList<Action>();
		isDstableWallActions.add(this.mcdg.forward);
		isDstableWallActions.add(this.mcdg.backward);
		isDstableWallActions.add(this.mcdg.left);
		isDstableWallActions.add(this.mcdg.right);
		isDstableWallActions.add(this.mcdg.destF);
		isDstableWallActions.add(this.mcdg.destB);
		isDstableWallActions.add(this.mcdg.destR);
		isDstableWallActions.add(this.mcdg.destL);
		
		ArrayList<Action> isOnGrainActions = new ArrayList<Action>();
		isOnGrainActions.add(this.mcdg.pickUpGrain);
		
		ArrayList<Action> isAdjOvenActions = new ArrayList<Action>();
		isAdjOvenActions.add(this.mcdg.placeGrain);
		
		// ----- DEFINE AFFORDANCES -----
		Affordance affIsPlane = new Affordance(this.pfIsPlane, this.pfIsAtGoal, isPlaneActions);
		Affordance affIsAdjTrench = new Affordance(this.pfIsAdjTrench, this.pfIsAtGoal, isTrenchActions);
		Affordance affIsAdjDoor = new Affordance(this.pfIsAdjDoor, this.pfIsAtGoal, isDoorActions);
		Affordance affIsAdjOven = new Affordance(this.pfIsAdjOven, this.pfIsAtGoal, isAdjOvenActions);
		Affordance affIsOnGrain = new Affordance(this.pfIsOnGrain, this.pfIsAtGoal, isOnGrainActions);
		Affordance affIsDstableWall = new Affordance(this.pfIsAdjDstableWall, this.pfIsAtGoal, isDstableWallActions);
		
		// ----- ADD AFFORDANCES -----
		affordances.add(affIsPlane);
		affordances.add(affIsAdjDoor);
		affordances.add(affIsAdjTrench);
		affordances.add(affIsAdjOven);
		affordances.add(affIsOnGrain);
		if (worldName.contains("tunnel")) {
			affordances.add(affIsDstableWall);
		}
		
		
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
			
			RTDP planner = new RTDP(domain, rf, tf, 0.99, hashingFactory, vInit, numRollouts, 0.01, maxDepth);
			statePasses += planner.planFromStateAffordance(currState, kb);
			
			Policy p = new GreedyQPolicy((QComputablePlanner)planner);
			EpisodeAnalysis ea = p.evaluateAffordanceBehavior(currState, rf, tf, kb, maxSteps);
			
			// Add low level plan to overall plan and update current state to the end of that subgoal plan
			actionSequence += ea.getActionSequenceString() + "; ";
			currState = ea.getLastState();
			totalReward += sumReward(ea.rewardSequence);
			
			rewardTable.remove(propfuncChain.getPropFunc());
			propfuncChain = propfuncChain.getParent();
			
		}
		rewardTable.put(pfIsAtGoal, (Double) goalReward);
		
		double completed = goalCondition.satisfies(currState) ? 1.0 : 0.0;
		
		
		return new double[]{statePasses, totalReward, completed};	
		
	}
	
	public static void getResults() throws IOException {
		
		String dir = "mutable/";
		File fout = new File("results/" + dir.substring(0, dir.length() - 1) + "_nondet_results.txt");
		FileWriter fw = new FileWriter(fout.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		File[] files = new File("maps/" + dir).listFiles();

		String[] planners = {"VI", "RTDP", "SG", "AFFVI", "AFFRTDP", "AFFSG"};

		double[] info = null;
		
		for (File f: files) {
			System.out.println("Testing with map: " + f.getName());
			bw.write("Testing with map: " + f.getName() + "\n");
			
			// Minecraft world and knowledge base setup
			MinecraftBehavior mcb = new MinecraftBehavior(dir + f.getName());
			ArrayList<Affordance> kb = mcb.generateAffordanceKB(f.getName());
			ArrayList<Subgoal> subgoals = mcb.generateSubgoalKB(f.getName());
			
			// Change terminal functions depending on map
			if (f.getName().contains("bread")) {
				tf = new SinglePFTF(pfAgentHasBread); 
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
				if(planner.equals("AFFVI")) {
					info = mcb.AffordanceVIPlanner(kb);
				}
				
				// AFFORDANCE - RTDP
				if(planner.equals("AFFRTDP")) {
					info = mcb.AffordanceRTDPPlanner(numRollouts, maxDepth, kb);
				}
				
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
	
	public static void main(String[] args) {
		
		// Collect Results
		try {
			getResults();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Setup Minecraft World
//		MinecraftBehavior mcb = new MinecraftBehavior("breadworld.map");
//		int numUpdates = 0;
//		int numRollouts = 1000;
//		int maxDepth = 250;
		// VANILLA OOMDP/VI
//		 numUpdates = mcb.ValueIterationPlanner();
//		System.out.println("VI: " + numUpdates);

		// RTDP
//		numUpdates = mcb.RTDPPlanner(numRollouts, maxDepth);
		
		// SUBGOALS
//		ArrayList<Subgoal> kb = mcb.generateSubgoalKB();
//		int numUpdates = mcb.SubgoalPlanner(kb, 1000, 200);
		
		// AFFORDANCE - VI
//		 ArrayList<Affordance> kb = mcb.generateAffordanceKB();
//		 numUpdates = mcb.AffordanceVIPlanner(kb);
		
		// AFFORDANCE - RTDP
//		 ArrayList<Affordance> kb = mcb.generateAffordanceKB();
//		 numUpdates = mcb.AffordanceRTDPPlanner(numRollouts, maxDepth, kb);
		
		// AFFORDANCE - SG
//		 ArrayList<Affordance> kb = mcb.generateAffordanceKB();
//		 ArrayList<Subgoal> subgoals = mcb.generateSubgoalKB();
//		 numUpdates = mcb.AffordanceSubgoalPlanner(kb, subgoals, numRollouts, maxDepth);

		// END TIMER
//		timeEnd = System.nanoTime();
//		timeDelta = (double) (System.nanoTime()- timeStart) / 1000000000;
//		System.out.println("Took "+ timeDelta + " s"); 

//		System.out.println("AFFVI: " + numUpdates);

		
	}
	
	
}