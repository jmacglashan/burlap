package burlap.behavior.singleagent.learning.modellearning;

import burlap.behavior.policy.Policy;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * Interface for defining planning algorithms that operate on iteratively learned models. Planning algorithms that operate on iteratively learned models
 * must support features for replanning when the model changes and returning the policy of the plan under the current model.
 * @author James MacGlashan
 *
 */
public interface ModelPlanner {

	/**
	 * This is method is expected to be called at the beginning of any new learning episode. This may be useful for planning algorithms
	 * that do not solve the policy for every state since new episodes may starts in epsidoes the planning algorithm had not previously considered.
	 * before a learning episode begins.
	 * @param s the input state
	 */
	public void initializePlannerIn(State s);
	
	/**
	 * Tells the valueFunction that the model has changed and that it will need to replan accordingly
	 * @param changedState the source state that caused a change in the model.
	 */
	public void modelChanged(State changedState);
	
	/**
	 * Returns a policy encoding the valueFunction's results.
	 * @return a policy object
	 */
	public Policy modelPlannedPolicy();
	
	/**
	 * Resets valueFunction as if no planning had never been called.
	 */
	public void resetPlanner();


	/**
	 * An interface for providing a method that can generated a {@link burlap.behavior.singleagent.learning.modellearning.ModelPlanner} with
	 * the {@link #getModelPlanner(burlap.oomdp.core.Domain, burlap.oomdp.singleagent.RewardFunction, burlap.oomdp.core.TerminalFunction, double)}
	 * method.
	 */
	public static interface ModelPlannerGenerator{
		public ModelPlanner getModelPlanner(Domain modelDomain, RewardFunction modeledRewardFunction, TerminalFunction modeledTerminalFunction, double discount);
	}
	
}
