package burlap.behavior.stochasticgame.mavaluefunction.policies;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.stochasticgame.JointPolicy;
import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.MAQSourcePolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MultiAgentQSourceProvider;
import burlap.behavior.stochasticgame.mavaluefunction.QSourceForSingleAgent;
import burlap.behavior.stochasticgame.solvers.CorrelatedEquilibriumSolver;
import burlap.behavior.stochasticgame.solvers.CorrelatedEquilibriumSolver.CorrelatedEquilibriumObjective;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SingleAction;

public class ECorrelatedQJOintPolicy extends MAQSourcePolicy {

	
	/**
	 * The multi-agent q-source provider
	 */
	protected MultiAgentQSourceProvider			qSourceProvider;
	
	/**
	 * The epsilon parameter specifying how often random joint actions are returned
	 */
	protected double							epsilon;
	
	/**
	 * The correlated Q objective type being solved
	 */
	protected CorrelatedEquilibriumObjective	objectiveType = CorrelatedEquilibriumObjective.UTILITARIAN;
	
	
	public ECorrelatedQJOintPolicy(double epsilon){
		this.epsilon = epsilon;
	}
	
	public ECorrelatedQJOintPolicy(CorrelatedEquilibriumObjective objectiveType, double epsilon){
		this.objectiveType = objectiveType;
		this.epsilon = epsilon;
	}
	
	
	public void setCorrelatedQObjective(CorrelatedEquilibriumObjective objectiveType){
		this.objectiveType = objectiveType;
	}
	
	@Override
	public void setTargetAgent(String agentName) {
		//do nothing
	}

	@Override
	public JointPolicy copy() {
		ECorrelatedQJOintPolicy jp = new ECorrelatedQJOintPolicy(this.objectiveType, this.epsilon);
		jp.setQSourceProvider(this.qSourceProvider);
		jp.setAgentsInJointPolicy(this.agentsInJointPolicy);
		return jp;
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		return sampleFromActionDistribution(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		
		List<String> agents = new ArrayList<String>(this.agentsInJointPolicy.keySet());
		String targetAgentName = agents.get(0);
		String otherAgentName = agents.get(1);
		
		AgentQSourceMap qSourceMap = this.qSourceProvider.getQSources();
		
		QSourceForSingleAgent forAgentQSource = qSourceMap.agentQSource(targetAgentName);
		QSourceForSingleAgent otherAgentQSource = qSourceMap.agentQSource(otherAgentName);
		
		List<GroundedSingleAction> forAgentGSAs = SingleAction.getAllPossibleGroundedSingleActions(s, targetAgentName, this.agentsInJointPolicy.get(targetAgentName).actions);
		List<GroundedSingleAction> otherAgentGSAs = SingleAction.getAllPossibleGroundedSingleActions(s, otherAgentName, this.agentsInJointPolicy.get(otherAgentName).actions);
		
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
		
		double [][] jointActionProbs = CorrelatedEquilibriumSolver.getCorrelatedEQJointStrategy(objectiveType, payout1, payout2);
		
		List<ActionProb> aps = new ArrayList<ActionProb>();
		double eCont = this.epsilon / (forAgentGSAs.size() + otherAgentGSAs.size());
		for(int i = 0; i < forAgentGSAs.size(); i++){
			for(int j = 0; j < otherAgentGSAs.size(); j++){
				JointAction ja = new JointAction();
				ja.addAction(forAgentGSAs.get(i));
				ja.addAction(otherAgentGSAs.get(j));
				double p = eCont + ((1. - this.epsilon) * jointActionProbs[i][j]);
				ActionProb ap = new ActionProb(ja, p);
				aps.add(ap);
			}
		}
		
		
		return aps;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}

	@Override
	public boolean isDefinedFor(State s) {
		return true;
	}

	@Override
	public void setQSourceProvider(MultiAgentQSourceProvider provider) {
		this.qSourceProvider = provider;
	}

}
