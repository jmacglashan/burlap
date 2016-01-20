package minecraft.MinecraftBehavior;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.SingleGoalMultipleLERF;
import common.SingleLETF;
import logicalexpressions.LogicalExpression;
import logicalexpressions.PFAtom;
import minecraft.MapIO;
import minecraft.MinecraftStateParser;
import minecraft.NameSpace;
import minecraft.MinecraftBehavior.Planners.BFSPlanner;
import minecraft.MinecraftBehavior.Planners.MinecraftPlanner;
import minecraft.MinecraftDomain.MinecraftDomainGenerator;
import minecraft.MinecraftDomain.MacroActions.BuildTrenchMacroAction;
import minecraft.MinecraftDomain.MacroActions.LookDownAlotMacroAction;
import minecraft.MinecraftDomain.MacroActions.SprintMacroAction;
import minecraft.MinecraftDomain.MacroActions.TurnAroundMacroAction;
import minecraft.MinecraftDomain.Options.TrenchBuildOption;
import minecraft.MinecraftDomain.Options.WalkUntilCantOption;
import affordances.KnowledgeBase;
import burlap.behavior.singleagent.*;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyDeterministicQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.*;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import minecraft.MinecraftStateGenerator.MinecraftStateGenerator;
import minecraft.MinecraftStateGenerator.Exceptions.StateCreationException;
import subgoals.*;

/**
 * The main behavior class for the minecraft domain
 * @author Dhershkowitz
 *
 */
public class MinecraftBehavior {
    // ----- CLASS variable -----
	public MinecraftDomainGenerator	MCDomainGenerator;
	private Domain						domain;
	private StateParser					MCStateParser;
	private RewardFunction				rewardFunction;
	private TerminalFunction			terminalFunction;
	private State						initialState;
	private DiscreteStateHashFactory	hashingFactory;
	public LogicalExpression 			currentGoal;
	
	//Propositional Functions
	public PropositionalFunction		pfAgentAtGoal;
	public PropositionalFunction		pfEmptySpace;
	public PropositionalFunction		pfBlockAt;
	public PropositionalFunction		pfAgentHasAtLeastXGoldOre;
	public PropositionalFunction		pfAgentHasAtLeastXGoldBar;
	public PropositionalFunction		pfBlockInFrontOfAgent;
	public PropositionalFunction		pfEndOfMapInFrontOfAgent;
	public PropositionalFunction		pfTrenchInFrontOfAgent;
	public PropositionalFunction		pfAgentInMidAir;
	public PropositionalFunction		pfTower;
	public PropositionalFunction		pfAgentInLava;
	public PropositionalFunction		pfAgentLookTowardGoal;
	public PropositionalFunction		pfAgentLookTowardGold;
	public PropositionalFunction		pfAgentLookTowardFurnace;
	public PropositionalFunction		pfAgentNotLookTowardGoal;
	public PropositionalFunction		pfAgentNotLookTowardGold;
	public PropositionalFunction		pfAgentNotLookTowardFurnace;
	public PropositionalFunction		pfAgentCanJump;
	public PropositionalFunction 		pfAlwaysTrue;
	
	// Dave's jenky hard coded prop funcs
	public PropositionalFunction		pfAgentCanWalk;
	public PropositionalFunction		pfTrenchBetweenAgentAndGoal;
	public PropositionalFunction		pfEmptyCellFrontAgentWalk;
	public PropositionalFunction		pfGoldBlockFrontOfAgent;
	public PropositionalFunction		pfFurnaceInFrontOfAgent;
	public PropositionalFunction		pfWallInFrontOfAgent;
	public PropositionalFunction		pfHurdleInFrontOfAgent;
	public PropositionalFunction 		pfLavaFrontAgent;
	public PropositionalFunction 		pfAgentLookDestWall;
	public PropositionalFunction 		pfAgentLookIndWall;
	public PropositionalFunction 		pfAgentLookLava;
	
	//Params for Planners
	public double						gamma = 0.99;
	public double						minDelta = .01;
	public int							maxSteps = 200;

	public int 							numRollouts = 1500; // RTDP
	public int							maxDepth = 50; // RTDP
	public int 							vInit = 1; // RTDP
	public int 							numRolloutsWithSmallChangeToConverge = 10; // RTDP
	public double						boltzmannTemperature = 0.5;
	public double						lavaReward = -10.0;
	private PropositionalFunction pfIndBlockFrontOfAgent;

	// ----- CLASS METHODS -----
	
	/**
	 * Blank constructor to instantiate behavior. Reads in the template map.
	 */
	public MinecraftBehavior() {
		MapIO mapIO = new MapIO(NameSpace.PATHTEMPLATEMAP);
		this.updateMap(mapIO);	
	}
	
