package burlap.behavior.stochasticgames.agents.twoplayer.repeatedsinglestage;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.AgentFactory;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentBase;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;


/**
 * A class for an agent that plays grim trigger. The agent starts by following a "cooperate" action. If at any point their opponent plays
 * a "defect" action, then this agent will play their "defect" action for the rest of the repeated game (until the {@link #gameStarting(World, int)} method is called again).
 * @author James MacGlashan
 *
 */
public class GrimTrigger extends SGAgentBase {

	/**
	 * This agent's cooperate action
	 */
	protected Action myCoop;
	
	/**
	 * This agent's defect action
	 */
	protected Action myDefect;
	
	/**
	 * The opponent's defect action
	 */
	protected Action opponentDefect;
	
	
	/**
	 * Whether this agent will play its defect action or not.
	 */
	protected boolean grimTrigger = false;

	protected int agentNum;
	
	
	/**
	 * Initializes with the specified cooperate and defect actions for both players.
	 * @param domain the domain in which this agent will play.
	 * @param coop the cooperate action for both players
	 * @param defect the defect action for both players
	 */
	public GrimTrigger(SGDomain domain, Action coop, Action defect){
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
	public GrimTrigger(SGDomain domain, Action myCoop, Action myDefect, Action opponentDefect){
		this.init(domain);
		this.myCoop = myCoop;
		this.myDefect = myDefect;
		this.opponentDefect = opponentDefect;
	}
	
	@Override
	public void gameStarting(World w, int agentNum) {
		this.world = w;
		this.agentNum = agentNum;
		grimTrigger = false;
	}

	@Override
	public Action action(State s) {
		if(this.grimTrigger){
			return myDefect;
		}
		return myCoop;
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			double[] jointReward, State sprime, boolean isTerminal) {

		int oagent = this.agentNum == 0 ? 1 : 0;
		if(jointAction.action(oagent).equals(opponentDefect)){
			grimTrigger = true;
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
		protected Action myCoop;
		
		/**
		 * The agent's defect action
		 */
		protected Action myDefect;
		
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
		public GrimTriggerAgentFactory(SGDomain domain, Action coop, Action defect){
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
		public GrimTriggerAgentFactory(SGDomain domain, Action myCoop, Action myDefect, Action opponentDefect){
			this.domain = domain;
			this.myCoop = myCoop;
			this.myDefect = myDefect;
			this.opponentDefect = opponentDefect;
		}
		
		@Override
		public SGAgent generateAgent(String agentName, SGAgentType type) {
			return new GrimTrigger(domain, myCoop, myDefect, opponentDefect)
					.setAgentDetails(agentName, type);
		}
		
		
		
	}
	
}
