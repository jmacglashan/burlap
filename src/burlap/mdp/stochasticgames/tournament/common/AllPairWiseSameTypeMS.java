package burlap.mdp.stochasticgames.tournament.common;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.stochasticgames.SGAgentType;
import burlap.mdp.stochasticgames.tournament.MatchEntry;
import burlap.mdp.stochasticgames.tournament.MatchSelector;


/**
 * This class defines a MatchSelctory that plays all pairwise matches of agents in a round robin. It sets
 * all agents to play as the same {@link burlap.mdp.stochasticgames.SGAgentType} and therefore is only valid in symmetric games.
 * @author James MacGlashan
 *
 */
public class AllPairWiseSameTypeMS implements MatchSelector {
	
	protected int n;
	protected SGAgentType at;
	
	protected int p0;
	protected int p1;
	
	
	/**
	 * Initializes the selector
	 * @param at the {@link burlap.mdp.stochasticgames.SGAgentType} that all agents will play as
	 * @param n the number of agents in the tournament
	 */
	public AllPairWiseSameTypeMS(SGAgentType at, int n){
		this.n = n;
		this.at = at;
		
		p0 = 0;
		p1 = 1;
	}
	
	@Override
	public List<MatchEntry> getNextMatch() {
		
		if(p0 >= n-1){
			return null; //no more matches
		}
		
		MatchEntry me0 = new MatchEntry(at, p0);
		MatchEntry me1 = new MatchEntry(at, p1);
		
		List <MatchEntry> match = new ArrayList<MatchEntry>();
		match.add(me0);
		match.add(me1);
		
		p1++;
		if(p1 >= n){
			p0++;
			p1 = p0+1;
		}
		
		return match;
	}

	@Override
	public void resetMatchSelections() {
		p0 = 0;
		p1 = 1;
	}

}
