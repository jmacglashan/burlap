package burlap.mdp.singleagent.pomdp;

import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.mdp.singleagent.SADomain;


/**
 * A class for defining POMDP domains. Primarily, this adds to the {@link burlap.mdp.singleagent.SADomain} definition
 * an {@link burlap.mdp.singleagent.pomdp.ObservationFunction}. It also adds an optional {@link burlap.behavior.singleagent.auxiliary.StateEnumerator}
 * since many POMDP algorithms require knowing in advance the full underlying MDP state space. However, not all domains
 * can enumerate all the states, and therefore this field is not required to be set. If a POMDP algorithm requires
 * a {@link burlap.behavior.singleagent.auxiliary.StateEnumerator}, then it should first check the {@link #providesStateEnumerator()}
 * method first. Querying the {@link #getStateEnumerator()} when it is unset will result in a Runtime exception.
 */
public class PODomain extends SADomain {

	/**
	 * The observation function
	 */
	protected ObservationFunction 	obsevationFunction;

	/**
	 * The underlying MDP state enumerator
	 */
	protected StateEnumerator		stateEnumerator;


	/**
	 * Sets the {@link burlap.mdp.singleagent.pomdp.ObservationFunction} used by the domain.
	 * @param observationFunction the {@link burlap.mdp.singleagent.pomdp.ObservationFunction} to be used by the domain.
	 */
	public void setObservationFunction(ObservationFunction observationFunction){
		this.obsevationFunction = observationFunction;
	}

	/**
	 * Returns the {@link burlap.mdp.singleagent.pomdp.ObservationFunction} used by this domain.
	 * @return the {@link burlap.mdp.singleagent.pomdp.ObservationFunction} used by this domain.
	 */
	public ObservationFunction getObservationFunction(){
		return this.obsevationFunction;
	}


	/**
	 * Indicates whether this domain has a {@link burlap.behavior.singleagent.auxiliary.StateEnumerator} defined for it.
	 * If true, then it does provide a {@link burlap.behavior.singleagent.auxiliary.StateEnumerator}, if false, then
	 * it does not. POMDP algorithms that require access to a {@link burlap.behavior.singleagent.auxiliary.StateEnumerator}
	 * should always query this method to check, because querying the {@link #getStateEnumerator()} when one is not provided
	 * by this domain will result in a runtime exception.
	 * @return True if this POMDP domain provides a {@link burlap.behavior.singleagent.auxiliary.StateEnumerator}, false otherwise.
	 */
	public boolean providesStateEnumerator(){ return this.stateEnumerator != null; }

	/**
	 * Gets the {@link burlap.behavior.singleagent.auxiliary.StateEnumerator} used by this domain to enumerate all underlying MDP states.
	 * If no {@link burlap.behavior.singleagent.auxiliary.StateEnumerator} is provided by this domain, then a runtime exception
	 * will be thrown. To check if a {@link burlap.behavior.singleagent.auxiliary.StateEnumerator} is provided, use the
	 * {@link #providesStateEnumerator()} method.
	 * @return the {@link burlap.behavior.singleagent.auxiliary.StateEnumerator} used by this domain to enumerate all underlying MDP states.
	 */
	public StateEnumerator getStateEnumerator() {
		if(this.stateEnumerator == null){
			throw new RuntimeException("This domain cannot return a StateEnumerator because one is not defined for it. " +
					"Use the providesStateEnumerator() method to check if one is provided in advance.");
		}
		return stateEnumerator;
	}


	/**
	 * Sets the {@link burlap.behavior.singleagent.auxiliary.StateEnumerator} used by this domain to enumerate all underlying MDP states.
	 * @param stateEnumerator the {@link burlap.behavior.singleagent.auxiliary.StateEnumerator} used by this domain to enumerate all underlying MDP states.
	 */
	public void setStateEnumerator(StateEnumerator stateEnumerator) {
		this.stateEnumerator = stateEnumerator;
	}
	
	
	
}
