package burlap.behavior.stochasticgame.mavaluefunction.policies;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.stochasticgame.JointPolicy;
import burlap.behavior.stochasticgame.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.MAQSourcePolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MultiAgentQSourceProvider;
import burlap.behavior.stochasticgame.mavaluefunction.QSourceForSingleAgent;
import burlap.behavior.stochasticgame.solvers.BimatrixGeneralSumSolver;
import burlap.behavior.stochasticgame.solvers.GeneralBimatrixSolverTools;
import burlap.behavior.stochasticgame.solvers.MinMaxSolver;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * Class for following a minmax joint policy. Given some target agent, a minmax joint policy is computed over the space of joint action Q-values. A fraction
 * epsilong of the time though, a random joint action is selected. If the input Q-values are not zero-sum, then they are forced to be from the perspective of the
 * target agent.
 * @author James MacGlashan
 *
 */
public class EMinMaxPolicy extends MAQSourcePolicy {

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
	protected String 						targetAgentQName;
	
	
	
	
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
	 */
	public EMinMaxPolicy(MultiAgentQLearning actingAgent, double epsilon){
		this.qSourceProvider = actingAgent;
		this.epsilon = epsilon;
		this.targetAgentQName = actingAgent.getAgentName();
	}
	
	
	@Override
	public void setTargetAgent(String agentName) {
		this.targetAgentQName = agentName;
	}

	@Override
	public JointPolicy copy() {
		EMinMaxPolicy np = new EMinMaxPolicy(this.epsilon);
		np.setTargetAgent(this.targetAgentQName);
		np.setQSourceProvider(this.qSourceProvider);
		np.setAgentsInJointPolicy(this.agentsInJointPolicy);
		return np;
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		return this.sampleFromActionDistribution(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		
		String otherAgentName = null;
		for(String aname : this.agentsInJointPolicy.keySet()){
			if(!aname.equals(this.targetAgentQName)){
				otherAgentName = aname;
				break;
			}
		}
		
		AgentQSourceMap qSourceMap = this.qSourceProvider.getQSources();
		
		QSourceForSingleAgent forAgentQSource = qSourceMap.agentQSource(this.targetAgentQName);
		QSourceForSingleAgent otherAgentQSource = qSourceMap.agentQSource(otherAgentName);
		
		List<GroundedSingleAction> forAgentGSAs = SingleAction.getAllPossibleGroundedSingleActions(s, this.targetAgentQName, this.agentsInJointPolicy.get(this.targetAgentQName).actions);
		List<GroundedSingleAction> otherAgentGSAs = SingleAction.getAllPossibleGroundedSingleActions(s, otherAgentName, this.agentsInJointPolicy.get(otherAgentName).actions);
		
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
		double eCont = this.epsilon / (forAgentGSAs.size() + otherAgentGSAs.size());
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
