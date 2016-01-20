//package minecraft.MinecraftBehavior.Planners;
//
//import burlap.behavior.singleagent.EpisodeAnalysis;
//import burlap.behavior.singleagent.Policy;
//import burlap.behavior.singleagent.planning.OOMDPPlanner;
//import burlap.behavior.singleagent.planning.QComputablePlanner;
//import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
//import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
//import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
//import burlap.oomdp.core.State;
//import minecraft.MinecraftBehavior.MinecraftBehavior;
//
//public class VIPlanner extends MinecraftPlanner{
//	private int maxSteps;
//	
//	/**
//	 * 
//	 * @param mcBeh
//	 * @param addOptions
//	 * @param addMacroActions
//	 */
//	public VIPlanner(MinecraftBehavior mcBeh, boolean addOptions,
//			boolean addMacroActions) {
//		super(mcBeh, addOptions, addMacroActions);
//		this.maxSteps = mcBeh.maxSteps;
//	}
//
//	@Override
//	protected OOMDPPlanner getPlanner() {
//		return new ValueIteration(domain, this.rf, this.tf, gamma, hashingFactory, 0.01, Integer.MAX_VALUE);
//
//	}
//
//	@Override
//	protected double[] runPlannerHelper(OOMDPPlanner iPlanner) {
//		
//		ValueIteration planner = (ValueIteration) iPlanner;
//		
//		TFGoalCondition goalCondition = new TFGoalCondition(this.tf);
//
//
//		long startTime = System.currentTimeMillis( );
//		
//		int bellmanUpdates = planner.planFromStateAndCount(initialState);
//		System.out.println("(VIPlanner) finished planning");
//		// Create a Q-greedy policy from the planner
//		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
//		
//		// Record the plan results to a file
//		EpisodeAnalysis ea = p.evaluateBehavior(initialState, this.rf, this.tf, this.maxSteps);
//
//		long totalPlanningTime  = System.currentTimeMillis( ) - startTime;
//		System.out.println(ea.getActionSequenceString());
//		// Count reward.
//		double totalReward = 0.;
//		for(Double d : ea.rewardSequence){
//			totalReward = totalReward + d;
//		}
//		
//		State finalState = ea.stateSequence.get(ea.stateSequence.size()-1);
//		double completed = goalCondition.satisfies(finalState) ? 1.0 : 0.0;
//		
//		double[] results = {bellmanUpdates, totalReward, completed, totalPlanningTime};
//		return results;
//	}
//
//}
