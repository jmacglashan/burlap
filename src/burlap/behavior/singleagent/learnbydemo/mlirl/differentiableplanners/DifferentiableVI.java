package burlap.behavior.singleagent.learnbydemo.mlirl.differentiableplanners;

import burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.planning.stochastic.ActionTransitions;
import burlap.behavior.singleagent.planning.stochastic.HashedTransitionProbability;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;

import java.util.*;

/**
 * Performs Differentiable Value Iteration using the Boltzmann backup operator and a
 * {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF}. This class
 * behaves the same as the normal {@link burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration}
 * planner except for being in the differentiable value function case.
 * @author James MacGlashan.
 */
public class DifferentiableVI extends DifferentiableVFPlanner implements Planner {

	/**
	 * When the maximum change in the value function is smaller than this value, VI will terminate.
	 */
	protected double												maxDelta;

	/**
	 * When the number of VI iterations exceeds this value, VI will terminate.
	 */
	protected int													maxIterations;


	/**
	 * Indicates whether the reachable states has been computed yet.
	 */
	protected boolean												foundReachableStates = false;


	/**
	 * When the reachability analysis to find the state space is performed, a breadth first search-like pass
	 * (spreading over all stochastic transitions) is performed. It can optionally be set so that the
	 * search is pruned at terminal states by setting this value to true. By default, it is false and the full
	 * reachable state space is found
	 */
	protected boolean												stopReachabilityFromTerminalStates = false;


	/**
	 * Indicates whether VI has been run or not
	 */
	protected boolean												hasRunVI = false;



	/**
	 * Initializes the planner.
	 * @param domain the domain in which to plan
	 * @param rf the differentiable reward function that will be used
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param boltzBeta the scaling factor in the boltzmann distribution used for the state value function. The larger the value, the more deterministic.
	 * @param hashingFactory the state hashing factor to use
	 * @param maxDelta when the maximum change in the value function is smaller than this value, VI will terminate.
	 * @param maxIterations when the number of VI iterations exceeds this value, VI will terminate.
	 */
	public DifferentiableVI(Domain domain, DifferentiableRF rf, TerminalFunction tf, double gamma, double boltzBeta, StateHashFactory hashingFactory, double maxDelta, int maxIterations){

		this.VFPInit(domain, rf, tf, gamma, hashingFactory);

		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		this.boltzBeta = boltzBeta;

	}


	/**
	 * Calling this method will force the planner to recompute the reachable states when the {@link #planFromState(burlap.oomdp.core.states.State)} method is called next.
	 * This may be useful if the transition dynamics from the last planning call have changed and if planning needs to be restarted as a result.
	 */
	public void recomputeReachableStates(){
		this.foundReachableStates = false;
		this.transitionDynamics = new HashMap<StateHashTuple, List<ActionTransitions>>();
	}


	/**
	 * Sets whether the state reachability search to generate the state space will be prune the search from terminal states.
	 * The default is not to prune.
	 * @param toggle true if the search should prune the search at terminal states; false if the search should find all reachable states regardless of terminal states.
	 */
	public void toggleReachabiltiyTerminalStatePruning(boolean toggle){
		this.stopReachabilityFromTerminalStates = toggle;
	}


	@Override
	public void planFromState(State initialState){
		this.initializeOptionsForExpectationComputations();
		if(!this.valueFunction.containsKey(this.hashingFactory.hashState(initialState))){
			this.performReachabilityFrom(initialState);
			this.runVI();
		}

	}

	@Override
	public void resetSolver(){
		super.resetSolver();
		this.foundReachableStates = false;
		this.hasRunVI = false;
	}

	/**
	 * Runs VI until the specified termination conditions are met. In general, this method should only be called indirectly through the {@link #planFromState(State)} method.
	 * The {@link #performReachabilityFrom(State)} must have been performed at least once
	 * in the past or a runtime exception will be thrown. The {@link #planFromState(State)} method will automatically call the {@link #performReachabilityFrom(State)}
	 * method first and then this if it hasn't been run.
	 */
	public void runVI(){

		if(!this.foundReachableStates){
			throw new RuntimeException("Cannot run VI until the reachable states have been found. Use the planFromState, performReachabilityFrom, addStateToStateSpace or addStatesToStateSpace methods at least once before calling runVI.");
		}

		Set<StateHashTuple> states = mapToStateIndex.keySet();

		int i = 0;
		for(i = 0; i < this.maxIterations; i++){

			double delta = 0.;
			for(StateHashTuple sh : states){

				double v = this.value(sh);
				double newV = this.performBellmanUpdateOn(sh);
				double [] ng = this.performDPValueGradientUpdateOn(sh);
				delta = Math.max(Math.abs(newV - v), delta);

			}

			if(delta < this.maxDelta){
				break; //approximated well enough; stop iterating
			}

		}

		DPrint.cl(this.debugCode, "Passes: " + i);

		this.hasRunVI = true;

	}


	/**
	 * Adds the given state to the state space over which VI iterates.
	 * @param s the state to add
	 */
	public void addStateToStateSpace(State s){
		StateHashTuple sh = this.hashingFactory.hashState(s);
		this.mapToStateIndex.put(sh, sh);
		this.foundReachableStates = true;
	}


	/**
	 * Adds a {@link java.util.Collection} of states over which VI will iterate.
	 * @param states the collection of states.
	 */
	public void addStatesToStateSpace(Collection<State> states){
		for(State s : states){
			this.addStateToStateSpace(s);
		}
	}

	/**
	 * This method will find all reachable states that will be used by the {@link #runVI()} method and will cache all the transition dynamics.
	 * This method will not do anything if all reachable states from the input state have been discovered from previous calls to this method.
	 * @param si the source state from which all reachable states will be found
	 * @return true if a reachability analysis had never been performed from this state; false otherwise.
	 */
	public boolean performReachabilityFrom(State si){



		StateHashTuple sih = this.stateHash(si);

		DPrint.cl(this.debugCode, "Starting reachability analysis");

		//add to the open list
		LinkedList<StateHashTuple> openList = new LinkedList<StateHashTuple>();
		Set <StateHashTuple> openedSet = new HashSet<StateHashTuple>();
		openList.offer(sih);
		openedSet.add(sih);


		while(openList.size() > 0){
			StateHashTuple sh = openList.poll();

			//skip this if it's already been expanded
			if(!mapToStateIndex.containsKey(sh)){
				mapToStateIndex.put(sh, sh);
			}

			//do not need to expand from terminal states if set to prune
			if(this.tf.isTerminal(sh.s) && stopReachabilityFromTerminalStates){
				continue;
			}


			//get the transition dynamics for each action and queue up new states
			List <ActionTransitions> transitions = this.getActionsTransitions(sh);
			for(ActionTransitions at : transitions){
				for(HashedTransitionProbability tp : at.transitions){
					StateHashTuple tsh = tp.sh;
					if(!openedSet.contains(tsh) && !transitionDynamics.containsKey(tsh)){
						openedSet.add(tsh);
						openList.offer(tsh);
					}
				}

			}


		}

		DPrint.cl(this.debugCode, "Finished reachability analysis; # states: " + mapToStateIndex.size());

		this.foundReachableStates = true;
		this.hasRunVI = false;

		return true;

	}


}
