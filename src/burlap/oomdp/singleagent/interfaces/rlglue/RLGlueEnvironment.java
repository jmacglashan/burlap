package burlap.oomdp.singleagent.interfaces.rlglue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class can be used to take a BURLAP domain and task and turn it into an RLGlue environment with which other RLGlue agents
 * can interact. Because of the nature of RLGlue there are a few limitations: 
 * <br/>
 * (1) each state must have the same number of objects of each class as any other state in the environment;<br/>
 * (2) the environment cannot represent object identifier invariance and will fill in RLGlue feature vectors by object class and in the order the objects appear for each class;<br/>
 * (3) while single target relational domains can be used, multi-target relational domains cannot.
 * <p/>
 * Because a fixed number of objects for each class is assumed, action parameterization is supported by multiplying out all the possible parameterizations. In order
 * for action parameterization and relational domains to work consistently for RLGlue, the state generator should always add objects of each class to the state object
 * in the same order. For instance, in a grid world, the state generator should always add the agent object instance to the state first and then all location objects (or always
 * do it in the reverse order). Object instance names, however, can vary between generated states.
 * @author James MacGlashan
 *
 */
public class RLGlueEnvironment implements EnvironmentInterface {

	
	/**
	 * The BURLAP domain
	 */
	protected Domain domain;
	
	/**
	 * The state generator for generating states for each episode
	 */
	protected StateGenerator stateGenerator;
	
	/**
	 * The reward function
	 */
	protected RewardFunction rf;
	
	/**
	 * The terminal function
	 */
	protected TerminalFunction tf;
	
	/**
	 * The reward function value range
	 */
	protected DoubleRange rewardRange;
	
	/**
	 * Whether this task is episodic (false will indicate that it is continuing)
	 */
	protected boolean isEpisodic;
	
	/**
	 * The discount factor of the task
	 */
	protected double discount;
	
	
	/**
	 * The number of objects of each object class that will appear in all states.
	 */
	protected Map<String, Integer> numObjectsOfEachClass;
	
	
	/**
	 * The total number of objects that will appear in all states
	 */
	protected int numObjects;
	
	
	/**
	 * The current state of the environment
	 */
	protected State curState;
	
	
	/**
	 * A mapping from action index identiifers (that RLGlue wille use) to BURLAP actions and their parameterizations specified as the index of objects in a state.
	 */
	protected Map<Integer, ActionIndexParameterization> actionMap = new HashMap<Integer, RLGlueEnvironment.ActionIndexParameterization>();
	
	/**
	 * The number of RLGlue discrete attributes that will be used
	 */
	protected int numDiscreteAtts = 0;
	
	/**
	 * The number of RLGlue continuous attributes that will be used
	 */
	protected int numContinuousAtts = 0;
	
	
	/**
	 * Whether the state generated from the state generator to gather auxiliariy information (like the number of objects of each class) has yet be used as a starting state for
	 * an RLGlue episode. When this value is false, the state generated in the constructor will be passed as the initial state of a new episodes. After that, this value
	 * is set to true and the states used for each RLGlue episode are generated fresh from the state generator.
	 */
	protected boolean usedConstructorState = false;
	
	
	
