package burlap.behavior.singleagent.planning.vfa.fittedvi;

import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for performing Fitted Value Iteration [1]. This is a variant of value iteration that takes a set of
 * sample states from a domain and performs synchronous value iteration on the samples by using the Bellman operator
 * and a current approximation of the value function. Specifically, the value function is seeded to some initial value
 * (by default zero, but it can be set to something else with the {@link #setVInit(burlap.behavior.valuefunction.ValueFunctionInitialization)}
 * method). For each state sample, a new value for the state is computed by applying the bellman operator (using the model
 * of the world and the current, initially zero-valued, value function approximation). The newly computed values for each
 * state are then used as a supervised instance to train the next iteration of the value function.
 *
 * <br/><br/>
 * To perform planning after specifying the state samples to use (either in the constructor or with the {@link #setSamples(java.util.List)} method,
 * you can perform planning with the {@link #runVI()} method. You can also use the standard {@link #planFromState(burlap.oomdp.core.State)} method,
 * but specifying the state does not change behavior; the method just calls the {@link #runVI()} method itself.
 *
 * <br/><br/>
 * To compute the value of a state sample with the current value function approximation, this class will invoke the
 * {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling} class. This enables it to
 * perform an approximate Bellman operator with sparse samples from the transition dynamics, which is useful if
 * the number of possible next state transitions is infinite or very large. Furthermore, it allows you to set
 * sparse sampling tree depth larger than one to get a more accurate estimate of the target state Value. The depth
 * of the tree can be independently set when planning (that is, running value iteration) and for control (that is,
 * the depth used to return the Q-values). See the {@link #setPlanningDepth(int)}, {@link #setControlDepth(int)}, and
 * {@link #setPlanningAndControlDepth(int)} methods for controlling the depth. By default, the depth will be 1.
 *
 *
 *
 * <br/><br/>
 * 1. Gordon, Geoffrey J. "Stable function approximation in dynamic programming." Proceedings of the twelfth international conference on machine learning. 1995.
 * @author James MacGlashan.
 */
public class FittedVI extends OOMDPPlanner implements ValueFunction, QFunction {


	/**
	 * The set of samples on which to perform value iteration.
	 */
	protected List <State> samples;

	/**
	 * The current value function approximation
	 */
	protected ValueFunction valueFunction;

	/**
	 * The {@link burlap.behavior.singleagent.planning.vfa.fittedvi.SupervisedVFA} instance used to train the value function on each iteration.
	 */
	protected SupervisedVFA valueFunctionTrainer;

	/**
	 * The initial value function to use
	 */
	protected ValueFunctionInitialization vinit = new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.);


	/**
	 * This class computes the Bellman operator by using an instance of {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling}
	 * and setting its leaf nodes values to the current value function approximation. This value function initialization is points to
	 * the current value function approximation for it to use.
	 */
	protected VFAVInit leafNodeInit = new VFAVInit();

	/**
	 * The {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling} planning depth used
	 * for computing Bellman operators during value iteration.
	 */
	protected int planningDepth = 1;

	/**
	 * The {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling} depth used when
	 * computing Q-values for the {@link #getQs(burlap.oomdp.core.State)} and {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)}
	 * methods used for control.
	 */
	protected int controlDepth = 1;


	/**
	 * The number of transition samples used when computing the bellman operator. When set to -1, the full transition
	 * dynamics are used rather than sampling.
	 */
	protected int transitionSamples;


	/**
	 * The maximum number of iterations to run.
	 */
	protected int maxIterations;

	/**
	 * The maximum change in the value function that will cause planning to terminate.
	 */
	protected double maxDelta;


	/**
	 * Initializes. Note that you will need to set the state samples to use for planning with the {@link #setSamples(java.util.List)} method before
	 * calling {@link #planFromState(burlap.oomdp.core.State)}, {@link #runIteration()}, or {@link #runVI()}, otherwise a runtime exception
	 * will be thrown.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param valueFunctionTrainer the supervised learning algorithm to use for each value iteration
	 * @param transitionSamples the number of transition samples to use when computing the bellman operator; set to -1 if you want to use the full transition dynamics without sampling.
	 * @param maxDelta the maximum change in the value function that will cause planning to terminate.
	 * @param maxIterations the maximum number of iterations to run.
	 */
	public FittedVI(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, SupervisedVFA valueFunctionTrainer, int transitionSamples, double maxDelta, int maxIterations){
		this.plannerInit(domain, rf, tf, gamma, new NameDependentStateHashFactory());
		this.valueFunctionTrainer = valueFunctionTrainer;
		this.transitionSamples = transitionSamples;
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		this.debugCode = 5263;

		this.valueFunction = this.vinit;
	}


	/**
	 * Initializes. Note that you will need to set the state samples to use for planning with the {@link #setSamples(java.util.List)} method before
	 * calling {@link #planFromState(burlap.oomdp.core.State)}, {@link #runIteration()}, or {@link #runVI()}, otherwise a runtime exception
	 * will be thrown.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param valueFunctionTrainer the supervised learning algorithm to use for each value iteration
	 * @param samples the set of state samples to use for planning.
	 * @param transitionSamples the number of transition samples to use when computing the bellman operator; set to -1 if you want to use the full transition dynamics without sampling.
	 * @param maxDelta the maximum change in the value function that will cause planning to terminate.
	 * @param maxIterations the maximum number of iterations to run.
	 */
	public FittedVI(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, SupervisedVFA valueFunctionTrainer, List<State> samples, int transitionSamples, double maxDelta, int maxIterations){
		this.plannerInit(domain, rf, tf, gamma, new NameDependentStateHashFactory());
		this.valueFunctionTrainer = valueFunctionTrainer;
		this.samples = samples;
		this.transitionSamples = transitionSamples;
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		this.debugCode = 5263;

		this.valueFunction = this.vinit;
	}


	/**
	 * Returns the value function initialization used at the start of planning.
	 * @return the value function initialization used at the start of planning.
	 */
	public ValueFunctionInitialization getVInit() {
		return vinit;
	}

	/**
	 * Sets the value function initialization used at the start of planning.
	 * @param vinit the value function initialization used at the start of planning.
	 */
	public void setVInit(ValueFunctionInitialization vinit) {
		if(this.valueFunction == this.vinit){
			this.valueFunction = vinit;
		}
		this.vinit = vinit;
	}


	/**
	 * Returns the Bellman operator depth used during planning.
	 * @return the Bellman operator depth used during planning.
	 */
	public int getPlanningDepth() {
		return planningDepth;
	}


	/**
	 * Sets the Bellman operator depth used during planning.
	 * @param planningDepth the Bellman operator depth used during planning.
	 */
	public void setPlanningDepth(int planningDepth) {
		this.planningDepth = planningDepth;
	}

	/**
	 * Returns the Bellman operator depth used for computing Q-values (the {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} and {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} methods).
	 * @return the Bellman operator depth used for computing Q-values (the {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} and {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} methods).
	 */
	public int getControlDepth() {
		return controlDepth;
	}


	/**
	 * Sets the Bellman operator depth used for computing Q-values (the {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} and {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} methods).
	 * @param controlDepth the Bellman operator depth used for computing Q-values (the {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} and {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} methods).
	 */
	public void setControlDepth(int controlDepth) {
		this.controlDepth = controlDepth;
	}


	/**
	 * Sets the Bellman operator depth used during planning for computing Q-values (the {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} and {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} methods).
	 * @param depth the Bellman operator depth used during planning for computing Q-values (the {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} and {@link #getQ(burlap.oomdp.core.State, burlap.oomdp.core.AbstractGroundedAction)} methods).
	 */
	public void setPlanningAndControlDepth(int depth){
		this.planningDepth = depth;
		this.controlDepth = depth;
	}


	/**
	 * Returns the state samples to which the value function will be fit.
	 * @return the state samples to which the value function will be fit.
	 */
	public List<State> getSamples() {
		return samples;
	}

	/**
	 * Sets the state samples to which the value function will be fit.
	 * @param samples the state samples to which the value function will be fit.
	 */
	public void setSamples(List<State> samples) {
		this.samples = samples;
	}


	/**
	 * Runs value iteration. Note that if the state samples have not been set, it will throw a runtime exception.
	 */
	public void runVI(){
		for(int i = 0; i < this.maxIterations || this.maxIterations == -1; i++){
			double change = this.runIteration();
			DPrint.cl(this.debugCode, "Finished iteration " + i + "; max change: " + change);
			if(change < this.maxDelta){
				break;
			}
		}
	}


	/**
	 * Runs a single iteration of value iteration. Note that if the state samples have not been set, it will throw a runtime exception.
	 * @return the maximum change in the value function.
	 */
	public double runIteration(){

		if(this.samples == null){
			throw new RuntimeException("FittedVI cannot run value iteration because the state samples have not been set. Use the setSamples method or the constructor to set them.");
		}

		SparseSampling ss = new SparseSampling(this.domain, this.rf, this.tf, this.gamma, this.hashingFactory, this.planningDepth, this.transitionSamples);
		ss.setValueForLeafNodes(this.leafNodeInit);
		ss.toggleDebugPrinting(false);

		List <SupervisedVFA.SupervisedVFAInstance> instances = new ArrayList<SupervisedVFA.SupervisedVFAInstance>(this.samples.size());
		List <Double> oldVs = new ArrayList<Double>(this.samples.size());
		for(State s : this.samples){
			oldVs.add(this.valueFunction.value(s));
			instances.add(new SupervisedVFA.SupervisedVFAInstance(s, QFunctionHelper.getOptimalValue(ss, s)));
		}

		this.valueFunction = this.valueFunctionTrainer.train(instances);

		double maxDiff = 0.;
		for(int i = 0; i < this.samples.size(); i++){
			double newV = this.valueFunction.value(this.samples.get(i));
			double diff = Math.abs(newV - oldVs.get(i));
			maxDiff = Math.max(maxDiff, diff);
		}

		return maxDiff;

	}


	@Override
	public void planFromState(State initialState) {
		this.runVI();
	}

	@Override
	public void resetPlannerResults() {
		this.valueFunction = this.vinit;
	}

	@Override
	public List<QValue> getQs(State s) {
		SparseSampling ss = new SparseSampling(this.domain, this.rf, this.tf, this.gamma, this.hashingFactory, this.controlDepth, this.transitionSamples);
		ss.setValueForLeafNodes(this.leafNodeInit);
		ss.toggleDebugPrinting(false);
		return ss.getQs(s);
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		SparseSampling ss = new SparseSampling(this.domain, this.rf, this.tf, this.gamma, this.hashingFactory, this.controlDepth, this.transitionSamples);
		ss.setValueForLeafNodes(this.leafNodeInit);
		ss.toggleDebugPrinting(false);
		return ss.getQ(s, a);
	}

	@Override
	public double value(State s) {
		return this.valueFunction.value(s);
	}


	/**
	 * A class for {@link burlap.behavior.valuefunction.ValueFunctionInitialization} that always points to the outer class's current value function approximation.
	 */
	public class VFAVInit implements ValueFunctionInitialization{

		@Override
		public double value(State s) {
			return FittedVI.this.valueFunction.value(s);
		}

		@Override
		public double qValue(State s, AbstractGroundedAction a) {
			return this.value(s);
		}
	}


}
