package burlap.behavior.functionapproximation.supervised;

import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * An interface for learning value function approximation via a supervised learning algorithm. This interface
 * defines the method {@link #train} which takes as input a list of {@link SupervisedVFA.SupervisedVFAInstance}
 * objects, runs a regression algorithm, and returns the learned function, which is an interface of {@link burlap.behavior.valuefunction.ValueFunction}.
 * <p>
 * A {@link SupervisedVFA.SupervisedVFAInstance} is a pair consisting
 * of a {@link State} and the target state value that is to be learned.
 * @author James MacGlashan.
 */
public interface SupervisedVFA {

	/**
	 * Uses supervised learning (regression) to learn a value function approximation of the input training data.
	 * @param trainingData the training data to fit.
	 * @return a {@link burlap.behavior.valuefunction.ValueFunction} that fits the training data.
	 */
	ValueFunction train(List<SupervisedVFAInstance> trainingData);


	/**
	 * A pair for a state and it's target value function value.
	 */
	class SupervisedVFAInstance{

		/**
		 * The state
		 */
		public State s;

		/**
		 * The state's associated value
		 */
		public double v;


		/**
		 * Initializes
		 * @param s tne state
		 * @param v the state's associated value
		 */
		public SupervisedVFAInstance(State s, double v){
			this.s = s;
			this.v = v;
		}

	}

}
