package burlap.behavior.singleagent;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.RandomPolicy;
import burlap.datastructures.AlphanumericSorting;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


/**
 * This class is used to keep track of all events that occur in an episode. This class should be created by either calling the constructor with the initial state of the episode,
 * or by calling the default constructor and then calling the {@link #initializeInState(State)} method to set the initial state of the episode, before recording
 * any transitions. It is then advised that transitions are recorded with the {@link #transition(Action, State, double)} method, which takes as input
 * the next state to which the agent transitioned, the action applied in the last recorded state, and the reward received fro the transition.
 * <p>
 * When querying about the state, action, and reward sequences, use the {@link #state(int)}, {@link #action(int)}, and {@link #reward(int)} methods.
 * These methods take as input the time step of the element you want. Note that t = 0 refers to the initial state step so calling getState(0) and getAction(0)
 * will return the initial state and the action taken in the initial state, respectively. However, rewards are always received in the next time step
 * from the state and action that produced them. Therefore, getReward(0) is undefined. Instead, the first reward received will be at time step 1: getReward(1).
 * 
 * 
 * @author James MacGlashan
 *
 */
public class Episode {

	/**
	 * The sequence of states observed
	 */
	public List<State> stateSequence = new ArrayList<State>();
	
	/**
	 * The sequence of actions taken
	 */
	public List<Action> actionSequence = new ArrayList<Action>();
	
	/**
	 * The sequence of rewards received. Note the reward stored at index i is the reward received at time step i+1.
	 */
	public List<Double>	rewardSequence = new ArrayList<Double>();
	
	
	/**
	 * Creates a new EpisodeAnalysis object. Before recording transitions, the {@link #initializeInState(State)} method
	 * should be called to set the initial state of the episode.
	 */
	public Episode(){

	}
	
	
	/**
	 * Initializes a new EpisodeAnalysis object with the initial state in which the episode started.
	 * @param initialState the initial state of the episode
	 */
	public Episode(State initialState){
		this.initializeInState(initialState);
	}
	
	/**
	 * Initializes this object with the initial state in which the episode started.
	 * @param initialState the initial state of the episode
	 */
	public void initializeInState(State initialState){
		if(this.stateSequence.size() > 0){
			throw new RuntimeException("Cannot initialize episode, because episode is already initialized in a state.");
		}
		this.stateSequence.add(initialState);
	}
	

	
	
	/**
	 * Adds a state to the state sequence. In general, it is recommended that {@link #initializeInState(State)} method
	 * along with subsequent calls to the {@link #transition(Action, State, double)} method is used instead, but this
	 * method can be used to manually add a state.
	 * @param s the state to add
	 */
	public void addState(State s){
		stateSequence.add(s);
	}
	
	/**
	 * Adds a GroundedAction to the action sequence. In general, it is recommended that {@link #initializeInState(State)} method
	 * along with subsequent calls to the {@link #transition(Action, State, double)} method is used instead, but this
	 * method can be used to manually add a GroundedAction.
	 * @param ga the GroundedAction to add
	 */
	public void addAction(Action ga){
		actionSequence.add(ga);
	}
	
	/**
	 * Adds a reward to the reward sequence. In general, it is recommended that {@link #initializeInState(State)} method
	 * along with subsequent calls to the {@link #transition(Action, State, double)} method is used instead, but this
	 * method can be used to manually add a reward.
	 * @param r the reward to add
	 */
	public void addReward(double r){
		rewardSequence.add(r);
	}


	/**
	 * Records a transition event where the agent applied the usingAction action in the last
	 * state in this object's state sequence, transitioned to state nextState, and received reward r,. 
	 * @param usingAction the action the agent used that caused the transition
	 * @param nextState the next state to which the agent transitioned
	 * @param r the reward the agent received for this transition.
	 */
	public void transition(Action usingAction, State nextState, double r){
		stateSequence.add(nextState);
		actionSequence.add(usingAction);
		rewardSequence.add(r);
	}

