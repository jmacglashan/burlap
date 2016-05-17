package burlap.behavior.stochasticgames.agents;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGAgent;
import burlap.mdp.stochasticgames.agentactions.SGActionUtils;
import burlap.mdp.stochasticgames.agentactions.SGAgentAction;

import java.util.List;
import java.util.Map;


/**
 * Stochastic games agent that chooses actions uniformly randomly.
 * @author James MacGlashan
 *
 */
public class RandomSGAgent extends SGAgent {

	@Override
	public void gameStarting() {
		//do nothing

	}

	@Override
	public SGAgentAction getAction(State s) {
		
		List<SGAgentAction> gsas = SGActionUtils.allApplicableActionsForTypes(this.agentType.actions, this.worldAgentName, s);
		
		int r = RandomFactory.getMapped(0).nextInt(gsas.size());
		SGAgentAction gsa = gsas.get(r);
		
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
