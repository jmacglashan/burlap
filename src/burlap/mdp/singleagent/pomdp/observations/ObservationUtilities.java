package burlap.mdp.singleagent.pomdp.observations;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Helper methods for defining {@link ObservationFunction} and {@link DiscreteObservationFunction} objects.
 * @author James MacGlashan.
 */
public class ObservationUtilities {

	/**
	 * A helper method for easily implementing the {@link DiscreteObservationFunction#probabilities(State, Action)} method
	 * that computes observation probability distribution by enumerating all possible observations (as defined by the {@link DiscreteObservationFunction#allObservations()} method)
	 * and assigning their probability according to the {@link ObservationFunction#probability(State, State, Action)}
	 * method. Note that because this method requires enumerating all observations, it may be more computationally efficient to instead directly implement domain specific code
	 * for the {@link DiscreteObservationFunction#probabilities(State, Action)} method.
	 * @param state the true MDP state that generated the observations
	 * @param action the action that led to the MDP state and which generated the observations.
	 * @return the observation probability mass/density function represented by a {@link java.util.List} of {@link ObservationProbability} objects.
	 */
	public static List<ObservationProbability> probabilitiesByEnumeration(DiscreteObservationFunction of, State state, Action action){
		List<State> possibleObservations = of.allObservations();
		List<ObservationProbability> probs = new ArrayList<ObservationProbability>(possibleObservations.size());

		for(State obs : possibleObservations){
			double p = of.probability(obs, state, action);
			if(p != 0){
				probs.add(new ObservationProbability(obs, p));
			}
		}

		return probs;
	}


	/**
	 * A helper method for easily implementing the {@link ObservationFunction#sample(State, Action)} method that
	 * samples an observation by first getting all non-zero probability observations, as returned by the {@link DiscreteObservationFunction#probabilities(State, Action)}
	 * method, and then sampling from the enumerated distribution. Note that enumerating all observation probabilities may be computationally
	 * inefficient; therefore, it may be better to directly implement the {@link ObservationFunction#sample(State, Action)}
	 * method with efficient domain specific code.
	 * @param state the true MDP state
	 * @param action the action that led to the MDP state
	 * @return an observation represented with a {@link State}.
	 */
	public static State sampleByEnumeration(DiscreteObservationFunction of, State state, Action action){
		List<ObservationProbability> obProbs = of.probabilities(state, action);
		Random rand = RandomFactory.getMapped(0);
		double r = rand.nextDouble();
		double sumProb = 0.;
		for(ObservationProbability op : obProbs){
			sumProb += op.p;
			if(r < sumProb){
				return op.observation;
			}
		}

		throw new RuntimeException("Could not sample observaiton because observation probabilities did not sum to 1; they summed to " + sumProb);
	}

}
