package burlap.behavior.stochasticgames.solvers;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

import com.joptimizer.optimizers.LPOptimizationRequest;
import com.joptimizer.optimizers.LPPrimalDualMethod;

/**
 * This class provides static methods for solving correlated equilibrium problems for Bimatrix games or values represented in a Bimatrix.
 * Correlated equilibrium provide joint strategy for the agents to follow such that there would be no incentive
 * for an agent to change their behavior if a referee selected a joint action accroding to the joint strategy and informed
 * each agent of the action they should take, assuming all other agents would follow their selected action.
 * <p>
 * 4 different correlated equilibrium problems can be solved:
 * utilitarian, egalitarian, libertarian, and republican, as defined by Greenwald and Hall [1]. The utilitarian
 * objective maximizes the sum payoff of the found joint strategy. Egalitarian maximizes the minimum player payoff.
 * Republican maximizes the maximum player payoff. Liberatarian maximizes the payoff for a specific player.
 * These different objectives can be specified with the {@link CorrelatedEquilibriumObjective} enumeration.
 * 
 * <p>
 * The primary method
 * to use to get the equilibrium is the {@link #getCorrelatedEQJointStrategy(CorrelatedEquilibriumObjective, double[][], double[][])} method.
 * 
 * <p>
 * This class depends on the SCPSolver library.
 * 
 * 
 * <p>
 * 1. Greenwald, Amy, Keith Hall, and Roberto Serrano. "Correlated Q-learning." ICML. Vol. 3. 2003.
 * @author James MacGlashan
 *
 */
public class CorrelatedEquilibriumSolver {

    private CorrelatedEquilibriumSolver() {
        // do nothing
    }
    
