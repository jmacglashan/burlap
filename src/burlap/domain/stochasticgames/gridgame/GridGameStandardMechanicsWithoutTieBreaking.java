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

	/*
	@Override
	protected List<Location2> resolveCollisions(List<Location2> originalPositions, List <Location2> desiredPositions){
		//get movement collisions
		Map <Integer, List <Integer>> collissionSets = this.getColissionSets(desiredPositions);
		
		if(collissionSets.size() == 0){
			return desiredPositions; //no resolutions needed
		}
		
		List <Location2> finalPoses = new ArrayList<GridGameStandardMechanics.Location2>();
		
	}*/
	
	@Override
	protected Map <Integer, Integer> getWinningAgentMovements(Map <Integer, List <Integer>> collissionSets){
		
		return new HashMap<Integer, Integer>();
			
	}
	
}
