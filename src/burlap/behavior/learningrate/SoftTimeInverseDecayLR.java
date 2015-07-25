package burlap.behavior.learningrate;

import java.util.HashMap;
import java.util.Map;

import burlap.behavior.statehashing.HashableStateFactory;
import burlap.behavior.statehashing.HashableState;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;


/**
 * Implements a learning rate decay schedule where the learning rate at time t is alpha_0 * (n_0 + 1) / (n_0 + t), where alpha_0 is the initial learning rate and n_0 is a parameter. When n_0 is 0, this behaves
 * has decaying a learning rate inversely porportional to the amount of time passed. The larger n_0 is, the slower the decay schedule. By default, the learning rate may
 * decrease to Double.MIN_NORMAL, which is the smallest fraction a double value can hold, but a larger minimum learning rate may also be set.
 * 
 * This class may be specified to use a universal learning rate that is shared regardless of state and action, or it can be set to have a different
 * learning rate for each state (or state feature) that is decayed independently of other states, or it may also be specified to have a learning rate that is independently
 * decayed for each state-action (or state feature-action) pair. However, the state-action decay will ignore any parameterizations of actions.
 * 
 * @author James MacGlashan
 *
 */
public class SoftTimeInverseDecayLR implements LearningRate {

	/**
	 * The initial learning rate value at time 0
	 */
	protected double initialLearningRate;
	
	/**
	 * The division scale offset
	 */
	protected double decayConstantShift;
	
	/**
	 * The minimum learning rate
	 */
	protected double minimumLR = Double.MIN_NORMAL;
	
	
	/**
	 * The universal number of learning rate polls
	 */
	protected int universalTime = 1;
	
	/**
	 * The state dependent or state-action dependent learning rate time indices
	 */
	protected Map<HashableState, StateWiseTimeIndex> stateWiseMap;
	
	/**
	 * The state feature dependent or state feature-action dependent learning rate time indicies
	 */
	protected Map<Integer, StateWiseTimeIndex> featureWiseMap;
	
	
	/**
	 * Whether the learning rate is dependent on the state
	 */
	protected boolean useStateWise = false;
	
	/**
	 * Whether the learning rate is dependent on state-actions
	 */
	protected boolean useStateActionWise = false;
	
	/**
	 * How to hash and perform equality checks of states
	 */
	protected HashableStateFactory hashingFactory;
	
	
	/**
	 * The last agent time at which they polled the learning rate
	 */
	protected int lastPollTime = -1;
	
	
	/**
	 * Initializes with an initial learning rate and decay constant shift for a state independent learning rate. Minimum learning rate that can be returned will be Double.MIN_NORMAL
	 * @param initialLearningRate the initial learning rate
	 * @param decayConstantShift the constant added to the inver time decay schedule (n_0). That is; learning rate time time t is alpha_0 * (n_0 + 1) / (n_0 + t)
	 */
	public SoftTimeInverseDecayLR(double initialLearningRate, double decayConstantShift){
		
		this.initialLearningRate = initialLearningRate;
		this.decayConstantShift = decayConstantShift;
	}
	
	
	/**
	 * Initializes with an initial learning rate and decay constant shift (n_0) for a state independent learning rate that will decay to a value no smaller than minimumLearningRate
	 * @param initialLearningRate the initial learning rate
	 * @param decayConstantShift the constant added to the inver time decay schedule (n_0). That is; learning rate time time t is alpha_0 * (n_0 + 1) / (n_0 + t)
	 * @param minimumLearningRate the smallest value to which the learning rate will decay
	 */
	public SoftTimeInverseDecayLR(double initialLearningRate, double decayConstantShift, double minimumLearningRate){
		
		this.initialLearningRate = initialLearningRate;
		this.decayConstantShift = decayConstantShift;
		this.minimumLR = minimumLearningRate;
	}
	
	
	/**
	 * Initializes with an initial learning rate and decay constant shift (n_0) for a state or state-action (or state feature-action) dependent learning rate. 
	 * Minimum learning rate that can be returned will be Double.MIN_NORMAL. If this learning rate function is to be used for state state features, rather than states,
	 * then the hashing factory can be null;
	 * @param initialLearningRate the initial learning rate for each state or state-action
	 * @param decayConstantShift the constant added to the inver time decay schedule (n_0). That is; learning rate time time t is alpha_0 * (n_0 + 1) / (n_0 + t)
	 * @param hashingFactory how to hash and compare states
	 * @param useSeparateLRPerStateAction whether to have an independent learning rate for each state-action pair, rather than just each state
	 */
	public SoftTimeInverseDecayLR(double initialLearningRate, double decayConstantShift, HashableStateFactory hashingFactory, boolean useSeparateLRPerStateAction){
		
		this.initialLearningRate = initialLearningRate;
		this.decayConstantShift = decayConstantShift;
		
		this.useStateWise = true;
		this.useStateActionWise = useSeparateLRPerStateAction;
		this.hashingFactory = hashingFactory;
		this.stateWiseMap = new HashMap<HashableState, StateWiseTimeIndex>();
		this.featureWiseMap = new HashMap<Integer, StateWiseTimeIndex>();
		
	}
	
