package burlap.behavior.singleagent.planning.stochastic.dpoperator;

import burlap.datastructures.BoltzmannDistribution;

/**
 * @author James MacGlashan.
 */
public class SoftmaxOperator implements DPOperator {
	protected double beta;
	protected double temp;

	public SoftmaxOperator() {
		this(1.);
	}

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
