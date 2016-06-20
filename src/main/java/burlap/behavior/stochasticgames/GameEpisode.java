package burlap.behavior.stochasticgames;

import burlap.behavior.stochasticgames.agents.RandomSGAgent;
import burlap.debugtools.DPrint;
import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.mdp.auxiliary.common.ConstantStateGenerator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.oo.OOSGDomain;
import burlap.mdp.stochasticgames.world.World;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;


/**
 * This class provides a means to record all the interactions in a stochastic game; specifically, the sequence of states, joint actions taken, and joint reward received.
 * It also has methods to converting everything to a string/file and parsing a string/file back into this object so that results can be recorded and saved to disk.
 * <p>
 * This class should be used either by constructing with an initial state ({@link #GameEpisode(State)}) or by constructing with the default constructor and then
 * using the {@link #initializeInState(State)} method before recording any further transitions. Transitions should then be recorded with the
 * {@link #transition(JointAction, State, double[])} method which takes as input the next state to which the agent transitions, the joint action taken
 * in the previously recorded state that causes the transition, and the joint reward received for the transition.
 * <p>
 * When querying about the state, joint action, or joint rewards, use the methods {@link #state(int)}, {@link #jointAction(int)}, and {@link #jointReward(int)}
 * respectively.
 * These methods take as input the time step of the element you want. Note that t = 0 refers to the initial state step so calling getState(0) and getJointAction(0)
 * will return the initial state and the joint action taken in the initial state, respectively. However, joint rewards are always received in the next time step
 * from the state and action that produced them. Therefore, getJointReward(0) is undefined. Instead, the first reward received will be at time step 1: getReward(1).
 * Additionally, the action and reward for a specific agent in a specific time step can be queried with {@link #agentAction(int, int)} and
 * {@link #agentReward(int, int)}, respectively.
 * 
 * @author James MacGlashan
 *
 */
public class GameEpisode {

	
	/**
	 * The sequence of states
	 */
	public List<State> states = new ArrayList<State>();
	
	/**
	 * The sequence of joint actions
	 */
	public List<JointAction> jointActions = new ArrayList<JointAction>();
	
	/**
	 * The sequence of joint rewards
	 */
	public List<double[]> jointRewards = new ArrayList<double[]>();

	
	
	/**
	 * Initialzes the datastructures. Note that the method {@link #initializeInState(State)} should be called
	 * to set the initial state of the game before any transitions are recorded.
	 */
	public GameEpisode(){

	}
	
	/**
	 * Initializes with an initial state of the game.
	 * @param initialState the initial state of the game.
	 */
	public GameEpisode(State initialState){
		this.initializeInState(initialState);
	}
	
	
	/**
	 * Clears out any already recorded states, joint actions, and rewards, and sets the initial state of the game.
	 * @param initialState the initial state of the game.
	 */
	public void initializeInState(State initialState){
		this.states.add(initialState);
	}

	
	
	/**
	 * Returns the state stored at time step t where t=0 refers to the initial state
	 * @param t the time step
	 * @return the state at time step t
	 */
	public State state(int t){
		if(t >= this.states.size()){
			throw new RuntimeException("This game only has " + this.states.size() + " states recorded; cannot return state at time step " + t);
		}
		return this.states.get(t);
	}
	
	
	/**
	 * Returns the joint action taken in time step t where t=0 refers to the joint action taken in the initial state.
	 * @param t the time step
	 * @return the joint action taken in time step t
	 */
	public JointAction jointAction(int t){
		if(t >= this.states.size()){
			throw new RuntimeException("This game only has " + this.jointActions.size() + " joint actions recoreded; cannot return joint action at time step " + t);
		}
		return this.jointActions.get(t);
	}
	
	
	/**
	 * Returns the joint reward received in time step t. Note that rewards are always returned in the time step after the joint action that generated them; therefore,
	 * this method is undefined for t = 0. Instead, the first time step with a reward is t=1 which refers to the joint reward received after the first joint
	 * action is taken in the initial state.
	 * @param t the time step
	 * @return the joint reward received at time step t
	 */
	public double[] jointReward(int t){
		if(t >= this.states.size()){
			throw new RuntimeException("This game only has " + this.jointRewards.size() + " joint rewards recoreded; cannot return joint reward at time step " + t);
		}
		return this.jointRewards.get(t-1);
	}
	
	
	/**
	 * Returns the action taken for the given agent at the given time step where t=0 refers to the joint action taken in the initial state.
	 * if there is no action taken by the agent with the given name, then a runtime exception is thrown.
	 * @param t the time step 
	 * @param agentNum the agent number for the action to be returned
	 * @return the action taken by the specified agent in the given time step
	 */
	public Action agentAction(int t, int agentNum){
		JointAction ja = this.jointAction(t);
		Action gsa = ja.action(agentNum);
		if(gsa == null){
			throw new RuntimeException("Agent " + agentNum + " did not take an action in joint action " + t);
		}
		return gsa;
	}
	
	
	/**
	 * Returns the reward received for the agent with the given name at the given step. Note that rewards are always returned in the time step after 
	 * the joint action that generated them; therefore,
	 * this method is undefined for t = 0. Instead, the first time step with a reward is t=1 which refers to the reward received after the first joint
	 * action is taken in the initial state.
	 * @param t the time step
	 * @param agentNum the agent for whom the reward should be returned
	 * @return the reward received by the agent
	 */
	public double agentReward(int t, int agentNum){
		double[] jr = this.jointReward(t);
		return jr[agentNum];
	}
	

	
	