	/**
	 * Constructor to instantiate behavior
	 * @param filePath map filepath on which to perform the planning
	 */
	public MinecraftBehavior(String filePath) {
		MapIO mapIO = new MapIO(filePath);
		this.updateMap(mapIO);	
	}
	
	public MinecraftBehavior(BufferedReader reader) {
		MapIO mapIO = new MapIO(reader);
		this.updateMap(mapIO);	
	}
	
	
	public MinecraftBehavior(MapIO mapIO) {
		this.updateMap(mapIO);	
	}
	
	/**
	 * 
	 * @param filePathOfMap a filepath to the location of the ascii map to update the behavior to
	 */
	public void updateMap(MapIO mapIO) {
		char[][][] mapAs3DArray = mapIO.getMapAs3DCharArray();
		HashMap<String, Integer> headerInfo = mapIO.getHeaderHashMap();
		
		//Update domain
		this.MCDomainGenerator = new MinecraftDomainGenerator(mapAs3DArray, headerInfo);
		this.domain = MCDomainGenerator.generateDomain();
		
		//Set state parser
		this.MCStateParser = new MinecraftStateParser(domain);
		
		// Set up the state hashing system
		this.hashingFactory = new DiscreteStateHashFactory();
		this.hashingFactory.setAttributesForClass(NameSpace.CLASSAGENT, domain.getObjectClass(NameSpace.CLASSAGENT).attributeList); 
		this.hashingFactory.setAttributesForClass(NameSpace.CLASSDIRTBLOCKNOTPICKUPABLE, domain.getObjectClass(NameSpace.CLASSDIRTBLOCKNOTPICKUPABLE).attributeList);
		this.hashingFactory.setAttributesForClass(NameSpace.CLASSDIRTBLOCKPICKUPABLE, domain.getObjectClass(NameSpace.CLASSDIRTBLOCKPICKUPABLE).attributeList);
		this.hashingFactory.setAttributesForClass(NameSpace.CLASSGOLDBLOCK, domain.getObjectClass(NameSpace.CLASSGOLDBLOCK).attributeList);

		//Set initial state
		try {
			this.initialState = MinecraftStateGenerator.createInitialState(mapAs3DArray, headerInfo, domain);
		} catch (StateCreationException e) {
			e.printStackTrace();
		}
		
		// Get propositional functions
		this.pfAgentAtGoal = domain.getPropFunction(NameSpace.PFATGOAL);
		this.pfEmptySpace = domain.getPropFunction(NameSpace.PFEMPSPACE);
		this.pfBlockAt = domain.getPropFunction(NameSpace.PFBLOCKAT);
		this.pfAgentHasAtLeastXGoldOre = domain.getPropFunction(NameSpace.PFATLEASTXGOLDORE);
		this.pfAgentHasAtLeastXGoldBar = domain.getPropFunction(NameSpace.PFATLEASTXGOLDBAR);
		this.pfBlockInFrontOfAgent = domain.getPropFunction(NameSpace.PFINDBLOCKINFRONT);
		this.pfEndOfMapInFrontOfAgent = domain.getPropFunction(NameSpace.PFENDOFMAPINFRONT);
		this.pfAgentInMidAir = domain.getPropFunction(NameSpace.PFAGENTINMIDAIR);
		this.pfAgentCanWalk = domain.getPropFunction(NameSpace.PFAGENTCANWALK);
		this.pfEmptyCellFrontAgentWalk = domain.getPropFunction(NameSpace.PFEMPTYCELLINWALK);
		this.pfTower = domain.getPropFunction(NameSpace.PFTOWER);
		// X front of agent (either at feet level or head)
		this.pfGoldBlockFrontOfAgent = domain.getPropFunction(NameSpace.PFAGENTLOOKGOLD);
		this.pfFurnaceInFrontOfAgent = domain.getPropFunction(NameSpace.PFFURNACEINFRONT);
		this.pfWallInFrontOfAgent = domain.getPropFunction(NameSpace.PFAGENTLOOKWALLOBJ);
		this.pfHurdleInFrontOfAgent = domain.getPropFunction(NameSpace.PFHURDLEINFRONTAGENT);
		this.pfIndBlockFrontOfAgent = domain.getPropFunction(NameSpace.PFINDBLOCKINFRONT);
		// Lava
		this.pfAgentInLava = domain.getPropFunction(NameSpace.PFAGENTINLAVA);
		this.pfLavaFrontAgent = domain.getPropFunction(NameSpace.PFLAVAFRONTAGENT);
		// Agent Looking at X
		this.pfAgentLookIndWall = domain.getPropFunction(NameSpace.PFAGENTLOOKINDBLOCK);
		this.pfAgentLookDestWall = domain.getPropFunction(NameSpace.PFAGENTLOOKDESTBLOCK);
		this.pfAgentLookLava = domain.getPropFunction(NameSpace.PFAGENTLOOKLAVA);
		// Agent looking toward X
		this.pfAgentLookTowardGoal = domain.getPropFunction(NameSpace.PFAGENTLOOKTOWARDGOAL);
		this.pfAgentLookTowardGold = domain.getPropFunction(NameSpace.PFAGENTLOOKTOWARDGOLD);
		this.pfAgentLookTowardFurnace = domain.getPropFunction(NameSpace.PFAGENTLOOKTOWARDFURNACE);
		// Agent not looking toward X
		this.pfAgentNotLookTowardGoal = domain.getPropFunction(NameSpace.PFAGENTNOTLOOKTOWARDGOAL);
		this.pfAgentNotLookTowardGold = domain.getPropFunction(NameSpace.PFAGENTNOTLOOKTOWARDGOLD);
		this.pfAgentNotLookTowardFurnace = domain.getPropFunction(NameSpace.PFAGENTNOTLOOKTOWARDFURNACE);
		// Misc
		this.pfTrenchInFrontOfAgent = domain.getPropFunction(NameSpace.PFTRENCHINFRONTAGENT);
		this.pfAgentCanJump = domain.getPropFunction(NameSpace.PFAGENTCANJUMP);
		this.pfAlwaysTrue = domain.getPropFunction(NameSpace.PFALWAYSTRUE);
		
		// Set up goal LE and lava LE for use in reward function
		PropositionalFunction pfToUse = getPFFromHeader(headerInfo);
		this.currentGoal = new PFAtom(pfToUse.getAllGroundedPropsForState(this.initialState).get(0)); 
		LogicalExpression lavaLE = new PFAtom(this.pfAgentInLava.getAllGroundedPropsForState(this.initialState).get(0));
		
		// Set up reward function
		HashMap<LogicalExpression, Double> rewardMap = new HashMap<LogicalExpression, Double>();
		rewardMap.put(this.currentGoal, 0.0);
		rewardMap.put(lavaLE, this.lavaReward);
		this.rewardFunction = new SingleGoalMultipleLERF(rewardMap, -1); 
		
		//Set up terminal function
		this.terminalFunction = new SingleLETF(currentGoal);
	}
	