	/**
	 * Records a transition event from the {@link EnvironmentOutcome}. Assumes that the last state recorded in
	 * this {@link Episode} is the same as the previous state ({@link EnvironmentOutcome#o} in the {@link EnvironmentOutcome}
	 * @param eo an {@link EnvironmentOutcome} specifying a new transition for this episode.
	 */
	public void transition(EnvironmentOutcome eo){
		this.stateSequence.add(eo.op);
		this.actionSequence.add(eo.a);
		this.rewardSequence.add(eo.r);
	}
	
	
	/**
	 * Returns the state observed at time step t. t=0 refers to the initial state.
	 * @param t the time step of the episode
	 * @return the state at time step t
	 */
	public State state(int t){
		if(t >= this.stateSequence.size()){
			throw new RuntimeException("Episode has nothing recorded for time step "  + t);
		}

		return stateSequence.get(t);
	}
	
	/**
	 * Returns the action taken in the state at time step t. t=0 refers to the action taken in the initial state.
	 * @param t the time step of the episode
	 * @return the action taken at time step t
	 */
	public Action action(int t){
		if(t == this.actionSequence.size()){
			throw new RuntimeException("Episode does not contain action at time step " + t + ". Note that an Episode " +
					"always has a final state at one time step larger than the last action time step " +
					"(the final state reached).");
		}
		if(t > this.actionSequence.size()){
			throw new RuntimeException("Episode has nothing recorded for time step "  + t);
		}

		return actionSequence.get(t);
	}
	
	/**
	 * Returns the reward received at timestep t. Note that the fist received reward will be at time step 1, which is the reward received
	 * after taking the first action in the initial state.
	 * @param t the time step of the episode
	 * @return the ith reward received in this episode
	 */
	public double reward(int t){
		if(t == 0){
			throw new RuntimeException("Cannot return the reward received at time step 0; the first received reward occurs after the initial state at time step 1");
		}
		if(t > rewardSequence.size()){
			throw new RuntimeException("There are only " + this.rewardSequence.size() + " rewards recorded; cannot return the reward for time step " + t);
		}
		return rewardSequence.get(t-1);
	}
	
	/**
	 * Returns the number of time steps in this episode, which is equivalent to the number of states. Note that
	 * there will be no action in the last time step.
	 * @return the number of time steps in this episode
	 */
	public int numTimeSteps(){
		return stateSequence.size(); //state sequence will always have the most because of initial state and terminal state
	}
	
	
	/**
	 * Returns the maximum time step index in this episode which is the {@link #numTimeSteps()}-1. Note that there
	 * is will be no action in the last time step.
	 * @return the maximum time step index in this episode
	 */
	public int maxTimeStep(){
		return this.stateSequence.size()-1;
	}


	/**
	 * Returns the number of actions, which is 1 less than the number of states.
	 * @return the number of actions
	 */
	public int numActions(){ return this.actionSequence.size();}

	/**
	 * Will return the discounted return received from the first state in the episode to the last state in the episode.
	 * @param discountFactor the discount factor to compute the discounted return; should be on [0, 1]
	 * @return the discounted return of the episode
	 */
	public double discountedReturn(double discountFactor){
		double discount = 1.;
		double sum = 0.;
		for(double r : rewardSequence){
			sum += discount*r;
			discount *= discountFactor;
		}
		return sum;
	}
	
	
	/**
	 * This method will append execution results in e to this object's results. Note that it is assumed that the initial state in e
	 * is the last state recorded in this object. This method is useful for appending the results of an option's execution
	 * to a episode.
	 * @param e the execution results to append to this episode.
	 */
	public void appendAndMergeEpisodeAnalysis(Episode e){
		for(int i = 0; i < e.numTimeSteps()-1; i++){
			this.transition(e.action(i), e.state(i+1), e.reward(i+1));
		}
	}
	
	
	/**
	 * Returns a string representing the actions taken in this episode. Actions are separated
	 * by ';' characters.
	 * @return a string representing the actions taken in this episode
	 */
	public String actionString(){
		return this.actionString("; ");
	}
	
	
	/**
	 * Returns a string representing the actions taken in this episode. Actions are separated
	 * by the provided delimiter string.
	 * @param delimiter the delimiter to separate actions in the string.
	 * @return a string representing the actions taken in this episode
	 */
	public String actionString(String delimiter){
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for(Action ga : actionSequence){
			if(!first){
				buf.append(delimiter);
			}
			buf.append(ga.toString());
			first = false;
		}
		
		return buf.toString();
	}


