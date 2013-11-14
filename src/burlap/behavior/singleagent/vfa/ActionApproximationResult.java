package burlap.behavior.singleagent.vfa;

import java.util.List;

import burlap.oomdp.singleagent.GroundedAction;


/**
 * A class that ties function approximation results to actions. This is useful for approximating Q-values.
 * @author James MacGlashan
 *
 */
public class ActionApproximationResult {

	/**
	 * The grounded action this approximation was for
	 */
	public GroundedAction			ga;
	
	/**
	 * The actual approximation result
	 */
	public ApproximationResult		approximationResult;
	
	
	/**
	 * Initializes with a given action and approximation result
	 * @param ga the grounded action that this approximation is for
	 * @param approximationResult the approximation result
	 */
	public ActionApproximationResult(GroundedAction ga, ApproximationResult approximationResult) {
		this.ga = ga;
		this.approximationResult = approximationResult;
	}
	
	
	/**
	 * Given a list of {@link ActionApproximationResult} objects, this method will return the corresponding {@link ActionApproximationResult}
	 * for the given action.
	 * @param approximations list of approximations
	 * @param ga the grounded action for which the corrsponding approximation result should be returned.
	 * @return the corresponding {@link ActionApproximationResult} for the given action. Null if there is no corresponding approximation result.
	 */
	public static ActionApproximationResult extractApproximationForAction(List<ActionApproximationResult> approximations, GroundedAction ga){
		for(ActionApproximationResult aar : approximations){
			if(aar.ga.equals(ga)){
				return aar;
			}
		}
		
		return null;
	}

}
