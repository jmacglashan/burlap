package minecraft;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tests.ResourceLoader;
import logicalexpressions.LogicalExpression;
import minecraft.MinecraftBehavior.MinecraftBehavior;
import affordances.WorldPerceptron.PerceptronHelpers;
import affordances.WorldPerceptron.PerceptualData;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyDeterministicQPolicy;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * Used to turn .map minecraft ascii files into easily usable data structures and vice versa
 * @author Dhershkowitz
 *
 */
public class MapIO {
	//-----CLASS VARIABLES-----
	/**
	 * Stores the first line of the map ascii file as a hashing from keys to values
	 */
	private HashMap<String, Integer> headerMap;
	
	/**
	 * Stores the ascii map (header excluded) as a 3D map of chars
	 */
	private char[][][] mapAsCharArray;
	private int rows;
	private int cols;
	private int height;
	
	private GreedyDeterministicQPolicy policy;
	private Boolean planUsedForLastAllStates;
	private List<State> allStates;
	private HashMap<State, AbstractGroundedAction> stateToAction;
	private LogicalExpression mapGoal;
	
	
	//-----CLASS METHODS-----
	public MapIO(String filePath) {
		//Open file
	
		// NOTE: old stuff
		BufferedReader reader = null;
		try {
		reader = new BufferedReader(new FileReader(filePath));
		} catch (FileNotFoundException e1) {
			System.out.println("Couldn't open map file: " + filePath);
		}
		
//		ResourceLoader resLoader = new ResourceLoader();
//		BufferedReader reader = resLoader.getBufferedReader(filePath);
		
		StringBuilder sb = new StringBuilder();
		
		//Build header string and map string
		String line = "";
		String stateInfoAsString = "";
		try {
			stateInfoAsString = reader.readLine();//String to store things like number of placeable blocks
			while ((line = reader.readLine()) != null) {
			    sb.append(line + "\n");
			}
			reader.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String mapAsString = sb.toString();
				
		this.headerMap = processHeader(stateInfoAsString);
		this.mapAsCharArray = processMapString(mapAsString);
		this.rows = this.mapAsCharArray.length;
		this.cols = this.mapAsCharArray[0].length;
		this.height = this.mapAsCharArray[0][0].length;
	}
	
	public MapIO(BufferedReader reader) {
		
		StringBuilder sb = new StringBuilder();
		
		//Build header string and map string
		String line = "";
		String stateInfoAsString = "";
		try {
			stateInfoAsString = reader.readLine();//String to store things like number of placeable blocks
			while ((line = reader.readLine()) != null) {
			    sb.append(line + "\n");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String mapAsString = sb.toString();
				
		this.headerMap = processHeader(stateInfoAsString);
		this.mapAsCharArray = processMapString(mapAsString);
		this.rows = this.mapAsCharArray.length;
		this.cols = this.mapAsCharArray[0].length;
		this.height = this.mapAsCharArray[0][0].length;
	}
	
	public MapIO(HashMap<String, Integer> headerInfo, char[][][] mapAsCharArray) {
		this.headerMap = headerInfo;
		this.mapAsCharArray = mapAsCharArray;
		this.rows = mapAsCharArray.length;
		this.cols = mapAsCharArray[0].length;
		this.height = mapAsCharArray[0][0].length;
	}
	
	/**
	 * @return a copy of 3D char array of the map of the ascii file
	 */
	public char[][][] getMapAs3DCharArray() {
		return this.mapAsCharArray.clone();
	}
	/**
	 * @return a hashmap mapping string keys to values in the first line of the ascii file
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Integer> getHeaderHashMap() {
		return (HashMap<String, Integer>) this.headerMap.clone();
	}
	
	/**
	 * Used to process the first line of a map file formatted "param1=value1,param2=value2..."
	 * @param stateInfo the first line of a map file with CSV values
	 * @returns hashmap mapping string of parameter to its int value
	 */
	public static HashMap<String, Integer> processHeader(String stateInfo) {
		HashMap<String, Integer> toReturn = new HashMap<String, Integer>();
		String [] splitOnCommas = stateInfo.split(",");
		
		for (int i = 0; i < splitOnCommas.length; i++) {
			String[] currKVPairAsStringArray = splitOnCommas[i].split("=");
			assert(currKVPairAsStringArray.length == 2);
			String key = currKVPairAsStringArray[0];
			Integer value = Integer.parseInt(currKVPairAsStringArray[1]);
			toReturn.put(key, value);
		}
		
		int goalType = toReturn.get("G");

		return toReturn;
	}
	
	/**
	 * @param mapString the ascii map stored as a string (header excluded)
	 * @return the input map string as a 3D char array
	 */
	public static char[][][] processMapString(String mapString) {
		String[] splitByHorPlanes = mapString.split(NameSpace.planeSeparator);
		int height = splitByHorPlanes.length;
		int rows = splitByHorPlanes[0].split(NameSpace.rowSeparator).length;
		int cols = splitByHorPlanes[0].split(NameSpace.rowSeparator)[0].length();
				
		char[][][] arrayToReturn = new char[rows][cols][height];
		
		for(int currHeight = height-1; currHeight >= 0; currHeight--) {
			String currPlane = splitByHorPlanes[currHeight];
			String[] planeIntoRows = currPlane.split(NameSpace.rowSeparator);
			for(int row = 0; row < rows; row++) {
				String currRow = planeIntoRows[row];
				for(int col = 0; col < cols; col++) {
					char currCharacter = currRow.charAt(col);
					arrayToReturn[row][col][height-currHeight-1] = currCharacter;
				}
			}
		}
		return arrayToReturn;
	}
	
	public String getCharArrayAsString() {
		StringBuilder sb = new StringBuilder();
		int rows = this.mapAsCharArray.length;
		int cols = this.mapAsCharArray[0].length;
		int height = this.mapAsCharArray[0][0].length;
		
		for (int currHeight = height-1; currHeight >= 0; currHeight--) {
			for(int row = 0; row < rows; row++) {
				for(int col = 0; col < cols; col++) {
					char currChar = this.mapAsCharArray[row][col][currHeight];
					sb.append(currChar);
				}
				if (!(row == rows-1)) {
					sb.append(NameSpace.rowSeparator);
				}
				else {
					if (!(currHeight == 0))
						sb.append(NameSpace.planeSeparator);
				}
			}
		}
		return sb.toString();
	}
	
	public String getHeaderAsString() {
		StringBuilder sb = new StringBuilder();
		for (String key : this.headerMap.keySet()) {
			Integer value = this.headerMap.get(key);
			sb.append(key + "=" + value + ",");
		}
		sb.deleteCharAt(sb.length()-1);
		
		sb.append("\n");
		
		return sb.toString();
	}
	
	/**
	 * Prints the header + charArray as a string to the filePath
	 * @param filePath
	 */
	public void printHeaderAndMapToFile(String filePath) {
		String toPrint = this.toString();
		PrintWriter outPrinter = null;
		try { 
			File f = new File(filePath);
			
			// Creates any intermediary directories that are missing
			f.getParentFile().mkdirs();
			
			outPrinter = new PrintWriter(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		outPrinter.print(toPrint);
		outPrinter.close();
	}
	
	@Override
	public String toString() {
		return getHeaderAsString() + getCharArrayAsString();
	}
	
	private List<State> determineAllStatesHelper(GreedyDeterministicQPolicy p, MinecraftBehavior mcBeh, ValueFunctionPlanner planner, boolean usePlan) {
		EpisodeAnalysis ea = p.evaluateBehavior(mcBeh.getInitialState(), mcBeh.getRewardFunction(), mcBeh.getTerminalFunction());

		List<State> allStates;
		//Use full state space
		if (!usePlan) {
			allStates = ((ValueFunctionPlanner) planner).getAllStates();
		}
		
		else {
			//Use plan
			allStates = ea.stateSequence;
			//Prune the terminal state
			allStates.remove(allStates.size()-1);
		}
		this.stateToAction = new HashMap<State, AbstractGroundedAction>();
		for (State state: allStates) {
			AbstractGroundedAction currAction = p.getAction(state);
			this.stateToAction.put(state, currAction);
		}
		
		return allStates;
	}
	
	/**
	 * 
	 * @param usePlan
	 * @return a list of all the states that could result from the mapIO
	 */
	private void determineAllStates(boolean usePlan) {
		//Suppress prints
		ByteArrayOutputStream theVoid = new ByteArrayOutputStream();
		System.setOut(new PrintStream(theVoid));
		//Get IO, behavior, planner and policy
		MinecraftBehavior mcBeh = new MinecraftBehavior(this);
		ValueFunctionPlanner planner = new ValueIteration(mcBeh.getDomain(), mcBeh.getRewardFunction(), mcBeh.getTerminalFunction(), mcBeh.getGamma(), mcBeh.getHashFactory(), mcBeh.getMinDelta(), Integer.MAX_VALUE);
		GreedyDeterministicQPolicy p = (GreedyDeterministicQPolicy)mcBeh.solve(planner);
		//Reset system out
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		//Get all states
		this.allStates = determineAllStatesHelper(p, mcBeh, planner, usePlan);
		this.planUsedForLastAllStates = usePlan;
	}
	
	private void determinePolicy() {
		MinecraftBehavior mcBeh = new MinecraftBehavior(this);
		ValueFunctionPlanner planner = new ValueIteration(mcBeh.getDomain(), mcBeh.getRewardFunction(), mcBeh.getTerminalFunction(), mcBeh.getGamma(), mcBeh.getHashFactory(), mcBeh.getMinDelta(), Integer.MAX_VALUE);
		this.policy = (GreedyDeterministicQPolicy)mcBeh.solve(planner);
	}
	
	private GreedyDeterministicQPolicy getPolicy() {
		if (this.policy == null) {
			determinePolicy();
		}
		
		return this.policy;
	}
	
	/**
	 * 
	 * @param usePlan
	 * @return a list of all states
	 */
	public List<State> getAllStates(boolean usePlan) {
		if (this.planUsedForLastAllStates == null || usePlan != this.planUsedForLastAllStates) {
			determineAllStates(usePlan);
		}

		return this.allStates;
	}
	
	
	/**
	 * 
	 * @param usePlan
	 * @return mapping from action to number of times it was ideal in VI
	 */
	public HashMap<GroundedAction, Integer> getActionCountsForAllStates(boolean usePlan) {
		MinecraftBehavior mcBeh = new MinecraftBehavior(this);
		ValueFunctionPlanner planner = new ValueIteration(mcBeh.getDomain(), mcBeh.getRewardFunction(), mcBeh.getTerminalFunction(), mcBeh.getGamma(), mcBeh.getHashFactory(), mcBeh.getMinDelta(), Integer.MAX_VALUE);
		GreedyDeterministicQPolicy p = (GreedyDeterministicQPolicy)mcBeh.solve(planner);
		
		//Reset system out
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		
		//Get all states
		List<State> allStates = this.getAllStates(usePlan);
		
		HashMap<GroundedAction, Integer> countsHashMapForMap = new HashMap<GroundedAction, Integer>();
		
		//Count action in each state
		for(State currState: allStates) {
			GroundedAction currGroundedAction = (GroundedAction) p.getAction(currState);
			Integer oldCount = countsHashMapForMap.get(currGroundedAction);
			if (oldCount == null) {
				oldCount = 0;
			}
			oldCount += 1;
			
			countsHashMapForMap.put(currGroundedAction, oldCount);	
		}
		return countsHashMapForMap;
	}
	
	/**
	 * 
	 * @param xLookDistance
	 * @param yLookDistance
	 * @param zLookDistance
	 * @param usePlan
	 * @param sampleDownBy used to sample down perception data. Use 1 for highest resolution, 2 for pairwise blocks etc.
	 * @return a list of int arrays -- the perception data
	 */
	public List<PerceptualData> getAllPercDataForMap(String percTag, int xLookDistance, int yLookDistance, int zLookDistance, boolean usePlan) {
		List<PerceptualData> toReturn = new ArrayList<PerceptualData>();
		//Get all states
		List<State> allStates = this.getAllStates(usePlan);
		//Get perceptions in each state from all states
		for (State currState : allStates) {
			char[][][] stateAsCharArray = MinecraftStateParser.stateToCharArray(currState, this.rows, this.cols, this.height);
			ObjectInstance agent = currState.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
			int rotDir = agent.getDiscValForAttribute(NameSpace.ATROTDIR);
			int vertRotDir = agent.getDiscValForAttribute(NameSpace.ATVERTDIR);
			int [] percDataForState = PerceptronHelpers.agentPerceptionToFactoredVector(stateAsCharArray, xLookDistance, yLookDistance, zLookDistance, rotDir, vertRotDir);			
			toReturn.add(new PerceptualData(percDataForState, percTag + this.stateToAction.get(currState).toString()));
		}
		
		return toReturn;
	}
	
	//ACCESSORS
	public int getRows() {
		return this.rows;
	}
	public int getCols() {
		return this.cols;
	}
	public int getHeight() {
		return this.height;
	}
	
	public static void main(String[] args) {
		String filePath = "minecraft/maps/";
		MapIO myIO = new MapIO(filePath + "bigPlane.map");
		System.out.println(myIO.toString());
	}
}