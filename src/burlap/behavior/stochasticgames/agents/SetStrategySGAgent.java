package burlap.behavior.stochasticgames.agents;

import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;

/**
 * A class for an agent who makes decisions by following a specified strategy and does not respond to the other player's actions.
 * The policy object that determines actions can leave the actingAgent field empty/null, because this the {@link #getAction(State)} method
 * will automatically rpelace it with whatever this agent's name is. 
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
	public GroundedSGAgentAction getAction(State s) {
		GroundedSGAgentAction actSelection = (GroundedSGAgentAction)this.policy.getAction(s);
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
		public SGAgent generateAgent() {
			return new SetStrategySGAgent(domain, policy);
		}
		
		
		
	}

}
