package burlap.behavior.stochasticgames.agents;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.core.action.ActionUtils;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgentBase;
import burlap.mdp.stochasticgames.world.World;

import java.util.List;


/**
 * Stochastic games agent that chooses actions uniformly randomly.
 * @author James MacGlashan
 *
 */
public class RandomSGAgent extends SGAgentBase {

	@Override
	public void gameStarting(World w, int agentNum) {
		//do nothing

	}

	@Override
	public Action action(State s) {
		
		List<Action> gsas = ActionUtils.allApplicableActionsForTypes(this.agentType.actions, s);

		
		int r = RandomFactory.getMapped(0).nextInt(gsas.size());
		Action gsa = gsas.get(r);
		
		return gsa;
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			double[] jointReward, State sprime, boolean isTerminal) {
		//do nothing

	}

	@Override
	public void gameTerminated() {
		//do nothing
	}

}
