package burlap.behavior.stochasticgames.madynamicprogramming.backupOperators;

import burlap.behavior.stochasticgames.madynamicprogramming.AgentQSourceMap;
import burlap.behavior.stochasticgames.madynamicprogramming.QSourceForSingleAgent;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.stochasticgames.solvers.CorrelatedEquilibriumSolver;
import burlap.behavior.stochasticgames.solvers.CorrelatedEquilibriumSolver.CorrelatedEquilibriumObjective;
import burlap.behavior.stochasticgames.solvers.GeneralBimatrixSolverTools;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgentType;

import java.util.List;


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
	public double performBackup(State s, int forAgent, List<SGAgentType> agentDefinitions, AgentQSourceMap qSourceMap) {

		if(agentDefinitions.size() != 2){
			throw new RuntimeException("CoCoQ only defined for two agents.");
		}

		int otherAgent = forAgent == 0 ? 1 : 0;
		QSourceForSingleAgent forAgentQSource = qSourceMap.agentQSource(forAgent);
		QSourceForSingleAgent otherAgentQSource = qSourceMap.agentQSource(otherAgent);

		List<Action> forAgentGSAs = ActionUtils.allApplicableActionsForTypes(agentDefinitions.get(forAgent).actions, s);
		List<Action> otherAgentGSAs = ActionUtils.allApplicableActionsForTypes(agentDefinitions.get(otherAgent).actions, s);

		double [][] forPlayerPaoyff = new double[forAgentGSAs.size()][otherAgentGSAs.size()];
		double [][] otherPlayerPaoyff = new double[forAgentGSAs.size()][otherAgentGSAs.size()];


		for(int i = 0; i < forAgentGSAs.size(); i++){
			for(int j = 0; j < otherAgentGSAs.size(); j++){
				JointAction ja = new JointAction();
				ja.setAction(forAgent, forAgentGSAs.get(i));
				ja.setAction(otherAgent, otherAgentGSAs.get(j));

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
