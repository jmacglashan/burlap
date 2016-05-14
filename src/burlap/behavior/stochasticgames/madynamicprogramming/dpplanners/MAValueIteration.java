package burlap.behavior.stochasticgames.madynamicprogramming.dpplanners;

import burlap.behavior.stochasticgames.madynamicprogramming.MADynamicProgramming;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.debugtools.DPrint;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.statehashing.HashableState;
import burlap.mdp.statehashing.HashableStateFactory;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.JointReward;
import burlap.mdp.stochasticgames.SGAgentType;
import burlap.mdp.stochasticgames.SGDomain;

import java.util.*;


/**
 * A class for performing multi-agent value iteration. This class extends the {@link burlap.behavior.stochasticgames.madynamicprogramming.MADynamicProgramming} class to provide value iteration-like
 * value function estimation. When an input state is provided via the {@link #planFromState(State)} method, if the state has already been seen
 * and planned for, then nothing happens. If the state has never been seen before, then a state reachiability analysis is first performed in which
 * all states possibly reachable from the input state are found. Then Value iteration proceeds for all states that have been found in the past.
 * The {@link #runVI()} method can also be called directly to force value iteration to be performed on all states that have been previously found,
 * but the state reachability must have been performed at least once before to seed the state space. State reachability can be performed manually
 * by calling the {@link #performStateReachabilityFrom(State)} method.
 * <p>
 * Value iteration will continue until either the maximum change in Q-value is less than some user provided threshold or until a max number
 * of iterations have passed. 
 * 
 * @author James MacGlashan
 *
 */
public class MAValueIteration extends MADynamicProgramming {

	/**
	 * The set of states that have been found
	 */
	protected Set<HashableState> states = new HashSet<HashableState>();
	
	/**
	 * The threshold that will cause VI to terminate when the max change in Q-value for is less than it
	 */
	protected double maxDelta;
	
	/**
	 * The maximum allowable number of iterations until VI termination
	 */
	protected int maxIterations;
	
	/**
	 * The debug code used for printing VI progress.
	 */
	protected int debugCode = 88934789;
	
	
	
	/**
	 * Initializes.
	 * @param domain the domain in which to perform planing
	 * @param jointReward the joint reward function
	 * @param terminalFunction the terminal state function
	 * @param discount the discount
	 * @param hashingFactory the hashing factory to use for storing states
	 * @param qInit the default Q-value to initialize all values to
	 * @param backupOperator the backup operator that defines the solution concept being solved
	 * @param maxDelta the threshold that causes VI to terminate when the max Q-value change is less than it
	 * @param maxIterations the maximum number of iterations allowed
	 */
	public MAValueIteration(SGDomain domain, JointReward jointReward, TerminalFunction terminalFunction,
			double discount, HashableStateFactory hashingFactory, double qInit, SGBackupOperator backupOperator, double maxDelta, int maxIterations){
		
		this.initMAVF(domain, null, jointReward, terminalFunction, discount, hashingFactory, new ValueFunctionInitialization.ConstantValueFunctionInitialization(qInit), backupOperator);
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		
	}
	
	
	/**
	 * Initializes.
	 * @param domain the domain in which to perform planing
	 * @param jointReward the joint reward function
	 * @param terminalFunction the terminal state function
	 * @param discount the discount
	 * @param hashingFactory the hashing factory to use for storing states
	 * @param qInit the q-value initialization function to use.
	 * @param backupOperator the backup operator that defines the solution concept being solved
	 * @param maxDelta the threshold that causes VI to terminate when the max Q-value change is less than it
	 * @param maxIterations the maximum number of iterations allowed
	 */
	public MAValueIteration(SGDomain domain, JointReward jointReward, TerminalFunction terminalFunction,
			double discount, HashableStateFactory hashingFactory, ValueFunctionInitialization qInit, SGBackupOperator backupOperator, double maxDelta, int maxIterations){
		
		this.initMAVF(domain, null, jointReward, terminalFunction, discount, hashingFactory, qInit, backupOperator);
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		
	}
	
	
	
