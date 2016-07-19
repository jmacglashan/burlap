package burlap.behavior.singleagent.learning.tdmethods.vfa;

import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.experiencereplay.ExperienceMemory;
import burlap.behavior.singleagent.learning.experiencereplay.FixedSizeMemory;
import burlap.behavior.singleagent.options.EnvironmentOptionOutcome;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.auxiliary.common.ShallowIdentityStateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract implementation of Q-learning with value function approximation and support for using experience replay
 * and the use of stale Q-functions to use for the target Q-value, as described in the DQN work [1]. Implementing this
 * class requires implementing the {@link #updateQFunction(List)} method, which gives it a list of experiences to use
 * for updating the Q-function. The Q-function is referred to by the vfa protected data member, which is a
 * {@link burlap.behavior.functionapproximation.ParametricFunction.ParametricStateActionFunction}. A standard
 * concrete implementation is provided by {@link GradientDescentQLearning} which uses {@link burlap.behavior.functionapproximation.DifferentiableStateActionValue}
 * objects and does standard gradient descent against them using the gradients they return.
 * <br><br>
 * By default, the state q-function is not used (equivalent to updating it with every step) and the there is not experience
 * replay. However, you can change these settings with the {@link #useStaleTarget(int)} and {@link #setExperienceReplay(ExperienceMemory, int)}
 * methods.
 * <br><br>
 * Finally, this implementation also has support for setting a {@link StateMapping} function so that states are transformed
 * from the {@link EnvironmentOutcome} observation into some other representation. By default, not state mapping is performed
 * (uses a {@link ShallowIdentityStateMapping}), but you can change that with the {@link StateMapping} method.
 * <br><br>
 * [1] Mnih, Volodymyr, et al. "Human-level control through deep reinforcement learning." Nature 518.7540 (2015): 529-533.
 * @author James MacGlashan.
 */
public abstract class ApproximateQLearning extends MDPSolver implements LearningAgent, QProvider {

	/**
	 * The value function approximation used for Q-values.
	 */
	protected ParametricFunction.ParametricStateActionFunction vfa;

	/**
	 * The stale value function approximation used as the target toward which Q-values are updated.
	 */
	protected ParametricFunction.ParametricStateActionFunction staleVfa;

	/**
	 * The number of time steps until the stale VFA used as the target is updated to the newest Q-function estimate
	 */
	protected int staleDuration = 1;


	/**
	 * The number of learners steps that have been made since the stale function was last updated
	 */
	protected int stepsSinceStale = 0;



	/**
	 * The experiences memory used for updating Q-values
	 */
	protected ExperienceMemory memory = new FixedSizeMemory(1, true);


	/**
	 * The state mapping to convert between states
	 */
	protected StateMapping stateMapping;


	/**
	 * The number of experiences to use for learners
	 */
	protected int numReplay = 1;

	/**
	 * The learners policy to use. Typically these will be policies that link back to this object so that they change as the Q-value estimate change.
	 */
	protected Policy learningPolicy;


	/**
	 * The total number of learners steps that have taken place
	 */
	protected int totalSteps = 0;

	/**
	 * Total number of learning episodes
	 */
	protected int totalEpisodes = 0;


	/**
	 * Initializes
	 * @param domain the learning domain
	 * @param gamma the discount factor
	 * @param vfa the value function approximation to use
	 */
	public ApproximateQLearning(SADomain domain, double gamma, ParametricFunction.ParametricStateActionFunction vfa) {
		this(domain, gamma, vfa, new ShallowIdentityStateMapping());
	}


	/**
	 * Initializes.
	 * @param domain the learning domain
	 * @param gamma the discount factor
	 * @param vfa the value function approximation to use
	 * @param stateMapping the state mapping to use to process a state observation from the environment
	 */
	public ApproximateQLearning(SADomain domain, double gamma, ParametricFunction.ParametricStateActionFunction vfa, StateMapping stateMapping) {
		this.vfa = vfa;
		this.staleVfa = vfa;
		this.learningPolicy = new EpsilonGreedy(this, 0.1);
		this.stateMapping = stateMapping;

		this.solverInit(domain, gamma, null);
	}

	/**
	 * Sets which policy this agent should use for learners.
	 * @param p the policy to use for learners.
	 */
	public void setLearningPolicy(Policy p){
		this.learningPolicy = p;
	}


	/**
	 * Sets the experience replay memory to use and the number of samples to take from the memory after each step for updating
	 * @param memory {@link ExperienceMemory} to use
	 * @param numReplay the number of samples from the memory used for updating the Q-function
	 */
	public void setExperienceReplay(ExperienceMemory memory, int numReplay){
		this.memory = memory;
		this.numReplay = numReplay;
	}


	/**
	 * Sets whether a state version of the value function should be used when updating the target
	 * @param staleDuration the number of learning steps for which a state version of Q-function is used as a target until it is updated to the current value function
	 */
	public void useStaleTarget(int staleDuration){
		if(this.staleDuration <= 1 && staleDuration > 1){
			this.staleVfa = (ParametricFunction.ParametricStateActionFunction)this.vfa.copy();
		}
		if(this.staleDuration > 1 && staleDuration <= 1){
			this.staleVfa = this.vfa;
		}
		this.staleDuration = staleDuration;
	}

	public StateMapping getStateMapping() {
		return stateMapping;
	}

	public void setStateMapping(StateMapping stateMapping) {
		this.stateMapping = stateMapping;
	}

	@Override
	public Episode runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {

		State initialState = env.currentObservation();
		Episode e = new Episode(initialState);


		int eStepCounter = 0;
		while(!env.isInTerminalState() && (eStepCounter < maxSteps || maxSteps == -1)){

			//check state
			State curState = stateMapping.mapState(env.currentObservation());

			//select action
			Action a = this.learningPolicy.action(curState);

			//take action
			EnvironmentOutcome eo = env.executeAction(a);

			//save outcome in memory
			this.memory.addExperience(eo);

			//record transition and manage option case
			int stepInc = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome)eo).numSteps() : 1;
			eStepCounter += stepInc;
			this.totalSteps += stepInc;
			e.transition(a, eo.op, eo.r);

			//perform learners
			List<EnvironmentOutcome> samples = this.memory.sampleExperiences(this.numReplay);
			this.updateQFunction(samples);

			//update stale function
			this.stepsSinceStale++;
			if(this.stepsSinceStale >= this.staleDuration){
				this.updateStaleFunction();
			}

		}

		this.totalEpisodes++;
		return e;
	}

	@Override
	public void resetSolver() {
		this.vfa.resetParameters();
		this.memory.resetMemory();
		this.totalSteps = 0;
		this.totalEpisodes = 0;
	}

	@Override
	public List<QValue> qValues(State s) {
		s = this.stateMapping.mapState(s);
		List<Action> actions = this.applicableActions(s);
		List<QValue> qs = new ArrayList<QValue>(actions.size());
		for(Action a : actions){
			QValue q = new QValue(s, a, this.qValue(s, a));
			qs.add(q);
		}
		return qs;
	}

	@Override
	public double qValue(State s, Action a) {
		s = this.stateMapping.mapState(s);
		return this.vfa.evaluate(s, a);
	}

	@Override
	public double value(State s) {
		s = this.stateMapping.mapState(s);
		List<QValue> qs = this.qValues(s);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			max = Math.max(max, q.q);
		}
		return max;
	}


	/**
	 * Returns all Q-value estimates from the current state Q-function
	 * @param s the state for which the Q-values are to be returned
	 * @return all Q-value estimates from the current state Q-function; a {@link List} of {@link QValue} objects.
	 */
	public List<QValue> getStaleQs(State s) {
		s = this.stateMapping.mapState(s);
		List<Action> actions = this.applicableActions(s);
		List<QValue> qs = new ArrayList<QValue>(actions.size());
		for(Action a : actions){
			QValue q = this.getStaleQ(s, a);
			qs.add(q);
		}
		return qs;
	}


	/**
	 * Returns the state Q-function estimate for the state-action pair
	 * @param s the {@link State}
	 * @param a the {@link Action}
	 * @return the {@link QValue} under the state Q-function
	 */
	public QValue getStaleQ(State s, Action a) {
		s = this.stateMapping.mapState(s);
		double qv = this.staleVfa.evaluate(s, a);
		return new QValue(s, a, qv);
	}


	/**
	 * The stale state value function estimate (max state Q-value)
	 * @param s the state for which the value should be returned
	 * @return stale state value function estimate (max state Q-value)
	 */
	public double staleValue(State s) {
		s = this.stateMapping.mapState(s);
		List<QValue> qs = this.getStaleQs(s);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			max = Math.max(max, q.q);
		}
		return max;
	}


	/**
	 * Updates the state Q-function to the current value function, by setting the state function to a copy of the current value
	 * function estimate. A copy is made by invoking the {@link ParametricFunction#copy()} method. However, if the state duration
	 * is set to 1 or zero (equivalent to not using a stale function) then the stale function is simply pointed to the same
	 * object as the current value function, without making a copy to save on compute and memory.
	 */
	public void updateStaleFunction(){
		if(this.staleDuration > 1){
			this.staleVfa = (ParametricFunction.ParametricStateActionFunction)this.vfa.copy();
		}
		else{
			this.staleVfa = this.vfa;
		}
		this.stepsSinceStale = 0;
	}


	/**
	 * Causes learned to be resumed as if stepNumber is the total number of learning steps and episodeNumber is the total number of learning episodes
	 * @param stepNumber the presumed number of learning steps
	 * @param episodeNumber the presume number of episode steps
	 */
	public void resumeFrom(int stepNumber, int episodeNumber) {
		totalSteps = stepNumber;
		totalEpisodes = episodeNumber;
		updateStaleFunction();
	}


	/**
	 * Causes this objects value function approximation to be updated with respect to the provided experiences. This should result in the
	 * vfa data member being updated. Updates should be made using target Q-values of the next state from the stale Q-function, which can be accessed
	 * by the staleVfa data member.
	 * @param samples the experience samples to use for updating the Q-function.
	 */
	public abstract void updateQFunction(List<EnvironmentOutcome> samples);

}
