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
 * An epsilon greedy joint policy, in which the joint action with the highest Q-value for a given target agent is returned a 1-epsilon fraction
 * of the time, and a random joint action an epsilon fraction of the time. Ties are broken randomly.
 * @author James MacGlashan
 *
 */
public class EGreedyJointPolicy extends MAQSourcePolicy {

	
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
	 * The agent whose q-values dictate which joint actions to return
	 */
	protected String						targetAgentQName;
	
	
	/**
	 * Initializes for a given epsilon value. The set of agents for which joint actions are returned, target agent whose q-values are maximized,
	 * and multi-agent q-source provider will need to be set manually with the methods {@link #setAgentsInJointPolicy(java.util.Map)}, 
	 * {@link #setTargetAgent(String)}, and {@link #setQSourceProvider(MultiAgentQSourceProvider)} before the policy can be queried.
	 * Note that the {@link MultiAgentQLearning} and {@link MultiAgentVFPlanningAgent} agents may do this themselves. Consult the documentation
	 * to check.
	 * @param epsilon the fraction of the time [0, 1] that the agent selections random actions.
	 */
	public EGreedyJointPolicy(double epsilon) {
		this.epsilon = epsilon;
	}
	
	
	/**
	 * Initializes for a multi-agent Q-learning object and epsilon value. The set of agents for which joint actions are to be returned
	 * must be subsequently defined with the {@link #setAgentsInJointPolicy(java.util.Map)}. Note that the {@link MultiAgentQLearning} and {@link MultiAgentVFPlanningAgent} 
	 * agents may do this themselves. Consult the documentation to check.
	 * @param actingAgent the agent whose Q-values are maximized.
	 * @param epsilon the fraction of the time [0, 1] that the agent selections random actions.
	 */
	public EGreedyJointPolicy(MultiAgentQLearning actingAgent, double epsilon) {
		this.qSourceProvider = actingAgent;
		this.epsilon = epsilon;
		this.targetAgentQName = actingAgent.getAgentName();
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
			double maxQ = Double.NEGATIVE_INFINITY;
			for(JointAction ja : jas){
				
				double q = qSources.agentQSource(this.targetAgentQName).getQValueFor(s, ja).q;

				if(q == maxQ){
					jasWithMax.add(ja);
				}
				else if(q > maxQ){
					jasWithMax.clear();
					jasWithMax.add(ja);
					maxQ = q;
				}
			}
			
			if(jasWithMax.size() == 1){
				selected = jasWithMax.get(0);
			}
			else{
				selected = jasWithMax.get(rand.nextInt(jasWithMax.size()));
			}
			
			if(maxQ > 0.){
				//System.out.println("choosing action with a Q greater than 0");
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
	public void setQSourceProvider(MultiAgentQSourceProvider provider) {
		this.qSourceProvider = provider;
	}

	@Override
	public void setTargetAgent(String agentName) {
		this.targetAgentQName = agentName;
	}

}