	/**
	 * Returns the number of time steps recorded which is equal to the number of states observed.
	 * @return the number of time steps recorded.
	 */
	public int numTimeSteps(){
		return this.states.size();
	}
	
	
	/**
	 * Returns the max time step index in this game which equals {@link #numTimeSteps()}-1.
	 * @return the max time step index in this game
	 */
	public int maxTimeStep(){
		return this.states.size()-1;
	}
	
	
	/**
	 * Records a transition from the last recorded state in this object using the specififed joint action to the specified next state and with the specified joint reward
	 * being received as a result.
	 * @param jointAction the joint action taken in the last recorded state in this object
	 * @param nextState the next state to which the agents transition
	 * @param jointReward the joint reward received for the transiton
	 */
	public void transition(JointAction jointAction, State nextState, double[] jointReward){
		this.states.add(nextState);
		this.jointActions.add(jointAction);
		this.jointRewards.add(jointReward);
	}

	/**
	 * Returns the state sequence list object
	 * @return the state sequence list object
	 */
	public List<State> getStates() {
		return states;
	}

	
	/**
	 * Returns the joint action sequence list object
	 * @return the joint action sequence list object
	 */
	public List<JointAction> getJointActions() {
		return jointActions;
	}

	
	/**
	 * Returns the joint reward sequence list object
	 * @return the joint reward sequence list object
	 */
	public List<double[]> getJointRewards() {
		return jointRewards;
	}





	public String serialize(){

		Yaml yaml = new Yaml();
		String yamlOut = yaml.dump(this);
		return yamlOut;
	}


	public static GameEpisode parse(String episodeString){

		Yaml yaml = new Yaml();
		GameEpisode ga = (GameEpisode)yaml.load(episodeString);
		return ga;
	}





	/**
	 * Writes this game to a file. If the the directory for the specified file path do not exist, then they will be created.
	 * If the file extension is not ".game" will automatically be added. States must be serializable.
	 * @param path the path to the file in which to write this game.
	 */
	public void write(String path){
		if(!path.endsWith(".game")){
			path = path + ".game";
		}

		File f = (new File(path)).getParentFile();
		if(f != null){
			f.mkdirs();
		}


		try{

			String str = this.serialize();
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			out.write(str);
			out.close();


		}catch(Exception e){
			System.out.println(e);
		}
	}
	



	/**
	 * Reads a game that was written to a file and turns into a {@link GameEpisode} object.
	 * @param path the path to the game file.
	 * @return an {@link GameEpisode} object.
	 */
	public static GameEpisode read(String path){

		//read whole file into string first
		String fcont = null;
		try{
			fcont = new Scanner(new File(path)).useDelimiter("\\Z").next();
		}catch(Exception E){
			System.out.println(E);
		}

		return parse(fcont);
	}



	

	
	



	public static void main(String[] args) {

		GridGame gg = new GridGame();
		OOSGDomain domain = gg.generateDomain();
		State s = GridGame.getTurkeyInitialState();

		JointRewardFunction jr = new GridGame.GGJointRewardFunction(domain);
		TerminalFunction tf = new GridGame.GGTerminalFunction(domain);
		World world = new World(domain, jr, tf, new ConstantStateGenerator(s));
		DPrint.toggleCode(world.getDebugId(),false);

		SGAgent ragent1 = new RandomSGAgent();
		SGAgent ragent2 = new RandomSGAgent();

		SGAgentType type = new SGAgentType("agent", domain.getActionTypes());

		world.join(ragent1);
		world.join(ragent2);

		GameEpisode ga = world.runGame(20);
		System.out.println(ga.maxTimeStep());

		String serialized = ga.serialize();
		System.out.println(serialized);

		GameEpisode read = GameEpisode.parse(serialized);
		System.out.println(read.maxTimeStep());
		System.out.println(read.state(0).toString());


	}

	
}
