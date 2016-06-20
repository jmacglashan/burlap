package burlap.behavior.stochasticgames.solvers;


/**
 * A class holding static methods for performing common operations on bimatrix games.
 * @author James MacGlashan
 *
 */
public class GeneralBimatrixSolverTools {
    
    private GeneralBimatrixSolverTools() {
        // do nothing
    }
	
	
	/**
	 * Computes the expected payoff for each player in a bimatrix game according to their strategies. 
	 * @param payoffRowPlayer the payoff for player 1. Rows are player 1's actions; columns player 2's.
	 * @param payoffColPlayer the payoff for player 2. Rows are player 1's actions; columns player 2's.
	 * @param rowPlayerStrategy the strategy for player 1. Should be array with dimension equal to the number of player 1's actions.
	 * @param colPlayerStrategy the stratgy for player 2. Should be array with dimension equal to the number of player 2's actions.
	 * @return a double array of dimension two. Index 0 holds the first player's expected payoff; index 1 holds the second player's expected payoff.
	 */
	public static double [] expectedPayoffs(double [][] payoffRowPlayer, double [][] payoffColPlayer, double [] rowPlayerStrategy, double [] colPlayerStrategy){
		
		double [][] joint = jointActionProbabilities(rowPlayerStrategy, colPlayerStrategy);
		return expectedPayoffs(payoffRowPlayer, payoffColPlayer, joint);
		
	}
	
	
	public static double [] expectedPayoffs(double [][] payoffRowPlayer, double [][] payoffColPlayer, double [][] jointActionProbabilities){
		
		double [] ep = new double[]{0., 0.};
		for(int i = 0; i < jointActionProbabilities.length; i++){
			for(int j = 0; j < jointActionProbabilities[i].length; j++){
				ep[0] += jointActionProbabilities[i][j] * payoffRowPlayer[i][j];
				ep[1] += jointActionProbabilities[i][j] * payoffColPlayer[i][j];
			}
		}
		
		return ep;
		
	}
	
	/**
	 * Computes the joint action probabilities accroding to each player's strategy and returns it as a matrix.
	 * @param rowPlayerStrategy the strategy of the player whose actions will be represented by the rows of the returned matrix.
	 * @param colPlayerStrategy the strategy of the player whose actions will represented by the columns of the returned matrix.
	 * @return the joint action probabilities in a matrix. m[i][j] represents the probability of the row player selecting action i and the column player selecting action j.
	 */
	public static double [][] jointActionProbabilities(double [] rowPlayerStrategy, double [] colPlayerStrategy){
		
		double [][] joint = new double[rowPlayerStrategy.length][colPlayerStrategy.length];
		
		for(int i = 0; i < rowPlayerStrategy.length; i++){
			for(int j = 0; j < colPlayerStrategy.length; j++){
				joint[i][j] = rowPlayerStrategy[i] * colPlayerStrategy[j];
			}
		}
		
		return joint;
		
	}
	
	
	/**
	 * Returns the row player's strategy by marginalizing it out from a joint action probability distribution represented as a matrix
	 * @param jointActionProbabilities a matrix of 2-player joint aciton probability distribution
	 * @return the row player's strategy
	 */
	public static double [] marginalizeRowPlayerStrategy(double [][] jointActionProbabilities){
		
		double [] strategy = new double[jointActionProbabilities.length];
		
		for(int i = 0; i < jointActionProbabilities.length; i++){
			double sum = 0.;
			for(int j = 0; j < jointActionProbabilities[i].length; j++){
				sum += jointActionProbabilities[i][j];
			}
			strategy[i] = sum;
		}
		
		return strategy;
	}
	
