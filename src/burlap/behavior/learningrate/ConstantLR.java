package burlap.behavior.learningrate;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;


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
	public double peekAtLearningRate(State s, AbstractGroundedAction ga) {
		return this.learningRate;
	}

	@Override
	public double pollLearningRate(State s, AbstractGroundedAction ga) {
		return this.learningRate;
	}

	@Override
	public void resetDecay() {
		//no change needed
	}

	@Override
	public double peekAtLearningRate(int featureId) {
		return this.learningRate;
	}

	@Override
	public double pollLearningRate(int featureId) {
		return this.learningRate;
	}

}
