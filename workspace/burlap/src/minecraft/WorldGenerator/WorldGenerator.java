package minecraft.WorldGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import minecraft.MapIO;
import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import minecraft.WorldGenerator.Exceptions.FloorNotDeepEnoughException;
import minecraft.WorldGenerator.Exceptions.RandomMapGenerationException;
import minecraft.WorldGenerator.Exceptions.WorldIsTooSmallException;
import minecraft.WorldGenerator.Exceptions.WorldNotTallEnoughException;

public class WorldGenerator {

	//Class variables
	final int rows;
	final int cols;
	final int height;

	final static int maxNumberMapRestarts = 10000;

	int depthOfDirtFloor = 1;
	double probTrenchChangeDirection = .2;

	char[][][] charArray;
	HashMap<String, Integer> headerInfo;
	HashMap<Character, Integer> characterHierarchy;
	HashMap<Integer, List<Integer>> usedWalkLocations;
	
	Random rand;

	/**
	 * 
	 * @param rows
	 * @param cols
	 * @param height
	 */
	public WorldGenerator(int rows, int cols, int height) {
		this.rows = rows;
		this.cols = cols;
		this.height = height;
		this.rand = new Random();
		this.characterHierarchy = createCharacterHierarchy();
		this.usedWalkLocations = new HashMap<Integer, List<Integer>>();
	}

	/**
	 * 
	 * @param rows
	 * @param cols
	 * @param height
	 * @param depthOfDirtFloor
	 * @param probTrenchChangeDir
	 */
	public WorldGenerator(int rows, int cols, int height, int depthOfDirtFloor, double probTrenchChangeDir) {
		this.rows = rows;
		this.cols = cols;
		this.height = height;
		this.rand = new Random();
		this.depthOfDirtFloor = depthOfDirtFloor;
		this.probTrenchChangeDirection = probTrenchChangeDir;
		this.characterHierarchy = createCharacterHierarchy();
		this.usedWalkLocations = new HashMap<Integer, List<Integer>>();
	}

	private HashMap<Character, Integer> createCharacterHierarchy() {
		HashMap<Character, Integer> hierarchy = new HashMap<Character, Integer>();



		//Don't matter
		
				
		//Less importants
		hierarchy.put(NameSpace.CHARDIRTBLOCKNOTPICKUPABLE, 5);
		hierarchy.put(NameSpace.CHARDIRTBLOCKPICKUPABLE, 5);
		hierarchy.put(NameSpace.CHARINDBLOCK, 5);
		hierarchy.put(NameSpace.CHAREMPTY, 0);

		//Must haves
		hierarchy.put(NameSpace.CHARAGENT, 10);
		hierarchy.put(NameSpace.CHARAGENTFEET, 10);
		hierarchy.put(NameSpace.CHARFURNACE, 10);
		hierarchy.put(NameSpace.CHARGOAL, 10);
		hierarchy.put(NameSpace.CHARGOLDBLOCK, 10);
		hierarchy.put(NameSpace.CHARLAVA, 10);
		
		return hierarchy;
	}

	private boolean addChar(char [][][] toAddTo, char newChar, int  toAddX, int toAddY, int toAddZ, boolean canReplaceEquals, Integer hierVal) {
		int oldHierVal = this.characterHierarchy.get(newChar);
		if (hierVal != null) this.characterHierarchy.put(newChar, hierVal);
		
		boolean toReturn = false;
		char oldChar = toAddTo[toAddY][toAddX][toAddZ];
		Integer oldCharVal = this.characterHierarchy.get(oldChar);
		if (oldCharVal == null) oldCharVal = -1;

		Integer newCharVal = this.characterHierarchy.get(newChar);
		if (newCharVal == null) newCharVal = -1;


		if (canReplaceEquals) newCharVal++;
		
		if (newCharVal > oldCharVal) {
			toAddTo[toAddY][toAddX][toAddZ] = newChar;
			toReturn = true;
		}
		
		this.characterHierarchy.put(newChar, oldHierVal);
		return toReturn;  
	}

	protected void emptifyCharArray(char [][][] toChange) {
		for(int row = 0; row < this.rows; row++) {
			for(int col = 0; col < this.cols; col++) {
				for(int currHeight = 0; currHeight < this.height; currHeight++) {
					toChange[row][col][currHeight] = NameSpace.CHAREMPTY;
				}
			}
		}
	}

	protected void addFloor(char [][][] toChange, char floorOf) {
		assert(this.height >= depthOfDirtFloor);
		for (int currHeight = 0; currHeight < depthOfDirtFloor; currHeight++) {
			for(int row = 0; row < this.rows; row++) {
				for(int col = 0; col < this.cols; col++) {
					addChar(toChange, floorOf,  col, row, currHeight, true, null);
				}
			}
		}
	}