	/**
	 * Constructs with all the BURLAP information necessary for generating an RLGlue Environment.
	 * @param domain the BURLAP domain
	 * @param stateGenerator a generated for generating states at the start of each episode.
	 * @param rf the reward function
	 * @param tf the terminal funciton
	 * @param rewardRange the reward function value range
	 * @param isEpisodic whether the task is episodic or continuing
	 * @param discount the discount factor to use for the task
	 */
	public RLGlueEnvironment(Domain domain, StateGenerator stateGenerator, RewardFunction rf, TerminalFunction tf,
			DoubleRange rewardRange, boolean isEpisodic, double discount){
		
		this.domain = domain;
		this.stateGenerator = stateGenerator;
		this.rf = rf;
		this.tf = tf;
		this.rewardRange = rewardRange;
		this.isEpisodic = isEpisodic;
		this.discount = discount;
		this.numObjectsOfEachClass = new HashMap<String, Integer>();
		
		this.numObjects = 0;
		for(Integer n : this.numObjectsOfEachClass.values()){
			numObjects += n;
		}
		
		State exampleState = this.stateGenerator.generateState();
		int actionInd = 0;
		for(burlap.oomdp.singleagent.Action a : this.domain.getActions()){
			//List<GroundedAction> gas = exampleState.getAllGroundedActionsFor(a);
			List<GroundedAction> gas = a.getAllApplicableGroundedActions(exampleState);
			for(GroundedAction ga : gas){
				ActionIndexParameterization ap = new ActionIndexParameterization(ga, exampleState);
				this.actionMap.put(actionInd, ap);
				actionInd++;
			}
		}
		
		List<List<ObjectInstance>> obsByClass = exampleState.getAllObjectsByTrueClass();
		for(List<ObjectInstance> obs : obsByClass){
			String className = obs.get(0).getTrueClassName();
			this.numObjectsOfEachClass.put(className, obs.size());
		}
		
		//set this to be the first state returned
		this.curState = exampleState;
		
		
	}
	
	/**
	 * Loads this environment into RLGlue
	 */
	public void load(){
		EnvironmentLoader loader = new EnvironmentLoader(this);
		loader.run();
	}
	
	/**
	 * Loads this environment into RLGLue with the specified host address and port
	 * @param hostAddress the RLGlue host address
	 * @param port the RLGlue port
	 */
	public void load(String hostAddress, String port){
		EnvironmentLoader loader = new EnvironmentLoader(hostAddress, port, this);
		loader.run();
	}
	
	@Override
	public void env_cleanup() {
		//nothing to do
	}

	@Override
	public String env_init() {
		
		TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
		
		if(this.isEpisodic){
			theTaskSpecObject.setEpisodic();
		}
		else{
			theTaskSpecObject.setContinuing();
		}
		
		theTaskSpecObject.setDiscountFactor(this.discount);
		theTaskSpecObject.setRewardRange(this.rewardRange);
		theTaskSpecObject.addDiscreteAction(new IntRange(0, this.actionMap.size()-1));
		
		
		for(Map.Entry<String, Integer> e : this.numObjectsOfEachClass.entrySet()){
			int n = e.getValue();
			List<Attribute> atts = this.domain.getObjectClass(e.getKey()).attributeList;
			for(int i = 0; i < n; i++){
				for(Attribute att: atts){
					this.addAttribute(theTaskSpecObject, att);
				}
			}
		}
		
		return theTaskSpecObject.toTaskSpec();
	}
	
	/**
	 * Adss a BURLAP attribute to the RLGlue task specification.
	 * BURLAP multi-target relational attributes are not supported and will cause a runtime exception to be thrown.
	 * @param theTaskSpecObject the RLGlue task specification
	 * @param att the BURLAP attribute to add to the spec
	 */
	protected void addAttribute(TaskSpecVRLGLUE3 theTaskSpecObject, Attribute att){
		Attribute.AttributeType type = att.type;
		if(type == AttributeType.DISC || type == AttributeType.BOOLEAN || type == AttributeType.INT){
			theTaskSpecObject.addDiscreteObservation(new IntRange((int)att.lowerLim, (int)att.upperLim));
			this.numDiscreteAtts++;
		}
		else if(type == AttributeType.RELATIONAL){
			theTaskSpecObject.addDiscreteObservation(new IntRange(0, this.numObjects-1));
			this.numDiscreteAtts++;
		}
		else if(type == AttributeType.REAL || type == AttributeType.REALUNBOUND){
			theTaskSpecObject.addContinuousObservation(new DoubleRange(att.lowerLim, att.upperLim));
			this.numContinuousAtts++;
		}
		else{
			throw new RuntimeException("Cannot create RLGlue Attribute for BURLAP att type: " + type);
		}
		
	}

	@Override
	public String env_message(String arg0) {
		return "Messages not supportd by default BURLAP RLGlueEnvironment"; 
	}

	@Override
	public Observation env_start() {
		if(usedConstructorState){
			this.curState = this.stateGenerator.generateState();
		}
		else{
			this.usedConstructorState = true;
		}
		
		return this.convertIntoObservation(this.curState);
	}

