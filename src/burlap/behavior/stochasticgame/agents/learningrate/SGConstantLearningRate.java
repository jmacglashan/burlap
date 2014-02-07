package burlap.behavior.stochasticgame.agents.learningrate;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;


/**
 * A class for specifying a constant learning rate that never changes.
 * @author James MacGlashan
 *
 */
public class SGConstantLearningRate implements SGLearningRate {

	public double learningRate = 0.1;
	
	/**
	 * Initializes with a default learning rate of 0.1
	 */
	public SGConstantLearningRate(){
		
	}
	
	/**
	 * Initializes with the given learning rate
	 * @param learningRate the learning rate to use
	 */
	public SGConstantLearningRate(double learningRate){
		this.learningRate = learningRate;
	}

	@Override
	public double peekAtLearningRate(State s, GroundedSingleAction ga) {
		return this.learningRate;
	}

	@Override
	public double pollLearningRate(State s, GroundedSingleAction ga) {
		return this.learningRate;
	}
	
	
	

}
