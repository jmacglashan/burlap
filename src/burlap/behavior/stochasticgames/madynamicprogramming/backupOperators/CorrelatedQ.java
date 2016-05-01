package burlap.behavior.stochasticgames.madynamicprogramming.backupOperators;

import java.util.List;
import java.util.Map;

import burlap.behavior.stochasticgames.madynamicprogramming.AgentQSourceMap;
import burlap.behavior.stochasticgames.madynamicprogramming.QSourceForSingleAgent;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.stochasticgames.solvers.CorrelatedEquilibriumSolver;
import burlap.behavior.stochasticgames.solvers.CorrelatedEquilibriumSolver.CorrelatedEquilibriumObjective;
import burlap.behavior.stochasticgames.solvers.GeneralBimatrixSolverTools;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.SGAgentType;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;


/**
 * A correlated Q backup operator [1] for using in stochastic game multi-agent Q-learning or dynamic programming.
 * 
 * <p>
 * 1. Greenwald, Amy, Keith Hall, and Roberto Serrano. "Correlated Q-learning." ICML. Vol. 3. 2003.
 * @author James MacGlashan
 *
 */
public class CorrelatedQ implements SGBackupOperator {

	/**
	 * The correlated equilibrium objective to be solved.
	 */
	protected CorrelatedEquilibriumObjective objectiveType = CorrelatedEquilibriumObjective.UTILITARIAN;
	
	
	/**
	 * Initializes an operator for the given correlated equilibrium objective.
	 * @param objectiveType the correlated equilibrium objective being solved.
	 */
	public CorrelatedQ(CorrelatedEquilibriumObjective objectiveType){
		this.objectiveType = objectiveType;
	}
	
	@Override
	public double performBackup(State s, String forAgent,
			Map<String, SGAgentType> agentDefinitions, AgentQSourceMap qSourceMap) {
		
		if(agentDefinitions.size() != 2){
			throw new RuntimeException("Correlated Q only defined for two agents.");
		}
		
		String otherAgentName = null;
		for(String aname : agentDefinitions.keySet()){
			if(!aname.equals(forAgent)){
				otherAgentName = aname;
				break;
			}
		}
		
		
		QSourceForSingleAgent forAgentQSource = qSourceMap.agentQSource(forAgent);
		QSourceForSingleAgent otherAgentQSource = qSourceMap.agentQSource(otherAgentName);
		
		List<GroundedSGAgentAction> forAgentGSAs = SGAgentAction.getAllApplicableGroundedActionsFromActionList(s, forAgent, agentDefinitions.get(forAgent).actions);
		List<GroundedSGAgentAction> otherAgentGSAs = SGAgentAction.getAllApplicableGroundedActionsFromActionList(s, otherAgentName, agentDefinitions.get(otherAgentName).actions);
		
		double [][] forPlayerPaoyff = new double[forAgentGSAs.size()][otherAgentGSAs.size()];
		double [][] otherPlayerPaoyff = new double[forAgentGSAs.size()][otherAgentGSAs.size()];
		
		
		for(int i = 0; i < forAgentGSAs.size(); i++){
			for(int j = 0; j < otherAgentGSAs.size(); j++){
				JointAction ja = new JointAction();
				ja.addAction(forAgentGSAs.get(i));
				ja.addAction(otherAgentGSAs.get(j));
				
				double q1 = forAgentQSource.getQValueFor(s, ja).q;
				double q2 = otherAgentQSource.getQValueFor(s, ja).q;
				
				forPlayerPaoyff[i][j] = q1;
				otherPlayerPaoyff[i][j] = q2;
				
			}
		}
		
		double [][] jointActionProbs = CorrelatedEquilibriumSolver.getCorrelatedEQJointStrategy(this.objectiveType, forPlayerPaoyff, otherPlayerPaoyff);
		double [] expectedValue = GeneralBimatrixSolverTools.expectedPayoffs(forPlayerPaoyff, otherPlayerPaoyff, jointActionProbs);
		
		
		return expectedValue[0];
	}

}
