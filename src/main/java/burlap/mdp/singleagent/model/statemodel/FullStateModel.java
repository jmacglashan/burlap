package burlap.mdp.singleagent.model.statemodel;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

/**
 * An extension of {@link SampleStateModel} for models that can enumerate the full probability distribution for a state
 * transition function. Typically, this model can on only be implemented when there are a finite number of possible outcomes.
 * This interface also contains a helper class for common approaches to implementing different aspects the model.
 * @author James MacGlashan.
 */
public interface FullStateModel extends SampleStateModel{

	/**
	 * Returns the set of possible transitions when {@link Action} is applied in {@link State} s. The returned
	 * list only needs to include transitions that have non-zero probability of occurring.
	 * @param s the source state
	 * @param a the action to be applied in the source state
	 * @return the probability distribution of the state transition function specified as a list of {@link StateTransitionProb} objects.
	 */
	List<StateTransitionProb> stateTransitions(State s, Action a);

	class Helper{

		/**
		 * Method to easily implement the {@link FullStateModel#stateTransitions(State, Action)} method for deterministic domains.
		 * Operates by getting a transition from the {@link SampleStateModel#sample(State, Action)} method and wraps
		 * it in a {@link StateTransitionProb} with probability 1 and then returns a list of that just one element.
		 * @param model the {@link SampleStateModel} with an implemented {@link SampleStateModel#sample(State, Action)} method.
		 * @param s the input state
		 * @param a the action taken.
		 * @return a List consisting of the single deterministic {@link StateTransitionProb}
		 */
		public static List<StateTransitionProb> deterministicTransition(SampleStateModel model, State s, Action a){
			return Arrays.asList(new StateTransitionProb(model.sample(s, a), 1.));
		}



		/**
		 * Method to implement the {@link SampleStateModel#sample(State, Action)} method when the
		 * {@link FullStateModel#stateTransitions(State, Action)} method is implemented. Operates by calling
		 * the {@link FullStateModel#stateTransitions(State, Action)} method, rolls a random number, and selects a
		 * transition according the probability specified by {@link FullStateModel#stateTransitions(State, Action)}.
		 * @param model the {@link FullStateModel} with the implemented {@link FullStateModel#stateTransitions(State, Action)} method.
		 * @param s the input state
		 * @param a the action to be applied in the input state
		 * @return a sampled state transition ({@link State}).
		 */
		public static State sampleByEnumeration(FullStateModel model, State s, Action a){

			List<StateTransitionProb> tps = model.stateTransitions(s, a);
			double roll = RandomFactory.getMapped(0).nextDouble();
			double sum = 0;
			for(StateTransitionProb tp : tps){
				sum += tp.p;
				if(roll < sum){
					return tp.s;
				}
			}

			throw new RuntimeException("Transition probabilities did not sum to one, they summed to " + sum);

		}
	}

}
