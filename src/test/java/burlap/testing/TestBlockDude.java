package burlap.testing;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.informed.NullHeuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.domain.singleagent.blockdude.BlockDude;
import burlap.domain.singleagent.blockdude.BlockDudeLevelConstructor;
import burlap.domain.singleagent.blockdude.BlockDudeTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestBlockDude {
		SADomain domain;
		BlockDude constructor;
		
		@Before
		public void setup() {
			constructor = new BlockDude();
			domain = constructor.generateDomain();
		}
	
		@After
		public void teardown() {
			this.domain = null;
			this.constructor = null;
		}
		
		public State generateState() {
			return BlockDudeLevelConstructor.getLevel3(domain);
		}

		@Test
		public void testDude() {
			State s = this.generateState();
			this.testDude(s);
		}
		
		public void testDude(State s) {
			TerminalFunction tf = new BlockDudeTF();
			StateConditionTest sc = new TFGoalCondition(tf);

			AStar astar = new AStar(domain, sc, new SimpleHashableStateFactory(), new NullHeuristic());
			astar.toggleDebugPrinting(false);
			astar.planFromState(s);

			Policy p = new SDPlannerPolicy(astar);
			Episode ea = PolicyUtils.rollout(p, s, domain.getModel(), 100);

			State lastState = ea.stateSequence.get(ea.stateSequence.size() - 1);
			Assert.assertEquals(true, tf.isTerminal(lastState));
			Assert.assertEquals(true, sc.satisfies(lastState));
			Assert.assertEquals(-94.0, ea.discountedReturn(1.0), 0.001);

			/*
			BlockDude constructor = new BlockDude();
			Domain d = constructor.generateDomain();

			List<Integer> px = new ArrayList<Integer>();
			List <Integer> ph = new ArrayList<Integer>();

			ph.add(15);
			ph.add(3);
			ph.add(3);
			ph.add(3);
			ph.add(0);
			ph.add(0);
			ph.add(0);
			ph.add(1);
			ph.add(2);
			ph.add(0);
			ph.add(2);
			ph.add(3);
			ph.add(2);
			ph.add(2);
			ph.add(3);
			ph.add(3);
			ph.add(15);
			
			State o = BlockDude.getCleanState(d, px, ph, 6);
			o = BlockDude.setAgent(o, 9, 3, 1, 0);
			o = BlockDude.setExit(o, 1, 0);
			
			o = BlockDude.setBlock(o, 0, 5, 1);
			o = BlockDude.setBlock(o, 1, 6, 1);
			o = BlockDude.setBlock(o, 2, 14, 3);
			o = BlockDude.setBlock(o, 3, 16, 4);
			o = BlockDude.setBlock(o, 4, 17, 4);
			o = BlockDude.setBlock(o, 5, 17, 5);
			
			TerminalFunction tf = new SinglePFTF(d.getPropFunction(BlockDude.PFATEXIT));
			StateConditionTest sc = new SinglePFSCT(d.getPropFunction(BlockDude.PFATEXIT));

			RewardFunction rf = new UniformCostRF();

			AStar astar = new AStar(d, rf, sc, new DiscreteStateHashFactory(), new NullHeuristic());
			astar.toggleDebugPrinting(false);
			astar.planFromState(o);

			Policy p = new SDPlannerPolicy(astar);
			EpisodeAnalysis ea = p.evaluateBehavior(o, rf, tf, 100);

			State lastState = ea.stateSequence.get(ea.stateSequence.size() - 1);
			Assert.assertEquals(true, tf.isTerminal(lastState));
			Assert.assertEquals(true, sc.satisfies(lastState));
			Assert.assertEquals(-94.0, ea.getDiscountedReturn(1.0), 0.001);
			*/
		}
}
