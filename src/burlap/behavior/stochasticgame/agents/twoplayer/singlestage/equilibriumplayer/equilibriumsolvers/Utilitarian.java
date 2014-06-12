package burlap.behavior.stochasticgame.agents.twoplayer.singlestage.equilibriumplayer.equilibriumsolvers;

import burlap.behavior.stochasticgame.agents.twoplayer.singlestage.equilibriumplayer.BimatrixEquilibriumSolver;
import burlap.behavior.stochasticgame.solvers.GeneralBimatrixSolverTools;


/**
 * Finds the maximum utilitarian value joint action and retuns a detemrinistic strategy respecting it. The utilitarian value
 * is the sum of the two player's payoffs for a cell. If there are multiple maximums, the first is always used.
 * @author James MacGlashan
 *
 */
public class Utilitarian extends BimatrixEquilibriumSolver {

	@Override
	public double[] computeRowStrategy(double[][] rowPayoff,
			double[][] colPayoff) {
		
		double max = Double.NEGATIVE_INFINITY;
		int maxInd = -1;
		for(int i = 0; i < rowPayoff.length; i++){
			for(int j = 0; j < rowPayoff[i].length; j++){
				double sumPay = rowPayoff[i][j] - colPayoff[i][j];
				if(sumPay > max){
					max = rowPayoff[i][j];
					maxInd = i;
				}
			}
		}
		
		double [] strat = GeneralBimatrixSolverTools.zero1Array(maxInd, rowPayoff.length);
		
		return strat;
	}

	@Override
	public double[] computeColStrategy(double[][] rowPayoff,
			double[][] colPayoff) {
		
		double max = Double.NEGATIVE_INFINITY;
		int maxInd = -1;
		for(int i = 0; i < rowPayoff.length; i++){
			for(int j = 0; j < rowPayoff[i].length; j++){
				double sumPay = rowPayoff[i][j] - colPayoff[i][j];
				if(sumPay > max){
					max = rowPayoff[i][j];
					maxInd = j;
				}
			}
		}
		
		double [] strat = GeneralBimatrixSolverTools.zero1Array(maxInd, rowPayoff[0].length);
		
		return strat;
		
	}

}
