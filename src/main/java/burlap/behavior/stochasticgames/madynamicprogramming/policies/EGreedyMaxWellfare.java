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
 * An epsilon greedy joint policy, in which the joint aciton with the highest aggregate Q-values for each agent is returned a 1-epsilon fraction of the time and a random
 * joint action an epsilon fraction of the time. Ties are broken deterministically (the first joint aciton
 * with the maximum value is selected), but can be set to default to break ties randomly. The former is useful to maintain consistency between agents selecting their action indepdently from each other. 
 * This policy is typically used for CoCo-Q agents.
 * @author James MacGlashan
 *
 */
public class EGreedyMaxWellfare extends MAQSourcePolicy implements EnumerablePolicy {

	
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
	 * Whether ties should be broken randomly or not.
	 */
	protected boolean						breakTiesRandomly = true;
	
	
	
	/**
	 * Initializes for a given epsilon value. The set of agents for which joint actions are returned
	 * and multi-agent q-source provider will need to be set manually with the methods {@link #setAgentsInJointPolicy(List)},
	 * and {@link #setQSourceProvider(MultiAgentQSourceProvider)} before the policy can be queried.
	 * Note that the {@link MultiAgentQLearning} and {@link burlap.behavior.stochasticgames.agents.madp.MultiAgentDPPlanningAgent} agents may do this themselves. Consult the documentation
	 * to check.
	 * @param epsilon the fraction of the time [0, 1] that the agent selections random actions.
	 */
	public EGreedyMaxWellfare(double epsilon){
		this.epsilon = epsilon;
	}
	
	/**
	 * Initializes for a given epsilon value and whether to break ties randomly. The set of agents for which joint actions are returned
	 * and multi-agent q-source provider will need to be set manually with the methods {@link #setAgentsInJointPolicy(List)} ,
	 * and {@link #setQSourceProvider(MultiAgentQSourceProvider)} before the policy can be queried.
	 * Note that the {@link MultiAgentQLearning} and {@link burlap.behavior.stochasticgames.agents.madp.MultiAgentDPPlanningAgent} agents may do this themselves. Consult the documentation
	 * to check.
	 * @param epsilon the fraction of the time [0, 1] that the agent selections random actions.
	 * @param breakTiesRandomly whether ties should be broken randomly (true) or not (false)
	 */
	public EGreedyMaxWellfare(double epsilon, boolean breakTiesRandomly){
		this.epsilon = epsilon;
		this.breakTiesRandomly = breakTiesRandomly;
	}
	
	
	/**
	 * Initializes for a multi-agent Q-learning object and epsilon value. The set of agents for which joint actions are to be returned
	 * must be subsequently defined with the {@link #setAgentsInJointPolicy(List)}. Note that the {@link MultiAgentQLearning} and {@link burlap.behavior.stochasticgames.agents.madp.MultiAgentDPPlanningAgent}
	 * agents may do this themselves. Consult the documentation to check. 
	 * @param actingAgent the agent who will use this policy.
	 * @param epsilon the fraction of the time [0, 1] that the agent selections random actions.
	 */
	public EGreedyMaxWellfare(MultiAgentQLearning actingAgent, double epsilon) {
		this.qSourceProvider = actingAgent;
		this.epsilon = epsilon;
	}
	
	/**
	 * Initializes for a multi-agent Q-learning object and epsilon value. The set of agents for which joint actions are to be returned
	 * must be subsequently defined with the {@link #setAgentsInJointPolicy(List)}. Note that the {@link MultiAgentQLearning} and {@link burlap.behavior.stochasticgames.agents.madp.MultiAgentDPPlanningAgent}
	 * agents may do this themselves. Consult the documentation to check. 
	 * @param actingAgent the agent who will use this policy.
	 * @param epsilon the fraction of the time [0, 1] that the agent selections random actions.
	 * @param breakTiesRandomly whether ties should be broken randomly (true) or not (false)
	 */
	public EGreedyMaxWellfare(MultiAgentQLearning actingAgent, double epsilon, boolean breakTiesRandomly) {
		this.qSourceProvider = actingAgent;
		this.epsilon = epsilon;
		this.breakTiesRandomly = breakTiesRandomly;
	}
	
	
	/**
	 * Whether to break ties randomly or deterministically. The former is useful for exploration during learning. The latter is useful
	 * to synchronize action selection for agents that must select an action indepdently from the same joint policy.
	 * @param breakTiesRandomly true if ties will be broken randomly; false if ties will be broken detemrinistically.
	 */
	public void setBreakTiesRandomly(boolean breakTiesRandomly){
		this.breakTiesRandomly = breakTiesRandomly;
	}
	
	@Override
	public void setQSourceProvider(MultiAgentQSourceProvider provider){
		this.qSourceProvider = provider;
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
			double maxSumQ = Double.NEGATIVE_INFINITY;
			for(JointAction ja : jas){
				double sumQ = 0.;
				for(int i = 0; i < this.agentsInJointPolicy.size(); i++){
					sumQ += qSources.agentQSource(i).getQValueFor(s, ja).q;
				}
				if(sumQ == maxSumQ && this.breakTiesRandomly){
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
		double maxSumQ = Double.NEGATIVE_INFINITY;
		for(JointAction ja : jas){
			double sumQ = 0.;
			for(int i = 0; i < this.agentsInJointPolicy.size(); i++){
				sumQ += qSources.agentQSource(i).getQValueFor(s, ja).q;
			}
			if(sumQ == maxSumQ && this.breakTiesRandomly){
				jasWithMax.add(ja);
			}
			else if(sumQ > maxSumQ){
				jasWithMax.clear();
				jasWithMax.add(ja);
				maxSumQ = sumQ;
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
	public void setTargetAgent(int agentNum) {
		//do nothing
	}

	@Override
	public JointPolicy copy() {
		EGreedyMaxWellfare np = new EGreedyMaxWellfare(this.epsilon, this.breakTiesRandomly);
		np.setAgentTypesInJointPolicy(this.agentsInJointPolicy);
		np.setQSourceProvider(this.qSourceProvider);
		return np;
	}

}
