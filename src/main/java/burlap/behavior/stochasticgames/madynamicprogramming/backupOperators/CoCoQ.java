package burlap.behavior.stochasticgames.madynamicprogramming.backupOperators;

import burlap.behavior.stochasticgames.madynamicprogramming.AgentQSourceMap;
import burlap.behavior.stochasticgames.madynamicprogramming.QSourceForSingleAgent;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.stochasticgames.solvers.GeneralBimatrixSolverTools;
import burlap.behavior.stochasticgames.solvers.MinMaxSolver;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.core.action.ActionUtils;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgentType;

import java.util.List;


/**
 * The CoCoQ backup operator for sequential stochastic games [1].
 * <p>
 * 1. Sodomka, Eric, et al. "Coco-Q: Learning in Stochastic Games with Side Payments." Proceedings of the 30th International Conference on Machine Learning (ICML-13). 2013.
 * @author Esha Gosh, John Meehan, Michalis Michaelidis, and James MacGlashan
 *
 */
public class CoCoQ implements SGBackupOperator {


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


		double [][] minMaxPayout = new double[forAgentGSAs.size()][otherAgentGSAs.size()];

		double maxmax = Double.NEGATIVE_INFINITY;

		for(int i = 0; i < forAgentGSAs.size(); i++){
			for(int j = 0; j < otherAgentGSAs.size(); j++){
				JointAction ja = new JointAction();
				ja.setAction(forAgent, forAgentGSAs.get(i));
				ja.setAction(otherAgent, otherAgentGSAs.get(j));

				double q1 = forAgentQSource.getQValueFor(s, ja).q;
				double q2 = otherAgentQSource.getQValueFor(s, ja).q;

				minMaxPayout[i][j] = (q1-q2)/2.;

				if(q1 + q2 > maxmax){
					maxmax = q1+q2;
				}

			}
		}

		double [] forAgentStrat = MinMaxSolver.getRowPlayersStrategy(minMaxPayout);
		double [] otherAgentStrat = MinMaxSolver.getColPlayersStrategy(GeneralBimatrixSolverTools.getNegatedMatrix(minMaxPayout));

		double minmaxQ = GeneralBimatrixSolverTools.expectedPayoffs(minMaxPayout, GeneralBimatrixSolverTools.getNegatedMatrix(minMaxPayout), forAgentStrat, otherAgentStrat)[0];

		double cocoQ = (maxmax/2.)+minmaxQ;


		return cocoQ;

	}
}