	/**
	 * Initializes with an initial learning rate and decay constant shift (n_0) for a state or state-action (or state feature-action) dependent learning rate that will decay to a value no smaller than minimumLearningRate
	 * If this learning rate function is to be used for state state features, rather than states,
	 * then the hashing factory can be null;
	 * @param initialLearningRate the initial learning rate for each state or state-action
	 * @param decayConstantShift the constant added to the inver time decay schedule (n_0). That is; learning rate time time t is alpha_0 * (n_0 + 1) / (n_0 + t)
	 * @param minimumLearningRate the smallest value to which the learning rate will decay
	 * @param hashingFactory how to hash and compare states
	 * @param useSeparateLRPerStateAction whether to have an independent learning rate for each state-action pair, rather than just each state
	 */
	public SoftTimeInverseDecayLR(double initialLearningRate, double decayConstantShift, double minimumLearningRate, HashableStateFactory hashingFactory, boolean useSeparateLRPerStateAction){
		
		this.initialLearningRate = initialLearningRate;
		this.decayConstantShift = decayConstantShift;
		this.minimumLR = minimumLearningRate;
		
		this.useStateWise = true;
		this.useStateActionWise = useSeparateLRPerStateAction;
		this.hashingFactory = hashingFactory;
		this.stateWiseMap = new HashMap<HashableState, StateWiseTimeIndex>();
		this.featureWiseMap = new HashMap<Integer, SoftTimeInverseDecayLR.StateWiseTimeIndex>();
		
	}
	
	
	
	
	
	
	
	
	@Override
	public double peekAtLearningRate(State s, AbstractGroundedAction ga) {
		if(!useStateWise){
			return this.learningRate(this.universalTime);
		}
		
		StateWiseTimeIndex slr = this.getStateWiseTimeIndex(s);
		if(!useStateActionWise){
			return this.learningRate(slr.timeIndex);
		}
		
		return this.learningRate(slr.getActionTimeIndexEntry(ga).mi);
	}

	@Override
	public double pollLearningRate(int agentTime, State s, AbstractGroundedAction ga) {
		if(!useStateWise){
			double oldVal = this.learningRate(this.universalTime);
			if(agentTime > this.lastPollTime){
				this.universalTime++;
				this.lastPollTime = agentTime;
			}
			return oldVal;
		}
		
		StateWiseTimeIndex slr = this.getStateWiseTimeIndex(s);
		if(!useStateActionWise){
			double oldVal = this.learningRate(slr.timeIndex);
			if(agentTime > slr.lastPollTime){
				slr.timeIndex++;
				slr.lastPollTime = agentTime;
			}
			return oldVal;
		}
		
		MutableInt md = slr.getActionTimeIndexEntry(ga);
		double oldVal = this.learningRate(slr.getActionTimeIndexEntry(ga).mi);
		if(agentTime > md.lastPollTime){
			md.mi++;
			md.lastPollTime = agentTime;
		}
		return oldVal;
	}
	
	
	
