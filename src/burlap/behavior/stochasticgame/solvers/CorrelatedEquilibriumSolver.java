package burlap.behavior.stochasticgame.solvers;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.joptimizer.optimizers.LPOptimizationRequest;
import com.joptimizer.optimizers.LPPrimalDualMethod;


public class CorrelatedEquilibriumSolver {

	public static enum CorrelatedEquilibriumObjective{
		UTILITARIAN, EGALITARIAN, REPUBLICAN, LIBERTARIAN
	}
	
	public static void main(String [] args){
		
		double [][] chickenRow = new double[][]{
				{6, 2},
				{7, 0}
		};
		
		double [][] chickenCol = new double[][]{
				{6, 7},
				{2, 0}
		};
		
		
		double [][] jointActionProbs = getCorrelatedEQJointStrategy(CorrelatedEquilibriumObjective.UTILITARIAN, chickenRow, chickenCol);
		double [] rowStrategy = GeneralBimatrixSolverTools.marginalizeRowPlayerStrategy(jointActionProbs);
		double [] colStrategy = GeneralBimatrixSolverTools.marginalizeColPlayerStrategy(jointActionProbs);
		
		for(int i = 0; i < jointActionProbs.length; i++){
			for(int j = 0; j < jointActionProbs[i].length; j++){
				System.out.print(jointActionProbs[i][j] + " ");
			}
			System.out.println("");
		}
		
		
		System.out.println("------");
		
		for(int i = 0; i < rowStrategy.length; i++){
			System.out.print(rowStrategy[i] + " ");
		}
		System.out.println("");
		for(int i = 0; i < colStrategy.length; i++){
			System.out.print(colStrategy[i] + " ");
		}
		System.out.println("");
		
	}
	
	public static double [][] getCorrelatedEQJointStrategy(CorrelatedEquilibriumObjective objectiveType, double [][] payoffRow, double [][] payoffCol){
		
		org.apache.log4j.BasicConfigurator.configure();
		LogManager.getRootLogger().setLevel(Level.OFF);
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int n = nRows * nCols;
		
		double [] c = getObjective(objectiveType, payoffRow, payoffCol);
		
		double [][] A = new double [1][n];
		for(int i = 0; i < n; i++){
			A[0][i] = 1.;
		}
		double [] b = new double[]{1.};
		
		double [] lb = GeneralBimatrixSolverTools.constantDoubleArray(0., n);
		
		double [][] G = new double[nRows*(nRows-1) + nCols*(nCols-1)][n];
		//zero out for safety
		for(int i = 0; i < G.length; i++){
			for(int j = 0; j < n; j++){
				G[i][j] = 0.;
			}
		}
		
		
		int gInd = 0;
		
		//add player 1 action constraints
		for(int a1 = 0; a1 < nRows; a1++){
			//consider constraint of taking other action
			for(int a1prime = 0; a1prime < nRows; a1prime++){
				if(a1prime == a1){
					continue;
				}
				for(int a2 = 0; a2 < nCols; a2++){
					int ind = jointIndex(a1, a2, nCols);
					
					//>= coeffecient
					double geVal = payoffRow[a1][a2] - payoffRow[a1prime][a2];
					
					//negate to turn into <= expression that joptimizer expects
					G[gInd][ind] = -geVal;
					
				}
				
				gInd++;
			}
		}
		
		//add player 2 action constraints
		for(int a2 = 0; a2 < nCols; a2++){
			//consider constraint of taking other action
			for(int a2prime = 0; a2prime < nCols; a2prime++){
				if(a2prime == a2){
					continue;
				}
				for(int a1 = 0; a1 < nRows; a1++){
					int ind = jointIndex(a1, a2, nCols);
					
					//>= coefficient
					double geVal = payoffCol[a1][a2] - payoffCol[a1][a2prime];
					
					//negate to turn into <= expression that joptimizer expects
					G[gInd][ind] = -geVal;
				}
				
				gInd++;
			}
		}
		
		double [] h = GeneralBimatrixSolverTools.constantDoubleArray(0., G.length);
		
		
		//optimization problem
		LPOptimizationRequest or = new LPOptimizationRequest();
		or.setC(c);
		or.setG(G);
		or.setH(h);
		or.setLb(lb);
		or.setA(A);
		or.setB(b);
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
		
		double [][] jointActionProbs = new double[nRows][nCols];
		for(int i = 0; i < sol.length; i++){
			int [] rc = rowCol(i, nCols);
			jointActionProbs[rc[0]][rc[1]] = sol[i];
		}
		
		
		return jointActionProbs;
		
	}
	
	public static double [] getObjective(CorrelatedEquilibriumObjective objectiveType, double [][] payoffRow, double [][] payoffCol){
		switch(objectiveType){
			case UTILITARIAN:
				return getUtilitarianObjective(payoffRow, payoffCol);
			default:
				return getUtilitarianObjective(payoffRow, payoffCol);
		}
	}
	
	public static double [] getUtilitarianObjective(double [][] payoffRow, double [][] payoffCol){
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int n = nRows * nCols;
		
		double [] objective = new double[n];
		for(int i = 0; i < n; i++){
			int [] rc = rowCol(i, nCols);
			int r = rc[0];
			int c = rc[1];
			//make negative sum for maximization
			objective[i] = -(payoffRow[r][c] + payoffCol[r][c]);
		}
		
		
		return objective;
		
	}
	
	
	protected static int jointIndex(int r, int c, int nCols){
		return r*nCols + c;
	}
	
	protected static int [] rowCol(int i, int nCols){
		return new int[]{i / nCols, i % nCols};
	}
	
}
