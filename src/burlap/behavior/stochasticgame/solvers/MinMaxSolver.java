package burlap.behavior.stochasticgame.solvers;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.joptimizer.optimizers.LPOptimizationRequest;
import com.joptimizer.optimizers.LPPrimalDualMethod;

public class MinMaxSolver {

	
	
	
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
		
		org.apache.log4j.BasicConfigurator.configure();
		LogManager.getRootLogger().setLevel(Level.OFF);
		
		//get positive matrix (finds the minimum value and adds -min + 1 to all elements)
		double [][] posMatrix = GeneralBimatrixSolverTools.getPositiveMatrix(payoffMatrix);
		
		//because we want to switch to <= that joptimizer wants, multiply by -1
		double [][] G = GeneralBimatrixSolverTools.getNegatedMatrix(posMatrix);
		
		//RHS of inequality is also inverted to -1s
		double [] h = GeneralBimatrixSolverTools.constantDoubleArray(-1., G.length);

		//lower bound
		double [] lb = GeneralBimatrixSolverTools.constantDoubleArray(0., G[0].length);
		
		//objective
		double [] c = GeneralBimatrixSolverTools.constantDoubleArray(1., G[0].length);
		
		//optimization problem
		LPOptimizationRequest or = new LPOptimizationRequest();
		or.setC(c);
		or.setG(G);
		or.setH(h);
		or.setLb(lb);
		or.setDumpProblem(true); 
		
		//optimization
		LPPrimalDualMethod opt = new LPPrimalDualMethod();		
		opt.setLPOptimizationRequest(or);

		try {
			opt.optimize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		double[] sol = opt.getOptimizationResponse().getSolution();
		
		//convert LP solution into probability vector.
		double z = 0.;
		for(Double d : sol){
			z += d;
		}
		
		double v = 1/z;
		
		for(int i = 0; i < sol.length; i++){
			sol[i] *= v;
		}
		
		
		
		return sol;
		
	}
	
	
	
}
