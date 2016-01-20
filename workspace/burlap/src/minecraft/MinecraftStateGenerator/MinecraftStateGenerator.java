package minecraft.MinecraftStateGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import minecraft.NameSpace;
import minecraft.NameSpace.RotDirection;
import minecraft.NameSpace.VertDirection;
import minecraft.MinecraftStateGenerator.Exceptions.NoSpecifiedGoalException;
import minecraft.MinecraftStateGenerator.Exceptions.NotOneAgentException;
import minecraft.MinecraftStateGenerator.Exceptions.NotOneAgentFeetException;
import minecraft.MinecraftStateGenerator.Exceptions.StateCreationException;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

/**
 *Used to generate a minecraft state from a 3D char map and hashmap of stateInfo
 * @author dhershko
 *
 */
public class MinecraftStateGenerator {
	
	public static State createInitialState(char[][][] mapAsCharArray, HashMap<String, Integer> stateInfo, Domain domain) throws StateCreationException {
		State toReturn = new State();
		//Process map
		processMapCharArray(mapAsCharArray, domain, toReturn);
		
		//Process stateInfo hashmap
		processStateInfoHM(stateInfo, domain,  toReturn);
		
		//Assert relevant info
		assertInfo(toReturn, stateInfo);
		
		return toReturn;
	}
	