	/**
	 * Takes a {@link java.util.List} of {@link Episode} objects and writes them to a directory.
	 * The format of the file names will be "baseFileName{index}.episode" where {index} represents the index of the
	 * episode in the list. States must be serializable.
	 * @param episodes the list of episodes to write to disk
	 * @param directoryPath the directory path in which the episodes will be written
	 * @param baseFileName the base file name to use for the episode files
	 */
	public static void writeEpisodes(List<Episode> episodes, String directoryPath, String baseFileName){


		if(!directoryPath.endsWith("/")){
			directoryPath += "/";
		}

		for(int i = 0; i < episodes.size(); i++){
			Episode ea = episodes.get(i);
			ea.write(directoryPath + baseFileName + i);
		}

	}



	/**
	 * Writes this episode to a file. If the the directory for the specified file path do not exist, then they will be created.
	 * If the file extension is not ".episode" will automatically be added. States must be serializable.
	 * @param path the path to the file in which to write this episode.
	 */
	public void write(String path){

		if(!path.endsWith(".episode")){
			path = path + ".episode";
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
	 * Takes a path to a directory containing .episode files and reads them all into a {@link java.util.List}
	 * of {@link Episode} objects.
	 * @param directoryPath the path to the directory containing the episode files
	 * @return a {@link java.util.List} of {@link Episode} objects.
	 */
	public static List<Episode> readEpisodes(String directoryPath){

		if(!directoryPath.endsWith("/")){
			directoryPath = directoryPath + "/";
		}

		File dir = new File(directoryPath);
		final String ext = ".episode";

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if(name.endsWith(ext)){
					return true;
				}
				return false;
			}
		};
		String[] children = dir.list(filter);
		Arrays.sort(children, new AlphanumericSorting());

		List<Episode> eas = new ArrayList<Episode>(children.length);

		for(int i = 0; i < children.length; i++){
			String episodeFile = directoryPath + children[i];
			Episode ea = read(episodeFile);
			eas.add(ea);
		}

		return eas;
	}


	/**
	 * Reads an episode that was written to a file and turns into an EpisodeAnalysis object.
	 * @param path the path to the episode file.
	 * @return an EpisodeAnalysis object.
	 */
	public static Episode read(String path){

		//read whole file into string first
		String fcont = null;
		try{
			fcont = new Scanner(new File(path)).useDelimiter("\\Z").next();
		}catch(Exception E){
			System.out.println(E);
		}

		return parseEpisode(fcont);
	}

	

	public String serialize(){

		Yaml yaml = new Yaml();
		String yamlOut = yaml.dump(this);
		return yamlOut;
	}


	/**
	 * Returns a copy of this {@link Episode}.
	 * @return a copy of this {@link Episode}.
	 */
	public Episode copy(){
		Episode ep = new Episode();
		ep.stateSequence = new ArrayList<State>(this.stateSequence);
		ep.actionSequence = new ArrayList<Action>(this.actionSequence);
		ep.rewardSequence = new ArrayList<Double>(this.rewardSequence);
		return ep;
	}


	public static Episode parseEpisode(String episodeString){

		Yaml yaml = new Yaml();
		Episode ea = (Episode)yaml.load(episodeString);
		return ea;
	}







	public static void main(String[] args) {
		GridWorldDomain gwd = new GridWorldDomain(11, 11);
		SADomain domain = gwd.generateDomain();
		State s = new GridWorldState(new GridAgent(1, 3));

		Policy p = new RandomPolicy(domain);
		Episode ea = PolicyUtils.rollout(p, s, domain.getModel(), 30);

		String yamlOut = ea.serialize();

		System.out.println(yamlOut);

		System.out.println("\n\n");

		Episode read = Episode.parseEpisode(yamlOut);

		System.out.println(read.actionString());
		System.out.println(read.state(0).toString());
		System.out.println(read.actionSequence.size());
		System.out.println(read.stateSequence.size());

	}
	
}