	private int[] addCharRandomly(char toAdd, Integer x, Integer y, Integer z, char[][][] toChange, boolean canReplaceEqualsInHier, Integer hierarchyVal) {
		
		// Randomly add the given char toAdd to the map so that it does not conflict with already placed characters
		int[] toReturn = addCharRandomlyHelper(toAdd,x,y,z,toChange,0,canReplaceEqualsInHier, hierarchyVal);
		
		
		return toReturn;
		
	}

	private int[] addCharRandomlyHelper(char toAdd, Integer x, Integer y, Integer z, char[][][] toChange, int counter, boolean canReplaceEqualsInHier, Integer hierVal) {
		// If we tried placing a reasonable number of times and failed, exit.
		try {
			if(counter > this.rows*this.cols*this.height) {
				throw new WorldIsTooSmallException();
			}
		}
		catch (WorldIsTooSmallException e) {
			e.printStackTrace();
			System.exit(0);
		}

		//Randomize any unspecifed coordinate
		Integer nx = x;
		Integer ny = y;
		Integer nz = z;

		if (nx == null) {
			nx = this.rand.nextInt(this.cols);
		} 

		if (ny == null) {
			ny = this.rand.nextInt(this.rows);
		}

		if (nz == null) {
			nz = this.rand.nextInt(this.height);
		}

		boolean charWasAdded = addChar(toChange, toAdd,  nx, ny, nz, canReplaceEqualsInHier, hierVal);

		if(!charWasAdded) {
			return addCharRandomlyHelper(toAdd, x, y, z, toChange, ++counter, canReplaceEqualsInHier, hierVal);
		}
		else {
			return new int[]{nx,ny,nz};
		}
	}

	protected int[] addRandomSpatialGoal(char[][][] toChange, int heightOfGoalShelf, char floorOf) {
		//Add shelf tower

		
		assert(this.depthOfDirtFloor+1 < this.height);


		// Put the goal {1,2} higher in z for certain worlds
		int goalZOffset = 1;
		if(heightOfGoalShelf > 0) {
			goalZOffset = 2;
		}
		int[] goalPosition = addCharRandomly(NameSpace.CHARGOAL, null, null, this.depthOfDirtFloor + goalZOffset + heightOfGoalShelf, toChange, false, null);
		if (heightOfGoalShelf > 0) {
			for (int currZ = this.depthOfDirtFloor; currZ < this.depthOfDirtFloor + heightOfGoalShelf + 1; currZ++) {
				addChar(toChange, floorOf,  goalPosition[0], goalPosition[1], currZ, true, 10);
			}
		}
		return goalPosition;
	}


	private boolean addCharColAt(int x, int y, char[][][] toChange, char toAdd, boolean canReplaceEqualsInHier, int hierVal) {
		boolean willNeedToRestart = false;
		for (int currHeight = 0; currHeight < this.height; currHeight++) {
			//System.out.println("Adding: " + toAdd + " and previously: " + toChange[y][x][currHeight]);
			boolean wasAdded = addChar(toChange, toAdd, x, y, currHeight, canReplaceEqualsInHier, hierVal);
			willNeedToRestart = willNeedToRestart || !wasAdded;
			//System.out.println("\twas added:" + wasAdded);
		}
		List<Integer> oldList = this.usedWalkLocations.get(x);
		if (oldList == null) oldList = new ArrayList<Integer>();
		if (oldList.contains(y)) return true;
		oldList.add(y);
		this.usedWalkLocations.put(x, oldList);
		
		return willNeedToRestart;
	}

	private boolean fairCoinFlip() {
		return this.rand.nextFloat() < .5;
	}

	private boolean allCharactersAtCol(char charToCheck, int x, int y, char[][][] toCheck) {
		boolean toReturn = true;
		for (int currHeight = 0; currHeight < this.height; currHeight++) {
			toReturn = toReturn && (toCheck[y][x][currHeight] == charToCheck);
		}
		return toReturn;
	}

	//ONLY USED FOR STRAIGHT TRENCHES
	private boolean walkInBetweenAgentAndGoal (int agentX, int agentY, int goalX, int goalY, HashMap<Integer, List<Integer>> walkLocations) {	  
		int xVel = (-agentX+goalX);
		if (xVel != 0) xVel /= Math.abs((agentX-goalX));
		int yVel = (-agentY+goalY);
		if (yVel != 0) yVel /= Math.abs((agentY-goalY));



		int currX = agentX;
		int currY = agentY;

		//Walk along X
		while(currX != goalX) {
			
			List<Integer> yList = walkLocations.get(currX);
			if (yList != null && yList.contains(agentY)) {
				return true;
			}
			currX += xVel;
		}
		
		
		//Walk along Y
		while(currY != goalY) {
			
			List<Integer> yList = walkLocations.get(currX);
			if (yList != null && yList.contains(currY)) {
				return true;
			}
			currY += yVel;
				
		}
		
		


		return false;
	}

