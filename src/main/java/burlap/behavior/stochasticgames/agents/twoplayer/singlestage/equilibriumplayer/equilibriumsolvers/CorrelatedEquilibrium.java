package burlap.behavior.stochasticgames.agents.twoplayer.singlestage.equilibriumplayer.equilibriumsolvers;

import burlap.behavior.stochasticgames.agents.twoplayer.singlestage.equilibriumplayer.BimatrixEquilibriumSolver;
import burlap.behavior.stochasticgames.solvers.CorrelatedEquilibriumSolver;
import burlap.behavior.stochasticgames.solvers.CorrelatedEquilibriumSolver.CorrelatedEquilibriumObjective;
import burlap.behavior.stochasticgames.solvers.GeneralBimatrixSolverTools;


/**
 * Computes the correlated equilibrium strategy. The default constructor will cause this object to compute
 * the correlated equilibrium for the {@link CorrelatedEquilibriumObjective#UTILITARIAN} objective.
 * A different objective may be set using the corresponding constructor.
 * @author James MacGlashan
 *
 */
public class CorrelatedEquilibrium extends BimatrixEquilibriumSolver {

	protected CorrelatedEquilibriumObjective objective = CorrelatedEquilibriumObjective.UTILITARIAN;
	
	public CorrelatedEquilibrium(){
		
	}
	
	public CorrelatedEquilibrium(CorrelatedEquilibriumObjective objective){
		this.objective = objective;
	}
	
	@Override
	public double[] computeRowStrategy(double[][] rowPayoff,
			double[][] colPayoff) {
		
		double [][] jointStrategy = CorrelatedEquilibriumSolver.getCorrelatedEQJointStrategy(this.objective, rowPayoff, colPayoff);
		return GeneralBimatrixSolverTools.marginalizeRowPlayerStrategy(jointStrategy);

	}

	@Override
	public double[] computeColStrategy(double[][] rowPayoff,
			double[][] colPayoff) {
		
		double [][] jointStrategy;
		if(this.objective != CorrelatedEquilibriumObjective.LIBERTARIAN){
			jointStrategy = CorrelatedEquilibriumSolver.getCorrelatedEQJointStrategy(this.objective, rowPayoff, colPayoff);
			return GeneralBimatrixSolverTools.marginalizeColPlayerStrategy(jointStrategy);
		}
		else{
			//libertarian assumes row player request, so transpose matrices
			jointStrategy = CorrelatedEquilibriumSolver.getCorrelatedEQJointStrategy(this.objective, 
					GeneralBimatrixSolverTools.transposeMatrix(rowPayoff), GeneralBimatrixSolverTools.transposeMatrix(colPayoff));
			
			//return row since we transposed players
			return GeneralBimatrixSolverTools.marginalizeRowPlayerStrategy(jointStrategy);
		}
		
		
	}

}
