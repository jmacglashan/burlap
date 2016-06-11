package burlap.behavior.singleagent.learning.modellearning.artdp;

import burlap.behavior.policy.BoltzmannQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.modellearning.KWIKModel;
import burlap.behavior.singleagent.learning.modellearning.LearnedModel;
import burlap.behavior.singleagent.learning.modellearning.models.TabularModel;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.mdp.core.Action;

import burlap.mdp.core.state.State;

import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;

import java.util.LinkedList;
import java.util.List;


/**
 * This class provides an implementation of Adaptive Realtime Dynamic Programming [1]. By default, a tabular model will be used and a boltzmann distribution with
 * a temperature of 0.1 will be used. A different model can be provided in the constructor as well as the value function initialization used. The policy
 * followed may be set with a setter ({@link #setPolicy(burlap.behavior.policy.SolverDerivedPolicy)}). The Q-value assigned to state-action pairs for entirely untried
 * transitions is reported as that returned by the value function initializer provided. In general, value function initialization should always be optimistic.
 * 
 * 
 * 1. Barto, Andrew G., Steven J. Bradtke, and Satinder P. Singh. "Learning to act using real-time dynamic programming." Artificial Intelligence 72.1 (1995): 81-138.
 * 
 * @author James MacGlashan
 *
 */
public class ARTDP extends MDPSolver implements QFunction,LearningAgent{

	/**
	 * The model of the world that is being learned.
	 */
	protected LearnedModel model;
	
	/**
	 * The valueFunction used on the modeled world to update the value function
	 */
	protected DynamicProgramming 				modelPlanner;
	
	/**
	 * the policy to follow
	 */
	protected Policy							policy;
	
	/**
	 * the saved previous learning episodes
	 */
	protected LinkedList<Episode>		episodeHistory = new LinkedList<Episode>();
	
	
	/**
	 * The maximum number of learning steps per episode before the agent gives up
	 */
	protected int								maxNumSteps = Integer.MAX_VALUE;
	
	
	/**
	 * The number of the most recent learning episodes to store.
	 */
	protected int								numEpisodesToStore = 1;
	
	
	
	
	/**
	 * Initializes using a tabular model of the world and a Boltzmann policy with a fixed temperature of 0.1. 
	 * @param domain the domain
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for the tabular model and the planning
	 * @param vInit the constant value function initialization to use; should be optimisitc.
	 */
	public ARTDP(SADomain domain, double gamma, HashableStateFactory hashingFactory, double vInit){
		
		this.solverInit(domain, gamma, hashingFactory);
		
		this.model = new TabularModel(domain, hashingFactory, 1);
		
		//initializing the value function planning mechanisms to use our model and not the real world
		this.modelPlanner = new DynamicProgramming();
		this.modelPlanner.DPPInit(domain, gamma, hashingFactory);
		this.modelPlanner.setModel(this.model);
		this.policy = new BoltzmannQPolicy(this, 0.1);
		
		
	}
	
	
	/**
	 * Initializes using a tabular model of the world and a Boltzmann policy with a fixed temperature of 0.1. 
	 * @param domain the domain
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for the tabular model and the planning
	 * @param vInit the value function initialization to use; should be optimisitc.
	 */
	public ARTDP(SADomain domain, double gamma, HashableStateFactory hashingFactory, ValueFunctionInitialization vInit){
		
		this.solverInit(domain, gamma, hashingFactory);
		
		this.model = new TabularModel(domain, hashingFactory, 1);
		
		//initializing the value function planning mechanisms to use our model and not the real world
		this.modelPlanner = new DynamicProgramming();
		this.modelPlanner.DPPInit(domain, gamma, hashingFactory);
		this.modelPlanner.setModel(this.model);
		this.policy = new BoltzmannQPolicy(this, 0.1);
		
		
	}
	
	
	/**
	 * Initializes using the provided model algorithm and a Boltzmann policy with a fixed temperature of 0.1. 
	 * @param domain the domain
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for the tabular model and the planning
	 * @param model the model algorithm to use
	 * @param vInit the constant value function initialization to use; should be optimisitc.
	 */
	public ARTDP(SADomain domain, double gamma, HashableStateFactory hashingFactory, LearnedModel model, ValueFunctionInitialization vInit){
		
		this.solverInit(domain, gamma, hashingFactory);
		
		this.model = model;
		
		//initializing the value function planning mechanisms to use our model and not the real world
		this.modelPlanner = new DynamicProgramming();
		this.modelPlanner.DPPInit(domain, gamma, hashingFactory);
		this.policy = new BoltzmannQPolicy(this, 0.1);
		
		
	}

	
	/**
	 * Sets the policy to the provided one. Should be a policy that operates on a {@link burlap.behavior.valuefunction.QFunction}. Will automatically set its
	 * Q-source to this object.
	 * @param policy the policy to use.
	 */
	public void setPolicy(SolverDerivedPolicy policy){
		this.policy = (Policy)policy;
		policy.setSolver(this);
		
	}


	@Override
	public Episode runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {

		State initialState = env.currentObservation();

		Episode ea = new Episode(initialState);

		State curState = initialState;
		int steps = 0;
		while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)){
			Action ga = policy.action(curState);
			EnvironmentOutcome eo = env.executeAction(ga);


			ea.transition(ga, eo.op, eo.r);

			this.model.updateModel(eo);

			this.modelPlanner.performBellmanUpdateOn(eo.o);

			curState = env.currentObservation();
			steps++;

		}

		return ea;
	}



	public Episode getLastLearningEpisode() {
		return episodeHistory.getLast();
	}

	public void setNumEpisodesToStore(int numEps) {
		if(numEps > 0){
			numEpisodesToStore = numEps;
		}
		else{
			numEpisodesToStore = 1;
		}
	}

	public List<Episode> getAllStoredLearningEpisodes() {
		return episodeHistory;
	}

	
	@Override
	public List<QValue> getQs(State s) {
		List<QValue> qs = this.modelPlanner.getQs(s);

		if(this.model instanceof KWIKModel){
			for(QValue q : qs){
				//if Q for unknown action, use value initialization of current state
				if(!((KWIKModel)this.model).transitionIsModeled(s, q.a)){
					q.q = this.modelPlanner.getValueFunctionInitialization().qValue(s, q.a);
				}
			}
		}


		return qs;
	}


	@Override
	public QValue getQ(State s, Action a) {
		
		QValue q = this.modelPlanner.getQ(s, a);

		if(this.model instanceof KWIKModel){
			//if Q for unknown action, use value initialization of curent state
			if(!((KWIKModel)this.model).transitionIsModeled(s, q.a)){
				q.q = this.modelPlanner.getValueFunctionInitialization().qValue(s, q.a);
			}
		}

		return q;
	}


	@Override
	public double value(State s) {
		return this.modelPlanner.value(s);
	}
	
	public void resetSolver(){
		this.model.resetModel();
		this.modelPlanner.resetSolver();
		this.episodeHistory.clear();
	}

	

}
