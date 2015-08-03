package burlap.testing;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.policy.Policy;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.behavior.singleagent.planning.deterministic.informed.NullHeuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.domain.singleagent.blockdude.BlockDude;
import burlap.domain.singleagent.blockdude.BlockDudeLevelConstructor;
import burlap.domain.singleagent.blockdude.BlockDudeTF;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import org.junit.Assert;
import org.junit.Test;

public class TestBlockDude {

		@Test
		public void testDude() {


			BlockDude constructor = new BlockDude();
			Domain d = constructor.generateDomain();

			State s = BlockDudeLevelConstructor.getLevel3(d);
			TerminalFunction tf = new BlockDudeTF();
			RewardFunction rf = new UniformCostRF();
			StateConditionTest sc = new TFGoalCondition(tf);

			AStar astar = new AStar(d, rf, sc, new SimpleHashableStateFactory(), new NullHeuristic());
			astar.toggleDebugPrinting(false);
			astar.planFromState(s);

			Policy p = new SDPlannerPolicy(astar);
			EpisodeAnalysis ea = p.evaluateBehavior(s, rf, tf, 100);

			State lastState = ea.stateSequence.get(ea.stateSequence.size() - 1);
			Assert.assertEquals(true, tf.isTerminal(lastState));
			Assert.assertEquals(true, sc.satisfies(lastState));
			Assert.assertEquals(-94.0, ea.getDiscountedReturn(1.0), 0.001);

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
