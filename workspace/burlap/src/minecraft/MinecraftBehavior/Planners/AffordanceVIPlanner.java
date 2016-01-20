//package minecraft.MinecraftBehavior.Planners;
//
//import affordances.KnowledgeBase;
//import burlap.behavior.affordances.AffordancesController;
//import burlap.behavior.singleagent.EpisodeAnalysis;
//import burlap.behavior.singleagent.Policy;
//import burlap.behavior.singleagent.planning.OOMDPPlanner;
//import burlap.behavior.singleagent.planning.QComputablePlanner;
//import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
//import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
//import burlap.behavior.singleagent.planning.stochastic.valueiteration.AffordanceValueIteration;
//import burlap.oomdp.core.State;
//import minecraft.MinecraftBehavior.MinecraftBehavior;
//
//public class AffordanceVIPlanner extends MinecraftPlanner {
//	private int maxSteps;
//	AffordancesController affController;
//	private KnowledgeBase affKB;
//	
//	/**
//	 * 
//	 * @param mcBeh
//	 * @param addOptions
//	 * @param addMacroActions
//	 * @param affKBString
//	 */
//	public AffordanceVIPlanner(MinecraftBehavior mcBeh, boolean addOptions,
//			boolean addMacroActions, KnowledgeBase affKB) {
//		super(mcBeh, addOptions, addMacroActions);
//		this.maxSteps = mcBeh.maxSteps;
//		this.affKB = affKB;
//		this.affController = this.affKB.getAffordancesController();
//		this.affController.setCurrentGoal(this.mcBeh.currentGoal); // Update goal to determine active affordances
//
//	}
//
//
//	@Override
//	protected OOMDPPlanner getPlanner() {
//		return new AffordanceValueIteration(domain, this.rf, this.tf, gamma, hashingFactory, 0.01, Integer.MAX_VALUE, affController);
//
//	}
//
//
//	@Override
//	protected double[] runPlannerHelper(OOMDPPlanner iPlanner) {
//		// Setup goal condition and planner
//		TFGoalCondition goalCondition = new TFGoalCondition(this.tf);
//		AffordanceValueIteration planner = (AffordanceValueIteration) iPlanner;
//		
//		// Time
//		long startTime = System.currentTimeMillis( );
//		
//		// Plan and record bellmanUpdates
//		int bellmanUpdates = planner.planFromStateAndCount(initialState);
//		
//		// Create a Q-greedy policy from the planner
//		Policy p = new AffordanceGreedyQPolicy(affController, (QComputablePlanner)planner);
//		
//		// Record the plan results to a file
//		EpisodeAnalysis ea = p.evaluateBehavior(initialState, this.rf, this.tf, maxSteps);
//		
//		long totalPlanningTime  = System.currentTimeMillis( ) - startTime;
//		
//		// Count reward.
//		double totalReward = 0.;
//		for(Double d : ea.rewardSequence){
//			totalReward = totalReward + d;
//		}
//		
//		// Check to see if the planner found the goal
//		State finalState = ea.stateSequence.get(ea.stateSequence.size()-1);
//		double completed = goalCondition.satisfies(finalState) ? 1.0 : 0.0;
//		
//		System.out.println(ea.getActionSequenceString());
//		
//		double[] results = {bellmanUpdates, totalReward, completed, totalPlanningTime};
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