	/**
	 * The four different equilibrium objectives that can be used:
	 * UTILITARIAN, EGALITARIAN, REPUBLICAN, and LIBERTARIAN.
	 * The utilitarian
	 * objective maximizes the sum payoff of the found joint strategy. Egalitarian maximizes the minimum player payoff.
	 * Republican maximizes the maximum player payoff. Liberatarian maximizes the payoff for a specific player.
	 * @author James MacGlashan
	 *
	 */
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
		//double [][] jointActionProbs = getCorrelatedEQJointStrategy(CorrelatedEquilibriumObjective.EGALITARIAN, chickenRow, chickenCol);
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
	
	
	/**
	 * Returns the correlated equilibrium joint strategy in a 2D double matrix, which represents the probability of each joint actino (where rows
	 * are player 1s actions and columns are player 2's actions). If the @{linke {@link CorrelatedEquilibriumObjective#LIBERTARIAN} objective
	 * is selected, it will maximize with respect to the row player's payoffs and return the strategy from their perspecitve. Therefore
	 * for a combined joint strategy for each player in libertrain, this method should then be called for each player
	 * and then combined as necessary to get a final joint strategy that will be followed by the players.
	 * @param objectiveType the maximizing objective for the correlated equilibrium being solved.
	 * @param payoffRow the payoff for the player whose actions correspond to the rows of the matrix
	 * @param payoffCol the payoff for the player whose actions correspond to the columns of the matrix
	 * @return the correlated equilibrium joint strategy as a 2D double matrix.
	 */
	public static double [][] getCorrelatedEQJointStrategy(CorrelatedEquilibriumObjective objectiveType, double [][] payoffRow, double [][] payoffCol){
		
		if(objectiveType.equals(CorrelatedEquilibriumObjective.UTILITARIAN)){
			return getCorrelatedEQJointStrategyUtilitarian(payoffRow, payoffCol);
		}
		if(objectiveType.equals(CorrelatedEquilibriumObjective.EGALITARIAN)){
			return getCorrelatedEQJointStrategyEgalitarian(payoffRow, payoffCol);
		}
		else if(objectiveType.equals(CorrelatedEquilibriumObjective.REPUBLICAN)){
			return getCorrelatedEQJointStrategyRepublican(payoffRow, payoffCol);
		}
		else if(objectiveType.equals(CorrelatedEquilibriumObjective.LIBERTARIAN)){
			return getCorrelatedEQJointStrategyLibertarianForRow(payoffRow, payoffCol);
		}
		
		throw new RuntimeException("Unknown objective type");
		
	}
	
	
	/**
	 * Returns the correlated equilibrium joint strategy in a 2D double matrix for the Utilitarian objective. 
	 * @param payoffRow the payoff for the player whose actions correspond to the rows of the matrix
	 * @param payoffCol the payoff for the player whose actions correspond to the columns of the matrix
	 * @return the correlated equilibrium joint strategy as a 2D double matrix.
	 */
	public static double [][] getCorrelatedEQJointStrategyUtilitarian(double [][] payoffRow, double [][] payoffCol){
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int n = nRows * nCols;
		
		double [] c = getUtilitarianObjective(payoffRow, payoffCol);
		LinearProgram lp = new LinearProgram(c);
		
		int cCount = 0;
		cCount = addCorrelatedEquilibriumMainConstraints(lp, payoffRow, payoffCol, n, cCount);
		
		return runLPAndGetJointActionProbs(lp, nRows, nCols);
		
	}
	
	
	/**
	 * Returns the correlated equilibrium joint strategy in a 2D double matrix for the Egalitarian objective. 
	 * @param payoffRow the payoff for the player whose actions correspond to the rows of the matrix
	 * @param payoffCol the payoff for the player whose actions correspond to the columns of the matrix
	 * @return the correlated equilibrium joint strategy as a 2D double matrix.
	 */
	public static double [][] getCorrelatedEQJointStrategyEgalitarian(double [][] payoffRow, double [][] payoffCol){
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int n = (nRows * nCols) + 1; //1 auxiliary variable
		
		double [] c = getEgalitarianObjective(payoffRow, payoffCol);
		LinearProgram lp = new LinearProgram(c);
		
		int cCount = 0;
		cCount = addCorrelatedEquilibriumMainConstraints(lp, payoffRow, payoffCol, n, cCount);
		
		//also need to add constraint that the aux value variable is less than or equal to any specific agents
		//value. This means we need to sum up their value, place a negative on the aux, and be >= 0
		double [] rowPlayerConstraint = new double[n];
		double [] colPlayerConstraint = new double[n];
		for(int i = 0; i < n-1; i++){
			int [] rc = rowCol(i, nCols);
			rowPlayerConstraint[i] = payoffRow[rc[0]][rc[1]];
			colPlayerConstraint[i] = payoffCol[rc[0]][rc[1]];
		}
		rowPlayerConstraint[n-1] = -1;
		colPlayerConstraint[n-1] = -1;
		
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(rowPlayerConstraint, 0.0, "c" + cCount));
		cCount++;
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(colPlayerConstraint, 0.0, "c" + cCount));
		
		
		return runLPAndGetJointActionProbs(lp, nRows, nCols);
		
	}
	
	
	/**
	 * Returns the correlated equilibrium joint strategy in a 2D double matrix for the Republican objective. 
	 * @param payoffRow the payoff for the player whose actions correspond to the rows of the matrix
	 * @param payoffCol the payoff for the player whose actions correspond to the columns of the matrix
	 * @return the correlated equilibrium joint strategy as a 2D double matrix.
	 */
	public static double [][] getCorrelatedEQJointStrategyRepublican(double [][] payoffRow, double [][] payoffCol){
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int n = nRows * nCols;
		
		//check row player solution value
		double [] cRow = getRepublicanObjective(payoffRow);
		LinearProgram lpRow = new LinearProgram(cRow);
		
		int cCount = 0;
		cCount = addCorrelatedEquilibriumMainConstraints(lpRow, payoffRow, payoffCol, n, cCount);
		
		double [][] rowSol = runLPAndGetJointActionProbs(lpRow, nRows, nCols);
		double rowSolVal = GeneralBimatrixSolverTools.expectedPayoffs(payoffRow, payoffCol, rowSol)[0];
		
		
		//check col player solution value
		double [] cCol = getRepublicanObjective(payoffCol);
		LinearProgram lpCol = new LinearProgram(cCol);
		
		cCount = 0;
		cCount = addCorrelatedEquilibriumMainConstraints(lpCol, payoffRow, payoffCol, n, cCount);
		
		double [][] colSol = runLPAndGetJointActionProbs(lpCol, nRows, nCols);
		double colSolVal = GeneralBimatrixSolverTools.expectedPayoffs(payoffRow, payoffCol, colSol)[0];
		
		if(rowSolVal > colSolVal){
			return rowSol;
		}
		
		return colSol;
	}
	
	/**
	 * Returns the correlated equilibrium joint strategy in a 2D double matrix for the Libertarian objective. The player payoff
	 * being used for maximization is the row player.
	 * @param payoffRow the payoff for the player whose actions correspond to the rows of the matrix
	 * @param payoffCol the payoff for the player whose actions correspond to the columns of the matrix
	 * @return the correlated equilibrium joint strategy as a 2D double matrix.
	 */
	public static double [][] getCorrelatedEQJointStrategyLibertarianForRow(double [][] payoffRow,  double [][] payoffCol){
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int n = nRows * nCols;
		
		//check row player solution value
		double [] cRow = getRepublicanObjective(payoffRow);
		LinearProgram lpRow = new LinearProgram(cRow);
		
		int cCount = 0;
		cCount = addCorrelatedEquilibriumMainConstraints(lpRow, payoffRow, payoffCol, n, cCount);
		
		return runLPAndGetJointActionProbs(lpRow, nRows, nCols);
	
	}
	
	/**
	 * Returns the correlated equilibrium joint strategy in a 2D double matrix for the Libertarian objective. The player payoff
	 * being used for maximization is the column player.
	 * @param payoffRow the payoff for the player whose actions correspond to the rows of the matrix
	 * @param payoffCol the payoff for the player whose actions correspond to the columns of the matrix
	 * @return the correlated equilibrium joint strategy as a 2D double matrix.
	 */
	public static double [][] getCorrelatedEQJointStrategyLibertarianForCol(double [][] payoffRow,  double [][] payoffCol){
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int n = nRows * nCols;
		
		//check row player solution value
		double [] cRow = getRepublicanObjective(payoffCol);
		LinearProgram lpRow = new LinearProgram(cRow);
		
		int cCount = 0;
		cCount = addCorrelatedEquilibriumMainConstraints(lpRow, payoffRow, payoffCol, n, cCount);
		
		return runLPAndGetJointActionProbs(lpRow, nRows, nCols);
	
	}
	
	
	/**
	 * Helper method for running the linear program optimization (after its constraints have already been set) and returning
	 * the result in the form of the 2D double matrix joint strategy.
	 * @param lp the linear program to be optimized
	 * @param nRows the number of rows in the bimatrix (number of player 1 actions)
	 * @param nCols the number of columns in the bimatrix (number of player 2 actions)
	 * @return a 2D double representing the joint strategy for the given linear program correlated equilibrium problem.
	 */
	protected static double [][] runLPAndGetJointActionProbs(LinearProgram lp, int nRows, int nCols){
		
		int nn = nRows*nCols;
		
		lp.setMinProblem(false); 
		LinearProgramSolver solver = SolverFactory.newDefault(); 
		double[] sol = solver.solve(lp);
		
		double [][] jointActionProbs = new double[nRows][nCols];
		for(int i = 0; i < nn; i++){
			int [] rc = rowCol(i, nCols);
			jointActionProbs[rc[0]][rc[1]] = sol[i];
		}
		
		
		return jointActionProbs;
	}
	
	/**
	 * Adds the common LP constraints for the correlated equilribum problem: rationalaity constraits (no agent has a motivation to diverge
	 * from a joint policy selection), the probability of all joint action variables must sum to 1, and all joint action variables are lower
	 * bound at 0.0. Note that depending on the objective, additional variables beyond the joint action variables may be part
	 * of the lp formulation. It is assumed that the first variables in the variable arrays are the sequnce of joint action variables
	 * and that any auxiliary variables that are necessary follow them. The parameter n is the total number of lp variables (joint actions
	 * and auxiliary). 
	 * @param lp the lineary program to which the constraints will be added
	 * @param payoffRow the payoffs for the row player
	 * @param payoffCol the payoffs for the col player
	 * @param n the total number of lp variables
	 * @param cCount the number of constraints that have been added to the lp so far
	 * @return the new number of constraints added to the lp
	 */
	protected static int addCorrelatedEquilibriumMainConstraints(LinearProgram lp, double [][] payoffRow, double [][] payoffCol, int n, int cCount){
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int nn = nRows*nCols;
		
		//add player 1 action constraints
		for(int a1 = 0; a1 < nRows; a1++){
			//consider constraint of taking other action
			for(int a1prime = 0; a1prime < nRows; a1prime++){
				if(a1prime == a1){
					continue;
				}
				double [] ineq = GeneralBimatrixSolverTools.constantDoubleArray(0., n);
				for(int a2 = 0; a2 < nCols; a2++){
					int ind = jointIndex(a1, a2, nCols);
					
					//>= coeffecient
					double geVal = payoffRow[a1][a2] - payoffRow[a1prime][a2];
					ineq[ind] = geVal;
					
				}
				
				lp.addConstraint(new LinearBiggerThanEqualsConstraint(ineq, 0., "c" + cCount));
				cCount++;
			}
		}
		
		//add player 2 action constraints
		for(int a2 = 0; a2 < nCols; a2++){
			//consider constraint of taking other action
			for(int a2prime = 0; a2prime < nCols; a2prime++){
				if(a2prime == a2){
					continue;
				}
				double [] ineq = GeneralBimatrixSolverTools.constantDoubleArray(0., n);
				for(int a1 = 0; a1 < nRows; a1++){
					int ind = jointIndex(a1, a2, nCols);
					
					//>= coefficient
					double geVal = payoffCol[a1][a2] - payoffCol[a1][a2prime];
					ineq[ind] = geVal;
					
				}
				lp.addConstraint(new LinearBiggerThanEqualsConstraint(ineq, 0., "c" + cCount));
				cCount++;
				
			}
		}
		
		//add sum to 1 constraint
		double [] eqConst = GeneralBimatrixSolverTools.constantDoubleArray(0., n);
		//only set the normal joint distribution variables to coefficients of 1
		for(int i = 0; i < nn; i++){
			eqConst[i] = 1.;
		}
		lp.addConstraint(new LinearEqualsConstraint(eqConst, 1., "c" + cCount));
		cCount++;
		
		//add lower bound constraints; one for each of the joint action variables (not any auxiliary variables)
		for(int i = 0; i < nn; i++){
			double [] lb = GeneralBimatrixSolverTools.zero1Array(i, n);
			lp.addConstraint(new LinearBiggerThanEqualsConstraint(lb, 0., "c" + cCount));
			cCount++;
		}
		
		return cCount;
		
	}
	
	
	/**
	 * Old code for solving the utilitarian equilibrium with JOptimizer. JOptmizer is often not able to find solutions to these
	 * problems because it uses an interior point method that requires an feasible solution on the interior and in the initial
	 * stages of stochastic games VI, this may not be the case. Therefore, this code is deprecatd, but is retained for reference.
	 * @param payoffRow the payoffs for the row player
	 * @param payoffCol the payoffs for the col player
	 * @return the correlated equilibrium
	 */
	@Deprecated
	protected static double [][] getCorrelatedEQJointStrategyUsingJOptimizer(double [][] payoffRow, double [][] payoffCol){
		
		org.apache.log4j.BasicConfigurator.configure();
		LogManager.getRootLogger().setLevel(Level.OFF);
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int n = nRows * nCols;
		
		double [] c = GeneralBimatrixSolverTools.getNegatedArray(getUtilitarianObjective(payoffRow, payoffCol));
		
		double [][] A = new double [1][n];
		for(int i = 0; i < n; i++){
			A[0][i] = 1.;
		}
		double [] b = new double[]{1.};
		
		double [] lb = GeneralBimatrixSolverTools.constantDoubleArray(0., n);
		//double [] lb = GeneralBimatrixSolverTools.constantDoubleArray(-1e-5, n);
		
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
		
		//shrink G to just nonzero rows
		G = removeZeroRows(G);
		
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
		
		
		double[] sol = roundNegativesToZero(opt.getOptimizationResponse().getSolution());
		
		double [][] jointActionProbs = new double[nRows][nCols];
		for(int i = 0; i < sol.length; i++){
			int [] rc = rowCol(i, nCols);
			jointActionProbs[rc[0]][rc[1]] = sol[i];
		}
		
		
		return jointActionProbs;
		
	}
	
	
	/**
	 * Returns the utilitarian objective for the given payoffs for the row and column player.
	 * @param payoffRow the row player's payoffs
	 * @param payoffCol the column player's payoffs
	 * @return the objective function as a double array of the LP variable coeffecients.
	 */
	public static double [] getUtilitarianObjective(double [][] payoffRow, double [][] payoffCol){
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int n = nRows * nCols;
		
		double [] objective = new double[n];
		for(int i = 0; i < n; i++){
			int [] rc = rowCol(i, nCols);
			int r = rc[0];
			int c = rc[1];
			
			objective[i] = (payoffRow[r][c] + payoffCol[r][c]);
		}
		
		
		return objective;
		
	}
	
	
	/**
	 * Returns the egalitarian objective for the given payoffs for the row and column player.
	 * @param payoffRow the row player's payoffs
	 * @param payoffCol the column player's payoffs
	 * @return the objective function as a double array of the LP variable coeffecients.
	 */
	public static double [] getEgalitarianObjective(double [][] payoffRow, double [][] payoffCol){
		
		int nRows = payoffRow.length;
		int nCols = payoffRow[0].length;
		int n = (nRows * nCols) + 1;
		
		double [] objective = GeneralBimatrixSolverTools.zero1Array(n-1, n);
		
		
		return objective;
		
	}
	
	
	/**
	 * Returns the republican/libertarian objective for the given player's payoffs that are to be maximized. The repubilican objective
	 * will use this method to solve an LP for each player. The libertarian will use this method to solve a single LP for the selected player.
	 * @param payoffForQueryPlayer the given player's player's payoffs that are to be maximized
	 * @return the objective function as a double array of the LP variable coeffecients.
	 */
	public static double [] getRepublicanObjective(double [][] payoffForQueryPlayer){
		
		int nRows = payoffForQueryPlayer.length;
		int nCols = payoffForQueryPlayer[0].length;
		int n = nRows * nCols;
		
		double [] objective = new double[n];
		for(int i = 0; i < n; i++){
			int [] rc = rowCol(i, nCols);
			int r = rc[0];
			int c = rc[1];
			
			objective[i] = payoffForQueryPlayer[r][c];
		}
		
		
		return objective;
		
	}
	
	
	/**
	 * Returns the 1D array index for a given row and column of a matrix with the given number of columns.
	 * Computed as r * nCols + c;
	 * @param r the row index
	 * @param c the column index
	 * @param nCols the number of columns in the matrix
	 * @return the 1D array index
	 */
	protected static int jointIndex(int r, int c, int nCols){
		return r*nCols + c;
	}
	
	
	/**
	 * Returns the 2D row column index in a matrix of a given number of columns for a given 1D array index.
	 * The row is computed as i / nCols; the column is computed as i mod nCols.
	 * @param i the 1D array index
	 * @param nCols the number of columns in the matrix
	 * @return an int array of length 2, with a[0] = rowIndex and a[1] = columnIndex.
	 */
	protected static int [] rowCol(int i, int nCols){
		return new int[]{i / nCols, i % nCols};
	}
	
	
	/**
	 * Takes an input 2D double matrix and returns a new matrix will all the all zero rows removed.
	 * @param m input 2D double matrix
	 * @return a new matrix will all the all zero rows removed.
	 */
	protected static double [][] removeZeroRows(double [][] m){
		
		//first fill in only non zero rows
		int n = 0;
		double [][] m2 = new double[m.length][m[0].length];
		for(int i = 0; i < m.length; i++){
			if(!isZeroArray(m[i])){
				for(int j = 0; j < m[i].length; j++){
					m2[n][j] = m[i][j];
				}
				n++;
			}
		}
		
		if(n == m.length){
			return m2;
		}
		
		//now shrink to only rows that matter
		double [][] m3 = new double[n][m[0].length];
		for(int i = 0; i < n; i++){
			for(int j = 0; j < m2[i].length; j++){
				m3[i][j] = m2[i][j];
			}
		}
		
		
		
		return m3;
	}
	
	
	/**
	 * Returns true if a if the input array is all zeros.
	 * @param a the input array
	 * @return true if a if the input array is all zeros; false otherwise
	 */
	protected static boolean isZeroArray(double [] a){
		for(double d : a){
			if(d != 0.){
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Creates a new 1D double array with all negative values rounded to 0.
	 * @param a and input array
	 * @return a new 1D double array with all negative values rounded to 0.
	 */
	public static double [] roundNegativesToZero(double [] a){
		double [] b = new double[a.length];
		for(int i = 0; i < a.length; i++){
			if(a[i] > 0){
				b[i] = a[i];
			}
			else{
				b[i] = 0.;
			}
		}
		return b;
	}
	
	
	
	
}
