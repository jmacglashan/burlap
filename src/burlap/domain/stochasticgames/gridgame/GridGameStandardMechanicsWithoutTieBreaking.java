package burlap.domain.stochasticgames.gridgame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.Domain;

public class GridGameStandardMechanicsWithoutTieBreaking extends GridGameStandardMechanics{

	public GridGameStandardMechanicsWithoutTieBreaking(Domain d) {
		super(d);
	}
	
	public GridGameStandardMechanicsWithoutTieBreaking(Domain d, double semiWallProb) {
		super(d, semiWallProb);
	}

	@Override
	protected Map <Integer, Integer> getWinningAgentMovements(Map <Integer, List <Integer>> collissionSets){
		
		return new HashMap<Integer, Integer>();
			
	}
	
}
