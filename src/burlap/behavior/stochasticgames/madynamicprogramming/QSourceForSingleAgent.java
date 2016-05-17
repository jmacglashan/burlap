package burlap.behavior.stochasticgames.madynamicprogramming;

import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.mdp.stochasticgames.JointAction;

import java.util.HashMap;
import java.util.Map;



/**
 * And interface for objects that can store and retrive Q-value objects for a specific agent and state and joint action query. The 
 * {@link JAQValue} returned can have their q datamember directly modified to modify the Q-values stored and retreived in this object.
 * A hash-backed implementation is also provided in this interface definition.
 * @author James MacGlashan
 *
 */
public interface QSourceForSingleAgent {

	
	/**
	 * Returns a Q-value (represented with a {@link JAQValue} object) stored for the given state and joint action. Modification to the returned object
	 * will directly modify the stored value that will be returned in subsequent calls to this method.
	 * @param s the Q-value's associated state
	 * @param ja the Q-value's associated joint aciton
	 * @return a {@link JAQValue} for the given state and joint action.
	 */
	public JAQValue getQValueFor(State s, JointAction ja);
	
	
	
	/**
	 * An implementation of the {@link QSourceForSingleAgent} interface that has Q-values backed by hashmaps. Specifically, a map from
	 * hashed stats to a map from joint actions to stored {@link JAQValue} objects. This object also takes as {@link ValueFunctionInitialization}
	 * object so that Q-values for state-jointaction pairs not previously stored can be intitialized.
	 * @author James MacGlashan
	 *
	 */
	public class HashBackedQSource implements QSourceForSingleAgent{

		protected HashMap<HashableState, Map<JointAction, JAQValue>>	qValues;
		protected HashableStateFactory hashingFactory;
		protected ValueFunctionInitialization							qInit;		
		
		
		
		/**
		 * Initializes with a given state hashing factory and value funciton initialization for Q-values.
		 * @param hashingFactory the state hashing factory used to index states
		 * @param qInit the value function intitliazaiton to use for previously unqueried states
		 */
		public HashBackedQSource(HashableStateFactory hashingFactory, ValueFunctionInitialization qInit){
			this.qValues = new HashMap<HashableState, Map<JointAction,JAQValue>>();
			this.hashingFactory = hashingFactory;
			this.qInit = qInit;
		}
		
		@Override
		public JAQValue getQValueFor(State s, JointAction ja) {
			
			Map<JointAction, JAQValue> jaQS = this.getJAMap(s);
			
			JAQValue q = jaQS.get(ja);
			if(q != null){
				return q;
			}
			
			//if we got here then we need to create the q-value
			q = new JAQValue(s, ja, this.qInit.qValue(s, ja));
			jaQS.put(ja, q);
			
			
			return q;
		}
		
		
		/**
		 * Returns the Map from joint actions to q-values for a given state. If the map does not already exist, then it is created and indexed.
		 * @param s the state for which the map is returned.
		 * @return the Map from joint actions to q-values for a given state
		 */
		protected Map<JointAction, JAQValue> getJAMap(State s){
			
			HashableState sh = this.hashingFactory.hashState(s);
			Map<JointAction, JAQValue> storedMap = this.qValues.get(sh);
			if(storedMap == null){
				storedMap = new HashMap<JointAction, JAQValue>();
				this.qValues.put(sh, storedMap);
			}
			return storedMap;
			
		}
		
		
		
	}
	
}
