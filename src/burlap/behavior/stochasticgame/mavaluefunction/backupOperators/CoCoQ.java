package burlap.behavior.stochasticgame.mavaluefunction.backupOperators;

import java.util.List;
import java.util.Map;

import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.QSourceForSingleAgent;
import burlap.behavior.stochasticgame.mavaluefunction.SGBackupOperator;
import burlap.behavior.stochasticgame.solvers.GeneralBimatrixSolverTools;
import burlap.behavior.stochasticgame.solvers.MinMaxSolver;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * The CoCoQ backup operator for sequential stochastic games [1].
 * <p/>
 * 1. Sodomka, Eric, et al. "Coco-Q: Learning in Stochastic Games with Side Payments." Proceedings of the 30th International Conference on Machine Learning (ICML-13). 2013.
 * @author Esha Gosh, John Meehan, Michalis Michaelidis, and James MacGlashan
 *
 */
public class CoCoQ implements SGBackupOperator {

	
	@Override
	public double performBackup(State s, String forAgent, Map<String, AgentType> agentDefinitions, AgentQSourceMap qSourceMap) {
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
		
		double [][] minMaxPayout = new double[forAgentGSAs.size()][otherAgentGSAs.size()];
		
		double maxmax = Double.NEGATIVE_INFINITY;
		
		for(int i = 0; i < forAgentGSAs.size(); i++){
			for(int j = 0; j < otherAgentGSAs.size(); j++){
				JointAction ja = new JointAction();
				ja.addAction(forAgentGSAs.get(i));
				ja.addAction(otherAgentGSAs.get(j));
				
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
