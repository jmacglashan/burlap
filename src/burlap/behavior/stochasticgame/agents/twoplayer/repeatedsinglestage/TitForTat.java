package burlap.behavior.stochasticgame.agents.twoplayer.repeatedsinglestage;

import java.util.Map;

import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * A class for an agent that plays tit-for-tat. The agent starts by playing a "cooperate" action. If their opponent "defects" on them, then
 * this agent plays their "defect" action in the next turn. If their opponent "cooperates" again, then this agent will cooperate in the next turn.
 * The corresponding "cooperate" and "defect" actions for each agent need to be specified. When a new game starts, this agent will reset
 * to trying cooperate.
 * @author James MacGlashan
 *
 */
public class TitForTat extends Agent {

	/**
	 * This agent's cooperate action
	 */
	protected SingleAction myCoop;
	
	/**
	 * This agent's defect action
	 */
	protected SingleAction myDefect;
	
	/**
	 * The opponent's cooperate action
	 */
	protected SingleAction opponentCoop;
	
	/**
	 * The opponent's defect action
	 */
	protected SingleAction opponentDefect;
	
	
	
	/**
	 * The last opponent's move
	 */
	protected SingleAction lastOpponentMove;
	
	
	/**
	 * Initializes with the specified cooperate and defect actions for both players.
	 * @param domain the domain in which this agent will play.
	 * @param coop the cooperate action for both players
	 * @param defect the defect action for both players
	 */
	public TitForTat(SGDomain domain, SingleAction coop, SingleAction defect){
		this.init(domain);
		this.myCoop = coop;
		this.myDefect = defect;
		this.opponentCoop = coop;
		this.opponentDefect = defect;
		
		this.lastOpponentMove = opponentCoop;
	}
	
	/**
	 * Initializes with differently specified cooperate and defect actions for both players.
	 * @param domain the domain in which this agent will play
	 * @param myCoop this agent's cooperate action
	 * @param myDefect this agent's defect action
	 * @param opponentCoop the opponent's cooperate action
	 * @param opponentDefect the opponent's defect action
	 */
	public TitForTat(SGDomain domain, SingleAction myCoop, SingleAction myDefect, SingleAction opponentCoop, SingleAction opponentDefect){
		this.init(domain);
		this.myCoop = myCoop;
		this.myDefect = myDefect;
		this.opponentCoop = opponentCoop;
		this.opponentDefect = opponentDefect;
		
		this.lastOpponentMove = opponentCoop;
	}
	
	@Override
	public void gameStarting() {
		this.lastOpponentMove = opponentCoop;
	}

	@Override
	public GroundedSingleAction getAction(State s) {
		if(lastOpponentMove.actionName.equals(opponentCoop.actionName)){
			return new GroundedSingleAction(this.worldAgentName, myCoop, "");
		}
		return new GroundedSingleAction(this.worldAgentName, myDefect, "");
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		for(GroundedSingleAction gsa : jointAction){
			if(!gsa.actingAgent.equals(this.worldAgentName)){
				this.lastOpponentMove = gsa.action;
			}
		}

	}

	@Override
	public void gameTerminated() {

	}
	
	
	
	/**
	 * An agent factory for a TitForTat player.
	 * @author James MacGlashan
	 *
	 */
	public static class TitForTatAgentFactory implements AgentFactory{

		/**
		 * This agent's cooperate action
		 */
		protected SingleAction myCoop;
		
		/**
		 * This agent's defect action
		 */
		protected SingleAction myDefect;
		
		/**
		 * The opponent's cooperate action
		 */
		protected SingleAction opponentCoop;
		
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
		public TitForTatAgentFactory(SGDomain domain, SingleAction coop, SingleAction defect){
			this.domain = domain;
			this.myCoop = coop;
			this.myDefect = defect;
			this.opponentCoop = coop;
			this.opponentDefect = defect;
			
		}
		
		/**
		 * Initializes with differently specified cooperate and defect actions for both players.
		 * @param domain the domain in which this agent will play
		 * @param myCoop the agent's cooperate action
		 * @param myDefect the agent's defect action
		 * @param opponentCoop the opponent's cooperate action
		 * @param opponentDefect the opponent's defect action
		 */
		public TitForTatAgentFactory(SGDomain domain, SingleAction myCoop, SingleAction myDefect, SingleAction opponentCoop, SingleAction opponentDefect){
			this.domain = domain;
			this.myCoop = myCoop;
			this.myDefect = myDefect;
			this.opponentCoop = opponentCoop;
			this.opponentDefect = opponentDefect;
			
		}
		
		@Override
		public Agent generateAgent() {
			return new TitForTat(domain, myCoop, myDefect, opponentCoop, opponentDefect);
		}
		
		
		
	}

}
