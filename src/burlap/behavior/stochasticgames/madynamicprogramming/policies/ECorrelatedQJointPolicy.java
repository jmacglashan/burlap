package burlap.behavior.stochasticgames.madynamicprogramming.policies;

import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.stochasticgames.JointPolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.AgentQSourceMap;
import burlap.behavior.stochasticgames.madynamicprogramming.MAQSourcePolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.MultiAgentQSourceProvider;
import burlap.behavior.stochasticgames.madynamicprogramming.QSourceForSingleAgent;
import burlap.behavior.stochasticgames.solvers.CorrelatedEquilibriumSolver;
import burlap.behavior.stochasticgames.solvers.CorrelatedEquilibriumSolver.CorrelatedEquilibriumObjective;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.action.JointAction;
import burlap.mdp.stochasticgames.action.SGActionUtils;
import burlap.mdp.stochasticgames.action.SGAgentAction;

import java.util.ArrayList;
import java.util.List;


/**
 * A joint policy that computes the correlated equilibrium using the Q-values of the agents as input and then either
 * follows that policy or returns a random action with probability epsilon. If the equilibrium is a mixed strategy,
 * then epislon has the effect of smoothing the probability of each joint action. This class is only defined for 2 player games.
 * @author James MacGlashan
 *
 */
public class ECorrelatedQJointPolicy extends MAQSourcePolicy {

	
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
	
	
	
	/**
	 * Initializes with the epislon probability of a random joint action.
	 * @param epsilon the probability that a random joint action is returned.
	 */
	public ECorrelatedQJointPolicy(double epsilon){
		this.epsilon = epsilon;
	}
	
	/**
	 * Initializes with the correlated equilibrium objective and the epsilon probability of a random joint action.
	 * @param objectiveType the correlated equilibirum objective.
	 * @param epsilon the probability that a random joint action is returned.
	 */
	public ECorrelatedQJointPolicy(CorrelatedEquilibriumObjective objectiveType, double epsilon){
		this.objectiveType = objectiveType;
		this.epsilon = epsilon;
	}
	
	
	/**
	 * Sets the correlated equilibrium objective to be solved.
	 * @param objectiveType the correlated equilibrium objective to be solved.
	 */
	public void setCorrelatedQObjective(CorrelatedEquilibriumObjective objectiveType){
		this.objectiveType = objectiveType;
	}
	
	@Override
	public void setTargetAgent(String agentName) {
		//do nothing
	}

	@Override
	public JointPolicy copy() {
		ECorrelatedQJointPolicy jp = new ECorrelatedQJointPolicy(this.objectiveType, this.epsilon);
		jp.setQSourceProvider(this.qSourceProvider);
		jp.setAgentsInJointPolicy(this.agentsInJointPolicy);
		return jp;
	}

	@Override
	public Action action(State s) {
		return PolicyUtils.sampleFromActionDistribution(this, s);
	}

	@Override
	public List<ActionProb> policyDistribution(State s) {
		
		List<String> agents = new ArrayList<String>(this.agentsInJointPolicy.keySet());
		String targetAgentName = agents.get(0);
		String otherAgentName = agents.get(1);
		
		AgentQSourceMap qSourceMap = this.qSourceProvider.getQSources();
		
		QSourceForSingleAgent forAgentQSource = qSourceMap.agentQSource(targetAgentName);
		QSourceForSingleAgent otherAgentQSource = qSourceMap.agentQSource(otherAgentName);

		List<SGAgentAction> forAgentGSAs = SGActionUtils.allApplicableActionsForTypes(this.agentsInJointPolicy.get(targetAgentName).actions, targetAgentName, s);
		List<SGAgentAction> otherAgentGSAs = SGActionUtils.allApplicableActionsForTypes(this.agentsInJointPolicy.get(otherAgentName).actions, otherAgentName, s);

		
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
		double eCont = this.epsilon / (forAgentGSAs.size() * otherAgentGSAs.size());
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
	public boolean stochastic() {
		return true;
	}

	@Override
	public boolean definedFor(State s) {
		return true;
	}

	@Override
	public void setQSourceProvider(MultiAgentQSourceProvider provider) {
		this.qSourceProvider = provider;
	}

}
