package burlap.behavior.stochasticgame.agents;

import java.util.List;
import java.util.Map;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * Stochastic games agent that chooses actions uniformly randomly.
 * @author James MacGlashan
 *
 */
public class RandomAgent extends Agent {

	@Override
	public void gameStarting() {
		//do nothing

	}

	@Override
	public GroundedSingleAction getAction(State s) {
		
		List<GroundedSingleAction> gsas = SingleAction.getAllPossibleGroundedSingleActions(s, this.worldAgentName, this.agentType.actions);
		
		int r = RandomFactory.getMapped(0).nextInt(gsas.size());
		GroundedSingleAction gsa = gsas.get(r);
		return gsa;
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		//do nothing

	}

	@Override
	public void gameTerminated() {
		//do nothing
	}

}
