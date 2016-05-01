package burlap.behavior.stochasticgames;

import burlap.behavior.stochasticgames.agents.RandomSGAgent;
import burlap.debugtools.DPrint;
import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.stochasticgames.*;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;
import burlap.oomdp.stochasticgames.common.ConstantSGStateGenerator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;


/**
 * This class provides a means to record all the interactions in a stochastic game; specifically, the sequence of states, joint actions taken, and joint reward received.
 * It also has methods to converting everything to a string/file and parsing a string/file back into this object so that results can be recorded and saved to disk.
 * <p>
 * This class should be used either by constructing with an initial state ({@link #GameAnalysis(State)}) or by constructing with the default constructor and then
 * using the {@link #initializeGameWithInitialState(State)} method before recording any further transitions. Transitions should then be recorded with the
 * {@link #recordTransitionTo(JointAction, State, Map)} method which takes as input the next state to which the agent transtions, the joint action taken
 * in the previously recorded state that causes the transition, and the joint reward received for the transition.
 * <p>
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
	public List<State>							states;
	
	/**
	 * The sequence of joint actions
	 */
	public List<JointAction>					jointActions;
	
	/**
	 * The sequence of joint rewards
	 */
	public List<Map<String, Double>>			jointRewards;
	
	
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
	public GroundedSGAgentAction getActionForAgent(int t, String agentName){
		JointAction ja = this.getJointAction(t);
		GroundedSGAgentAction gsa = ja.action(agentName);
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




	public String serialize(){

		Yaml yaml = new Yaml(new GameAnalysisYamlRepresenter());
		String yamlOut = yaml.dump(this);
		return yamlOut;
	}


	private class GameAnalysisYamlRepresenter extends Representer {

		public GameAnalysisYamlRepresenter() {
			super();
			this.representers.put(JointAction.class, new JointActionYamlRepresent());
		}


		private class JointActionYamlRepresent implements Represent{
			@Override
			public Node representData(Object o) {
				return representScalar(new Tag("!action"), ((JointAction)o).toString());
			}
		}
	}


	public static GameAnalysis parseGame(SGDomain domain, String episodeString){

		Yaml yaml = new Yaml(new GameAnalysisConstructor(domain));
		GameAnalysis ga = (GameAnalysis)yaml.load(episodeString);
		return ga;
	}

	private static class GameAnalysisConstructor extends Constructor {

		SGDomain domain;

		public GameAnalysisConstructor(SGDomain domain) {
			this.domain = domain;
			yamlConstructors.put(new Tag("!action"), new ActionConstruct());
		}


		private class ActionConstruct extends AbstractConstruct {

			@Override
			public Object construct(Node node) {
				String val = (String) constructScalar((ScalarNode)node);
				JointAction ja = parseStringIntoJointAction(val, domain);
				return ja;
			}
		}
	}




	/**
	 * Writes this game to a file. If the the directory for the specified file path do not exist, then they will be created.
	 * If the file extension is not ".game" will automatically be added. States must be serializable.
	 * @param path the path to the file in which to write this game.
	 */
	public void writeToFile(String path){
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
	 * Reads a game that was written to a file and turns into a {@link GameAnalysis} object.
	 * @param path the path to the game file.
	 * @param domain the stochastic games domain to which the states and actions belong
	 * @return an {@link GameAnalysis} object.
	 */
	public static GameAnalysis parseFileIntoGA(String path, SGDomain domain){

		//read whole file into string first
		String fcont = null;
		try{
			fcont = new Scanner(new File(path)).useDelimiter("\\Z").next();
		}catch(Exception E){
			System.out.println(E);
		}

		return parseGame(domain, fcont);
	}



	
	/**
	 * returns a string representation of a joint reward in the form:
	 * <p>
	 * agent1:r1;agent2:r2
	 * @param jointReward the joint reward
	 * @return a string representation of the joint reward
	 */
	private static String jointRewardStringRep(Map<String, Double> jointReward){
	    StringBuilder buf = new StringBuilder();
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
			SGAgentAction sa = domain.getSGAgentAction(actionName);
			GroundedSGAgentAction gsa = sa.getAssociatedGroundedAction(agentName);
			gsa.initParamsWithStringRep(actionParams);
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


	public static void main(String[] args) {

		GridGame gg = new GridGame();
		SGDomain domain = (SGDomain)gg.generateDomain();
		State s = GridGame.getTurkeyInitialState(domain);

		JointReward jr = new GridGame.GGJointRewardFunction(domain);
		TerminalFunction tf = new GridGame.GGTerminalFunction(domain);
		World world = new World(domain, jr, tf, new ConstantSGStateGenerator(s));
		DPrint.toggleCode(world.getDebugId(),false);

		SGAgent ragent1 = new RandomSGAgent();
		SGAgent ragent2 = new RandomSGAgent();

		SGAgentType type = new SGAgentType("agent", domain.getAgentActions());

		ragent1.joinWorld(world, type);
		ragent2.joinWorld(world, type);

		GameAnalysis ga = world.runGame(20);
		System.out.println(ga.maxTimeStep());

		String serialized = ga.serialize();
		System.out.println(serialized);

		GameAnalysis read = GameAnalysis.parseGame(domain, serialized);
		System.out.println(read.maxTimeStep());
		System.out.println(read.getState(0).toString());


	}

	
}
