package burlap.behavior.stochasticgame.mavaluefunction.policies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.stochasticgame.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgame.agents.mavf.MultiAgentVFPlanningAgent;
import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.MAQSourcePolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MultiAgentQSourceProvider;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.JointAction;


/**
 * An epsilon greedy joint policy, in which the joint aciton with the highest aggregate Q-values for each agent is returned a 1-epsilon fraction of the time and a random
 * joint action an epsilon fraction of the time. Ties are broken randomly. This policy is typically used for CoCo-Q agents.
 * @author James MacGlashan
 *
 */
public class EGreedyMaxWellfare extends MAQSourcePolicy {

	
	/**
	 * The multi-agent q-source provider
	 */
	protected MultiAgentQSourceProvider		qSourceProvider;
	
	/**
	 * The epsilon parameter specifying how often random joint actions are returned
	 */
	protected double						epsilon;
	
	/**
	 * A random object used for sampling
	 */
	protected Random						rand = RandomFactory.getMapped(0);
	
	
	
	/**
	 * Initializes for a given epsilon value. The set of agents for which joint actions are returned
	 * and multi-agent q-source provider will need to be set manually with the methods {@link #setAgentsInJointPolicy(java.util.Map)}, 
	 * and {@link #setQSourceProvider(MultiAgentQSourceProvider)} before the policy can be queried.
	 * Note that the {@link MultiAgentQLearning} and {@link MultiAgentVFPlanningAgent} agents may do this themselves. Consult the documentation
	 * to check.
	 * @param epsilon the fraction of the time [0, 1] that the agent selections random actions.
	 */
	public EGreedyMaxWellfare(double epsilon){
		this.epsilon = epsilon;
	}
	
	
	/**
	 * Initializes for a multi-agent Q-learning object and epsilon value. The set of agents for which joint actions are to be returned
	 * must be subsequently defined with the {@link #setAgentsInJointPolicy(java.util.Map)}. Note that the {@link MultiAgentQLearning} and {@link MultiAgentVFPlanningAgent} 
	 * agents may do this themselves. Consult the documentation to check. 
	 * @param actingAgent the agent who will use this policy.
	 * @param epsilon the fraction of the time [0, 1] that the agent selections random actions.
	 */
	public EGreedyMaxWellfare(MultiAgentQLearning actingAgent, double epsilon) {
		this.qSourceProvider = actingAgent;
		this.epsilon = epsilon;
	}
	
	
	@Override
	public void setQSourceProvider(MultiAgentQSourceProvider provider){
		this.qSourceProvider = provider;
	}
	
	

	@Override
	public AbstractGroundedAction getAction(State s) {
		
		List<JointAction> jas = this.getAllJointActions(s);
		JointAction selected = null;
		AgentQSourceMap qSources = this.qSourceProvider.getQSources();
		
		double r = rand.nextDouble();
		if(r < this.epsilon){
			selected = jas.get(rand.nextInt(jas.size()));
		}
		else{
			List<JointAction> jasWithMax = new ArrayList<JointAction>(jas.size());
			double maxSumQ = Double.NEGATIVE_INFINITY;
			for(JointAction ja : jas){
				double sumQ = 0.;
				for(String aname : ja.getAgentNames()){
					sumQ += qSources.agentQSource(aname).getQValueFor(s, ja).q;
				}
				if(sumQ == maxSumQ){
					jasWithMax.add(ja);
				}
				else if(sumQ > maxSumQ){
					jasWithMax.clear();
					jasWithMax.add(ja);
					maxSumQ = sumQ;
				}
			}
			
			if(jasWithMax.size() == 1){
				selected = jasWithMax.get(0);
			}
			else{
				selected = jasWithMax.get(rand.nextInt(jasWithMax.size()));
			}
			
		}
		
		
		return selected;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		throw new RuntimeException("Action distribution currently unsupported for epsilon greedy max wellfare.");
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
	public void setTargetAgent(String agentName) {
		//do nothing
	}

}
