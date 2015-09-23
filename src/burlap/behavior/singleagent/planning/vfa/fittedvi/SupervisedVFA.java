package burlap.behavior.singleagent.planning.vfa.fittedvi;

import burlap.behavior.singleagent.planning.ValueFunction;
import burlap.oomdp.core.State;

import java.util.List;

/**
 * An interface for learning value function approximation via a supervised learning algorithm. This interface
 * defines the method {@link #train} which takes as input a list of {@link burlap.behavior.singleagent.planning.vfa.fittedvi.SupervisedVFA.SupervisedVFAInstance}
 * objects, runs a regression algorithm, and returns the learned function, which is an interface of {@link burlap.behavior.singleagent.planning.ValueFunction}.
 * <br/><br/>
 * A {@link burlap.behavior.singleagent.planning.vfa.fittedvi.SupervisedVFA.SupervisedVFAInstance} is a pair consisting
 * of a {@link burlap.oomdp.core.State} and the target state value that is to be learned.
 * @author James MacGlashan.
 */
public interface SupervisedVFA {

	/**
	 * Uses supervised learning (regression) to learn a value function approximation of the input training data.
	 * @param trainingData the training data to fit.
	 * @return a {@link burlap.behavior.singleagent.planning.ValueFunction} that fits the training data.
	 */
	public ValueFunction train(List<SupervisedVFAInstance> trainingData);


	/**
	 * A pair for a state and it's target value function value.
	 */
	public static class SupervisedVFAInstance{

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
