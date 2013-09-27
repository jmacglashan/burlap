package burlap.oomdp.stocashticgames.tournament.common;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.stocashticgames.AgentType;
import burlap.oomdp.stocashticgames.tournament.MatchEntry;
import burlap.oomdp.stocashticgames.tournament.MatchSelector;


public class AllPairWiseSameTypeMS implements MatchSelector {
	
	protected int n;
	protected AgentType at;
	
	protected int p0;
	protected int p1;
	
	public AllPairWiseSameTypeMS(AgentType at, int n){
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
