package burlap.behavior.stochasticgames.madynamicprogramming.policies;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.stochasticgames.JointPolicy;
import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgames.madynamicprogramming.AgentQSourceMap;
import burlap.behavior.stochasticgames.madynamicprogramming.MAQSourcePolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.MultiAgentQSourceProvider;
import burlap.behavior.stochasticgames.madynamicprogramming.QSourceForSingleAgent;
import burlap.behavior.stochasticgames.solvers.GeneralBimatrixSolverTools;
import burlap.behavior.stochasticgames.solvers.MinMaxSolver;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.stochasticgames.JointAction;

import java.util.ArrayList;
import java.util.List;


/**
 * Class for following a minmax joint policy. Given some target agent, a minmax joint policy is computed over the space of joint action Q-values. A fraction
 * epsilong of the time though, a random joint action is selected. If the input Q-values are not zero-sum, then they are forced to be from the perspective of the
 * target agent.
 * @author James MacGlashan
 *
 */
public class EMinMaxPolicy extends MAQSourcePolicy implements EnumerablePolicy {

	/**
	 * The multi-agent q-source provider
	 */
	protected MultiAgentQSourceProvider		qSourceProvider;
	
	/**
	 * The epsilon parameter specifying how often random joint actions are returned
	 */
	protected double						epsilon;
	
	/**
	 * The target agent who is maximizing action selection
	 */
	protected int targetAgent;
	
	
	
	
	/**
	 * Initializes for a given epsilon value; the fraction of the time a random joint action is selected
	 * @param epsilon the espilon parameter
	 */
	public EMinMaxPolicy(double epsilon){
		this.epsilon = epsilon;
	}
	
	
	/**
	 * Initializes for a given Q-learning agent and epsilon value. The Q-learning agent is set as the Q-source and they are set as the target agent.
	 * Epsilon is the fraction of the time a random joint action is selected.
	 * @param actingAgent the Q-learning agent
	 * @param epsilon the epsilon parameter
	 * @param targetAgentNum the agent number of the target agent
	 */
	public EMinMaxPolicy(MultiAgentQLearning actingAgent, double epsilon, int targetAgentNum){
		this.qSourceProvider = actingAgent;
		this.epsilon = epsilon;
		this.targetAgent = targetAgentNum;
	}
	
	
	@Override
	public void setTargetAgent(int agentNum) {
		this.targetAgent = agentNum;
	}

	@Override
	public JointPolicy copy() {
		EMinMaxPolicy np = new EMinMaxPolicy(this.epsilon);
		np.setTargetAgent(this.targetAgent);
		np.setQSourceProvider(this.qSourceProvider);
		np.setAgentTypesInJointPolicy(this.agentsInJointPolicy);
		return np;
	}

	@Override
	public Action action(State s) {
		return PolicyUtils.sampleFromActionDistribution(this, s);
	}

	@Override
	public double actionProb(State s, Action a) {
		return PolicyUtils.actionProbFromEnum(this, s, a);
	}

	@Override
	public List<ActionProb> policyDistribution(State s) {

		int oagent = this.targetAgent == 0 ? 1 : 0;
		
		AgentQSourceMap qSourceMap = this.qSourceProvider.getQSources();
		
		QSourceForSingleAgent forAgentQSource = qSourceMap.agentQSource(this.targetAgent);
		QSourceForSingleAgent otherAgentQSource = qSourceMap.agentQSource(oagent);

		List<Action> forAgentGSAs = ActionUtils.allApplicableActionsForTypes(this.agentsInJointPolicy.get(targetAgent).actions, s);
		List<Action> otherAgentGSAs = ActionUtils.allApplicableActionsForTypes(this.agentsInJointPolicy.get(oagent).actions, s);

		double [][] payout1 = new double[forAgentGSAs.size()][otherAgentGSAs.size()];
		
		
		for(int i = 0; i < forAgentGSAs.size(); i++){
			for(int j = 0; j < otherAgentGSAs.size(); j++){
				JointAction ja = new JointAction();
				ja.addAction(forAgentGSAs.get(i));
				ja.addAction(otherAgentGSAs.get(j));
				
				double q1 = forAgentQSource.getQValueFor(s, ja).q;
				double q2 = otherAgentQSource.getQValueFor(s, ja).q;
				
				payout1[i][j] = (q1-q2)/2.;
				
				
			}
		}
		
		double [] forAgentStrat = MinMaxSolver.getRowPlayersStrategy(payout1);
		double [] otherAgentStrat = MinMaxSolver.getColPlayersStrategy(GeneralBimatrixSolverTools.getNegatedMatrix(payout1));
		double[][] outcomeProbability = GeneralBimatrixSolverTools.jointActionProbabilities(forAgentStrat, otherAgentStrat);
		
		
		List<ActionProb> aps = new ArrayList<ActionProb>();
		double eCont = this.epsilon / (forAgentGSAs.size() * otherAgentGSAs.size());
		for(int i = 0; i < forAgentGSAs.size(); i++){
			for(int j = 0; j < otherAgentGSAs.size(); j++){
				JointAction ja = new JointAction();
				ja.addAction(forAgentGSAs.get(i));
				ja.addAction(otherAgentGSAs.get(j));
				double p = eCont + ((1. - this.epsilon) * outcomeProbability[i][j]);
				ActionProb ap = new ActionProb(ja, p);
				aps.add(ap);
			}
		}
		
		
		return aps;
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
