package burlap.behavior.stochasticgames.agents.twoplayer.repeatedsinglestage;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.AgentFactory;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentBase;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;


/**
 * A class for an agent that plays tit-for-tat. The agent starts by playing a "cooperate" action. If their opponent "defects" on them, then
 * this agent plays their "defect" action in the next turn. If their opponent "cooperates" again, then this agent will cooperate in the next turn.
 * The corresponding "cooperate" and "defect" actions for each agent need to be specified. When a new game starts, this agent will reset
 * to trying cooperate.
 * @author James MacGlashan
 *
 */
public class TitForTat extends SGAgentBase {

	/**
	 * This agent's cooperate action
	 */
	protected Action myCoop;
	
	/**
	 * This agent's defect action
	 */
	protected Action myDefect;
	
	/**
	 * The opponent's cooperate action
	 */
	protected Action opponentCoop;
	
	/**
	 * The opponent's defect action
	 */
	protected Action opponentDefect;
	
	
	
	/**
	 * The last opponent's move
	 */
	protected Action lastOpponentMove;


	protected int agentNum;

	protected int otherNum;
	
	/**
	 * Initializes with the specified cooperate and defect actions for both players.
	 * @param domain the domain in which this agent will play.
	 * @param coop the cooperate action for both players
	 * @param defect the defect action for both players
	 */
	public TitForTat(SGDomain domain, Action coop, Action defect){
		this.init(domain);
		this.myCoop = coop;
		this.myDefect = defect;
		this.opponentCoop = coop;
		this.opponentDefect = defect;
		
		this.lastOpponentMove = null;
	}
	
	/**
	 * Initializes with differently specified cooperate and defect actions for both players.
	 * @param domain the domain in which this agent will play
	 * @param myCoop this agent's cooperate action
	 * @param myDefect this agent's defect action
	 * @param opponentCoop the opponent's cooperate action
	 * @param opponentDefect the opponent's defect action
	 */
	public TitForTat(SGDomain domain, Action myCoop, Action myDefect, Action opponentCoop, Action opponentDefect){
		this.init(domain);
		this.myCoop = myCoop;
		this.myDefect = myDefect;
		this.opponentCoop = opponentCoop;
		this.opponentDefect = opponentDefect;

		this.lastOpponentMove = null;
	}
	
	@Override
	public void gameStarting(World w, int agentNum) {
		this.agentNum = agentNum;
		this.world = w;
		this.otherNum = agentNum == 0 ? 1 : 0;
		this.lastOpponentMove = null;
	}

	@Override
	public Action action(State s) {
		if(lastOpponentMove.equals(opponentCoop)){
			return myCoop;
		}
		return myDefect;
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, double[] jointReward, State sprime, boolean isTerminal) {
		this.lastOpponentMove = jointAction.action(otherNum);
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
		protected Action myCoop;
		
		/**
		 * This agent's defect action
		 */
		protected Action myDefect;
		
		/**
		 * The opponent's cooperate action
		 */
		protected Action opponentCoop;
		
		/**
		 * The opponent's defect action
		 */
		protected Action opponentDefect;
		
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
		public TitForTatAgentFactory(SGDomain domain, Action coop, Action defect){
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
		public TitForTatAgentFactory(SGDomain domain, Action myCoop, Action myDefect, Action opponentCoop, Action opponentDefect){
			this.domain = domain;
			this.myCoop = myCoop;
			this.myDefect = myDefect;
			this.opponentCoop = opponentCoop;
			this.opponentDefect = opponentDefect;
			
		}
		
		@Override
		public SGAgent generateAgent(String agentName, SGAgentType type) {
			return new TitForTat(domain, myCoop, myDefect, opponentCoop, opponentDefect)
					.setAgentDetails(agentName, type);
		}
		
		
		
	}

}
