package burlap.behavior.stochasticgame.agents;

import java.util.Map;

import burlap.behavior.stochasticgame.Strategy;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;

/**
 * A class for an agent who makes decisions by following a specified strategy and does not respond to the other player's actions.
 * @author James MacGlashan
 *
 */
public class SetStrategyAgent extends Agent {

	/**
	 * The strategy this agent will follow
	 */
	protected Strategy		strategy;
	
	
	/**
	 * Initializes for the given domain in which the agent will play and the strategy that they will follow.
	 * @param domain the domain in which the agent will play
	 * @param strategy the strategy that the agent will follow
	 */
	public SetStrategyAgent(SGDomain domain, Strategy strategy){
		this.init(domain);
		this.strategy = strategy;
	}
	
	@Override
	public void gameStarting() {
	}

	@Override
	public GroundedSingleAction getAction(State s) {
		return this.strategy.getAction(s);
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
		protected Strategy		strategy;
		
		/**
		 * The domain in which the agent will play
		 */
		protected SGDomain		domain;
		
		public SetStrategyAgentFactory(SGDomain domain, Strategy strategy){
			this.strategy = strategy;
			this.domain = domain;
		}
		
		
		@Override
		public Agent generateAgent() {
			return new SetStrategyAgent(domain, strategy);
		}
		
		
		
	}

}