	/**
	 * 
	 * @param toChange
	 * @param startX
	 * @param startY
	 * @param startXChange
	 * @param startYChange
	 * @param charToAdd
	 * @param straightWalk
	 * @param walkPositions
	 * @return if need to restart
	 */
	private boolean runCharColWalk(char[][][] toChange, int startX, int startY, int startXChange,
			int startYChange, char charToAdd, boolean straightWalk, HashMap<Integer, List<Integer>> walkPositions,
			boolean canReplaceEqualsInHier, int hierVal) {
		int currX = startX;
		int currY = startY;
		int xChange = startXChange;
		int yChange = startYChange;

		double probabilityToUse = this.probTrenchChangeDirection;
		if (straightWalk) probabilityToUse = 0;

		//Random walk until off map or doubled back on self or other walk (a la snake)
		while (Helpers.withinMapAt(currX, currY, 0, this.cols, this.rows, this.height) && !allCharactersAtCol(charToAdd, currX, currY, toChange)) {
			List<Integer> oldYList = walkPositions.get(currX);
			if (oldYList == null) oldYList = new ArrayList<Integer>();
			oldYList.add(currY);
			walkPositions.put(currX, oldYList);
			//Add column
			this.addCharColAt(currX, currY, toChange, charToAdd, canReplaceEqualsInHier, hierVal);

			//Change direction with some probability
			if (rand.nextFloat() < probabilityToUse) {
				if (this.fairCoinFlip()) {
					//Horizontal change
					yChange = 0;
					if (this.fairCoinFlip()) {
						xChange = 1;

					}
					else {
						xChange = -1;
					}
				}
				else {
					//Vertical change
					xChange = 0;
					if (this.fairCoinFlip()) {
						yChange = 1;
					}
					else {
						yChange = -1;
					}
				}
			}
			currX += xChange;

			currY += yChange;
		}
		return false;
	}

	private boolean randomWalkInsertOfCharacterColumns(char[][][] toChange, char toInsert,
			boolean straightAndBetweenAgentAndGoal, int agentX, int agentY, int goalX,
			int goalY, boolean canReplaceEqualsInHier, Integer hierVal) {

		boolean startingBotOrTop = this.fairCoinFlip();

		int startX = 0;
		int startY = 0;
		int startXChange = 0;
		int startYChange = 0;

		if (startingBotOrTop) {
			startX = rand.nextInt(this.cols);
			// Starting at top
			if (this.fairCoinFlip()) {
				startYChange = 1;
				startY = 0;
			}
			//Starting at bottom
			else {
				startYChange = -1;
				startY = this.rows-1;
			}
		}
		else {
			startY = rand.nextInt(this.rows);
			// Starting at left
			if (this.fairCoinFlip()) {
				startXChange = 1;
				startX = 0;
			}
			//Starting at right
			else {
				startXChange = -1;
				startX = this.cols-1;
			}
		}


		//Do the walk

		HashMap<Integer, List<Integer>> walkPositions = new HashMap<Integer, List<Integer>>();
		boolean needToRestart = runCharColWalk(toChange, startX, startY, startXChange, startYChange, toInsert, straightAndBetweenAgentAndGoal,
				walkPositions, canReplaceEqualsInHier, hierVal);

		//Break if straight and no trench between
		if(!(straightAndBetweenAgentAndGoal && walkInBetweenAgentAndGoal(agentX, agentY, goalX, goalY, walkPositions))) {
			return true;
		}

		return needToRestart;
	}

	/**
	 * 
	 * @param numTrenches
	 * @param toChange
	 * @param trenchStraightAndBetweenAgentAndGoal
	 * @param agentX
	 * @param agentY
	 * @param goalX
	 * @param goalY
	 * @param canReplaceEqualsInHier
	 * @return
	 */
	protected boolean addTrenches(int numTrenches, char[][][] toChange, boolean trenchStraightAndBetweenAgentAndGoal, int agentX, int agentY, int goalX, int goalY, boolean canReplaceEqualsInHier) {
		boolean toReturn = false;	
		for (int trenchIndex = 0; trenchIndex < numTrenches; trenchIndex++) {
			toReturn = toReturn || this.randomWalkInsertOfCharacterColumns(toChange, NameSpace.CHAREMPTY, trenchStraightAndBetweenAgentAndGoal, agentX, agentY, goalX, goalY, canReplaceEqualsInHier, 10);
		}
		return toReturn;
	}

