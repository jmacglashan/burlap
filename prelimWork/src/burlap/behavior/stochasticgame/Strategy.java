package burlap.behavior.stochasticgame;

import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;


/**
 * This class is the analog of the {@link burlap.behavior.Policy} class, except for stochastic games. It can be used
 * to define how an agent will act in any given state.
 * @author James MacGlashan
 *
 */
public abstract class Strategy {

	/**
	 * Returns the action selection of this agent for the given state.
	 * @param s the state in which to select an action.
	 * @return the action selection of this agent for the given state.
	 */
	public abstract GroundedSingleAction getAction(State s);
	
	/**
	 * Returns the agents mixed action distribution for a given state. That is, the probability
	 * the agent will take each action in the given state
	 * @param s the state in which to return the action distribution.
	 * @return the agents mixed action distribution for a given state
	 */
	public abstract List<SingleActionProb> getActionDistributionForState(State s);
	
	/**
	 * Returns whether this strategy is stochastic/mixed.
	 * @return true if this strategy is stochastic/mixed; false otherwise
	 */
	public abstract boolean isStochastic();
	
	
	
	/**
	 * A class for associating probabilities with action selections.
	 * @author James MacGlashan
	 *
	 */
	public class SingleActionProb{
		
		/**
		 * The action to select
		 */
		public GroundedSingleAction ga;
		
		/**
		 * The probability of the action selection
		 */
		public double pSelection;
		
		
		/**
		 * Initializes for a given action selection and probability of that selection
		 * @param ga the action to select
		 * @param p the probability of selecting it.
		 */
		public SingleActionProb(GroundedSingleAction ga, double p){
			this.ga = ga;
			this.pSelection = p;
		}
		
	}

}
