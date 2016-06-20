package burlap.behavior.singleagent.planning.stochastic.dpoperator;

import burlap.datastructures.BoltzmannDistribution;

/**
 * A softmax/Boltzmann operator. As its beta parameter goes to infinity, the operator goes to max. As it goes to zero, it takes an average.
 * As it goes to -infinity, it goes to min. Beta is the inverse of the "temperature."
 * @author James MacGlashan.
 */
public class SoftmaxOperator implements DPOperator {
	protected double beta;
	protected double temp;

	/**
	 * Initializes with beta = 1.0
	 */
	public SoftmaxOperator() {
		this(1.);
	}

	/**
	 * Initializes. As beta goes to infinity, the operator goes to max. As it goes to zero, it takes an average.
	 * As it goes to -infinity, it goes to min. Beta is the inverse of the "temperature."
	 * @param beta the softmax knob.
	 */
	public SoftmaxOperator(double beta) {
		this.beta = beta;
		this.temp = 1./beta;
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		this.beta = beta;
		this.temp = 1./beta;
	}

	@Override
	public double apply(double[] qs) {
		BoltzmannDistribution bd = new BoltzmannDistribution(qs, temp);
		double [] dist = bd.getProbabilities();
		double sum = 0.;
		for(int i = 0; i < qs.length; i++){
			sum += qs[i] * dist[i];
		}
		return sum;
	}
}
