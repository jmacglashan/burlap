package burlap.oomdp.stocashticgames.tournament;

import java.util.List;

public interface MatchSelector {
	public List<MatchEntry> getNextMatch();
	public void resetMatchSelections();
}
