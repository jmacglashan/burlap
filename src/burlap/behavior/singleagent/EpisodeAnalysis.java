package burlap.behavior.singleagent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.NullAction;



/**
 * This class is used to keep track of all events that occur in an episode.
 * @author James MacGlashan
 *
 */
public class EpisodeAnalysis {

	public List<State>									stateSequence;
	public List<GroundedAction>							actionSequence;
	public List<Double>									rewardSequence;
	
	
	/**
	 * Creates a new EpisodeAnalysis object. Before recording transitions, the {@link initializeEpisideWithInitialState(State)} method
	 * should be called to set the initial state of the episode.
	 */
	public EpisodeAnalysis(){
		this.initializeDatastructures();
	}
	
	
	/**
	 * Initializes a new EpisodeAnalysis object with the initial state in which the episode started.
	 * @param initialState the initial state of the episode
	 */
	public EpisodeAnalysis(State initialState){
		this.initializeEpisideWithInitialState(initialState);
	}
	
	/**
	 * Initializes this object with the initial state in which the episode started.
	 * @param initialState the initial state of the episode
	 */
	public void initializeEpisideWithInitialState(State initialState){
		this.initializeDatastructures();
		this.stateSequence.add(initialState);
	}
	
	protected void initializeDatastructures(){
		stateSequence = new ArrayList<State>();
		actionSequence = new ArrayList<GroundedAction>();
		rewardSequence = new ArrayList<Double>();
	}
	
	
	/**
	 * Adds a state to the state sequence. In general, it is recommended that {@link initializeEpisideWithInitialState(State)} method
	 * along with subsequent calls to the {@link recordTransitionTo(State, GroundedAction, double)} method is used instead, but this
	 * method can be used to manually add a state.
	 * @param s the state to add
	 */
	public void addState(State s){
		stateSequence.add(s);
	}
	
	/**
	 * Adds a GroundedAction to the action sequence. In general, it is recommended that {@link initializeEpisideWithInitialState(State)} method
	 * along with subsequent calls to the {@link recordTransitionTo(State, GroundedAction, double)} method is used instead, but this
	 * method can be used to manually add a GroundedAction.
	 * @param ga the GroundedAction to add
	 */
	public void addAction(GroundedAction ga){
		actionSequence.add(ga);
	}
	
	/**
	 * Adds a reward to the reward sequence. In general, it is recommended that {@link initializeEpisideWithInitialState(State)} method
	 * along with subsequent calls to the {@link recordTransitionTo(State, GroundedAction, double)} method is used instead, but this
	 * method can be used to manually add a reward.
	 * @param r the reward to add
	 */
	public void addReward(double r){
		rewardSequence.add(r);
	}
	
	
	/**
	 * Records an transition event where the agent applied the usingAction argument in the last
	 * state in this object's state sequence, received reward r, and transitioned to state next. 
	 * @param next the next state to which the agent transitioned
	 * @param usingAction the action the agent used that caused the transition
	 * @param r the reward the agent received for this transition.
	 */
	public void recordTransitionTo(State next, GroundedAction usingAction, double r){
		stateSequence.add(next);
		actionSequence.add(usingAction);
		rewardSequence.add(r);
	}
	
	
	/**
	 * Returns the ith state in this episode. i=0 refers to the initial state.
	 * @param i the index of the state in this episode
	 * @return the ith state in this episode
	 */
	public State getState(int i){
		return stateSequence.get(i);
	}
	
	/**
	 * Returns the ith action taken in this episode. i=0 refers to the action taken in the initial state.
	 * @param i the index of the action in this episode
	 * @return the ith action taken in this episode
	 */
	public GroundedAction getAction(int i){
		return actionSequence.get(i);
	}
	
	/**
	 * Returns the ith reward received in this episode. i=0 refers to the reward received 
	 * after taking the first action in the initial state.
	 * @param i
	 * @return
	 */
	public double getReward(int i){
		return rewardSequence.get(i);
	}
	
