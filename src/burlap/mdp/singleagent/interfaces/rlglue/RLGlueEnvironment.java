package burlap.mdp.singleagent.interfaces.rlglue;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.ActionType;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class can be used to take a BURLAP domain and task with discrete actions and turn it into an RLGlue environment with which other RLGlue agents
 * can interact. Because RLGLue requires flat vector representations of states, you must provide a {@link DenseStateFeatures}
 * to flatten the BURLAP states; it should always return arrays of the same length for all visitable states.
 * Additionally, RLGlue does not support action preconditions, so each action must be available everywhere.
 * <p>
 * Note that RLGlue does not support observations of terminal states; it only gives the final reward upon entering a terminal state.
 * Therefore, this class will not terminate in a terminal state indicated by the provided {@link burlap.mdp.core.TerminalFunction}.
 * Instead, it will allow one more transition from the terminal state, which will transition back to itself with reward zero, which
 * is mathematically equivalent to transitioning to terminal state and observing it.
 * @author James MacGlashan
 *
 */
public class RLGlueEnvironment implements EnvironmentInterface {

	/**
	 * The BURLAP domain
	 */
	protected SADomain domain;
	
	/**
	 * The state generator for generating states for each episode
	 */
	protected StateGenerator stateGenerator;

	/**
	 * Used to flatten states into a vector representation
	 */
	protected DenseStateFeatures stateFlattener;


	/**
	 * The value ranges for the vector representation of the state
	 */
	protected DoubleRange[] valueRanges;


	/**
	 * Indicates the number of times a terminal state has been visited by the agent within the same episode.
	 * This variable is used because RLGLue does not support observations into terminal states and so
	 * a terminal flag will only be set once the agent has taken one action in the terminal state, which will transition back
	 * to itself.
	 */
	protected int terminalVisits = 0;
	
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
	 * The current state of the environment
	 */
	protected State curState;
	
	
	/**
	 * A mapping from action index identifiers (that RLGlue will use) to BURLAP actions and their parametrization specified as the index of objects in a state.
	 */
	protected Map<Integer, burlap.mdp.core.Action> actionMap = new HashMap<Integer, burlap.mdp.core.Action>();
	

	
	
	/**
	 * Whether the state generated from the state generator to gather auxiliary information (like the number of objects of each class) has yet be used as a starting state for
	 * an RLGlue episode. When this value is false, the state generated in the constructor will be passed as the initial state of a new episodes. After that, this value
	 * is set to true and the states used for each RLGlue episode are generated fresh from the state generator.
	 */
	protected boolean usedConstructorState = false;
	
	
	
	/**
	 * Constructs with all the BURLAP information necessary for generating an RLGlue Environment.
	 * @param domain the BURLAP domain
	 * @param stateGenerator a generated for generating states at the start of each episode.
	 * @param stateFlattener used to flatten states into a numeric representation
	 * @param valueRanges the value ranges of the flattened vector state
	 * @param rewardRange the reward function value range
	 * @param isEpisodic whether the task is episodic or continuing
	 * @param discount the discount factor to use for the task
	 */
	public RLGlueEnvironment(SADomain domain, StateGenerator stateGenerator, DenseStateFeatures stateFlattener,
							 DoubleRange[] valueRanges,
							 DoubleRange rewardRange, boolean isEpisodic, double discount){

		if(domain.getModel() == null){
			throw new RuntimeException("RLGlueEnvironment requires a BURLAP domain with a SampleModel, but the domain does not provide one.");
		}

		this.domain = domain;
		this.stateGenerator = stateGenerator;
		this.stateFlattener = stateFlattener;
		this.valueRanges = valueRanges;
		this.rewardRange = rewardRange;
		this.isEpisodic = isEpisodic;
		this.discount = discount;
		
		State exampleState = this.stateGenerator.generateState();
		int actionInd = 0;
		for(ActionType a : this.domain.getActionTypes()){
			List<burlap.mdp.core.Action> gas = a.allApplicableActions(exampleState);
			for(burlap.mdp.core.Action ga : gas){
				this.actionMap.put(actionInd, ga);
				actionInd++;
			}
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
		

		for(int i = 0; i < this.valueRanges.length; i++){
			theTaskSpecObject.addContinuousObservation(this.valueRanges[i]);
		}
		
		return theTaskSpecObject.toTaskSpec();
	}


	@Override
	public String env_message(String arg0) {
		return "Messages not supported by default BURLAP RLGlueEnvironment";
	}

	@Override
	public Observation env_start() {
		this.terminalVisits = 0;
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
		burlap.mdp.core.Action burlapAction = this.actionMap.get(arg0.getInt(0));
		EnvironmentOutcome eo;
		State nextState;
		if(this.terminalVisits == 0) {
			eo = domain.getModel().sampleTransition(curState, burlapAction);
			if(eo.terminated){
				this.terminalVisits++;
			}
		}
		else{
			eo = new EnvironmentOutcome(this.curState, burlapAction, this.curState, 0., true);
			this.terminalVisits++;
		}
		Observation o = this.convertIntoObservation(eo.op);
		double r = eo.r;

		boolean flagTerminal = this.terminalVisits > 2;
		this.curState = eo.op;
		
		Reward_observation_terminal toRet = new Reward_observation_terminal(r, o, flagTerminal);
		
		return toRet;
	}
	
	
	/**
	 * Takes a OO-MDP state and converts it into an RLGlue observation
	 * @param s the OO-MDP state
	 * @return an RLGlue Observation
	 */
	protected Observation convertIntoObservation(State s){
		
		Observation o = new Observation(0, this.valueRanges.length);

		double [] flatRep = this.stateFlattener.features(s);
		for(int i = 0; i < flatRep.length; i++){
			o.setDouble(i, flatRep[i]);
		}
		
		return o;
	}


}
