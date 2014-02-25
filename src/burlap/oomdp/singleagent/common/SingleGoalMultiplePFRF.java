package burlap.oomdp.singleagent.common;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class defines a reward function that returns a goal reward when any grounded form of a propositional
 * function is true in the resulting state and a default non-goal reward otherwise.
 * @author James MacGlashan
 *
 */
public class SingleGoalMultiplePFRF implements RewardFunction {

	private Map 					rewardTable;
	private double 					normalReward;
	
	
	/**
	 * Initializes the reward table to return the specified reward when any grounded from of the propositional functions
	 * are true in the resulting state and the specified non-goal reward otherwise.
	 * @param rewardTable a mapping from propositional functions to reward values.
	 * @param goalReward the goal reward value to be returned
	 * @param nonGoalReward the non goal reward value to be returned.
	 */
	public SingleGoalMultiplePFRF(Map<PropositionalFunction, Double> rewardTable, double normalReward){
		this.rewardTable = rewardTable;
		this.normalReward = normalReward;
	}
	
	/**
	 * This returns the value of a state. If the state returns true for any of the propositional functions in the
	 * rewardTable, then we return the corresponding reward. Otherwise normalReward is returned
	 * @param s The current state.
	 * @param a The current action.
	 * @param sprime the successor state
	 */
	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		
		Iterator it = rewardTable.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			PropositionalFunction pf = (PropositionalFunction)pairs.getKey();
			Double reward = (Double)pairs.getValue();
			
			if(pf.isTrue(s)) {
				return reward;
			}
			
//			List<GroundedProp> gps = sprime.getAllGroundedPropsFor(pf);
//			
//			for(GroundedProp gp : gps){
//				if(gp.isTrue(sprime)){
//					return reward;
//				}
//			}			
		}
		return normalReward;
	}

}
