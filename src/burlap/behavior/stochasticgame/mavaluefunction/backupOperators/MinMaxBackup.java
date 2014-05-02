package burlap.behavior.stochasticgame.mavaluefunction.backupOperators;

import java.util.List;
import java.util.Map;

import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.QSourceForSingleAgent;
import burlap.behavior.stochasticgame.mavaluefunction.SGBackupOperator;
import burlap.behavior.stochasticgame.solvers.BimatrixGeneralSumSolver;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * A minmax operator. This operator is useful for zero sum two player games. If there are more than two players in the game, a runtime exception will be thrown.
 * @author James MacGlashan
 *
 */
public class MinMaxBackup implements SGBackupOperator {

	@Override
	public double performBackup(State s, String forAgent,
			Map<String, AgentType> agentDefinitions, AgentQSourceMap qSourceMap) {
		
		if(agentDefinitions.size() != 2){
			throw new RuntimeException("MinMax only defined for two agents.");
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
		
		List<GroundedSingleAction> forAgentGSAs = SingleAction.getAllPossibleGroundedSingleActions(s, forAgent, agentDefinitions.get(forAgent).actions);
		List<GroundedSingleAction> otherAgentGSAs = SingleAction.getAllPossibleGroundedSingleActions(s, otherAgentName, agentDefinitions.get(otherAgentName).actions);
		
		double [][] payout1 = new double[forAgentGSAs.size()][otherAgentGSAs.size()];
		double [][] payout2 = new double[forAgentGSAs.size()][otherAgentGSAs.size()];
		
		double [][] truePayout1 = new double[forAgentGSAs.size()][otherAgentGSAs.size()];
		
		for(int i = 0; i < forAgentGSAs.size(); i++){
			for(int j = 0; j < otherAgentGSAs.size(); j++){
				JointAction ja = new JointAction();
				ja.addAction(forAgentGSAs.get(i));
				ja.addAction(otherAgentGSAs.get(j));
				
				double q1 = forAgentQSource.getQValueFor(s, ja).q;
				double q2 = otherAgentQSource.getQValueFor(s, ja).q;
				
				truePayout1[i][j] = q1;
				payout1[i][j] = (q1-q2)/2.;
				payout2[i][j] = (q2-q1)/2;
				
				
			}
		}
		
		BimatrixGeneralSumSolver.Joint<double[]> strategies = BimatrixGeneralSumSolver.solveForMixedStrategies(payout1, payout2);
		double[][] outcomeProbability = BimatrixGeneralSumSolver.getDistributionOverJointActions(strategies.getForPlayer(0), strategies.getForPlayer(1));
		
		double expectedpayoffforPlayer1 = BimatrixGeneralSumSolver.getExpectedPayoffsForPlayer(truePayout1, outcomeProbability);
		
		
		return expectedpayoffforPlayer1;
	}

}