	@Override
	public Reward_observation_terminal env_step(Action arg0) {
		GroundedAction burlapAction = this.actionMap.get(arg0.getInt(0)).generateGroundedActionForState(this.curState);
		State nextState = burlapAction.executeIn(this.curState);
		Observation o = this.convertIntoObservation(nextState);
		double r = this.rf.reward(curState, burlapAction, nextState);
		boolean isTermainal = this.tf.isTerminal(nextState);
		this.curState = nextState;
		
		Reward_observation_terminal toRet = new Reward_observation_terminal(r, o, isTermainal);
		
		return toRet;
	}
	
	
	/**
	 * Takes a OO-MDP state and converts it into an RLGlue observation
	 * @param s the OO-MDP state
	 * @return an RLGlue Observation
	 */
	protected Observation convertIntoObservation(State s){
		
		Observation o = new Observation(numDiscreteAtts, numContinuousAtts);
		
		int discCounter = 0;
		int contCounter = 0;
		for(Map.Entry<String, Integer> e : this.numObjectsOfEachClass.entrySet()){
			List<ObjectInstance> obs = s.getObjectsOfClass(e.getKey());
			List<Attribute> atts = this.domain.getObjectClass(e.getKey()).attributeList;
			for(int i = 0; i < obs.size(); i++){
				ObjectInstance oi = obs.get(i);
				for(Attribute att : atts){
					if(att.type == AttributeType.DISC || att.type == AttributeType.INT || att.type == AttributeType.BOOLEAN){
						o.setInt(discCounter, oi.getDiscValForAttribute(att.name));
						discCounter++;
					}
					else if(att.type == AttributeType.REAL || att.type == AttributeType.REALUNBOUND){
						o.setDouble(contCounter, oi.getRealValForAttribute(att.name));
						contCounter++;
					}
					else if(att.type == AttributeType.RELATIONAL){
						o.setDouble(discCounter, this.objectIndex(s, oi.getName()));
						discCounter++;
					}
					
				}
			}
		}
		
		return o;
	}
	
	
	/**
	 * Returns the index of the object instance with name obName in state s.
	 * @param s the state holding the object
	 * @param obName the name of the object
	 * @return the index of obName in state s
	 */
	protected int objectIndex(State s, String obName){
		List<ObjectInstance> obs = s.getAllObjects();
		int i = 0;
		for(ObjectInstance o : obs){
			if(o.getName().equals(obName)){
				return i;
			}
			i++;
		}
		
		throw new RuntimeException("Could not find object " + obName);
	}
	
	
	
	/**
	 * A class that represents an action parameterization in terms of the object index in a state, rather than object name.
	 * @author James MacGlashan
	 *
	 */
	protected class ActionIndexParameterization{
		
		/**
		 * The BURLAP action
		 */
		public burlap.oomdp.singleagent.Action action;
		
		/**
		 * The parameters of the action specified as indecies into a state
		 */
		public int [] params;
		
		
		/**
		 * Constructs from a grounded action
		 * @param ga the grounded action to convert
		 * @param srcState the source state from which the grounded action was generated
		 */
		public ActionIndexParameterization(GroundedAction ga, State srcState){
			this.action = ga.action;
			this.params = new int[ga.params.length];
			for(int i = 0; i < ga.params.length; i++){
				this.params[i] = RLGlueEnvironment.this.objectIndex(srcState, ga.params[i]);
			}
		}
		
		
		/**
		 * Returns a grounded action for a given state by finding the object instance names for the objects in the positions of this objects
		 * parameter indcies.
		 * @param s the state for which a grounded action should be returned
		 * @return a grounded action.
		 */
		public GroundedAction generateGroundedActionForState(State s){
			List<ObjectInstance> allObjects = s.getAllObjects();
			String [] gaParams = new String[this.params.length];
			for(int i = 0; i < gaParams.length; i++){
				gaParams[i] = allObjects.get(this.params[i]).getName();
			}
			return new GroundedAction(this.action, gaParams);
		}
		
	}

}