	private PropositionalFunction getPFFromHeader(HashMap<String, Integer> headerInfo) {
		switch(headerInfo.get(Character.toString(NameSpace.CHARGOALDESCRIPTOR))) {
		case NameSpace.INTXYZGOAL:
			return this.pfAgentAtGoal;
		
		case NameSpace.INTGOLDOREGOAL:
			return this.pfAgentHasAtLeastXGoldOre;
			
		case NameSpace.INTGOLDBARGOAL:
			return this.pfAgentHasAtLeastXGoldBar;
		case NameSpace.INTTOWERGOAL:
			return this.pfTower;
		default:
			break;
		}
		
		return null;

	}
	
	// --- ACCESSORS ---
	
	public Domain getDomain() {
		return this.domain;
	}
	
	public RewardFunction getRewardFunction() {
		return this.rewardFunction;
	}
	
	public TerminalFunction getTerminalFunction() {
		return this.terminalFunction;
	}
	
	public double getGamma() {
		return this.gamma;
	}
	
	public DiscreteStateHashFactory getHashFactory() {
		return this.hashingFactory;
	}

	public double getMinDelta() {
		return this.minDelta;
	}
	
	public State getInitialState() {
		return this.initialState;

	}
	
	public MinecraftDomainGenerator getDomainGenerator() {
		return this.MCDomainGenerator;
	}

	
	// ---------- PLANNERS ---------- 
	
	/**
	 * Takes in an instance of an OOMDP planner and solves the OO-MDP
	 * @param planner
	 * @return p: The Policy from the solved OO-MDP
	 */
	public Policy solve(OOMDPPlanner planner) {
		// Solve the OO-MDP
		planner.planFromState(initialState);

		// Create a Q-greedy policy from the planner
		GreedyQPolicy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		// Print out some infos
		EpisodeAnalysis ea = p.evaluateBehavior(initialState, this.rewardFunction, this.terminalFunction, maxSteps);
		
		System.out.println(ea.getActionSequenceString());

		return p;
	}
	
//	public int countReachableStates() {
//		OOMDPPlanner vi = new ValueIteration(domain, rewardFunction, terminalFunction, gamma, hashingFactory, boltzmannTemperature, maxDepth);
//		
//		((ValueIteration)vi).performReachabilityFrom(initialState);
//		return vi.getMapToStateIndex().size();
//	}
	