	/**
	 * Initializes.
	 * @param domain the domain in which to perform planing
	 * @param agentDefinitions the agents involved in the planning problem
	 * @param jointReward the joint reward function
	 * @param terminalFunction the terminal state function
	 * @param discount the discount
	 * @param hashingFactory the hashing factory to use for storing states
	 * @param vInit the default value to initialize all state values to
	 * @param backupOperator the backup operator that defines the solution concept being solved
	 * @param maxDelta the threshold that causes VI to terminate when the max Q-value change is less than it
	 * @param maxIterations the maximum number of iterations allowed
	 */
	public MAValueIteration(SGDomain domain, Map<String, SGAgentType> agentDefinitions, JointReward jointReward, TerminalFunction terminalFunction,
			double discount, HashableStateFactory hashingFactory, double vInit, SGBackupOperator backupOperator, double maxDelta, int maxIterations){
		
		this.initMAVF(domain, agentDefinitions, jointReward, terminalFunction, discount, hashingFactory, new ValueFunctionInitialization.ConstantValueFunctionInitialization(vInit), backupOperator);
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		
	}
	
	
	
	/**
	 * Initializes.
	 * @param domain the domain in which to perform planing
	 * @param agentDefinitions the agents involved in the planning problem
	 * @param jointReward the joint reward function
	 * @param terminalFunction the terminal state function
	 * @param discount the discount
	 * @param hashingFactory the hashing factory to use for storing states
	 * @param vInit the state value initialization function to use.
	 * @param backupOperator the backup operator that defines the solution concept being solved
	 * @param maxDelta the threshold that causes VI to terminate when the max Q-value change is less than it
	 * @param maxIterations the maximum number of iterations allowed
	 */
	public MAValueIteration(SGDomain domain, Map<String, SGAgentType> agentDefinitions, JointReward jointReward, TerminalFunction terminalFunction,
			double discount, HashableStateFactory hashingFactory, ValueFunctionInitialization vInit, SGBackupOperator backupOperator, double maxDelta, int maxIterations){
		
		this.initMAVF(domain, agentDefinitions, jointReward, terminalFunction, discount, hashingFactory, vInit, backupOperator);
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		
	}
	
	

	@Override
	public void planFromState(State s) {
		
		if(this.performStateReachabilityFrom(s)){
			this.runVI();
		}
		
	}
	
	
	
	/**
	 * Runs Value Iteration over the set of states that have been discovered. VI terminates either when the max change in Q-value is less than the threshold stored
	 * in this object's maxDelta parameter
	 * or when the number of iterations exceeds  this object's maxIterations parameter.
	 * <p>
	 * If {@link #performStateReachabilityFrom(State)} has not yet been called, then the state set will be empty and a runtime exception will be thrown.
	 */
	public void runVI(){
		
		if(this.states.isEmpty()){
			throw new RuntimeException("No states to iterate over. Note that state reacability needs to be performed before runVI() can be called. Consider using planFromState(State s) method instead or using the performStateReachabilityFrom(State s) method first.");
		}
		
		int i;
		for(i = 0; i < this.maxIterations; i++){
			
			double maxChange = Double.NEGATIVE_INFINITY;
			for(HashableState sh : this.states){
				double change = this.backupAllValueFunctions(sh.s);
				maxChange = Math.max(change, maxChange);
			}
			
			DPrint.cl(this.debugCode, "Finished pass: " + i + " with max change: " + maxChange);
			
			if(maxChange < this.maxDelta){
				break ;
			}
			
			
			
		}
		
		DPrint.cl(this.debugCode, "Performed " + i + " passes.");
		
	}
	
	
	/**
	 * Finds and stores all states that are reachable from input state s.
	 * @param s the state from which all reachable states will be indexed
	 * @return true if input s was not previously indexed resulting in new states being found; false if s was already previously indexed resulting in no change in the discovered state set.
	 */
	public boolean performStateReachabilityFrom(State s){
		
		HashableState shi = this.hashingFactory.hashState(s);
		if(this.states.contains(shi)){
			return false;
		}
		
		this.states.add(shi);
		
		LinkedList<HashableState> openQueue = new LinkedList<HashableState>();
		openQueue.add(shi);
		
		while(!openQueue.isEmpty()){
			
			HashableState sh = openQueue.poll();
			
			//expand
			List<JointAction> jas = JointAction.getAllJointActions(sh.s, this.agentDefinitions);
			for(JointAction ja : jas){
				List<StateTransitionProb> tps = this.jointActionModel.transitionProbsFor(sh.s, ja);
				for(StateTransitionProb tp : tps){
					HashableState shp = this.hashingFactory.hashState(tp.s);
					if(!this.states.contains(shp)){
						this.states.add(shp);
						openQueue.add(shp);
					}
				}
			}
			
		}
		
		
		DPrint.cl(this.debugCode, "Finished State reachability; " + this.states.size() + " unique states found.");
		
		
		return true;
	}
	
}
