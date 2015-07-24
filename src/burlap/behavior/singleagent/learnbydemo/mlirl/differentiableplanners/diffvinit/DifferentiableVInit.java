package burlap.behavior.singleagent.learnbydemo.mlirl.differentiableplanners.diffvinit;

import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;

import java.util.Random;

/**
 * An interface for value function initialization that is differentiable with respect to some parameters. This
 * interface is useful for DifferentiableSparseSampling which may be used to learn the value of leaf nodes
 * in a finite horizon planner.
 *
 * @author James MacGlashan.
 */
public interface DifferentiableVInit extends ValueFunctionInitialization {

	/**
	 * Returns the value function gradient.
	 * @param s the state on which the value function is to be evaluated
	 * @return the value function gradient.
	 */
	public double [] getVGradient(State s);


	/**
	 * Returns the Q-value function gradient.
	 * @param s the state on which the Q-value is to be evaluated.
	 * @param ga the action on which the Q-value is to be evaluated.
	 * @return the Q-value function gradient
	 */
	public double [] getQGradient(State s, AbstractGroundedAction ga);


	/**
	 * A abstract class for {@link burlap.behavior.singleagent.learnbydemo.mlirl.differentiableplanners.diffvinit.DifferentiableVInit}
	 * that includes a double array of parameters and methods to modify them.
	 *
	 * @author James MacGlashan
	 */
	public static abstract class ParamedDiffVInit implements DifferentiableVInit{

		/**
		 * The parameters of the reward functions.
		 */
		protected double []								parameters;

		/**
		 * The parameter dimensionality
		 */
		protected int									dim;


		/**
		 * Sets the parameters of this differentiable value function initialization
		 * @param parameters the parameter values of this function
		 */
		public void setParameters(double [] parameters){
			this.parameters = parameters;
			this.dim = parameters.length;
		}


		/**
		 * Sets the value of a given parameter.
		 * @param i which parameter to set
		 * @param p the value of the parameter
		 */
		public void setParameter(int i, double p){
			this.parameters[i] = p;
		}


		/**
		 * Returns the parameter dimensionality
		 * @return the parameter dimensionality
		 */
		public int getParameterDimension(){
			return this.dim;
		}


		/**
		 * Returns the parameters of this reward function. The parameters are *not* copied, so changes
		 * made externally to them affect this reward function.
		 * @return the parameters of this reward function
		 */
		public double [] getParameters(){
			return this.parameters;
		}


		/**
		 * Randomizes the parameter values using the given random number generator.
		 * @param lowerVal the lower parameter range value
		 * @param upperVal the upper parameter range value
		 * @param rand the random number generator to use
		 */
		public void randomizeParameters(double lowerVal,double upperVal, Random rand){
			double range = upperVal - lowerVal;
			for(int i = 0; i < this.parameters.length; i++){
				this.parameters[i] = rand.nextDouble()*range + lowerVal;
			}
		}


	}

}