	@Override
	public double peekAtLearningRate(int featureId) {
		if(!useStateWise){
			return this.learningRate(this.universalTime);
		}
		
		StateWiseTimeIndex slr = this.getFeatureWiseTimeIndex(featureId);

		return this.learningRate(slr.timeIndex);
		

	}


	@Override
	public double pollLearningRate(int agentTime, int featureId) {
		if(!useStateWise){
			double oldVal = this.learningRate(this.universalTime);
			if(agentTime > this.lastPollTime){
				this.universalTime++;
				this.lastPollTime = agentTime;
			}
			return oldVal;
		}
		
		StateWiseTimeIndex slr = this.getFeatureWiseTimeIndex(featureId);

		double oldVal = this.learningRate(slr.timeIndex);
		if(agentTime > slr.lastPollTime){
			slr.timeIndex++;
			slr.lastPollTime = agentTime;
		}
		return oldVal;
		
		

	}
	
	
	
	

	@Override
	public void resetDecay() {
		this.universalTime = 1;
		this.stateWiseMap.clear();
		this.featureWiseMap.clear();

	}
	
	
	protected double learningRate(int time){
		double r = 0.;
		if(time == 0){
			r = this.initialLearningRate;
		}
		else{
			r = this.initialLearningRate * ((this.decayConstantShift + 1) / (this.decayConstantShift + time));
		}
		r = Math.max(r, this.minimumLR);
		return r;
	}
	
	
	
	
	/**
	 * Returns the learning rate data structure for the given state. An entry will be created if it does not already exist.
	 * @param s the state to get a learning rate time index for
	 * @return the learning rate data structure for the given state feature
	 */
	protected StateWiseTimeIndex getStateWiseTimeIndex(State s){
		HashableState sh = this.hashingFactory.hashState(s);
		StateWiseTimeIndex slr = this.stateWiseMap.get(sh);
		if(slr == null){
			slr = new StateWiseTimeIndex();
			this.stateWiseMap.put(sh, slr);
		}
		return slr;
	}
	
	/**
	 * Returns the learning rate data structure for the given state feature. An entry will be created if it does not already exist.
	 * @param featureId the state feature id to get a learning rate time index for
	 * @return the learning rate data structure for the given state feature
	 */
	protected StateWiseTimeIndex getFeatureWiseTimeIndex(int featureId){
		StateWiseTimeIndex slr = this.featureWiseMap.get(featureId);
		if(slr == null){
			slr = new StateWiseTimeIndex();
			this.featureWiseMap.put(featureId, slr);
		}
		return slr;
	}
	
	
	
	/**
	 * A class for storing a time index for a state, or a time index for each action for a given state
	 * @author James MacGlashan
	 *
	 */
	protected class StateWiseTimeIndex{
		int timeIndex;
		Map<String, MutableInt> actionLearningRates = null;
		int lastPollTime = -1;
		
		public StateWiseTimeIndex(){
			this.timeIndex = 1;
			if(useStateActionWise){
				this.actionLearningRates = new HashMap<String, MutableInt>();
			}
		}
		
		/**
		 * Returns the mutable int entry for the time index for the action for the state with which this object is associated.
		 * @param ga the input action for which the learning rate is returned.
		 * @return the mutable int entry for the time index for the action for the state with which this object is associated.
		 */
		public MutableInt getActionTimeIndexEntry(AbstractGroundedAction ga){
			MutableInt entry = this.actionLearningRates.get(ga);
			if(entry == null){
				entry = new MutableInt(1);
				this.actionLearningRates.put(ga.actionName(), entry);
			}
			return entry;
		}
		
		
		
	}
	
	/**
	 * A class for storing a mutable int value object
	 * @author James MacGlashan
	 *
	 */
	protected class MutableInt{
		int mi;
		int lastPollTime = -1;
		public MutableInt(int mi){
			this.mi = mi;
		}
	}

	

}