	private static void assertInfo (State state, HashMap<String, Integer> stateInfo) throws StateCreationException {
		//Assert 1 agent
		List<ObjectInstance> agents = state.getObjectsOfTrueClass(NameSpace.CLASSAGENTFEET);
		if (agents.size() != 1) {
			throw new NotOneAgentException();
		}
		//Assert 1 agent feet
		List<ObjectInstance> feets =state.getObjectsOfTrueClass(NameSpace.CLASSAGENTFEET);
		if (feets.size() != 1) {
			throw new NotOneAgentFeetException();
		}
		
		//Assert a specified goal
		if (!stateInfo.containsKey(Character.toString(NameSpace.CHARGOALDESCRIPTOR))) {
			throw new NoSpecifiedGoalException();
		}
		
		
	}
	
	
	/**
	 * Parses the header as a hashmap and adjusts the state as necessary
	 * @param stateInfo the header as a hashmap from string keys to int values.
	 *  Used for things like the number of blocks that the agent can carry.
	 * @param domain the relevant domain
	 * @param stateToAddTo
	 */
	private static void processStateInfoHM(HashMap<String, Integer> stateInfo, Domain domain, State stateToAddTo) {
		ObjectInstance agent = stateToAddTo.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		for (String key : stateInfo.keySet()) {
			Integer value = stateInfo.get(key);
			char keyAsChar = key.charAt(0);
			switch(keyAsChar) {
			case NameSpace.CHARPLACEABLEBLOCKS:
				agent.setValue(NameSpace.ATPLACEBLOCKS, value);
				break;
			case NameSpace.CHARSTARTINGGOLDORE:
				agent.setValue(NameSpace.ATAMTGOLDORE, value);
				break;
			case NameSpace.CHARSTARTINGGOLDBAR:
				agent.setValue(NameSpace.ATAMTGOLDBAR, value);
			default:
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param mapAsCharArray the minecraft ascii map (header excluded) as a 3D char array
	 * @param domain the domain that the state belonds to
	 * @param stateToAddTo the state to add to
	 */
	private static void processMapCharArray(char[][][] mapAsCharArray, Domain domain, State stateToAddTo) {
		int objectIndex = 0;//Stores index of object added -- for naming purposes
		int rows = mapAsCharArray.length;
		int cols = mapAsCharArray[0].length;
		int height = mapAsCharArray[0][0].length;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				for (int currHeight = 0; currHeight < height; currHeight++) {
					char currChar = mapAsCharArray[row][col][currHeight];
					if (processChar(currChar, row, col, currHeight, domain, stateToAddTo)) {
						objectIndex += 1;
					}
				}
			}		
		}
	}
	
	/**
	 * Adds necessary object to the state for a given character.
	 * @returns if the object was added to the input state
	 */
	private static boolean processChar(char inputChar, int row, int col, int height, Domain domain, State stateToAddto) {
		ObjectInstance toAdd = null;
		Random rand = new Random();
		int objectIndex = rand.nextInt(999999);
		//Determine what ObjectInstance needs to be added
		switch (inputChar) {
		case NameSpace.CHARDIRTBLOCKPICKUPABLE:
			toAdd = createPickupableDirtBlock(domain, col, row, height, objectIndex);
			break;
		case NameSpace.CHARDIRTBLOCKNOTPICKUPABLE:
			toAdd = createNotPickupableDirtBlock(domain, col, row, height, objectIndex);
			break;
		case NameSpace.CHARGOAL:
			toAdd = createGoal(domain, col, row, height, objectIndex);
			break;
		case NameSpace.CHARAGENT:
			toAdd = createAgent(domain, col, row, height, objectIndex);
			break;
		case NameSpace.CHARAGENTFEET:
			toAdd = createAgentFeet(domain, col, row, height, objectIndex);
			break;
		case NameSpace.CHARGOLDBLOCK:
			toAdd = createGoldBlock(domain, col, row, height, objectIndex);
			break;
		case NameSpace.CHARFURNACE:
			toAdd = createFurnace(domain, col, row, height, objectIndex);
			break;
		case NameSpace.CHARINDBLOCK:
			toAdd = createIndWall(domain, col, row, height, objectIndex);
			break;
		case NameSpace.CHARLAVA:
			toAdd = createLava(domain, col, row, height, objectIndex);
			break;
		default:
			return false;
		}
		
		//Actually add the object
		stateToAddto.addObject(toAdd);
		return true;
	}
	
	
	//METHODS FOR CREATING OBJECTS TO ADD TO THE STATE
	private static ObjectInstance createGoal(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSGOAL;
		ObjectInstance goal = new ObjectInstance(d.getObjectClass(objectName), objectName + objectIndex);
		setObjectLocation(goal, x, y, z, false, true, false, false);
		return goal;
	}
	
	private static ObjectInstance createGoldBlock(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSGOLDBLOCK;
		ObjectInstance block = new ObjectInstance(d.getObjectClass(objectName), objectName+objectIndex);
		setObjectLocation(block, x, y, z, true, true, false, true);
		return block;
	}
	
	public static ObjectInstance createGoldBlockItem(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSGOLDBLOCK;
		ObjectInstance block = new ObjectInstance(d.getObjectClass(objectName), objectName+objectIndex);
		setObjectLocation(block, x, y, z, false, false, true, false);
		return block;
	}
	
	public static ObjectInstance createPickupableDirtBlock(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSDIRTBLOCKPICKUPABLE;
		ObjectInstance block = new ObjectInstance(d.getObjectClass(objectName), objectName+objectIndex);
		setObjectLocation(block, x, y, z, true, true, false, true);
		return block;	
	}
	
	public static ObjectInstance createNotPickupableDirtBlock(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSDIRTBLOCKNOTPICKUPABLE;
		ObjectInstance block = new ObjectInstance(d.getObjectClass(objectName), objectName+objectIndex);
		setObjectLocation(block, x, y, z, true, true, false, true);
		return block;	
	}
	
	public static ObjectInstance createDirtBlockItem(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSDIRTBLOCKPICKUPABLE;
		ObjectInstance block = new ObjectInstance(d.getObjectClass(objectName), objectName+objectIndex);
		setObjectLocation(block, x, y, z, false, false, true, false);
		return block;
	}
	
	private static ObjectInstance createAgent(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSAGENT;
		Random rand = new Random();
		ObjectInstance agent = new ObjectInstance(d.getObjectClass(objectName), objectName + objectIndex);
//		agent.setValue(NameSpace.ATROTDIR, 0);//Facing north by default
//		agent.setValue(NameSpace.ATVERTDIR, 3);//Facing ahead by default
		agent.setValue(NameSpace.ATROTDIR, rand.nextInt(4));
		agent.setValue(NameSpace.ATVERTDIR, rand.nextInt(4));
		setObjectLocation(agent, x, y, z, false, false, false, false);
		return agent;
	}
	
	private static ObjectInstance createAgentFeet(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSAGENTFEET;
		ObjectInstance agentFeet = new ObjectInstance(d.getObjectClass(objectName), objectName + objectIndex);
		setObjectLocation(agentFeet, x, y, z, false, false, false, false);
		return agentFeet;
	}
	
	public static ObjectInstance createFurnace(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSFURNACE;
		ObjectInstance block = new ObjectInstance(d.getObjectClass(objectName), objectName+objectIndex);
		setObjectLocation(block, x, y, z, true, true, false, false);
		return block;
	}
	
	public static ObjectInstance createIndWall(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSINDWALL;
		ObjectInstance block = new ObjectInstance(d.getObjectClass(objectName), objectName+objectIndex);
		setObjectLocation(block, x, y, z, true, true, false, false);
		return block;
	}
	
	public static ObjectInstance createLava(Domain d, int x, int y, int z, int objectIndex) {
		String objectName = NameSpace.CLASSLAVA;
		ObjectInstance block = new ObjectInstance(d.getObjectClass(objectName), objectName+objectIndex);
		setObjectLocation(block, x, y, z, false, false, false, false);
		return block;
	}
	
/**
 * 
 * @param object
 * @param x
 * @param y
 * @param z
 * @param collides
 * @param objectFloats
 * @param destroyedWhenWalked
 * @param destroyable
 */
	private static void setObjectLocation(ObjectInstance object, int x, int y, int z, boolean collides, boolean objectFloats, boolean destroyedWhenWalked, boolean destroyable) {
		object.setValue(NameSpace.ATX, x);
		object.setValue(NameSpace.ATY, y);
		object.setValue(NameSpace.ATZ, z);
		if (collides) {
			object.setValue(NameSpace.ATCOLLIDES, 1);
		}
		else {
			object.setValue(NameSpace.ATCOLLIDES, 0);
		}
		
		if (objectFloats) {
			object.setValue(NameSpace.ATFLOATS, 1);
		}
		
		else {
			object.setValue(NameSpace.ATFLOATS, 0);
		}
		
		if (destroyedWhenWalked) {
			object.setValue(NameSpace.ATDESTWHENWALKED, 1);
		}
		
		else {
			object.setValue(NameSpace.ATDESTWHENWALKED, 0);
		}
		
		if (destroyable) {
			object.setValue(NameSpace.ATDEST, 1);
		}
		else {
			object.setValue(NameSpace.ATDEST, 0);
		}
		
	}
	

	

}
