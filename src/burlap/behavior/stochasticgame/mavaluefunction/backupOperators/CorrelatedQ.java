package burlap.behavior.stochasticgame.mavaluefunction.backupOperators;

import java.util.List;
import java.util.Map;

import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.QSourceForSingleAgent;
import burlap.behavior.stochasticgame.mavaluefunction.SGBackupOperator;
import burlap.behavior.stochasticgame.solvers.CorrelatedEquilibriumSolver;
import burlap.behavior.stochasticgame.solvers.CorrelatedEquilibriumSolver.CorrelatedEquilibriumObjective;
import burlap.behavior.stochasticgame.solvers.GeneralBimatrixSolverTools;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SingleAction;

public class CorrelatedQ implements SGBackupOperator {

	protected CorrelatedEquilibriumObjective objectiveType = CorrelatedEquilibriumObjective.UTILITARIAN;
	
	
	@Override
	public double performBackup(State s, String forAgent,
			Map<String, AgentType> agentDefinitions, AgentQSourceMap qSourceMap) {
		
		if(agentDefinitions.size() != 2){
			throw new RuntimeException("CoCoQ only defined for two agents.");
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
