package burlap.behavior.singleagent.vfa;

import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


public interface ValueFunctionApproximation {

	public ApproximationResult getStateValue(State s);
	public List<ActionApproximationResult> getStateActionValues(State s, List <GroundedAction> gas);
	
	public WeightGradient getWeightGradient(ApproximationResult approximationResult);
	
	
	
}
