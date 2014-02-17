package burlap.behavior.stochasticgame.agents.twoplayer.repeatedsinglestage;

import java.util.Map;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * A class for an agent that plays grim trigger. The agent starts by following a "cooperate" action. If at any point their opponent plays
 * a "defect" action, then this agent will play their "defect" action for the rest of the repeated game (until the {@link #gameStarting()} method is called again).
 * @author James MacGlashan
 *
 */
public class GrimTrigger extends Agent {

	/**
	 * This agent's cooperate action
	 */
	protected SingleAction myCoop;
	
	/**
	 * This agent's defect action
	 */
	protected SingleAction myDefect;
	
	/**
	 * The opponent's defect action
	 */
	protected SingleAction opponentDefect;
	
	
	/**
	 * Whether this agent will play its defect action or not.
	 */
	protected boolean grimTrigger = false;
	
	
	/**
	 * Initializes with the specified cooperate and defect actions for both players.
	 * @param domain the domain in which this agent will play.
	 * @param coop the cooperate action for both players
	 * @param defect the defect action for both players
	 */
	public GrimTrigger(SGDomain domain, SingleAction coop, SingleAction defect){
		this.init(domain);
		this.myCoop = coop;
		this.myDefect = defect;
		this.opponentDefect = defect;
	}
	
	
	/**
	 * Initializes with differently specified cooperate and defect actions for both players.
	 * @param domain the domain in which this agent will play
	 * @param myCoop this agent's cooperate action
	 * @param myDefect this agent's defect action
	 * @param opponentDefect the opponent's defect action
	 */
	public GrimTrigger(SGDomain domain, SingleAction myCoop, SingleAction myDefect, SingleAction opponentDefect){
		this.init(domain);
		this.myCoop = myCoop;
		this.myDefect = myDefect;
		this.opponentDefect = opponentDefect;
	}
	
	@Override
	public void gameStarting() {
		grimTrigger = false;
	}

	@Override
	public GroundedSingleAction getAction(State s) {
		if(this.grimTrigger){
			return new GroundedSingleAction(this.worldAgentName, myDefect, "");
		}
		return new GroundedSingleAction(this.worldAgentName, myCoop, "");
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		for(GroundedSingleAction gsa : jointAction){
			if(!gsa.actingAgent.equals(this.worldAgentName)){
				if(this.opponentDefect.actionName.equals(gsa.action.actionName)){
					grimTrigger = true;
				}
			}
		}

	}

	@Override
	public void gameTerminated() {
	}

	
	
	/**
	 * An agent factory for GrimTrigger
	 * @author James MacGlashan
	 *
	 */
	public static class GrimTriggerAgentFactory implements AgentFactory{

		/**
		 * The agent's cooperate action
		 */
		protected SingleAction myCoop;
		
		/**
		 * The agent's defect action
		 */
		protected SingleAction myDefect;
		
		/**
		 * The opponent's defect action
		 */
		protected SingleAction opponentDefect;
		
		/**
		 * The domain in which the agent will play
		 */
		protected SGDomain domain;
		
		
		/**
		 * Initializes with the specified cooperate and defect actions for both players.
		 * @param domain the domain in which this agent will play.
		 * @param coop the cooperate action for both players
		 * @param defect the defect action for both players
		 */
		public GrimTriggerAgentFactory(SGDomain domain, SingleAction coop, SingleAction defect){
			this.domain = domain;
			this.myCoop = coop;
			this.myDefect = defect;
			this.opponentDefect = defect;
		}
		
		
		/**
		 * Initializes with differently specified cooperate and defect actions for both players.
		 * @param domain the domain in which this agent will play
		 * @param myCoop the agent's cooperate action
		 * @param myDefect the agent's defect action
		 * @param opponentDefect the opponent's defect action
		 */
		public GrimTriggerAgentFactory(SGDomain domain, SingleAction myCoop, SingleAction myDefect, SingleAction opponentDefect){
			this.domain = domain;
			this.myCoop = myCoop;
			this.myDefect = myDefect;
			this.opponentDefect = opponentDefect;
		}
		
		@Override
		public Agent generateAgent() {
			return new GrimTrigger(domain, myCoop, myDefect, opponentDefect);
		}
		
		
		
	}
	
}
