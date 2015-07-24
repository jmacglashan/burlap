package burlap.testing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.deterministic.uninformed.dfs.DFS;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldStateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;

public class TestPlanning {
	public static final double delta = 0.000001;
	GridWorldDomain gw;
	Domain domain;
	GridWorldStateParser parser;
	RewardFunction rf;
	TerminalFunction tf;
	StateConditionTest goalCondition;
	DiscreteStateHashFactory hashingFactory;
	@Before
	public void setup() {
		this.gw = new GridWorldDomain(11, 11);
		this.gw.setMapToFourRooms(); 
		this.domain = this.gw.generateDomain();
		this.rf = new UniformCostRF();
		this.tf = new SinglePFTF(this.domain.getPropFunction(GridWorldDomain.PFATLOCATION));
		this.goalCondition = new TFGoalCondition(this.tf);
		this.hashingFactory = new DiscreteStateHashFactory();
		this.hashingFactory.setAttributesForClass(GridWorldDomain.CLASSAGENT,
				this.domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList);
	}
	
	@Test
	public void testBFS() {
		State initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 10, 10);
		
		DeterministicPlanner planner = new BFS(this.domain, this.goalCondition, this.hashingFactory);
		planner.planFromState(initialState);
		Policy p = new SDPlannerPolicy(planner);
		EpisodeAnalysis analysis = p.evaluateBehavior(initialState, this.rf, this.tf);
		this.evaluateEpisode(analysis, true);
	}
	
	@Test
	public void testDFS() {
		State initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 10, 10);
		
		DeterministicPlanner planner = new DFS(this.domain, this.goalCondition, this.hashingFactory, -1 , true);
		planner.planFromState(initialState);
		Policy p = new SDPlannerPolicy(planner);
		EpisodeAnalysis analysis = p.evaluateBehavior(initialState, this.rf, this.tf);
		this.evaluateEpisode(analysis);
	}
	
	@Test
	public void testAStar() {
		State initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 10, 10);
		
		Heuristic mdistHeuristic = new Heuristic() {
			
			@Override
			public double h(State s) {
				
				String an = GridWorldDomain.CLASSAGENT;
				String ln = GridWorldDomain.CLASSLOCATION;

				ObjectInstance agent = s.getObjectsOfClass(an).get(0); 
				ObjectInstance location = s.getObjectsOfClass(ln).get(0); 

				
				//get agent position
				int ax = agent.getIntValForAttribute(GridWorldDomain.ATTX);
				int ay = agent.getIntValForAttribute(GridWorldDomain.ATTY);
				
				//get location position
				int lx = location.getIntValForAttribute(GridWorldDomain.ATTX);
				int ly = location.getIntValForAttribute(GridWorldDomain.ATTY);
				
				//compute Manhattan distance
				double mdist = Math.abs(ax-lx) + Math.abs(ay-ly);
				
				return -mdist;
			}
		};
		
		//provide A* the heuristic as well as the reward function so that it can keep
		//track of the actual cost
		DeterministicPlanner planner = new AStar(domain, rf, goalCondition, 
			hashingFactory, mdistHeuristic);
		planner.planFromState(initialState);
		Policy p = new SDPlannerPolicy(planner);
		
		EpisodeAnalysis analysis = p.evaluateBehavior(initialState, this.rf, this.tf);
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

		Assert.assertEquals(true, this.tf.isTerminal(analysis.stateSequence.get(analysis.stateSequence.size()-1)));
		Assert.assertEquals(true, this.goalCondition.satisfies(analysis.stateSequence.get(analysis.stateSequence.size()-1)));
	}
	
	@After
	public void teardown() {
		
	}
}
