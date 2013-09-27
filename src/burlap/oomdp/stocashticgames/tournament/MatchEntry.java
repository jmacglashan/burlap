package burlap.oomdp.stocashticgames.tournament;

import burlap.oomdp.stocashticgames.AgentType;

public class MatchEntry {

	public AgentType agentType;
	public int agentId;
	
	public MatchEntry(AgentType at, int ai){
		this.agentType = at;
		this.agentId = ai;
	}

}
