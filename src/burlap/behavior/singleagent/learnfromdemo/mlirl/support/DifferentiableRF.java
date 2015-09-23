package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

import java.util.Random;


/**
 * An abstract class for defining differentiable reward functions.
 * @author James MacGlashan.
 */
public abstract class DifferentiableRF implements RewardFunction{

	/**
	 * The parameters of the reward functions.
	 */
	protected double []								parameters;

	/**
	 * The parameter dimensionality
	 */
	protected int									dim;


	/**
	 * Returns the gradient of the reward function for the given state transition.
	 * @param s the source state
	 * @param ga the action taken in the source state
	 * @param sp the resulting state from the action
	 * @return the gradient of the reward function for the given transition.
	 */
	public abstract double [] getGradient(State s, GroundedAction ga, State sp);

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

	/**
	 * A helper method for making a copy of this reward function. THe parameters and dimensionality
	 * do not have to be copied, because they will be copied in the public {@link #copy()} method.
	 * @return a copy of this reward function.
	 */
	protected abstract DifferentiableRF copyHelper();


	/**
	 * Creates a copy of this reward function. The parameters of the copy can be modified
	 * without affecting the parameters this source reward function.
	 * @return a copy of this reward function
	 */
	public DifferentiableRF copy(){
		DifferentiableRF c = this.copyHelper();
		c.setParameters(this.parameters.clone());
		return c;
	}



	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < this.parameters.length; i++){
			if(i > 0){
				buf.append(", ");
			}
			buf.append(this.parameters[i]);
		}

		return buf.toString();
	}



}
