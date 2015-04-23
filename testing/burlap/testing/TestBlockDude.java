package burlap.testing;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.SinglePFSCT;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.NullHeuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.debugtools.MyTimer;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.auxiliary.common.StateJSONParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.visualizer.Visualizer;
import burlap.testing.Domain.BlockDude;

public class TestBlockDude {

		@Test
		public void testDude() {

			int maxx = 18;
			int maxy = 18;
			
			BlockDude constructor = new BlockDude(maxx, maxy);
			Domain d = constructor.generateDomain();
			
			List <Integer> px = new ArrayList<Integer>();
			List <Integer> ph = new ArrayList<Integer>();
			
			px.add(0);
			px.add(2);
			px.add(3);
			px.add(4);
			px.add(5);
			px.add(6);
			px.add(7);
			px.add(8);
			px.add(9);
			px.add(11);
			px.add(12);
			px.add(13);
			px.add(14);
			px.add(15);
			px.add(16);
			px.add(17);
			px.add(18);

			
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
			
			State s = BlockDude.getCleanState(d, px, ph, 6);
			BlockDude.setAgent(s, 9, 3, 1, 0);
			BlockDude.setExit(s, 1, 0);
			
			BlockDude.setBlock(s, 0, 5, 1);
			BlockDude.setBlock(s, 1, 6, 1);
			BlockDude.setBlock(s, 2, 14, 3);
			BlockDude.setBlock(s, 3, 16, 4);
			BlockDude.setBlock(s, 4, 17, 4);
			BlockDude.setBlock(s, 5, 17, 5);
			
			TerminalFunction tf = new SinglePFTF(d.getPropFunction(BlockDude.PFATEXIT));
			StateConditionTest sc = new SinglePFSCT(d.getPropFunction(BlockDude.PFATEXIT));
			RewardFunction rf = new UniformCostRF();
			Heuristic h = new NullHeuristic();
			
			//VisualActionObserver obs = new VisualActionObserver(d, v);
			//obs.initGUI();
			//((SADomain)d).addActionObserverForAllAction(obs);
			
			AStar astar = new AStar(d, rf, sc, new DiscreteStateHashFactory(), h);
			astar.toggleDebugPrinting(false);
			astar.planFromState(s);
			
			//StateParser sp = new StateYAMLParser(d);
			StateParser sp = new StateJSONParser(d);
			Policy p = new SDPlannerPolicy(astar);
			EpisodeAnalysis ea = p.evaluateBehavior(s, rf, tf);
			
			State lastState = ea.stateSequence.get(ea.stateSequence.size() - 1);
			Assert.assertEquals(true, tf.isTerminal(lastState));
			Assert.assertEquals(true, sc.satisfies(lastState));
			Assert.assertEquals(-94.0, ea.getDiscountedReturn(1.0), 0.001);
		}
}
