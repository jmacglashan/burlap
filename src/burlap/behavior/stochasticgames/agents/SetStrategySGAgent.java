package burlap.behavior.stochasticgames.agents;

import burlap.behavior.policy.Policy;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.agent.AgentFactory;
import burlap.mdp.stochasticgames.action.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.action.SGAgentAction;

import java.util.Map;

/**
 * A class for an agent who makes decisions by following a specified strategy and does not respond to the other player's actions.
 * The policy object that determines actions can leave the actingAgent field empty/null, because this the {@link #getAction(State)} method
 * will automatically replace it with whatever this agent's name is.
 * @author James MacGlashan
 *
 */
public class SetStrategySGAgent extends SGAgent {

	/**
	 * The policy encoding the strategy this agent will follow
	 */
	protected Policy		policy;
	
	
	/**
	 * Initializes for the given domain in which the agent will play and the strategy that they will follow.
	 * @param domain the domain in which the agent will play
	 * @param policy the strategy that the agent will follow
	 */
	public SetStrategySGAgent(SGDomain domain, Policy policy){
		this.init(domain);
		this.policy = policy;
	}
	
	@Override
	public void gameStarting() {
	}

	@Override
	public SGAgentAction getAction(State s) {
		SGAgentAction actSelection = (SGAgentAction)this.policy.getAction(s);
		actSelection.setActingAgent(this.worldAgentName);
		return actSelection;
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {
	}

	@Override
	public void gameTerminated() {
	}
	
	
	
	public static class SetStrategyAgentFactory implements AgentFactory{

		/**
		 * The strategy this agent will follow
		 */
		protected Policy		policy;
		
		/**
		 * The domain in which the agent will play
		 */
		protected SGDomain		domain;
		
		public SetStrategyAgentFactory(SGDomain domain, Policy policy){
			this.policy = policy;
			this.domain = domain;
		}
		
		
		@Override
		public SGAgent generateAgent() {
			return new SetStrategySGAgent(domain, policy);
		}
		
		
		
	}

}
