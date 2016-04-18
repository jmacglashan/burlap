package burlap.behavior.stochasticgames.solvers;

import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

public class MinMaxSolver {
    
    private MinMaxSolver() {
        // do nothing
    }

	
	
	
	/**
	 * Computes the minmax strategy for the row player of the given payoff matrix.
	 * The entries of the payoff matrix are assumed to be the payouts for the *row* player.
	 * @param payoffMatrix payoffs for the row player.
	 * @return the strategy of the row player.
	 */
	public static double [] getRowPlayersStrategy(double [][] payoffMatrix){
		double [][] t = GeneralBimatrixSolverTools.transposeMatrix(payoffMatrix);
		return getColPlayersStrategy(t);
	}
	
	
	
	/**
	 * Computes the minmax strategy for the column player of the given payoff matrix.
	 * The entries of the payoff matrix are assumed to be the payouts for the *column* player.
	 * @param payoffMatrix payoffs for column player.
	 * @return strategy of the column player.
	 */
	public static double [] getColPlayersStrategy(double [][] payoffMatrix){
		
		//get positive matrix (finds the minimum value and adds -min + 1 to all elements)
		double [][] G = GeneralBimatrixSolverTools.getPositiveMatrix(payoffMatrix);
		
		LinearProgram lp = new LinearProgram(GeneralBimatrixSolverTools.constantDoubleArray(1., G[0].length));
		
		int cCount = 0;
		
		//add payoff matrix constraints
		for(int i = 0; i < G.length; i++){
			lp.addConstraint(new LinearBiggerThanEqualsConstraint(G[i], 1., "c" + cCount));
			cCount++;
		}
		
		//add lower bound constraints
		for(int i = 0; i < G[0].length; i++){
			lp.addConstraint(new LinearBiggerThanEqualsConstraint(GeneralBimatrixSolverTools.zero1Array(i, G[0].length), 0., "c" + cCount));
			cCount++;
		}
		
		//solve it
		lp.setMinProblem(true);
		LinearProgramSolver solver = SolverFactory.newDefault(); 
		double[] sol = solver.solve(lp);
		
		//convert LP solution into probability vector.
		double z = 0.;
		for(double d : sol){
			z += d;
		}
		
		double v = 1/z;
		
		for(int i = 0; i < sol.length; i++){
			sol[i] *= v;
		}
		
		
		
		return sol;
	}
	
	
	
	
}