	public static void main(String[] args) {
//		String mapsPath = System.getProperty("user.dir") + "/maps/";
		
		String mapName = "src/minecraft/maps/planeMaps/plane1.map";
		
		MinecraftBehavior mcBeh = new MinecraftBehavior(mapName);
		double [] results;
		
		//BFS
//		BFSPlanner bfsPlanner = new BFSPlanner(mcBeh, true, true);
//		results = bfsPlanner.runPlanner();
//		System.out.println("(minecraftBehavior) results: " + results[0] + "," + results[1] + "," + results[2] + "," + results[3]);

		//RTDP
//		RTDPPlanner rtdpPlanner = new RTDPPlanner(mcBeh, false, false);
//		results = rtdpPlanner.runPlanner();
//		System.out.println("(minecraftBehavior) results [rtdp]: " + results[0] + "," + results[1] + "," + results[2] + "," + results[3]);
		
		//BFSRTDP
//		BFSRTDPPlanner BFSRTDPPlanner = new BFSRTDPPlanner(mcBeh, false, false);
//		results = BFSRTDPPlanner.runPlanner();
//		System.out.println("(minecraftBehavior) results: " + results[0] + "," + results[1] + "," + results[2] + "," + results[3]);
		
		//AFFRTDP
		boolean useOptions = false;
		boolean useMAs = false;
		boolean hardFlag = true;
		boolean expert = false;
		
		String kbName = "";
		if(expert) kbName += "expert/expert";
		else kbName += "learned/grid";
		
		if(useMAs) kbName += "_ma";
		if(useOptions) kbName += "_o";
		if(!useMAs && !useOptions) kbName += "_prim_acts";
		kbName += ".kb";
		
		// Load knowledge base
//		KnowledgeBase affKB = new KnowledgeBase();
//		affKB.load(mcBeh.getDomain(), MinecraftPlanner.getMapOfMAsAndOptions(mcBeh, useOptions, useMAs), kbName, hardFlag);
//		AffordanceRTDPPlanner affRTDPPlanner = new AffordanceRTDPPlanner(mcBeh, useOptions, useMAs, affKB);
//		results = affRTDPPlanner.runPlanner();
//		System.out.println("(minecraftBehavior) results expert(" + expert + ") : " + results[0] + "," + results[1] + "," + results[2] + "," + results[3]);

		//VI
//		VIPlanner viPlan = new VIPlanner(mcBeh, false, false);
//		results = viPlan.runPlanner();
//		System.out.println("(minecraftBehavior) results [VI]: " + results[0] + "," + results[1] + "," + results[2] + "," + results[3]);

		// Affordance VI
//		AffordanceVIPlanner affVIPlan = new AffordanceVIPlanner(mcBeh, true, true, "somekb.kb");
//		results = affVIPlan.runPlanner();
//		System.out.println("(minecraftBehavior) results: " + results[0] + "," + results[1] + "," + results[2] + "," + results[3]);

		
		// Subgoal Planner
//		OOMDPPlanner lowLevelPlanner = new RTDP(mcBeh.domain, mcBeh.rewardFunction, mcBeh.terminalFunction, mcBeh.gamma, mcBeh.hashingFactory, mcBeh.vInit, mcBeh.numRollouts, mcBeh.minDelta, mcBeh.maxDepth);
//		mcBeh.SubgoalPlanner(lowLevelPlanner);
		
		//		SubgoalKnowledgeBase subgoalKB = new SubgoalKnowledgeBase(mapName, mcBeh.domain);
//		List<Subgoal> highLevelPlan = subgoalKB.generateSubgoalKB(mapName);
//		SubgoalPlanner sgp = new SubgoalPlanner(mcBeh.domain, mcBeh.getInitialState(), mcBeh.rewardFunction, mcBeh.terminalFunction, lowLevelPlanner, highLevelPlan);
//		sgp.solve();
		
		// Collect results and write to file
//		File resultsFile = new File("src/tests/results/mcBeh_results.result");
//		BufferedWriter bw;
//		FileWriter fw;
//		try {
//			fw = new FileWriter(resultsFile.getAbsoluteFile());
//			bw = new BufferedWriter(fw);
//			bw.write("(minecraftBehavior) results: LRTDP " + results[0] + "," + results[1] + "," + results[2] + "," + String.format("%.2f", results[3] / 1000) + "s");
//			bw.flush();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	
}