package burlap.behavior.singleagent.learning.modellearning.rmax;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.modellearning.KWIKModel;
import burlap.behavior.singleagent.learning.modellearning.ModelLearningPlanner;
import burlap.behavior.singleagent.learning.modellearning.modelplanners.VIModelLearningPlanner;
import burlap.behavior.singleagent.learning.modellearning.models.TabularModel;
import burlap.behavior.singleagent.shaping.potential.PotentialFunction;
import burlap.mdp.core.Action;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;

import java.util.LinkedList;
import java.util.List;


/**
 * Potential Shaped RMax [1] is a generalization of RMax in which a potential-shaped reward function is used to provide less (but still admissible)
 * optimistic views of unknown state transitions. If no potential function is provided to this class, then it defaults to classic RMax optimism.
 *
 * The default constructor will use value iteration for planning, but you can provide any valueFunction you'd like. Similarly,
 * the default constructor will use a tabular transition/reward model, but you can also provide your own model learning.
 * See the {@link burlap.behavior.singleagent.learning.modellearning.KWIKModel} class for more information on defining your
 * own model.
 * 
 * 1. John Asmuth, Michael L. Littman, and Robert Zinkov. "Potential-based Shaping in Model-based Reinforcement Learning." AAAI. 2008.
 * 
 * @author James MacGlashan
 *
 */
public class PotentialShapedRMax extends MDPSolver implements LearningAgent{

	/**
	 * The model of the world that is being learned.
	 */
	protected RMaxModel model;

	
	/**
	 * The modeled reward function that is being learned.
	 */
	protected RewardFunction					modeledRewardFunction;
	
	/**
	 * The modeled terminal state function.
	 */
	protected TerminalFunction					modeledTerminalFunction;
	
	
	/**
	 * The model-adaptive planning algorithm to use
	 */
	protected ModelLearningPlanner 				modelPlanner;
	
	
	/**
	 * The maximum number of learning steps per episode before the agent gives up
	 */
	protected int								maxNumSteps = Integer.MAX_VALUE;
	
	/**
	 * the saved previous learning episodes
	 */
	protected LinkedList<Episode>		episodeHistory = new LinkedList<Episode>();
	
	/**
	 * The number of the most recent learning episodes to store.
	 */
	protected int								numEpisodesToStore = 1;
	
	
	/**
	 * Initializes for a tabular model, VI valueFunction, and standard RMax paradigm
	 * @param domain the real world domain
	 * @param gamma the discount factor
	 * @param hashingFactory the hashing factory to use for VI and the tabular model
	 * @param maxReward the maximum possible reward
	 * @param nConfident the number of observations required for the model to be confident in a transition
	 * @param maxVIDelta the maximum change in value function for VI to terminate
	 * @param maxVIPasses the maximum number of VI iterations per replan.
	 */
	public PotentialShapedRMax(SADomain domain, double gamma, HashableStateFactory hashingFactory, double maxReward, int nConfident,
							   double maxVIDelta, int maxVIPasses){
		
		this.solverInit(domain, gamma, hashingFactory);
		this.model = new RMaxModel(new TabularModel(domain, hashingFactory, nConfident),
				new RMaxPotential(maxReward, gamma), gamma, domain.getActionTypes());

		
		this.modelPlanner = new VIModelLearningPlanner(domain, this.model, gamma, hashingFactory, maxVIDelta, maxVIPasses);
		
	}
	
	
	/**
	 * Initializes for a tabular model, VI valueFunction, and potential shaped function.
	 * @param domain the real world domain
	 * @param gamma the discount factor
	 * @param hashingFactory the hashing factory to use for VI and the tabular model
	 * @param potential the admissible potential function
	 * @param nConfident the number of observations required for the model to be confident in a transition
	 * @param maxVIDelta the maximum change in value function for VI to terminate
	 * @param maxVIPasses the maximum number of VI iterations per replan.
	 */
	public PotentialShapedRMax(SADomain domain, double gamma, HashableStateFactory hashingFactory, PotentialFunction potential, int nConfident,
			double maxVIDelta, int maxVIPasses){
		
		this.solverInit(domain, gamma, hashingFactory);
		this.model = new RMaxModel(new TabularModel(domain, hashingFactory, nConfident),
				potential, gamma, domain.getActionTypes());


		
		this.modelPlanner = new VIModelLearningPlanner(domain, this.model, gamma, hashingFactory, maxVIDelta, maxVIPasses);
		
	}
	
	
	/**
	 * Initializes for a given model, model learning planner, and potential shaped function.
	 * @param domain the real world domain
	 * @param hashingFactory a state hashing factory for indexing states
	 * @param potential the admissible potential function
	 * @param model the model/model-learning algorithm to use
	 * @param plannerGenerator a generator for a model valueFunction
	 */
	public PotentialShapedRMax(SADomain domain, HashableStateFactory hashingFactory, PotentialFunction potential,
			KWIKModel model, ModelLearningPlanner plannerGenerator){
		
		this.solverInit(domain, gamma, hashingFactory);
		this.model = new RMaxModel(model,
				potential, gamma, domain.getActionTypes());


		this.modelPlanner = plannerGenerator;
		this.modelPlanner.setModel(this.model);

		
	}

