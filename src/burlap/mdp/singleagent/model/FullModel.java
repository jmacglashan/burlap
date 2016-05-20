package burlap.mdp.singleagent.model;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.Arrays;
import java.util.List;

/**
 * An interface extension for {@link SampleModel} that not only can provide samples of transitions, but can enumerate the probability distribution
 * of the transitions. Typically, this model can on only be implemented when there are a finite number of possible outcomes.
 * This interface also contains a helper class for common approaches to implementing different aspects the model.
 * @author James MacGlashan.
 */
public interface FullModel extends SampleModel{

	/**
	 * Returns the set of possible transitions when {@link Action} is applied in {@link State} s. The returned
	 * list only needs to include transitions that have non-zero probability of occurring.
	 * @param s the source {@link State}
	 * @param a the {@link Action} applied in the source state
	 * @return the probability distribution over possible transitions.
	 */
	List<TransitionProb> transitions(State s, Action a);

	class Helper{

		/**
		 * Method to easily implement the {@link FullModel#transitions(State, Action)} method for deterministic domains.
		 * Operates by getting a transition from the {@link SampleModel#sample(State, Action)} method and wraps
		 * it in a {@link TransitionProb} with probability 1 and then returns a list of that just one element.
		 * @param model the {@link SampleModel} with an implemented {@link SampleModel#sample(State, Action)} method.
		 * @param s the input state
		 * @param a the action taken.
		 * @return a List consisting of the single deterministic {@link TransitionProb}
		 */
		public static List<TransitionProb> deterministicTransition(SampleModel model, State s, Action a){
			EnvironmentOutcome eo = model.sample(s, a);
			return Arrays.asList(new TransitionProb(1., eo));
		}


		/**
		 * Method to implement the {@link SampleModel#sample(State, Action)} method when the
		 * {@link FullModel#transitions(State, Action)} method is implemented. Operates by calling
		 * the {@link FullModel#transitions(State, Action)} method, rolls a random number, and selects a
		 * transition according the probability specified by {@link FullModel#transitions(State, Action)}.
		 * @param model the {@link FullModel} with the implemented {@link FullModel#transitions(State, Action)} method.
		 * @param s the input state
		 * @param a the action to be applied in the input state
		 * @return a sampled transition ({@link EnvironmentOutcome}).
		 */
		public static EnvironmentOutcome sampleByEnumeration(FullModel model, State s, Action a){
			List<TransitionProb> tps = model.transitions(s, a);
			double roll = RandomFactory.getMapped(0).nextDouble();
			double sum = 0;
			for(TransitionProb tp : tps){
				sum += tp.p;
				if(roll < sum){
					return tp.eo;
				}
			}

			throw new RuntimeException("Transition probabilities did not sum to one, they summed to " + sum);
		}
	}

}
