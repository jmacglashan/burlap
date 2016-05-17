package burlap.testing;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.deterministic.uninformed.dfs.DFS;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridLocation;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.auxiliary.common.SinglePFTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.SimpleHashableStateFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static burlap.domain.singleagent.gridworld.GridWorldDomain.PF_AT_LOCATION;

public class TestPlanning {
	public static final double delta = 0.000001;
	GridWorldDomain gw;
	OOSADomain domain;
	StateConditionTest goalCondition;
	SimpleHashableStateFactory hashingFactory;
	@Before
	public void setup() {
		this.gw = new GridWorldDomain(11, 11);
		this.gw.setMapToFourRooms();
		this.gw.setRf(new UniformCostRF());
		TerminalFunction tf = new SinglePFTF(PropositionalFunction.getPropositionalFunction(gw.generatePfs(), PF_AT_LOCATION));
		this.gw.setTf(tf);
		this.domain = this.gw.generateDomain();
		this.goalCondition = new TFGoalCondition(tf);
		this.hashingFactory = new SimpleHashableStateFactory();
	}
	
	@Test
	public void testBFS() {
		GridWorldState initialState = new GridWorldState(new GridAgent(0, 0), new GridLocation(10, 10, 0, "loc0"));

		DeterministicPlanner planner = new BFS(this.domain, this.goalCondition, this.hashingFactory);
		planner.planFromState(initialState);
		Policy p = new SDPlannerPolicy(planner);
		EpisodeAnalysis analysis = p.evaluateBehavior(initialState, domain.getModel());
		this.evaluateEpisode(analysis, true);
	}
	
	@Test
	public void testDFS() {
		GridWorldState initialState = new GridWorldState(new GridAgent(0, 0), new GridLocation(10, 10, 0, "loc0"));
		
		DeterministicPlanner planner = new DFS(this.domain, this.goalCondition, this.hashingFactory, -1 , true);
		planner.planFromState(initialState);
		Policy p = new SDPlannerPolicy(planner);
		EpisodeAnalysis analysis = p.evaluateBehavior(initialState, domain.getModel());
		this.evaluateEpisode(analysis);
	}
	
	@Test
	public void testAStar() {
		GridWorldState initialState = new GridWorldState(new GridAgent(0, 0), new GridLocation(10, 10, 0, "loc0"));
		
		Heuristic mdistHeuristic = new Heuristic() {
			
			@Override
			public double h(State s) {

				GridAgent agent = ((GridWorldState)s).agent;
				GridLocation location = ((GridWorldState)s).locations.get(0);

				//get agent position
				int ax = agent.x;
				int ay = agent.y;
				
				//get location position
				int lx = location.x;
				int ly = location.y;
				
				//compute Manhattan distance
				double mdist = Math.abs(ax-lx) + Math.abs(ay-ly);
				
				return -mdist;
			}
		};
		
		//provide A* the heuristic as well as the reward function so that it can keep
		//track of the actual cost
		DeterministicPlanner planner = new AStar(domain, goalCondition,
			hashingFactory, mdistHeuristic);
		planner.planFromState(initialState);
		Policy p = new SDPlannerPolicy(planner);
		
		EpisodeAnalysis analysis = p.evaluateBehavior(initialState, domain.getModel());
		this.evaluateEpisode(analysis, true);
	}
	
	public void evaluateEpisode(EpisodeAnalysis analysis) {
		this.evaluateEpisode(analysis, false);
	}
	
	public void evaluateEpisode(EpisodeAnalysis analysis, Boolean expectOptimal) {
		if (expectOptimal) {
			Assert.assertEquals(this.gw.getHeight() + this.gw.getWidth() - 1, analysis.stateSequence.size());
			Assert.assertEquals(analysis.stateSequence.size()-1, analysis.actionSequence.size());
			Assert.assertEquals(analysis.actionSequence.size(), analysis.rewardSequence.size());
			Assert.assertEquals(-analysis.actionSequence.size(), analysis.getDiscountedReturn(1.0), TestPlanning.delta);
		}

		Assert.assertEquals(true, domain.getModel().terminal(analysis.stateSequence.get(analysis.stateSequence.size()-1)));
		Assert.assertEquals(true, this.goalCondition.satisfies(analysis.stateSequence.get(analysis.stateSequence.size()-1)));
	}
	
	@After
	public void teardown() {
		
	}
}
