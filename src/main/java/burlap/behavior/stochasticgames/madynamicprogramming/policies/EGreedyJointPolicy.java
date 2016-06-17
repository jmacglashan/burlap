package burlap.behavior.stochasticgames.madynamicprogramming.policies;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.stochasticgames.JointPolicy;
import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgames.madynamicprogramming.AgentQSourceMap;
import burlap.behavior.stochasticgames.madynamicprogramming.MAQSourcePolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.MultiAgentQSourceProvider;
import burlap.datastructures.HashedAggregator;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * An epsilon greedy joint policy, in which the joint action with the highest Q-value for a given target agent is returned a 1-epsilon fraction
 * of the time, and a random joint action an epsilon fraction of the time. Ties are broken randomly.
 * @author James MacGlashan
 *
 */
public class EGreedyJointPolicy extends MAQSourcePolicy implements EnumerablePolicy {

	
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
	protected int targetAgent;
	
	
	/**
	 * Initializes for a given epsilon value. The set of agents for which joint actions are returned, target agent whose q-values are maximized,
	 * and multi-agent q-source provider will need to be set manually with the methods {@link #setAgentsInJointPolicy(List)},
	 * {@link #setTargetAgent(int)}, and {@link #setQSourceProvider(MultiAgentQSourceProvider)} before the policy can be queried.
	 * Note that the {@link MultiAgentQLearning} and {@link burlap.behavior.stochasticgames.agents.madp.MultiAgentDPPlanningAgent} agents may do this themselves. Consult the documentation
	 * to check.
	 * @param epsilon the fraction of the time [0, 1] that the agent selections random actions.
	 */
	public EGreedyJointPolicy(double epsilon) {
		this.epsilon = epsilon;
	}
	
	
	/**
	 * Initializes for a multi-agent Q-learning object and epsilon value. The set of agents for which joint actions are to be returned
	 * must be subsequently defined with the {@link #setAgentsInJointPolicy(List)}. Note that the {@link MultiAgentQLearning} and {@link burlap.behavior.stochasticgames.agents.madp.MultiAgentDPPlanningAgent}
	 * agents may do this themselves. Consult the documentation to check.
	 * @param actingAgent the agent whose Q-values are maximized.
	 * @param epsilon the fraction of the time [0, 1] that the agent selections random actions.
	 */
	public EGreedyJointPolicy(MultiAgentQLearning actingAgent, double epsilon, int targetAgentNum) {
		this.qSourceProvider = actingAgent;
		this.epsilon = epsilon;
		this.targetAgent = targetAgentNum;
	}
	
	@Override
	public Action action(State s) {
		
		List<JointAction> jas = this.getAllJointActions(s);
		JointAction selected;
		AgentQSourceMap qSources = this.qSourceProvider.getQSources();
		
		
		double r = rand.nextDouble();
		if(r < this.epsilon){
			selected = jas.get(rand.nextInt(jas.size()));
		}
		else{
			List<JointAction> jasWithMax = new ArrayList<JointAction>(jas.size());
			double maxQ = Double.NEGATIVE_INFINITY;
			for(JointAction ja : jas){
				
				double q = qSources.agentQSource(this.targetAgent).getQValueFor(s, ja).q;

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
			
			
		}
		
		return selected;
	}

	@Override
	public double actionProb(State s, Action a) {
		return PolicyUtils.actionProbFromEnum(this, s, a);
	}

	@Override
	public List<ActionProb> policyDistribution(State s) {
		
		List<JointAction> jas = this.getAllJointActions(s);
		AgentQSourceMap qSources = this.qSourceProvider.getQSources();
		
		HashedAggregator<JointAction> sumProb = new HashedAggregator<JointAction>();
		double eCont = this.epsilon / jas.size();
		
		for(JointAction ja : jas){
			sumProb.add(ja, eCont);
		}
		
		
		List<JointAction> jasWithMax = new ArrayList<JointAction>(jas.size());
		double maxQ = Double.NEGATIVE_INFINITY;
		for(JointAction ja : jas){
			
			double q = qSources.agentQSource(this.targetAgent).getQValueFor(s, ja).q;

			if(q == maxQ){
				jasWithMax.add(ja);
			}
			else if(q > maxQ){
				jasWithMax.clear();
				jasWithMax.add(ja);
				maxQ = q;
			}
		}
		
		double maxCont = (1. - this.epsilon) / jasWithMax.size();
		for(JointAction ja : jasWithMax){
			sumProb.add(ja, maxCont);
		}
		
		List<ActionProb> aps = new ArrayList<ActionProb>(jas.size());
		for(JointAction ja : jas){
			double p = sumProb.v(ja);
			if(p > 0.){
				aps.add(new ActionProb(ja, p));
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

	@Override
	public void setTargetAgent(int agentNum) {
		this.targetAgent = agentNum;
	}


	@Override
	public JointPolicy copy() {
		EGreedyJointPolicy np = new EGreedyJointPolicy(this.epsilon);
		np.setAgentTypesInJointPolicy(this.agentsInJointPolicy);
		np.setQSourceProvider(this.qSourceProvider);
		np.setTargetAgent(this.targetAgent);
		return np;
	}

}
