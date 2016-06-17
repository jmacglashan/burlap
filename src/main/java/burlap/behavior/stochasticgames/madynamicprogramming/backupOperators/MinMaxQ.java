package burlap.behavior.stochasticgames.madynamicprogramming.backupOperators;

import burlap.behavior.stochasticgames.madynamicprogramming.AgentQSourceMap;
import burlap.behavior.stochasticgames.madynamicprogramming.QSourceForSingleAgent;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.stochasticgames.solvers.GeneralBimatrixSolverTools;
import burlap.behavior.stochasticgames.solvers.MinMaxSolver;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgentType;

import java.util.List;


/**
 * A minmax operator. This operator is useful for zero sum two player games. If there are more than two players in the game, a runtime exception will be thrown.
 * Before solving the minmax strategy, the Q-values are transformed into a minmax game. Then the resulting minmax strategy is used to compute
 * the expected "payoff" using the true Q-values of the query agent, which is then returned as the new Q-value.
 * @author James MacGlashan
 *
 */
public class MinMaxQ implements SGBackupOperator {

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

		double [][] payout1 = new double[forAgentGSAs.size()][otherAgentGSAs.size()];

		double [][] truePayout1 = new double[forAgentGSAs.size()][otherAgentGSAs.size()];

		for(int i = 0; i < forAgentGSAs.size(); i++){
			for(int j = 0; j < otherAgentGSAs.size(); j++){
				JointAction ja = new JointAction();
				ja.setAction(forAgent, forAgentGSAs.get(i));
				ja.setAction(otherAgent, otherAgentGSAs.get(j));

				double q1 = forAgentQSource.getQValueFor(s, ja).q;
				double q2 = otherAgentQSource.getQValueFor(s, ja).q;

				truePayout1[i][j] = q1;
				payout1[i][j] = (q1-q2)/2.;


			}
		}


		double [] forAgentStrat = MinMaxSolver.getRowPlayersStrategy(payout1);
		double [] otherAgentStrat = MinMaxSolver.getColPlayersStrategy(GeneralBimatrixSolverTools.getNegatedMatrix(payout1));

		//we can use true payoff for player 1 for both players, because we're ignoring the payout for the second player.
		double expectedpayoffforPlayer1 = GeneralBimatrixSolverTools.expectedPayoffs(truePayout1, truePayout1, forAgentStrat, otherAgentStrat)[0];


		return expectedpayoffforPlayer1;
	}

}
