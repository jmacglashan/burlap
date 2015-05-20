package burlap.behavior.stochasticgame.mavaluefunction.backupOperators;

import java.util.List;
import java.util.Map;

import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.QSourceForSingleAgent;
import burlap.behavior.stochasticgame.mavaluefunction.SGBackupOperator;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.JointAction;


/**
 * A classic MDP-style max backup operator in which an agent back ups his max Q-value in the state.
 * @author James MacGlashan
 *
 */
public class MaxQ implements SGBackupOperator {

	@Override
	public double performBackup(State s, String forAgent, Map<String, AgentType> agentDefinitions, AgentQSourceMap qSourceMap) {
		
		List<JointAction> allJAs = JointAction.getAllJointActions(s, agentDefinitions);
		
		double maxQ = Double.NEGATIVE_INFINITY;
		
		QSourceForSingleAgent myQs = qSourceMap.agentQSource(forAgent);
		
		for(JointAction ja : allJAs){
			double q = myQs.getQValueFor(s, ja).q;
			maxQ = Math.max(q, maxQ);
		}
		
		
		return maxQ;
	}

}
