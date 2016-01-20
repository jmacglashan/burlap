//package minecraft.MinecraftBehavior.Planners;
//
//import affordances.KnowledgeBase;
//import burlap.behavior.affordances.AffordancesController;
//import burlap.behavior.singleagent.EpisodeAnalysis;
//import burlap.behavior.singleagent.Policy;
//import burlap.behavior.singleagent.planning.OOMDPPlanner;
//import burlap.behavior.singleagent.planning.QComputablePlanner;
//import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
//import burlap.behavior.singleagent.planning.stochastic.rtdp.AffordanceRTDP;
//import burlap.oomdp.core.State;
//import minecraft.MinecraftBehavior.MinecraftBehavior;
//
//public class AffordanceRTDPPlanner extends MinecraftPlanner {
//	private int vInit;
//	private int numRollouts;
//	private double minDelta;
//	private int maxDepth;
//	private int numRolloutsWithSmallChangeToConverge;
//	private int maxSteps;
//	private String kbPath;
//	private AffordancesController affController;
//	private KnowledgeBase affKB;
//
//	/**
//	 * 
//	 * @param mcBeh
//	 * @param addOptions
//	 * @param addMacroActions
//	 * @param kbPath
//	 */
//	public AffordanceRTDPPlanner(MinecraftBehavior mcBeh, boolean addOptions,
//			boolean addMacroActions, KnowledgeBase affKB) {
//		super(mcBeh, addOptions, addMacroActions);
//		this.vInit = mcBeh.vInit;
//		this.numRollouts = mcBeh.numRollouts;
//		this.minDelta = mcBeh.minDelta;
//		this.maxDepth = mcBeh.maxDepth;
//		this.maxSteps = mcBeh.maxSteps;
//		this.numRolloutsWithSmallChangeToConverge = mcBeh.numRolloutsWithSmallChangeToConverge;
//		this.affKB = affKB;
//		this.addOptionsAndMAsToOOMDPPlanner(this.getPlanner());
//		
//		this.affController = affKB.getAffordancesController();
//		affController.setCurrentGoal(this.mcBeh.currentGoal); // Update goal to determine active affordances
//	}
//
//	@Override
//	protected OOMDPPlanner getPlanner() {
//
//		AffordanceRTDP planner = new AffordanceRTDP(domain, this.rf, this.tf, gamma, hashingFactory, vInit, numRollouts, minDelta, maxDepth, affController, numRolloutsWithSmallChangeToConverge);
//		
//		return planner;
//	}
//
//	@Override
//	protected double[] runPlannerHelper(OOMDPPlanner planner) {
//	
//		AffordanceRTDP affPlanner = (AffordanceRTDP) planner;
//		
//		long startTime = System.currentTimeMillis( );
//		
//		int bellmanUpdates = affPlanner.planFromStateAndCount(initialState);
//
//		// Create a Policy from the planner
////		Policy p = new AffordanceBoltzmannQPolicy((QComputablePlanner)planner, boltzmannTemperature, affController);
//		Policy p = new AffordanceGreedyQPolicy(this.affController, (QComputablePlanner)planner);
//		EpisodeAnalysis ea = p.evaluateBehavior(this.initialState, this.rf, this.tf, this.maxSteps);
//		
//		// Compute CPU time
//		long totalPlanningTime  = System.currentTimeMillis( ) - startTime;
//		
//		// Count reward.
//		double totalReward = 0.;
//		for(Double d : ea.rewardSequence){
//			totalReward = totalReward + d;
//		}
//		
//		// Check if task completed
//		State finalState = ea.getState(ea.stateSequence.size() - 1);
//		double completed = this.tf.isTerminal(finalState) ? 1.0 : 0.0;
//		
////		System.out.println(ea.getActionSequenceString());
//
//		double[] results = {bellmanUpdates, totalReward, completed, totalPlanningTime};
//		
//		return results;
//	}
//	
//	public void updateKB(KnowledgeBase affKB) {
//		this.affKB = affKB;
//		this.affController = affKB.getAffordancesController();
//		affController.setCurrentGoal(this.mcBeh.currentGoal); // Update goal to determine active affordances
//	}
//
//}
