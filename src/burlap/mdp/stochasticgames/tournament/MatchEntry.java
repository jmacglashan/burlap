package burlap.mdp.stochasticgames.tournament;

import burlap.mdp.stochasticgames.SGAgentType;

/**
 * This class indicates which player in a tournament is to play in a match and what {@link burlap.mdp.stochasticgames.SGAgentType} role they will play.
 * @author James MacGlashan
 *
 */
public class MatchEntry {

	public SGAgentType agentType;
	public int agentId;
	
	/**
	 * Initializes the MatchEntry
	 * @param at the {@link burlap.mdp.stochasticgames.SGAgentType} the agent will play as
	 * @param ai the index of this agent in the tournament
	 */
	public MatchEntry(SGAgentType at, int ai){
		this.agentType = at;
		this.agentId = ai;
	}

}
