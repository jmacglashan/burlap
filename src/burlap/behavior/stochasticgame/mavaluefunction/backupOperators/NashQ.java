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
 * Provides an implementation of NashQ [1] which enables learning/planning in sequential general sum stochastic games. Note that NashQ is not guaranteed to converge
 * in every scenario. This implementation only supports NashQ for 2 player games and will throw an exception if more than two players are present.
 * <p/>
 * 1. Hu, Junling, and Michael P. Wellman. "Nash Q-learning for general-sum stochastic games." The Journal of Machine Learning Research 4 (2003): 1039-1069.
 * @author James MacGlashan
 *
 */
public class NashQ implements SGBackupOperator {

	@Override
	public double performBackup(State s, String forAgent,
			Map<String, AgentType> agentDefinitions, AgentQSourceMap qSourceMap) {
		
		if(agentDefinitions.size() != 2){
			throw new RuntimeException("NashQ is only implemented for two players.");
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
		
		for(int i = 0; i < forAgentGSAs.size(); i++){
			for(int j = 0; j < otherAgentGSAs.size(); j++){
				JointAction ja = new JointAction();
				ja.addAction(forAgentGSAs.get(i));
				ja.addAction(otherAgentGSAs.get(j));
				
				double q1 = forAgentQSource.getQValueFor(s, ja).q;
				double q2 = otherAgentQSource.getQValueFor(s, ja).q;
				
				payout1[i][j] = q1;
				payout2[i][j] = q2;
				
				
			}
		}
		
		double nashq = BimatrixGeneralSumSolver.generalSumNash(payout1, payout2)[0];
	
		return nashq;
	}

}
