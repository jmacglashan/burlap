package burlap.behavior.stochasticgame.agents;

import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;

/**
 * A class for an agent who makes decisions by following a specified strategy and does not respond to the other player's actions.
 * The policy object that determines actions can leave the actingAgent field empty/null, because this the {@link #getAction(State)} method
 * will automatically replace it with whatever this agent's name is.
 * @author James MacGlashan
 *
 */
public class SetStrategyAgent extends Agent {

	/**
	 * The policy encoding the strategy this agent will follow
	 */
	protected Policy		policy;
	
	
	/**
	 * Initializes for the given domain in which the agent will play and the strategy that they will follow.
	 * @param domain the domain in which the agent will play
	 * @param policy the strategy that the agent will follow
	 */
	public SetStrategyAgent(SGDomain domain, Policy policy){
		this.init(domain);
		this.policy = policy;
	}
	
	@Override
	public void gameStarting() {
	}

	@Override
	public GroundedSingleAction getAction(State s) {
		GroundedSingleAction actSelection = (GroundedSingleAction)this.policy.getAction(s);
		actSelection.actingAgent = this.worldAgentName;
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
		public Agent generateAgent() {
			return new SetStrategyAgent(domain, policy);
		}
		
		
		
	}

}