	/**
	 * Returns the number of time steps in this episode, which is equivalent to the number of states. Note that there
	 * will always be one less action and reward than there are time steps, since the agent will not act in the final state.
	 * @return the number of time steps in this episode
	 */
	public int numTimeSteps(){
		return stateSequence.size(); //state sequence will always have the most because of initial state and terminal state
	}
	
	
	/**
	 * Will return the discounted return received from the first state in the episode to the last state in the episode.
	 * @param discountFactor the discount factor to compute the discounted return; should be on [0, 1]
	 * @return the discounted return of the episode
	 */
	public double getDiscountedReturn(double discountFactor){
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
	public void appendAndMergeEpisodeAnalysis(EpisodeAnalysis e){
		for(int i = 0; i < e.numTimeSteps()-1; i++){
			this.recordTransitionTo(e.getState(i+1), e.getAction(i), e.getReward(i));
		}
	}
	
	
	/**
	 * Returns a string representing the actions taken in this episode. Actions are separated
	 * by ';' characters.
	 * @return a string representing the actions taken in this episode
	 */
	public String getActionSequenceString(){
		return this.getActionSequenceString("; ");
	}
	
	
	/**
	 * Returns a string representing the actions taken in this episode. Actions are separated
	 * by the provided delimiter string.
	 * @param delimiter the delimiter to separate actions in the string.
	 * @return a string representing the actions taken in this episode
	 */
	public String getActionSequenceString(String delimiter){
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for(GroundedAction ga : actionSequence){
			if(!first){
				buf.append(delimiter);
			}
			buf.append(ga.toString());
			first = false;
		}
		
		return buf.toString();
	}
	
	
	/**
	 * Writes this episode to a file. If the the directories for the specified file path do not exist, then they will be created.
	 * If the file extension is not ".episode" will automatically be added.
	 * @param path the path to the file in which to write this episode.
	 * @param sp the state parser to use to convert state objects to string representations.
	 */
	public void writeToFile(String path, StateParser sp){
		
		if(!path.endsWith(".episode")){
			path = path + ".episode";
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
	 * Converts this episode into a string representation.
	 * @param sp the state parser to use to convert state objects to string representations.
	 * @return a string representation of this episode.
	 */
	public String parseIntoString(StateParser sp){
		
		StringBuffer sbuf = new StringBuffer(256);
		
		for(int i = 0; i < stateSequence.size(); i++){
			
			sbuf.append("#EL#\n").append(sp.stateToString(stateSequence.get(i))).append("\n#ES#\n");
			if(i < stateSequence.size()-1){
				sbuf.append(getSpaceDelimGAString(actionSequence.get(i))).append("\n").append(rewardSequence.get(i)).append("\n");
			}
			
		}
		
		
		return sbuf.toString();
		
	}
	
	
	/**
	 * Reads an episode that was written to a file and turns into an EpisodeAnalysis object.
	 * @param path the path to the episode file.
	 * @param d the domain to which the states and actions belong
	 * @param sp a state parser that can parse the state string representation in the file
	 * @return an EpisodeAnalysis object.
	 */
	public static EpisodeAnalysis parseFileIntoEA(String path, Domain d, StateParser sp){
		
		//read whole file into string first
		String fcont = null;
		try{
			fcont = new Scanner(new File(path)).useDelimiter("\\Z").next();
		}catch(Exception E){
			System.out.println(E);
		}
		
		return parseStringIntoEA(fcont, d, sp);
	}
	
	
	/**
	 * Parses a string representation of an episode into an EpisodeAnalysis object.
	 * @param str a string represenation of the episode.
	 * @param d the domain to which the states and actions belong
	 * @param sp a state parser that can parse the state string representation in the file
	 * @return an EpisodeAnalysis object.
	 */
	public static EpisodeAnalysis parseStringIntoEA(String str, Domain d, StateParser sp){
		
		EpisodeAnalysis ea = new EpisodeAnalysis();
		
		String [] elComps = str.split("#EL#\n");
		
		//System.out.println("--" + elComps[0] + "--");
		
		for(int i = 1; i < elComps.length; i++){
			
			String spToken = "\n#ES#";
			if(!elComps[i].endsWith(spToken)){
				spToken += "\n";
			}
			
			String [] parts = elComps[i].split(spToken);
			
			State s = sp.stringToState(parts[0]);
			if(i < elComps.length-1){
				String [] ars = parts[1].split("\n");
				ea.recordTransitionTo(s, getGAFromSpaceDelimGASTring(d, ars[0]), Double.parseDouble(ars[1]));
			}
			else{
				ea.addState(s);
			}
		}
		
		
		return ea;
	}
	
	
	private static GroundedAction getGAFromSpaceDelimGASTring(Domain d, String str){
		
		String [] scomps = str.split(" ");
		Action a = d.getAction(scomps[0]);
		if(a == null){
			//the domain does not have a reference, so create a null action in its place
			a = new NullAction(scomps[0]);
		}
		String [] params = new String[scomps.length-1];
		for(int i = 1; i < scomps.length; i++){
			params[i-1] = scomps[i];
		}
		
		return new GroundedAction(a, params);
	}
	
	private static String getSpaceDelimGAString(GroundedAction ga){
		StringBuffer sbuf = new StringBuffer(30);
		sbuf.append(ga.action.getName());
		for(int i = 0; i < ga.params.length; i++){
			sbuf.append(" ").append(ga.params[i]);
		}
		
		return sbuf.toString();
	}
	
	
	
}
