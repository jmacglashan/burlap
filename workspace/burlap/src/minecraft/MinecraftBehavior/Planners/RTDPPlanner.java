//package minecraft.MinecraftBehavior.Planners;
//
//import burlap.behavior.singleagent.EpisodeAnalysis;
//import burlap.behavior.singleagent.Policy;
//import burlap.behavior.singleagent.planning.OOMDPPlanner;
//import burlap.behavior.singleagent.planning.QComputablePlanner;
//import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
//import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
//import burlap.oomdp.core.State;
//import minecraft.MinecraftBehavior.MinecraftBehavior;
//
//public class RTDPPlanner extends MinecraftPlanner{
//	private int vInit;
//	private int numRollouts;
//	private double minDelta;
//	private int maxDepth;
//	private int numRolloutsWithSmallChangeToConverge;
//	private int maxSteps;
//
//	/**
//	 * 
//	 * @param mcBeh
//	 * @param addOptions
//	 * @param addMacroActions
//	 */
//	public RTDPPlanner(MinecraftBehavior mcBeh, boolean addOptions,
//			boolean addMacroActions) {
//		super(mcBeh, addOptions, addMacroActions);
//		this.vInit = mcBeh.vInit;
//		this.numRollouts = mcBeh.numRollouts;
//		this.minDelta = mcBeh.minDelta;
//		this.maxDepth = mcBeh.maxDepth;
//		this.maxSteps = mcBeh.maxSteps;
//		this.numRolloutsWithSmallChangeToConverge = mcBeh.numRolloutsWithSmallChangeToConverge;
//	}
//
//	@Override
//	protected OOMDPPlanner getPlanner() {
//		RTDP planner = new RTDP(domain, this.rf, this.tf, this.gamma, this.hashingFactory,
//				this.vInit, this.numRollouts, this.minDelta, this.maxDepth);
//		return planner;
//	}
//
//	@Override
//	protected double[] runPlannerHelper(OOMDPPlanner planner) {
//		RTDP rPlanner = (RTDP) planner;
//		rPlanner.setMinNumRolloutsWithSmallValueChange(this.numRolloutsWithSmallChangeToConverge);
//		
//		long startTime = System.currentTimeMillis( );
//		
//		int bellmanUpdates = rPlanner.planFromStateAndCount(initialState);
//		// Create a Q-greedy policy from the planner
//		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
//		EpisodeAnalysis ea = p.evaluateBehavior(initialState, this.rf, this.tf, maxSteps);
//		
//		// Compute CPU time
//		long totalPlanningTime  = System.currentTimeMillis( ) - startTime;
//		
//		// Count reward
//		double totalReward = 0.;
//		for(Double d : ea.rewardSequence){
//			totalReward = totalReward + d;
//		}
//		
//		// Check if task completed
//		State finalState = ea.getState(ea.stateSequence.size() - 1);
//		double completed = this.tf.isTerminal(finalState) ? 1.0 : 0.0;
//		
//		System.out.println(ea.getActionSequenceString());
//
//		double[] results = {bellmanUpdates, totalReward, completed, totalPlanningTime};
//
//		return results;
//	}
//	
//	
//
//	
//
//}