	/**
	 * Returns the model learning algorithm being used.
	 * @return the model learning algorithm being used.
	 */
	public RMaxModel getModel() {
		return model;
	}



	/**
	 * Returns the planning algorithm used on the model that can be iteratively updated as the model changes.
	 * @return the planning algorithm used on the model
	 */
	public ModelLearningPlanner getModelPlanner() {
		return modelPlanner;
	}


	/**
	 * Returns the model reward function. This is expected to have larger values for unknown states.
	 * @return the model reward function
	 */
	public RewardFunction getModeledRewardFunction() {
		return modeledRewardFunction;
	}


	/**
	 * Returns the model terminal function. This should start as a null termination and add terminal states as it obsreves them.
	 * @return the model terminal function
	 */
	public TerminalFunction getModeledTerminalFunction() {
		return modeledTerminalFunction;
	}


	@Override
	public Episode runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {

		State initialState = env.currentObservation();

		this.modelPlanner.initializePlannerIn(initialState);

		Episode ea = new Episode(initialState);

		Policy policy = this.createUnmodeledFavoredPolicy();

		State curState = initialState;
		int steps = 0;
		while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)){

			Action ga = policy.action(curState);
			EnvironmentOutcome eo = env.executeAction(ga);
			ea.transition(ga, eo.op, eo.r);

			boolean modeledTerminal = this.model.terminal(eo.op);

			if(!this.model.transitionIsModeled(curState, ga)
					|| (!KWIKModel.Helper.stateTransitionsModeled(model, this.getActionTypes(), eo.op) && !modeledTerminal)){
				this.model.updateModel(eo);
				if(this.model.transitionIsModeled(curState, ga) || (eo.terminated != modeledTerminal && modeledTerminal != this.model.terminal(eo.op))){
					this.modelPlanner.modelChanged(curState);
					policy = this.createUnmodeledFavoredPolicy();
				}
			}


			curState = env.currentObservation();

			steps++;
		}

		if(episodeHistory.size() >= numEpisodesToStore){
			episodeHistory.poll();
		}
		episodeHistory.offer(ea);


		return ea;

	}



	protected Policy createUnmodeledFavoredPolicy(){
		return new UnmodeledFavoredPolicy(
				this.modelPlanner.modelPlannedPolicy(),
				this.model,
				this.getActionTypes());
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
	public void resetSolver(){
		this.model.resetModel();
		this.modelPlanner.resetSolver();
		this.episodeHistory.clear();
	}
	

	
	/**
	 * A potential function for vanilla RMax; all states have a potential value of R_max/(1-gamma)
	 * @author James MacGlashan
	 *
	 */
	public static class RMaxPotential implements PotentialFunction{
		
		/**
		 * The vmax value
		 */
		double vmax;
		
		
		/**
		 * Initializes for a given maximum reward and discount factor. Sets potential for all states to rMax/(1-gamma)
		 * @param rMax the maximum possible reward
		 * @param gamma the discount factor.
		 */
		public RMaxPotential(double rMax, double gamma){
			this.vmax = rMax / (1. - gamma);
		}

		/**
		 * Initializes using the given maximum value function value
		 * @param vMax the maximum value function value
		 */
		public RMaxPotential(double vMax){
			this.vmax = vMax;
		}

		@Override
		public double potentialValue(State s) {
			return this.vmax;
		}
		
	}
	

	
}
