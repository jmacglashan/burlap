package burlap.behavior.singleagent.learning.tdmethods.vfa;

import burlap.behavior.functionapproximation.DifferentiableStateActionValue;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.options.EnvironmentOptionOutcome;
import burlap.datastructures.HashedAggregator;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.List;
import java.util.Map;

/**
 * An implementation of Gradient Descent Q-learning. By default, updates the Q-function for the most recent example,
 * but because it extends {@link ApproximateQLearning}, it can set to use experience replay and state Q-functions.
 * @author James MacGlashan.
 */
public class GradientDescentQLearning extends ApproximateQLearning {

	/**
	 * A learning rate function to use
	 */
	protected LearningRate learningRate;

	/**
	 * Initializes.
	 * @param domain the learning domain
	 * @param gamma the discount factor
	 * @param vfa the value function approximation to use
	 * @param learningRate the learning rate to use
	 */
	public GradientDescentQLearning(SADomain domain, double gamma, DifferentiableStateActionValue vfa, double learningRate) {
		super(domain, gamma, vfa);
		this.learningRate = new ConstantLR(learningRate);
	}


	/**
	 * Initializes.
	 * @param domain the learning domain
	 * @param gamma the discount factor
	 * @param vfa the value function to use
	 * @param learningRate the {@link LearningRate} object for the learning rate schedule.
	 */
	public GradientDescentQLearning(SADomain domain, double gamma, DifferentiableStateActionValue vfa, LearningRate learningRate) {
		super(domain, gamma, vfa);
		this.learningRate = learningRate;
	}

	public LearningRate getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(LearningRate learningRate) {
		this.learningRate = learningRate;
	}

	@Override
	public void updateQFunction(List<EnvironmentOutcome> samples) {

		HashedAggregator<Integer> sumGradient = new HashedAggregator<Integer>();
		for(EnvironmentOutcome eo : samples){

			//get statistics
			double curQ = this.vfa.evaluate(eo.o, eo.a);
			double nextQV = 0.;
			if(!eo.terminated) {
				nextQV = this.staleValue(eo.op);
			}
			double discount = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome)eo).discount : this.gamma;

			//compute function delta
			double delta = eo.r + (discount*nextQV) - curQ;

			//get gradient and add it
			FunctionGradient gradient = ((DifferentiableStateActionValue)this.vfa).gradient(eo.o, eo.a);
			for(FunctionGradient.PartialDerivative pd : gradient.getNonZeroPartialDerivatives()){
				double errorPd = pd.value*delta;
				sumGradient.add(pd.parameterId, errorPd);
			}


		}

		//now update parameters
		double scalar = 1. / samples.size();
		for(Map.Entry<Integer, Double> pd : sumGradient.entrySet()){
			int pind = pd.getKey();
			double oldP = this.vfa.getParameter(pind);
			double lr = this.learningRate.pollLearningRate(this.totalSteps, pind);
			double errorGrad = pd.getValue();
			double nP = oldP + lr * scalar * errorGrad;
			this.vfa.setParameter(pind, nP);
		}

	}


}
