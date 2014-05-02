package burlap.behavior.stochasticgame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * This class provides a means to record all the interactions in a stochastic game; specifically, the sequence of states, joint actions taken, and joint reward received.
 * It also has methods to converting everything to a string/file and parsing a string/file back into this object so that results can be recorded and saved to disk.
 * <p/>
 * This class should be used either by constructing with an initial state ({@link #GameAnalysis(State)}) or by constructing with the default constructor and then
 * using the {@link #initializeGameWithInitialState(State)} method before recording any further transitions. Transitions should then be recorded with the
 * {@link #recordTransitionTo(JointAction, State, Map)} method which takes as input the next state to which the agent transtions, the joint action taken
 * in the previously recorded state that causes the transition, and the joint reward received for the transition.
 * <p/>
 * When querying about the state, joint action, or joint rewards, use the methods {@link #getState(int)}, {@link #getJointAction(int)}, and {@link #getJointReward(int)}
 * respectively.
 * These methods take as input the time step of the element you want. Note that t = 0 refers to the initial state step so calling getState(0) and getJointAction(0)
 * will return the initial state and the joint action taken in the initial state, respectively. However, joint rewards are always received in the next time step
 * from the state and action that produced them. Therefore, getJointReward(0) is undefined. Instead, the first reward received will be at time step 1: getReward(1).
 * Additionally, the action and reward for a specific agent in a specific time step can be queried with {@link #getActionForAgent(int, String)} and
 * {@link #getRewardForAgent(int, String)}, respectively.
 * 
 * @author James MacGlashan
 *
 */
public class GameAnalysis {

	
	/**
	 * The sequence of states
	 */
	protected List<State>						states;
	
	/**
	 * The sequence of joint actions
	 */
	protected List<JointAction>					jointActions;
	
	/**
	 * The sequence of joint rewards
	 */
	protected List<Map<String, Double>>			jointRewards;
	
	
	/**
	 * The set of agents involved in this game
	 */
	protected Set<String>						agentsInvolvedInGame;
	
	
	/**
	 * Initialzes the datastructures. Note that the method {@link #initializeGameWithInitialState(State)} should be called
	 * to set the initial state of the game before any transitions are recorded.
	 */
	public GameAnalysis(){
		this.initializeDatastructures();
	}
	
	/**
	 * Initializes with an initial state of the game.
	 * @param initialState the initial state of the game.
	 */
	public GameAnalysis(State initialState){
		this.initializeGameWithInitialState(initialState);
	}
	
	
	/**
	 * Clears out any already recorded states, joint actions, and rewards, and sets the initial state of the game.
	 * @param initialState the initial state of the game.
	 */
	public void initializeGameWithInitialState(State initialState){
		this.initializeDatastructures();
		this.states.add(initialState);
	}
	
	
	/**
	 * Instantiates the datastructures of this object.
	 */
	protected void initializeDatastructures(){
		this.states = new ArrayList<State>();
		this.jointActions = new ArrayList<JointAction>();
		this.jointRewards = new ArrayList<Map<String,Double>>();
		this.agentsInvolvedInGame = new HashSet<String>();
	}
	
	
	/**
	 * Returns the state stored at time step t where t=0 refers to the initial state
	 * @param t the time step
	 * @return the state at time step t
	 */
	public State getState(int t){
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
	public JointAction getJointAction(int t){
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
	public Map<String, Double> getJointReward(int t){
		if(t >= this.states.size()){
			throw new RuntimeException("This game only has " + this.jointRewards.size() + " joint rewards recoreded; cannot return joint reward at time step " + t);
		}
		return this.jointRewards.get(t-1);
	}
	
	
	/**
	 * Returns the action taken for the given agent at the given time step where t=0 refers to the joint action taken in the initial state.
	 * if there is no action taken by the agent with the given name, then a runtime exception is thrown.
	 * @param t the time step 
	 * @param agentName the name of the agent
	 * @return the action taken by the specified agent in the given time step
	 */
	public GroundedSingleAction getActionForAgent(int t, String agentName){
		JointAction ja = this.getJointAction(t);
		GroundedSingleAction gsa = ja.action(agentName);
		if(gsa == null){
			throw new RuntimeException("Agent " + agentName + " did not take an action in joint action " + t);
		}
		return gsa;
	}
	
	
	/**
	 * Returns the reward received for the agent with the given name at the given step. Note that rewards are always returned in the time step after 
	 * the joint action that generated them; therefore,
	 * this method is undefined for t = 0. Instead, the first time step with a reward is t=1 which refers to the reward received after the first joint
	 * action is taken in the initial state.
	 * @param t the time step
	 * @param agentName the name of the agent
	 * @return the reward received by the agent
	 */
	public double getRewardForAgent(int t, String agentName){
		Map<String, Double> jr = this.getJointReward(t);
		Double r = jr.get(agentName);
		if(r == null){
			throw new RuntimeException("Agent "  + agentName + " did not receive a reward in joint reward " + t);
		}
		return r;
	}
	
	
	/**
	 * Returns true if an agent with the given name took any actions in the course of this game.
	 * @param agentName the name of the agent
	 * @return true if the agent took an action in this game; false otherwise.
	 */
	public boolean agentIsInvolvedInGame(String agentName){
		return this.agentsInvolvedInGame.contains(agentName);
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
	 * being recieved as a result.
	 * @param jointAction the joint action taken in the last recorded state in this object
	 * @param nextState the next state to which the agents transition
	 * @param jointReward the joint reward received for the transiton
	 */
	public void recordTransitionTo(JointAction jointAction, State nextState,  Map<String, Double> jointReward){
		this.states.add(nextState);
		this.jointActions.add(jointAction);
		this.jointRewards.add(jointReward);
		for(String agent : jointAction.getAgentNames()){
			this.agentsInvolvedInGame.add(agent);
		}
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
	public List<Map<String, Double>> getJointRewards() {
		return jointRewards;
	}

	
	/**
	 * Returns the set of agents involved in this game
	 * @return the set of agents involved in this game
	 */
	public Set<String> getAgentsInvolvedInGame() {
		return agentsInvolvedInGame;
	}
	
	
	/**
	 * Converts this game into a string representation.
	 * @param sp the state parser to use to convert state objects to string representations.
	 * @return a string representation of this episode.
	 */
	public String parseIntoString(StateParser sp){
		
		StringBuffer sbuf = new StringBuffer(256);
		
		for(int i = 0; i < states.size(); i++){
			
			sbuf.append("#EL#\n").append(sp.stateToString(states.get(i))).append("\n#ES#\n");
			if(i < states.size()-1){
				sbuf.append(jointActions.get(i).toString()).append("\n").append(jointRewardStringRep(jointRewards.get(i))).append("\n");
			}
			
		}
		
		
		return sbuf.toString();
		
	}
	
	
	/**
	 * Writes this game to a file. If the the directory for the specified file path do not exist, then they will be created.
	 * If the file extension is not ".game" will automatically be added.
	 * @param path the path to the file in which to write this game.
	 * @param sp the state parser to use to convert state objects to string representations.
	 */
	public void writeToFile(String path, StateParser sp){
		
		if(!path.endsWith(".game")){
			path = path + ".game";
		}
		
		File f = (new File(path)).getParentFile();
		f.mkdirs();
		
		
		try{
			
			String str = this.parseIntoString(sp);
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			out.write(str);
			out.close();
			
			
		}catch(Exception e){
			System.out.println(e);
		}
		
	}
	
	
	
	/**
	 * Parses a string representing a {@link GameAnalysis} object into an actual {@link GameAnalysis} object.
	 * @param str the string representation
	 * @param domain the stochastic games domain to which the actions belong
	 * @param sp the state parser used to represent states
	 * @return a {@link GameAnalysis} object.
	 */
	public static GameAnalysis parseStringIntoGameAnalysis(String str, SGDomain domain, StateParser sp){
		
		GameAnalysis ga = new GameAnalysis();
		
		String [] elComps = str.split("#EL#\n");
		
		for(int i = 1; i < elComps.length; i++){
			
			String spToken = "\n#ES#";
			if(!elComps[i].endsWith(spToken)){
				spToken += "\n";
			}
			
			String [] parts = elComps[i].split(spToken);
			
			State s = sp.stringToState(parts[0]);
			if(i < elComps.length-1){
				String [] ars = parts[1].split("\n");
				ga.recordTransitionTo( parseStringIntoJointAction(ars[0], domain), s, parseStringIntoJointReward(ars[1]));
			}
			else{
				ga.getStates().add(s);
			}
		}
		
		
		return ga;
	}
	
	
	
	/**
	 * Reads a game that was written to a file and turns into a {@link GameAnalysis} object.
	 * @param path the path to the game file.
	 * @param domain the stochastic games domain to which the states and actions belong
	 * @param sp a state parser that can parse the state string representation in the file
	 * @return an {@link GameAnalysis} object.
	 */
	public static GameAnalysis parseFileIntoEA(String path, SGDomain domain, StateParser sp){
		
		//read whole file into string first
		String fcont = null;
		try{
			fcont = new Scanner(new File(path)).useDelimiter("\\Z").next();
		}catch(Exception E){
			System.out.println(E);
		}
		
		return parseStringIntoGameAnalysis(fcont, domain, sp);
	}
	
	
	
	
	/**
	 * returns a string representation of a joint reward in the form:
	 * <br/>
	 * agent1:r1;agent2:r2
	 * @param jointReward the joint reward
	 * @return a string representation of the joint reward
	 */
	private static String jointRewardStringRep(Map<String, Double> jointReward){
		StringBuffer buf = new StringBuffer();
		boolean doneFirst = false;
		for(Map.Entry<String, Double> e : jointReward.entrySet()){
			if(doneFirst){
				buf.append(";");
			}
			buf.append(e.getKey()).append(":").append(e.getValue().toString());
			doneFirst = true;
		}
		
		return buf.toString();
	}
	
	
	/**
	 * Parses a string representation into a joint action aassumign the same {@link JointAction#toString()} format.
	 * @param str the string representation
	 * @param domain the stochastic games domain for the relevant actions
	 * @return a joint action
	 */
	private static JointAction parseStringIntoJointAction(String str, SGDomain domain){
		
		JointAction ja = new JointAction();
		
		String [] agentWiseComps = str.split(";");
		for(String aa : agentWiseComps){
			String [] agentActionComps = aa.split(":");
			String agentName = agentActionComps[0];
			String [] actionElements = agentActionComps[1].split(" ");
			String actionName = actionElements[0];
			String [] actionParams = new String[actionElements.length-1];
			for(int i = 1; i < actionElements.length; i++){
				actionParams[i-1] = actionElements[i];
			}
			SingleAction sa = domain.getSingleAction(actionName);
			GroundedSingleAction gsa = new GroundedSingleAction(agentName, sa, actionParams);
			ja.addAction(gsa);
		}
		
		return ja;
	}
	
	
	/**
	 * Parses a string formatted according to {@link #jointRewardStringRep(Map)} into a Joint reward map.
	 * @param str the string rep of the joint reward
	 * @return the joint reward map
	 */
	private static Map<String, Double> parseStringIntoJointReward(String str){
		Map<String, Double> jointReward  = new HashMap<String, Double>();
		String [] arComps = str.split(";");
		for(String ar : arComps){
			String [] comps = ar.split(":");
			jointReward.put(comps[0], Double.parseDouble(comps[1]));
		}
		return jointReward;
	}
	
	
}
