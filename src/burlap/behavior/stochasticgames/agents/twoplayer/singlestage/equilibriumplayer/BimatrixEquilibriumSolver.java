package burlap.behavior.stochasticgames.agents.twoplayer.singlestage.equilibriumplayer;


/**
 * This abstract class is used for computing the strategies according to a solution concept for a single stage bimatrix game.
 * The {@link #solve(double[][], double[][])} method takes as input the payoff matrice for the row and column players of the Bimatrix game.
 * If the Bimatrix is identical the to Bimatrix requested in the previous call to the solve method (if it was previously called), then
 * nothing is done as the previous results are cached and can be retrieved. If the bimatrix is different, then the strategy
 * for the row player and column player are computed using the abstract {@link #computeRowStrategy(double[][], double[][])}
 * and {@link #computeColStrategy(double[][], double[][])} methods and then cached. The cahced results
 * can be retreived using the {@link #getLastComputedRowStrategy()} and {@link #getLastComputedColStrategy()} methods.
 * 
 * <p/>
 * Note that the bimatrix is compared to the last cached version for equality with an epislon difference comparions
 * of the individual double values. That is, if the difference absolute difference |a - b| < {@link #doubleEpislon},
 * then the a and b are treated as equal. By default, {@link #doubleEpislon} is set to 1e-8; but this is a public
 * static member that can be changed.
 * @author James MacGlashan
 *
 */
public abstract class BimatrixEquilibriumSolver {
	
	/**
	 * The epislon difference used to test for double equality.
	 */
	public static double doubleEpislon = 1e-8;
	
	
	/**
	 * The last cached row player strategy
	 */
	protected double [] lastRowStrategy = null;
	
	/**
	 * The last cached column player strategy
	 */
	protected double [] lastColsStrategy = null;
	
	/**
	 * The last cached row player payoff matrix
	 */
	protected double [][] lastRowPlayerPayoff = null;
	
	/**
	 * The last cached column player payoff matrix
	 */
	protected double [][] lastColPlayerPayoff = null;
	
	
	/**
	 * Solves and caches the solution for the given bimatrix. If the given bimatrix
	 * is the same as the last bimatrix solved by this object (if there was one), then
	 * nothing is computed and the same solution is returned.
	 * @param rowPayoff the row player payoff matrix
	 * @param colPayoff the column player payoff matrix
	 */
	public void solve(double [][] rowPayoff, double [][] colPayoff){
		if(!this.bimatrixEqualsLast(rowPayoff, colPayoff)){
			this.lastRowPlayerPayoff = rowPayoff;
			this.lastColPlayerPayoff = colPayoff;
			this.lastRowStrategy = computeRowStrategy(rowPayoff, colPayoff);
			this.lastColsStrategy = computeColStrategy(rowPayoff, colPayoff);
		}
	}
	
	
	/**
	 * Returns the last row player strategy computed by the {@link #solve(double[][], double[][])} method.
	 * The strategy is represented as a double array where a[i] is the probability of action i being selected.
	 * @return the last row player strategy.
	 */
	public double [] getLastComputedRowStrategy(){
		return this.lastRowStrategy;
	}
	
	/**
	 * Returns the last column player strategy computed by the {@link #solve(double[][], double[][])} method.
	 * The strategy is represented as a double array where a[i] is the probability of action i being selected.
	 * @return the last column player strategy.
	 */
	public double [] getLastComputedColStrategy(){
		return this.lastColsStrategy;
	}
	
	
	/**
	 * Computes and returns the row player strategy for the given bimatrix game. 
	 * The strategy is represented as a double array where a[i] is the probability of action i being selected.
	 * @param rowPayoff the row player payoffs.
	 * @param colPayoff the column player payoffs.
	 * @return the row player strategy.
	 */
	public abstract double [] computeRowStrategy(double [][] rowPayoff, double [][] colPayoff);
	
	
	/**
	 * Computes and returns the column player strategy for the given bimatrix game. 
	 * The strategy is represented as a double array where a[i] is the probability of action i being selected.
	 * @param rowPayoff the row player payoffs.
	 * @param colPayoff the column player payoffs.
	 * @return the column player strategy.
	 */
	public abstract double [] computeColStrategy(double [][] rowPayoff, double [][] colPayoff);
	
	
	
	/**
	 * Tests whether the inputed bimatrix is equal to the last bimatrix cached by the {@link #solve(double[][], double[][])} method. 
	 * If there have been no previous calls to the {@link #solve(double[][], double[][])} method and therefore no cached bimatrix,
	 * then this method returns false. If the input bimatrix as a different dimensionality that the cached matrix, this method returns false.
	 * If any row or column player payoffs for the input bimatrix are different than cached bimatrix this method returns false. Otherwise
	 * this method returns true.
	 * Equality
	 * between two double values is tested with an epsilon difference where value a and b are evaluated as equal if
	 * |a - b| < {@link #doubleEpislon}. 
	 * @param rowPayoff
	 * @param colPayoff
	 * @return true if the input bimatrix is equal to the last cached bimatrix; false otherwise.
	 */
	protected boolean bimatrixEqualsLast(double [][] rowPayoff, double [][] colPayoff){
		
		if(this.lastRowPlayerPayoff == null){
			return false;
		}
		
		if(rowPayoff.length != lastRowPlayerPayoff.length || rowPayoff[0].length != lastRowPlayerPayoff[0].length){
			return false;
		}
		
		for(int i = 0; i < rowPayoff.length; i++){
			for(int j = 0; j < colPayoff.length; j++){
				if(!doubleEquality(rowPayoff[i][j], this.lastRowPlayerPayoff[i][j]) || !doubleEquality(colPayoff[i][j], this.lastColPlayerPayoff[i][j])){
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	/**
	 * Returns true if |a - b| < {@link #doubleEpislon}; false otherwise.
	 * @param a first input
	 * @param b second input
	 * @return true if |a - b| < {@link #doubleEpislon}; false otherwise.
	 */
	protected static boolean doubleEquality(double a, double b){
		double diff = Math.abs(a-b);
		if(diff < doubleEpislon){
			return true;
		}
		return false;
	}
}