	/**
	 * Returns the column player's strategy by marginalizing it out from a joint action probability distribution represented as a matrix
	 * @param jointActionProbabilities a matrix of 2-player joint aciton probability distribution
	 * @return the column player's strategy
	 */
	public static double [] marginalizeColPlayerStrategy(double [][] jointActionProbabilities){
		
		double [] strategy = new double[jointActionProbabilities[0].length];
		
		for(int j = 0; j < jointActionProbabilities[0].length; j++){
			double sum = 0.;
			for(int i = 0; i < jointActionProbabilities.length; i++){
				sum += jointActionProbabilities[i][j];
			}
			strategy[j] = sum;
		}
		
		return strategy;
	}
	
	
	/**
	 * Returns a double array of a given dimension filled with the same value.
	 * @param constant the constant value with which the double array is filled.
	 * @param dimension the dimension of the double array.
	 * @return a double array of size dimension filled with the value constant.
	 */
	public static double [] constantDoubleArray(double constant, int dimension){
		double [] a = new double[dimension];
		for(int i = 0; i < dimension; i++){
			a[i] = constant;
		}
		return a;
	}
	
	
	
	/**
	 * Returns a negated version of the input matrix in a new matrix object. That is,
	 * a new 2D double array (m2) is created with the m2[i][j] value set to -1 * m[i][j].
	 * @param m the input matrix.
	 * @return the negated matrix.
	 */
	public static double [][] getNegatedMatrix(double [][] m){
		double [][] m2 = new double[m.length][m[0].length];
		
		for(int i = 0; i < m.length; i++){
			for(int j = 0; j < m[i].length; j++){
				m2[i][j] = -m[i][j];
			}
		}
		
		return m2;
	}
	
	
	/**
	 * Returns a negated version of the input array in a new array object. That is,
	 * a new double array (b) is created with b[i] value set to -1 * a[i].
	 * @param a the input double array
	 * @return a negated version of the input array
	 */
	public static double [] getNegatedArray(double [] a){
		double [] b = new double[a.length];
		
		for(int i = 0; i < a.length; i++){
			b[i] = -a[i];
		}
		
		return b;
	}
	
	
	/**
	 * Creates a new matrix (m2) whose values are the values of m shifted by a constant amount c such that all the values
	 * in m2 are positive. If all entries in m are already postive, then c = 0 (no shift). Otherwise, c = 1 - min(m).
	 * @param m the input matrix.
	 * @return a constant shifted version of m that is positive.
	 */
	public static double [][] getPositiveMatrix(double [][] m){
		double [][] m2 = new double[m.length][m[0].length];
		double min = Double.POSITIVE_INFINITY;
		for(int i = 0; i < m.length; i++){
			for(int j = 0; j < m[i].length; j++){
				if(m[i][j] < min){
					min = m[i][j];
				}
			}
		}
		
		if(min > 0.){
			return m.clone();
		}
		
		double add = -min + 1.;
		for(int i = 0; i < m.length; i++){
			for(int j = 0; j < m[i].length; j++){
				m2[i][j] = m[i][j] + add;
			}
		}
		
		return m2;
	}
	
	
	/**
	 * Creates and returns a new matrix that is a transpose of m.
	 * @param m the input matrix.
	 * @return the transposed version of m.
	 */
	public static double [][] transposeMatrix(double [][] m){
		double [][] m2 = new double[m[0].length][m.length];
		
		for(int i = 0; i < m.length; i++){
			for(int j = 0; j < m[i].length; j++){
				m2[j][i] = m[i][j];
			}
		}
		
		return m2;
	}
	
	
	/**
	 * Returns the dot product of two vectors
	 * @param a first vector
	 * @param b second vector
	 * @return the dot product
	 */
	public static double dot(double [] a, double [] b){
		double sum = 0.;
		for(int i = 0; i < a.length; i++){
			sum += a[i] * b[i];
		}
		return sum;
	}
	
	
	/**
	 * Creates an array that is all zeros except one index which has a value of 1. For example zero1Array(2, 4) will return {0., 0., 1., 0.}.
	 * @param index the index which will have a valu of 1
	 * @param dim the dimension of the array to create
	 * @return an array that is all zeros except one index which has a value of 1.
	 */
	public static double [] zero1Array(int index, int dim){
		double [] a = new double[dim];
		for(int i = 0; i < dim; i++){
			if(i != index){
				a[i] = 0.;
			}
			else{
				a[i] = 1.;
			}
		}
		return a;
	}
	
}