	protected int[] addGoldOre(char[][][] toChange, Integer depthOfGoldOre) throws FloorNotDeepEnoughException {
		if (this.depthOfDirtFloor + depthOfGoldOre < 0) throw new FloorNotDeepEnoughException();
		
		return this.addCharRandomly(NameSpace.CHARGOLDBLOCK, null, null, this.depthOfDirtFloor + depthOfGoldOre, toChange, false, null);
	}

	protected int [] addFurnace(char[][][] toChange) {
		return this.addCharRandomly(NameSpace.CHARFURNACE, null, null, this.depthOfDirtFloor, toChange, false, null);
	}

	protected boolean addWalls(int numWalls, char[][][] toChange, char wallOf, boolean wallStraightAndBetweenAgentAndGoal, int agentX, int agentY, int goalX, int goalY, boolean canReplaceEqualsInHier) {
		boolean needToRestart = false;
		for (int wallIndex = 0; wallIndex < numWalls; wallIndex++) {
			needToRestart = needToRestart || this.randomWalkInsertOfCharacterColumns(toChange, wallOf, wallStraightAndBetweenAgentAndGoal, agentX, agentY, goalX, goalY, canReplaceEqualsInHier, 5);
		}
		return needToRestart;
	}

	protected int [] addAgent(char [][][] toChange) throws WorldNotTallEnoughException {
		if (this.depthOfDirtFloor + 2 > this.height) throw new WorldNotTallEnoughException();
		
		// Add agent's head
		int[] headLocation = addCharRandomly(NameSpace.CHARAGENT, null, null, this.depthOfDirtFloor+1, toChange, false, 5);

		// Add agent's feet
		toChange[headLocation[1]][headLocation[0]][headLocation[2]-1] = NameSpace.CHARAGENTFEET;

		return headLocation;

	}

	private char[][][] generateNewCharArray(int goal, char floorOf, int numTrenches, boolean trenchStraightAndBetweenAgentAndGoal, int numWalls, char wallOf, boolean wallStraightAndBetweenAgentAndGoal, Integer depthOfGoldOre, int heightOfGoalShelf, int numLava) throws RandomMapGenerationException {
		//System.out.println("RESTARTING");
		char[][][] toReturn = new char[this.rows][this.cols][this.height];

		for (int numberOfRestarts = 0; numberOfRestarts <= maxNumberMapRestarts; numberOfRestarts++) {
			this.usedWalkLocations = new HashMap<Integer, List<Integer>>();
			if (numberOfRestarts == maxNumberMapRestarts) throw new WorldIsTooSmallException();
			//Initialize empty
			this.emptifyCharArray(toReturn);

			//Add dirt floor
			this.addFloor(toReturn, floorOf);


			List<int[]> impPositions = new ArrayList<int[]>();
			
			int[] agentPosition = this.addAgent(toReturn);
			int[] goalPosition = new int[2];
			
			//Add lava
			for (int i = 0; i < numLava; i++) {
				impPositions.add(addCharRandomly(NameSpace.CHARLAVA, null, null, this.depthOfDirtFloor-1, toReturn, false, null));
			}
			
			
			//Add goal
			if (goal == NameSpace.INTXYZGOAL) {
				goalPosition = addRandomSpatialGoal(toReturn, heightOfGoalShelf, floorOf);
				impPositions.add(goalPosition);
			}
			
			//Add agent
			impPositions.add(agentPosition);
			
			//Add gold ore
			if (goal == NameSpace.INTGOLDBARGOAL || goal == NameSpace.INTGOLDOREGOAL) {
				goalPosition =  addGoldOre(toReturn, depthOfGoldOre);
				impPositions.add(goalPosition);
			}

			//Add furnace
			if (goal == NameSpace.INTGOLDBARGOAL) {
				impPositions.add(addFurnace(toReturn));
			}
			

			//Add trench
			if (this.addTrenches(numTrenches, toReturn, trenchStraightAndBetweenAgentAndGoal, agentPosition[0], agentPosition[1], goalPosition[0], goalPosition[1], true)) {
				continue;
			}
			//Add walls
			if (this.addWalls(numWalls, toReturn, wallOf, wallStraightAndBetweenAgentAndGoal, agentPosition[0], agentPosition[1], goalPosition[0], goalPosition[1], true)) {
				continue;
				
			}
			


			//Restart if agent or goal in walk location
			
			if (shouldRestart(impPositions, toReturn)) {
				continue;
			}
			
			//Restart if no agent

			break;
		}
		return toReturn;
	}
	
