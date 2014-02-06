package burlap.behavior.singleagent.learning.learningrate;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * A class for specifying a constant learning rate that never changes.
 * @author James MacGlashan
 *
 */
public class ConstantLR implements LearningRate {

	public double learningRate = 0.1;
	
	/**
	 * Constructs constant learning rate of 0.1
	 */
	public ConstantLR(){
		//do nothing
	}
	
	/**
	 * Constructs a constant learning rate for the given value
	 * @param learningRate the constant learning rate to use
	 */
	public ConstantLR(Double learningRate){
		this.learningRate = learningRate;
	}
	
	@Override
	public double peekAtLearningRate(State s, GroundedAction ga) {
		return this.learningRate;
	}

	@Override
	public double pollLearningRate(State s, GroundedAction ga) {
		return this.learningRate;
	}

}
