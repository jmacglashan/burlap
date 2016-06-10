package burlap.behavior.stochasticgames.agents.twoplayer.singlestage.equilibriumplayer.equilibriumsolvers;

import java.util.HashSet;
import java.util.Set;

import burlap.behavior.stochasticgames.agents.twoplayer.singlestage.equilibriumplayer.BimatrixEquilibriumSolver;


/**
 * A class for finding a stategy that maxmizes the player's payoff under the assumption that their "opponent" is friendly
 * and will try to do the same. This amounts to finding the joint aciton that has the maximum payoff for the query player
 * and playing the corresponding action. For example, if rowPayoff[i][j] is the maximum element
 * in the matrix, the {@link #computeRowStrategy(double[][], double[][])} method will return a strategy that always plays aciton
 * i. If there are ties for the maximum with different row actions, then the strategy returns is uniformly random
 * among the ties. The {@link #computeColStrategy(double[][], double[][])} does the same, except for the colPayoff matrix
 * and for column actions.
 *
 * @author James MacGlashan
 *
 */
public class MaxMax extends BimatrixEquilibriumSolver {

	@Override
	public double[] computeRowStrategy(double [][] rowPayoff, double [][] colPayoff) {
		
		double max = Double.NEGATIVE_INFINITY;
		Set<Integer> candidates = new HashSet<Integer>(rowPayoff.length);
		for(int i = 0; i < rowPayoff.length; i++){
			for(int j = 0; j < rowPayoff[i].length; j++){
				if(BimatrixEquilibriumSolver.doubleEquality(max, rowPayoff[i][j])){
					candidates.add(i);
				}
				else if(rowPayoff[i][j] > max){
					max = rowPayoff[i][j];
					candidates.clear();
					candidates.add(i);
				}
			}
		}
		
		double p = 1. / (double)candidates.size();
		
		double [] strat = new double[rowPayoff.length];
		for(int i = 0; i < strat.length; i++){
			if(candidates.contains(i)){
				strat[i] = p;
			}
			else{
				strat[i] = 0.;
			}
		}
		
		return strat;
	}

	@Override
	public double[] computeColStrategy(double [][] rowPayoff, double [][] colPayoff) {
		double max = Double.NEGATIVE_INFINITY;
		Set<Integer> candidates = new HashSet<Integer>(rowPayoff.length);
		for(int j = 0; j < colPayoff[0].length; j++){
			for(int i = 0; i < colPayoff.length; i++){
				if(BimatrixEquilibriumSolver.doubleEquality(max, colPayoff[i][j])){
					candidates.add(j);
				}
				else if(colPayoff[i][j] > max){
					max = colPayoff[i][j];
					candidates.clear();
					candidates.add(i);
				}
			}
		}
		
		double p = 1. / (double)candidates.size();
		
		double [] strat = new double[colPayoff[0].length];
		for(int i = 0; i < strat.length; i++){
			if(candidates.contains(i)){
				strat[i] = p;
			}
			else{
				strat[i] = 0.;
			}
		}
		
		return strat;
	}

}
