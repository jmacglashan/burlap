package burlap.behavior.stochasticgame.mavaluefunction.policies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import burlap.behavior.stochasticgame.JointPolicy;
import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.MAQSourcePolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MultiAgentQSourceProvider;
import burlap.behavior.stochasticgame.mavaluefunction.QSourceForSingleAgent;
import burlap.behavior.stochasticgame.solvers.BimatrixGeneralSumSolver;
import burlap.behavior.stochasticgame.solvers.BimatrixGeneralSumSolver.Joint;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * Joint Policy that follows the a Nash Eqiliribrium policy from the Q-values of the agents and selects a random joint action a fraction epsilon of the time.
 * @author James MacGlashan
 *
 */
public class ENashQPolicy extends MAQSourcePolicy {

	/**
	 * The multi-agent q-source provider
	 */
	protected MultiAgentQSourceProvider		qSourceProvider;
	
	/**
	 * The epsilon parameter specifying how often random joint actions are returned
	 */
	protected double						epsilon;
	
	
	/**
	 * Intializes with epsilon, the fraction of the time that a random action is selected.
	 * @param epsilon the fraction of the time that a random action is selected.
	 */
	public ENashQPolicy(double epsilon){
		this.epsilon = epsilon;
	}
	
	
	/**
	 * Initializes with a Q-value source provider and epsilon, the fraction of the time that a random joint aciton is selected.
	 * @param qProvider the Q-source provider from which the Nash will be computed.
	 * @param epsilon the fraction of the time that a random action is selected
	 */
	public ENashQPolicy(MultiAgentQSourceProvider qProvider, double epsilon){
		this.qSourceProvider = qProvider;
		this.epsilon = epsilon;
	}
	
	@Override
	public void setQSourceProvider(MultiAgentQSourceProvider provider) {
		this.qSourceProvider = provider;
	}

	@Override
	public void setTargetAgent(String agentName) {
		//do nothing
	}

	@Override
	public JointPolicy copy() {
		
		ENashQPolicy np = new ENashQPolicy(this.epsilon);
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
		
		Iterator<String> anames = this.agentsInJointPolicy.keySet().iterator();
		String forAgent = anames.next();
		String otherAgentName = anames.next();
		
		AgentQSourceMap qSourceMap = this.qSourceProvider.getQSources();
		
		QSourceForSingleAgent forAgentQSource = qSourceMap.agentQSource(forAgent);
		QSourceForSingleAgent otherAgentQSource = qSourceMap.agentQSource(otherAgentName);
		
		List<GroundedSingleAction> forAgentGSAs = SingleAction.getAllPossibleGroundedSingleActions(s, forAgent, this.agentsInJointPolicy.get(forAgent).actions);
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
		
		Joint<double[]> strategies = BimatrixGeneralSumSolver.solveForMixedStrategies(payout1, payout2);

		double[] player1Strategy = strategies.getForPlayer(0); 
		double[] player2Strategy = strategies.getForPlayer(1); 

		double[][] outcomeProbability = BimatrixGeneralSumSolver.getDistributionOverJointActions(player1Strategy, player2Strategy);
		
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

}
