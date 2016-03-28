package burlap.behavior.singleagent.learning.tdmethods.vfa;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.experiencereplay.ExperiencesMemory;
import burlap.behavior.singleagent.learning.experiencereplay.FixedSizeMemory;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.support.EnvironmentOptionOutcome;
import burlap.behavior.singleagent.vfa.ParametricFunction;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

import java.util.ArrayList;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public abstract class ApproximateQLearning extends MDPSolver implements LearningAgent, QFunction {


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
	 * The number of learning steps that have been made since the stale function was last updated
	 */
	protected int stepsSinceStale = 0;



	/**
	 * The experiences memory used for updating Q-values
	 */
	protected ExperiencesMemory memory = new FixedSizeMemory(1);


	/**
	 * The number of experiences to use for learning
	 */
	protected int numReplay = 1;

	/**
	 * The learning policy to use. Typically these will be policies that link back to this object so that they change as the Q-value estimate change.
	 */
	protected Policy learningPolicy;


	/**
	 * Whether options should be decomposed into actions in the returned {@link burlap.behavior.singleagent.EpisodeAnalysis} objects.
	 */
	protected boolean shouldDecomposeOptions = true;


	/**
	 * Whether decomposed options should have their primitive actions annotated with the options name in the returned {@link burlap.behavior.singleagent.EpisodeAnalysis} objects.
	 */
	protected boolean shouldAnnotateOptions = true;


	/**
	 * The total number of learning steps that have taken place
	 */
	protected int totalSteps = 0;


	public ApproximateQLearning(Domain domain, double gamma, ParametricFunction.ParametricStateActionFunction vfa) {
		this.vfa = vfa;
		this.staleVfa = vfa;
		this.learningPolicy = new EpsilonGreedy(this, 0.1);
		this.solverInit(domain, null, null, gamma, null);
	}

	/**
	 * Sets which policy this agent should use for learning.
	 * @param p the policy to use for learning.
	 */
	public void setLearningPolicy(Policy p){
		this.learningPolicy = p;
	}


	public void setExperienceReplay(ExperiencesMemory memory, int numReplay){
		this.memory = memory;
		this.numReplay = numReplay;
	}

	public void useStaleTarget(int staleDuration){
		if(this.staleDuration <= 1 && staleDuration > 1){
			this.staleVfa = (ParametricFunction.ParametricStateActionFunction)this.vfa.copy();
		}
		if(this.staleDuration > 1 && staleDuration <= 1){
			this.staleVfa = this.vfa;
		}
		this.staleDuration = staleDuration;
	}


	/**
	 * Sets whether the primitive actions taken during an options will be included as steps in produced EpisodeAnalysis objects.
	 * The default value is true. If this is set to false, then EpisodeAnalysis objects returned from a learning episode will record options
	 * as a single "action" and the steps taken by the option will be hidden.
	 * @param toggle whether to decompose options into the primitive actions taken by them or not.
	 */
	public void toggleShouldDecomposeOption(boolean toggle){

		this.shouldDecomposeOptions = toggle;
		for(Action a : actions){
			if(a instanceof Option){
				((Option)a).toggleShouldRecordResults(toggle);
			}
		}
	}

	/**
	 * Sets whether options that are decomposed into primitives will have the option that produced them and listed.
	 * The default value is true. If option decomposition is not enabled, changing this value will do nothing. When it
	 * is enabled and this is set to true, primitive actions taken by an option in EpisodeAnalysis objects will be
	 * recorded with a special action name that indicates which option was called to produce the primitive action
	 * as well as which step of the option the primitive action is. When set to false, recorded names of primitives
	 * will be only the primitive action's name it will be unclear which option was taken to generate it.
	 * @param toggle whether to annotate the primitive actions of options with the calling option's name.
	 */
	public void toggleShouldAnnotateOptionDecomposition(boolean toggle){
		shouldAnnotateOptions = toggle;
		for(Action a : actions){
			if(a instanceof Option){
				((Option)a).toggleShouldAnnotateResults(toggle);
			}
		}
	}


	@Override
	public EpisodeAnalysis runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps) {

		State initialState = env.getCurrentObservation();
		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);


		int eStepCounter = 0;
		while(!env.isInTerminalState() && (eStepCounter < maxSteps || maxSteps == -1)){

			//check state
			State curState = env.getCurrentObservation();

			//select action
			GroundedAction a = (GroundedAction)this.learningPolicy.getAction(curState);

			//take action
			EnvironmentOutcome eo = a.executeIn(env);

			//save outcome in memory
			this.memory.addExperience(eo);

			//record transition and manage option case
			int stepInc = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome)eo).numSteps : 1;
			eStepCounter += stepInc;
			this.totalSteps += stepInc;
			if(a.action.isPrimitive() || !this.shouldAnnotateOptions){
				ea.recordTransitionTo(a, eo.op, eo.r);
			}
			else{
				ea.appendAndMergeEpisodeAnalysis(((Option)a.action).getLastExecutionResults());
			}

			//perform learning
			List<EnvironmentOutcome> samples = this.memory.sampleExperiences(this.numReplay);
			this.updateQFunction(samples);

			//update stale function
			this.stepsSinceStale++;
			if(this.stepsSinceStale >= this.staleDuration){
				this.updateStaleFunction();
			}

		}

		return ea;
	}

	@Override
	public void resetSolver() {
		this.vfa.resetParameters();
		this.memory.resetMemory();
		this.totalSteps = 0;
	}

	@Override
	public List<QValue> getQs(State s) {
		List<GroundedAction> actions = this.getAllGroundedActions(s);
		List<QValue> qs = new ArrayList<QValue>(actions.size());
		for(GroundedAction a : actions){
			QValue q = this.getQ(s, a);
			qs.add(q);
		}
		return qs;
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		double qv = this.vfa.evaluate(s, a);
		return new QValue(s, a, qv);
	}

	@Override
	public double value(State s) {
		List<QValue> qs = this.getQs(s);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			max = Math.max(max, q.q);
		}
		return max;
	}


	public List<QValue> getStaleQs(State s) {
		List<GroundedAction> actions = this.getAllGroundedActions(s);
		List<QValue> qs = new ArrayList<QValue>(actions.size());
		for(GroundedAction a : actions){
			QValue q = this.getStaleQ(s, a);
			qs.add(q);
		}
		return qs;
	}

	public QValue getStaleQ(State s, AbstractGroundedAction a) {
		double qv = this.staleVfa.evaluate(s, a);
		return new QValue(s, a, qv);
	}

	public double staleValue(State s) {
		List<QValue> qs = this.getStaleQs(s);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			max = Math.max(max, q.q);
		}
		return max;
	}

	public void updateStaleFunction(){
		if(this.staleDuration > 1){
			this.staleVfa = (ParametricFunction.ParametricStateActionFunction)this.vfa.copy();
		}
		else{
			this.staleVfa = this.vfa;
		}
		this.stepsSinceStale = 0;
	}

	public abstract void updateQFunction(List<EnvironmentOutcome> samples);


}
