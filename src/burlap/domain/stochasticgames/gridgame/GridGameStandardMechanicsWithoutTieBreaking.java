package burlap.domain.stochasticgames.gridgame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.Domain;

public class GridGameStandardMechanicsWithoutTieBreaking extends GridGameStandardMechanics{

	public GridGameStandardMechanicsWithoutTieBreaking(Domain domain, double semiWallProb) {
		super(domain, semiWallProb);
	}

	/**
	 * Overrides Standard mechanics by allowing no agent to win when in collision
	 */
	@Override
	protected Map <Integer, Integer> getWinningAgentMovements(Map <Integer, List <Integer>> collissionSets) {
		return new HashMap<Integer, Integer>();
	}
	
}
