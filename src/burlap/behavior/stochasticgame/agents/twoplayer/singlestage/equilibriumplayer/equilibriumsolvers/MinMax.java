package burlap.behavior.stochasticgame.agents.twoplayer.singlestage.equilibriumplayer.equilibriumsolvers;

import burlap.behavior.stochasticgame.agents.twoplayer.singlestage.equilibriumplayer.BimatrixEquilibriumSolver;
import burlap.behavior.stochasticgame.solvers.MinMaxSolver;


/**
 * Finds the MinMax equilibrium using linear programming and returns the appropraite strategy. Note that
 * if the game is not zero sum, the resulting strategy produced will be as if it was by setting the opponent's payoff
 * matrix to the the negation of the querying player.
 * @author James MacGlashan
 *
 */
public class MinMax extends BimatrixEquilibriumSolver {

	@Override
	public double[] computeRowStrategy(double[][] rowPayoff,
			double[][] colPayoff) {
		return MinMaxSolver.getRowPlayersStrategy(rowPayoff);
	}

	@Override
	public double[] computeColStrategy(double[][] rowPayoff,
			double[][] colPayoff) {
		return MinMaxSolver.getColPlayersStrategy(colPayoff);
	}

}