	boolean shouldRestart(List<int []> impPositions, char[][][] charArray) {
		if (aLocationWasWalked(impPositions)) return true;
		if (noAgent(charArray)) return true;
		
		return false;
	}
	
	boolean noAgent(char[][][] charArray) {
		for (char[][] row: charArray) {
			for (char[] col: row) {
				for (char currChar : col) {
					if (currChar == NameSpace.CHARAGENT) return false;
				}
			}
		}
		return true;
	}
	
	boolean aLocationWasWalked(List<int[]> positions) {
		boolean toReturn = false;
		for(int i = 0; i < positions.size(); i++) {
			int currX = positions.get(i)[0];
			int currY = positions.get(i)[1];
			toReturn = toReturn || locationWasWalked(currX, currY);
		}
		
		return toReturn;
	}
	
	boolean locationWasWalked(int x, int y) {
		List<Integer> list = this.usedWalkLocations.get(x);
		if (list != null && list.contains(y)) {
			return true;
		}
		return false;
	}

	private HashMap<String, Integer> generateHeaderInfo(int goal, int numTrenches, int numPlaceBlocks) {
		HashMap<String, Integer> toReturn = new HashMap<String, Integer>();

		//Goal
		toReturn.put(Character.toString(NameSpace.CHARGOALDESCRIPTOR), goal);

		//Starting ore
		toReturn.put(Character.toString(NameSpace.CHARSTARTINGGOLDORE), 0);

		//Starting gold bars
		toReturn.put(Character.toString(NameSpace.CHARSTARTINGGOLDBAR), 0);

		//Placeable blocks
		toReturn.put(Character.toString(NameSpace.CHARPLACEABLEBLOCKS), numPlaceBlocks);
		
		return toReturn;
	}

	/**
	 * 
	 * @param goal
	 * @param floorOf
	 * @param numTrenches
	 * @param trenchStraightAndBetweenAgentAndGoal
	 * @param numWalls
	 * @param wallsDropItem
	 * @param wallsStraightAndBetweenAgentAndGoal
	 * @throws WorldIsTooSmallException 
	 */
	public void randomizeMap(int goal, char floorOf, int numTrenches, boolean trenchStraightAndBetweenAgentAndGoal,
			int numWalls, char wallOf, boolean wallsStraightAndBetweenAgentAndGoal, Integer depthOfGoldOre, 
			int floorDepth, int numPlaceBlocks, int heightOfGoalShelf, int numLava) throws RandomMapGenerationException {
		
		this.depthOfDirtFloor = floorDepth;
		this.charArray = generateNewCharArray(goal, floorOf, numTrenches, trenchStraightAndBetweenAgentAndGoal,
				numWalls, wallOf, wallsStraightAndBetweenAgentAndGoal, depthOfGoldOre, heightOfGoalShelf, numLava);
		this.headerInfo = generateHeaderInfo(goal, numTrenches, numPlaceBlocks);
	}

	public char[][][] getCurrCharArray() {
		return this.charArray;
	}

	public void setCharArray(char[][][] newCharArray) {
		this.charArray = newCharArray;
	}


	public String getCurrMapIOAsString() {
		return getCurrMapIO().toString();
	}

	public MapIO getCurrMapIO() {
		return new MapIO(this.headerInfo, this.charArray);

	}

	public static void main(String[] args) {
		String fileName = "src/minecraft/maps/testingWorld.map";
		WorldGenerator generator = new WorldGenerator(5, 5, 4);
		
		//Map parameters
		int goal = NameSpace.INTTOWERGOAL;
		char floorOf = NameSpace.CHARDIRTBLOCKNOTPICKUPABLE;
		int numTrenches = 0;
		boolean trenchStraightAndBetweenAgentAndGoal = true;
		int numWalls = 0;
		char wallOf = NameSpace.CHARDIRTBLOCKNOTPICKUPABLE;
		boolean wallsStraightAndBetweenAgentAndGoal = true;
		Integer depthOfGoldOre = 0;
		int floorDepth = 1;
		
//		try {
////			generator.randomizeMap(goal, floorOf, numTrenches, trenchStraightAndBetweenAgentAndGoal, numWalls, wallOf, wallsStraightAndBetweenAgentAndGoal, depthOfGoldOre, floorDepth, 0);
//		} catch (RandomMapGenerationException e) {
//			e.printStackTrace();
//		}
		String map = generator.getCurrMapIOAsString();
		System.out.println(map);
	}
}
