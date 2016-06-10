package burlap.mdp.stochasticgames.tournament;

import java.util.List;

/**
 * An interface for defining how matches in a tournament will be determined
 * @author James MacGlashan
 *
 */
public interface MatchSelector {
	/**
	 * Returns the next match information, which is a list of {@link MatchEntry} objects
	 * @return the next match information, which is a list of {@link MatchEntry} objects
	 */
	public List<MatchEntry> getNextMatch();
	
	/**
	 * Resets the match selections and causes the {@link #getNextMatch()} method to start from the beginning of matches
	 */
	public void resetMatchSelections();
}
