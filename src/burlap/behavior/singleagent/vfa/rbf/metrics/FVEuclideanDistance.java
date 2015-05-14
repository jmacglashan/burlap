package burlap.behavior.singleagent.vfa.rbf.metrics;

import burlap.behavior.singleagent.vfa.rbf.FVDistanceMetric;

/**
 * @author James MacGlashan.
 */
public class FVEuclideanDistance implements FVDistanceMetric{

	@Override
	public double distance(double[] s0, double[] s1) {

		if(s0.length != s1.length){
			throw new RuntimeException("Cannot compute Euclidean distance; feature vectors for the two input states are not equal in size.");
		}

		double sum = 0.;
		for(int i = 0; i < s0.length; i++){
			double diff = s0[i] - s1[i];
			sum += diff*diff;
		}

		double dist = Math.sqrt(sum);

		return dist;
	}
}
