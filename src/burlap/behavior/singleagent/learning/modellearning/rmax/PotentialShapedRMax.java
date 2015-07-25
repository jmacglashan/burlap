package burlap.behavior.singleagent.learning.modellearning.rmax;

import java.util.LinkedList;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.behavior.singleagent.learning.modellearning.ModelPlanner;
import burlap.behavior.singleagent.learning.modellearning.ModelPlanner.ModelPlannerGenerator;
import burlap.behavior.singleagent.learning.modellearning.ModeledDomainGenerator;
import burlap.behavior.singleagent.learning.modellearning.modelplanners.VIModelPlanner;
import burlap.behavior.singleagent.learning.modellearning.models.TabularModel;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.shaping.potential.PotentialFunction;
import burlap.behavior.statehashing.HashableStateFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;


/**
 * Potential Shaped RMax [1] is a generalization of RMax in which a potential-shaped reward function is used to provide less (but still admissible)
 * optimistic views of unknown state transitions. If no potential function is provided to this class, then it defaults to classic RMax optimism.
 *
 * The default constructor will use value iteration for planning, but you can provide any valueFunction you'd like. Similarly,
 * the default constructor will use a tabular transition/reward model, but you can also provide your own model learning.
 * See the {@link burlap.behavior.singleagent.learning.modellearning.Model} class for more information on defining your
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
	protected Model								model;
	
	/**
	 * The modeled domain object containing the modeled actions that a valueFunction will use.
	 */
	protected Domain							modeledDomain;
	
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
	protected ModelPlanner						modelPlanner;
	
	
	/**
	 * The maximum number of learning steps per episode before the agent gives up
	 */
	protected int								maxNumSteps = Integer.MAX_VALUE;
	
	/**
	 * the saved previous learning episodes
	 */
	protected LinkedList<EpisodeAnalysis>		episodeHistory = new LinkedList<EpisodeAnalysis>();
	
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
	public PotentialShapedRMax(Domain domain, double gamma, HashableStateFactory hashingFactory, double maxReward, int nConfident,
			double maxVIDelta, int maxVIPasses){
		
		this.solverInit(domain, null, null, gamma, hashingFactory);
		this.model = new TabularModel(domain, hashingFactory, nConfident);
		
		ModeledDomainGenerator mdg = new ModeledDomainGenerator(domain, this.model);
		this.modeledDomain = mdg.generateDomain();
		
		this.modeledTerminalFunction = new PotentialShapedRMaxTerminal(this.model.getModelTF());
		this.modeledRewardFunction = new PotentialShapedRMaxRF(this.model.getModelRF(), new RMaxPotential(maxReward, gamma));
		
		this.modelPlanner = new VIModelPlanner(modeledDomain, modeledRewardFunction, modeledTerminalFunction, gamma, hashingFactory, maxVIDelta, maxVIPasses);
		
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
	public PotentialShapedRMax(Domain domain, double gamma, HashableStateFactory hashingFactory, PotentialFunction potential, int nConfident,
			double maxVIDelta, int maxVIPasses){
		
		this.solverInit(domain, null, null, gamma, hashingFactory);
		this.model = new TabularModel(domain, hashingFactory, nConfident);
		
		ModeledDomainGenerator mdg = new ModeledDomainGenerator(domain, this.model);
		this.modeledDomain = mdg.generateDomain();
		
		this.modeledTerminalFunction = new PotentialShapedRMaxTerminal(this.model.getModelTF());
		this.modeledRewardFunction = new PotentialShapedRMaxRF(this.model.getModelRF(), potential);
		
		this.modelPlanner = new VIModelPlanner(modeledDomain, modeledRewardFunction, modeledTerminalFunction, gamma, hashingFactory, maxVIDelta, maxVIPasses);
		
	}
	
	
	/**
	 * Initializes for a given model, model valueFunction, and potential shaped function.
	 * @param domain the real world domain
	 * @param gamma the discount factor
	 * @param hashingFactory a state hashing factory for indexing states
	 * @param potential the admissible potential function
	 * @param model the model/model-learning algorithm to use
	 * @param plannerGenerator a generator for a model valueFunction
	 */
	public PotentialShapedRMax(Domain domain, double gamma, HashableStateFactory hashingFactory, PotentialFunction potential,
			Model model, ModelPlannerGenerator plannerGenerator){
		
		this.solverInit(domain, null, null, gamma, hashingFactory);
		this.model = model;
		
		ModeledDomainGenerator mdg = new ModeledDomainGenerator(domain, this.model);
		this.modeledDomain = mdg.generateDomain();
		
		this.modeledTerminalFunction = new PotentialShapedRMaxTerminal(this.model.getModelTF());
		this.modeledRewardFunction = new PotentialShapedRMaxRF(this.model.getModelRF(), potential);
		
		this.modelPlanner = plannerGenerator.getModelPlanner(modeledDomain, modeledRewardFunction, modeledTerminalFunction, gamma);
		
	}

	/**
	 * Returns the model learning algorithm being used.
	 * @return the model learning algorithm being used.
	 */
	public Model getModel() {
		return model;
	}


	/**
	 * Returns the model domain for planning. This model domain may differ from the real domain in the actions it uses for planning.
	 * @return the model domain for planning
	 */
	public Domain getModeledDomain() {
		return modeledDomain;
	}


	/**
	 * Returns the planning algorithm used on the model that can be iteratively updated as the model changes.
	 * @return the planning algorithm used on the model
	 */
	public ModelPlanner getModelPlanner() {
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
	public EpisodeAnalysis runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps) {

		State initialState = env.getCurState();

		this.modelPlanner.initializePlannerIn(initialState);

		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);

		Policy policy = this.createUnmodeledFavoredPolicy();

		State curState = initialState;
		int steps = 0;
		while(!env.curStateIsTerminal() && (steps < maxSteps || maxSteps == -1)){

			GroundedAction ga = (GroundedAction)policy.getAction(curState);
			EnvironmentOutcome eo = ga.executeIn(env);
			ea.recordTransitionTo(ga, eo.sp, eo.r);

			boolean modeledTerminal = this.model.getModelTF().isTerminal(eo.sp);

			if(!this.model.transitionIsModeled(curState, ga) || (!this.model.stateTransitionsAreModeled(eo.sp) && !modeledTerminal)){
				this.model.updateModel(eo);
				if(this.model.transitionIsModeled(curState, ga) || (eo.terminated != modeledTerminal && modeledTerminal != this.model.getModelTF().isTerminal(eo.sp))){
					this.modelPlanner.modelChanged(curState);
					policy = this.createUnmodeledFavoredPolicy();
				}
			}


			curState = env.getCurState();

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
				this.modeledDomain.getActions());
	}

	public EpisodeAnalysis getLastLearningEpisode() {
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

	public List<EpisodeAnalysis> getAllStoredLearningEpisodes() {
		return episodeHistory;
	}

	
	@Override
	public void resetSolver(){
		this.model.resetModel();
		this.modelPlanner.resetPlanner();
		this.episodeHistory.clear();
	}
	
	
	
	/**
	 * A Terminal function that treats transitions to RMax fictious nodes as terminal states as well as what the model reports as terminal states.
	 * @author James MacGlashan
	 *
	 */
	public class PotentialShapedRMaxTerminal implements TerminalFunction{

		/**
		 * The modeled terminal function
		 */
		TerminalFunction sourceModelTF;
		
		
		/**
		 * Initializes with a modeled terminal function
		 * @param sourceModelTF the model terminal function.
		 */
		public PotentialShapedRMaxTerminal(TerminalFunction sourceModelTF){
			this.sourceModelTF = sourceModelTF;
		}
		
		@Override
		public boolean isTerminal(State s) {
			//states with unmodeled transitions are terminal states; bias will be captured by the potential function
			if(!PotentialShapedRMax.this.model.stateTransitionsAreModeled(s)){
				return true;
			}
			
			return this.sourceModelTF.isTerminal(s);
		}
			
		
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
	
	
	
	/**
	 * This class is a special version of a potential shaped reward function that does not remove the potential value for transitions to states with uknown action transitions
	 * that are followed. This is accomplished by returning a value of zero when the fictious RMax state is recached, rather than subtracting off the previous
	 * states potential.
	 * @author James MacGlashan
	 *
	 */
	protected class PotentialShapedRMaxRF implements RewardFunction{

		/**
		 * The source reward function
		 */
		protected RewardFunction sourceRF;
		
		/**
		 * The state potential function
		 */
		protected PotentialFunction potential;
		
		
		/**
		 * Initializes.
		 * @param sourceRF the source reward function to which the potential is added.
		 * @param potential the state potential function
		 */
		public PotentialShapedRMaxRF(RewardFunction sourceRF, PotentialFunction potential){
			this.sourceRF = sourceRF;
			this.potential = potential;
		}
		
		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			double nextStatePotential = 0.;
			if(!PotentialShapedRMax.this.model.getModelTF().isTerminal(sprime)){
				nextStatePotential = this.potential.potentialValue(sprime);
			}
			
			return this.sourceRF.reward(s, a, sprime) 
					+ (PotentialShapedRMax.this.gamma * nextStatePotential) - this.potential.potentialValue(s);
			
		}
		
		
		
	}
	
}
