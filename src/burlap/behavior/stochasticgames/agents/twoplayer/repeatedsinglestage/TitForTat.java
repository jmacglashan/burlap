package burlap.behavior.stochasticgames.agents.twoplayer.repeatedsinglestage;

import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.agent.AgentFactory;
import burlap.mdp.stochasticgames.action.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.action.SGAgentAction;
import burlap.mdp.stochasticgames.action.SGAgentActionType;

import java.util.Map;


/**
 * A class for an agent that plays tit-for-tat. The agent starts by playing a "cooperate" action. If their opponent "defects" on them, then
 * this agent plays their "defect" action in the next turn. If their opponent "cooperates" again, then this agent will cooperate in the next turn.
 * The corresponding "cooperate" and "defect" actions for each agent need to be specified. When a new game starts, this agent will reset
 * to trying cooperate.
 * @author James MacGlashan
 *
 */
public class TitForTat extends SGAgent {

	/**
	 * This agent's cooperate action
	 */
	protected SGAgentActionType myCoop;
	
	/**
	 * This agent's defect action
	 */
	protected SGAgentActionType myDefect;
	
	/**
	 * The opponent's cooperate action
	 */
	protected SGAgentActionType opponentCoop;
	
	/**
	 * The opponent's defect action
	 */
	protected SGAgentActionType opponentDefect;
	
	
	
	/**
	 * The last opponent's move
	 */
	protected SGAgentAction lastOpponentMove;
	
	
	/**
	 * Initializes with the specified cooperate and defect actions for both players.
	 * @param domain the domain in which this agent will play.
	 * @param coop the cooperate action for both players
	 * @param defect the defect action for both players
	 */
	public TitForTat(SGDomain domain, SGAgentActionType coop, SGAgentActionType defect){
		this.init(domain);
		this.myCoop = coop;
		this.myDefect = defect;
		this.opponentCoop = coop;
		this.opponentDefect = defect;
		
		this.lastOpponentMove = opponentCoop.associatedAction("", "");
	}
	
	/**
	 * Initializes with differently specified cooperate and defect actions for both players.
	 * @param domain the domain in which this agent will play
	 * @param myCoop this agent's cooperate action
	 * @param myDefect this agent's defect action
	 * @param opponentCoop the opponent's cooperate action
	 * @param opponentDefect the opponent's defect action
	 */
	public TitForTat(SGDomain domain, SGAgentActionType myCoop, SGAgentActionType myDefect, SGAgentActionType opponentCoop, SGAgentActionType opponentDefect){
		this.init(domain);
		this.myCoop = myCoop;
		this.myDefect = myDefect;
		this.opponentCoop = opponentCoop;
		this.opponentDefect = opponentDefect;

		this.lastOpponentMove = opponentCoop.associatedAction("", "");
	}
	
	@Override
	public void gameStarting() {
		this.lastOpponentMove = opponentCoop.associatedAction("","");
	}

	@Override
	public SGAgentAction getAction(State s) {
		if(lastOpponentMove.actionName().equals(opponentCoop.typeName())){
			return myCoop.associatedAction(this.worldAgentName, "");
		}
		return myDefect.associatedAction(this.worldAgentName, "");
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		for(SGAgentAction gsa : jointAction){
			if(!gsa.actingAgent().equals(this.worldAgentName)){
				this.lastOpponentMove = gsa;
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
		protected SGAgentActionType myCoop;
		
		/**
		 * This agent's defect action
		 */
		protected SGAgentActionType myDefect;
		
		/**
		 * The opponent's cooperate action
		 */
		protected SGAgentActionType opponentCoop;
		
		/**
		 * The opponent's defect action
		 */
		protected SGAgentActionType opponentDefect;
		
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
		public TitForTatAgentFactory(SGDomain domain, SGAgentActionType coop, SGAgentActionType defect){
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
		public TitForTatAgentFactory(SGDomain domain, SGAgentActionType myCoop, SGAgentActionType myDefect, SGAgentActionType opponentCoop, SGAgentActionType opponentDefect){
			this.domain = domain;
			this.myCoop = myCoop;
			this.myDefect = myDefect;
			this.opponentCoop = opponentCoop;
			this.opponentDefect = opponentDefect;
			
		}
		
		@Override
		public SGAgent generateAgent() {
			return new TitForTat(domain, myCoop, myDefect, opponentCoop, opponentDefect);
		}
		
		
		
	}

}
