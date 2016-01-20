package minecraft.MinecraftBehavior.Planners;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.oomdp.core.State;
import minecraft.MinecraftBehavior.MinecraftBehavior;

public class BFSPlanner extends MinecraftPlanner{

	/**
	 * 
	 * @param mcBeh
	 * @param addOptions
	 * @param addMacroActions
	 */
	public BFSPlanner(MinecraftBehavior mcBeh, boolean addOptions,
			boolean addMacroActions) {
		super(mcBeh, addOptions, addMacroActions);
	}

	@Override
	protected OOMDPPlanner getPlanner() {
		TFGoalCondition goalCondition = new TFGoalCondition(this.tf);
		return new BFS(this.domain, goalCondition, this.hashingFactory);

	}

	@Override
	protected double[] runPlannerHelper(OOMDPPlanner iPlanner) {
		DeterministicPlanner planner = (DeterministicPlanner) iPlanner;
		
		long startTime = System.currentTimeMillis( );

		planner.planFromState(initialState);
		
		Policy p = new SDPlannerPolicy(planner);
		
		p.evaluateBehavior(initialState, this.rf, this.tf);
		
		EpisodeAnalysis ea = p.evaluateBehavior(initialState, this.rf, this.tf);
		System.out.println(ea.getActionSequenceString());
		
		// Compute CPU time
		long totalPlanningTime  = System.currentTimeMillis( ) - startTime;
		
		// Count reward
		double totalReward = 0.;
		for(Double d : ea.rewardSequence){
			totalReward = totalReward + d;
		}
		
		// Check if task completed
		State finalState = ea.getState(ea.stateSequence.size() - 1);
		double completed = this.tf.isTerminal(finalState) ? 1.0 : 0.0;
		
		double[] results = {0, totalReward, completed, totalPlanningTime};

		return results;
	}

	
}
