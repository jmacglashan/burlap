package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.List;

import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class StateHelpers {
	
	public static int [] stateToBitStringOfPreds(State state, List<PropositionalFunction> propFuns) {	
		int numPreds = propFuns.size();
		
		int [] toReturn = new int [numPreds];
		
		//Determine preds values
		int index = 0;
		for (PropositionalFunction currFn : propFuns) {			
			toReturn[index] = currFn.somePFGroundingIsTrue(state) ? 1 : 0;;
			index += 1;
		}
		
		return toReturn;
	}
	
	public static String stateToBitStringOfPredsString(State state, List<PropositionalFunction> propFuns) {	
		int [] intsArr = stateToBitStringOfPreds(state, propFuns);
		StringBuilder ret = new StringBuilder();
		for (int curr: intsArr) {
			ret.append(curr);
		}
			
		return ret.toString();
	}
}
