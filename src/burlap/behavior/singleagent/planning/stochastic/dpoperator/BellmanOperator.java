package burlap.behavior.singleagent.planning.stochastic.dpoperator;

/**
 * The standard Bellman operator: max.
 * @author James MacGlashan.
 */
public class BellmanOperator implements DPOperator{
	@Override
	public double apply(double[] qs) {
		double mx = qs[0];
		for(int i = 1; i < qs.length; i++){
			double qi = qs[i];
			mx = mx > qi ? mx : qi;
		}
		return mx;
	}
}
