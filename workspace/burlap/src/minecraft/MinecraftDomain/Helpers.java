package minecraft.MinecraftDomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import minecraft.MapIO;
import minecraft.NameSpace;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class Helpers {
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param state
	 * @return a boolean of whether there is no block with ATCOLLIDES == 1 at the input x,y,z
	 */
	public static Boolean emptySpaceAt(int x, int y, int z, State state) {
		List<ObjectInstance> objects = objectsAt(x,y,z, state);
		for(ObjectInstance object : objects) {
			if (object.getObjectClass().hasAttribute(NameSpace.ATCOLLIDES) && object.getDiscValForAttribute(NameSpace.ATCOLLIDES) == 1) {
				return false;
			}
			
		}
		return true;		
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param cols
	 * @param rows
	 * @param height
	 * @return boolean of whether the input x,y,z is in bounds of the input cols,rows,height
	 */
	public static Boolean withinMapAt(int x, int y, int z, int cols, int rows, int height) {
		boolean toReturn = x >= 0 && x < cols && y >=0 && y < rows && z >= 0 && z < height;
		return toReturn;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param state
	 * @return a list of all burlap objects with the input x,y,z coordinates
	 */
	public static ArrayList<ObjectInstance> objectsAt(int x, int y, int z, State state) {
		List<ObjectInstance> allObjects = state.getAllObjects();
		ArrayList<ObjectInstance> toReturn = new ArrayList<ObjectInstance>();

		//Loop over all objects that collide with the agent and perform collision detection
		for (ObjectInstance object: allObjects) {
				if (object.getDiscValForAttribute(NameSpace.ATX) == x && 
						object.getDiscValForAttribute(NameSpace.ATY) == y &&
						(object.getDiscValForAttribute(NameSpace.ATZ) == z)) {
					toReturn.add(object);
			}
		}
		return toReturn;
	}
	
	/**
	 * Determines if a given point (p) lies inside the bounds of the trench object specified by t and trenchVector
	 * @param p [x,y,z]
	 * @param t [x,y,z]
	 * @param trenchVector : a vector, indicating the bounds of the trench (starting at t)
	 * @return
	 */
	public static boolean isPointInTrench(int[] p, int[] t, int[] trenchVector) {
		
		int px = p[0];
		int py = p[1];
		int pz = p[2];
		
		int tx = t[0];
		int ty = t[1];
		int tz = t[2];	
		
		int tEndX = tx + trenchVector[0];
		int tEndY = ty + trenchVector[1];
		int tEndZ = tz + trenchVector[2];
		
		if (Math.min(tx, tEndX) <= px && px <= Math.max(tx, tEndX)
		&& Math.min(ty, tEndY) <= py && py <= Math.max(ty, tEndY)) {
			return true;
		}
		
		return false;
			
	}
	
	/**
	 * 
	 * @param distanceFromAgent how far away from the agents perspective you want
	 * @param state
	 * @return a 3 element array of ints of the position in front of the agents (x,y,z ordered)
	 */
	public static int[] positionInFrontOfAgent(int distanceFromAgent, State state, boolean toWalkTo) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		int oldX = agent.getDiscValForAttribute(NameSpace.ATX);
		int oldY = agent.getDiscValForAttribute(NameSpace.ATY);
		int oldZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		int xChange = 0;
		int yChange = 0;
		int zChange = 0;
		
		//Account for rotational direction
		int directionInt = agent.getDiscValForAttribute(NameSpace.ATROTDIR);
		NameSpace.RotDirection directionEnum = NameSpace.RotDirection.fromInt(directionInt);
		switch(directionEnum){
		case NORTH:
			yChange = -distanceFromAgent;
			break;
		case EAST:
			xChange = distanceFromAgent;
			break;
		case SOUTH:
			yChange = distanceFromAgent;
			break;
		case WEST:
			xChange = -distanceFromAgent;
			break;
		default:
			System.out.println("Couldn't find this rot direction for value: " + directionInt);
			break;
		}
		
		//Account for vertical direction
		int vertDirectionInt = agent.getDiscValForAttribute(NameSpace.ATVERTDIR);
		NameSpace.VertDirection vertDirectionEnum = NameSpace.VertDirection.fromInt(vertDirectionInt);
		switch(vertDirectionEnum) {
		case AHEAD:
			break;
		case DOWNONE:
			if (!toWalkTo) {
				zChange = -1;
			}
			break;
		case DOWNTWO:
			if (!toWalkTo) {
				zChange = -2;
			}
			break;
		case DOWNTHREE://All the way down
			if (!toWalkTo) {
				xChange = 0;
				yChange = 0;
				zChange = -1 - distanceFromAgent;
			}
			break;
		default:
			System.out.println("Couldn't find this vert direction for value: " + vertDirectionInt);
			break;
		}
		int newX = oldX+xChange;
		int newY = oldY+yChange;
		int newZ = oldZ+zChange;
		
		return new int[]{newX, newY, newZ};

	}
	
	public static boolean isPlaneBetweenTwoPoints(int[] a, int[] b, int[] plane, int[] planeVector) {
		
		int ax = a[0];
		int ay = a[1];
		int az = a[2];
		
		int px = plane[0];
		int py = plane[1];
		int pz = plane[2];
		
		int bx = b[0];
		int by = b[1];
		int bz = b[2];
		
		// Get plane end points
		int pxEnd = px + planeVector[0];
		int pyEnd = py + planeVector[1];
		int pzEnd = pz + planeVector[2];
		
		double avgX = (bx + ax) / 2;
		double avgY = (by + ay) / 2;
		
		if((px < avgX && avgX < pxEnd) || (py < avgY && avgY < pyEnd)
		|| (pxEnd < avgX && avgX < px) || (pyEnd < avgY && avgY < py)) {
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * 
	 * @param distanceFromAgent
	 * @param state
	 * @return a list of the objects at the input distance from the agent which the agent has visual access to
	 */
	public static List<ObjectInstance> getBlocksInfrontOfAgent(int distanceFromAgent, State state) {
		assert(distanceFromAgent <= 2);
		
		int[] positionInfrontAgent = positionInFrontOfAgent(distanceFromAgent, state, false);
		assert(positionInfrontAgent.length == 3);
		int newX = positionInfrontAgent[0];
		int newY = positionInfrontAgent[1];
		int newZ = positionInfrontAgent[2];
		
		
		//Get objects at this position
		List<ObjectInstance> objectsHere = objectsAt(newX, newY, newZ, state);
		
		//Filter out those that agent can't see
		ArrayList<ObjectInstance> toReturn = new ArrayList<ObjectInstance>();
		for (ObjectInstance object: objectsHere) {
			if (agentHasVisualAccessToCloseFace(object, distanceFromAgent, state)) {
				toReturn.add(object);
			}
		}
		
		return toReturn;
	}
	
	private static boolean agentHasVisualAccessToCloseFace(ObjectInstance block, int distanceOfBlock, State state) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		int blockX = block.getDiscValForAttribute(NameSpace.ATX);
		int blockY = block.getDiscValForAttribute(NameSpace.ATY);
		int blockZ = block.getDiscValForAttribute(NameSpace.ATZ);
		
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		int zDif = agentZ-blockZ;
		
		//If looking down two, can't see block if there is a block above it
		if (distanceOfBlock == 1 && zDif == 2 && !emptySpaceAt(blockX, blockY, blockZ+1, state) ){
			return false;
		}
		
		//If looking down two, can't see block if there is a block above it and towards the agent
		if (distanceOfBlock == 2 && zDif == 2 && !emptySpaceAt(agentX + (blockX - agentX)/2, blockY, blockZ+1, state)) {
			return false;
		}
		
		//If looking two ahead, straight ahead and there's a block in the way, don't have access
		if (distanceOfBlock == 2 && zDif == 0 && !emptySpaceAt(agentX + (blockX - agentX)/2, blockY, blockZ, state)) {
			return false;
		}
		
		//If looking two ahead, down one and there's a block in the way, don't have access
		if (distanceOfBlock == 2 && zDif == 1 && !emptySpaceAt(agentX + (blockX - agentX)/2, blockY, blockZ, state)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Used to remove an object from the state in such a way as to have the appropriate side effects
	 * @param object the object to remove
	 * @param state
	 * @param domain
	 */
	public static void removeObjectFromState(ObjectInstance object, State state, Domain domain) {
		String objectName = object.getTrueClassName();
		
		//PICKUPABLEDIRTBLOCKS
		if (objectName.equals(NameSpace.CLASSDIRTBLOCKPICKUPABLE)) {
			//A dirt block item
			if (object.getDiscValForAttribute(NameSpace.ATDESTWHENWALKED) == 1){
				ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
				int oldBlocksPlaced = agent.getDiscValForAttribute(NameSpace.ATPLACEBLOCKS);
				agent.setValue(NameSpace.ATPLACEBLOCKS, oldBlocksPlaced+1);
				state.removeObject(object);
			}
			//A dirt block
			else {
				blockToItem(object);
			}
		}
		
		//NOTPICKUPABLEDIRTBLOCKS
		if (objectName.equals(NameSpace.CLASSDIRTBLOCKNOTPICKUPABLE)) {
			state.removeObject(object);
		}		
		
		
		//GOLDBLOCKS
		else if (objectName.equals(NameSpace.CLASSGOLDBLOCK)) {
			//A gold block item
			if (object.getDiscValForAttribute(NameSpace.ATDESTWHENWALKED) == 1){
				ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
				int oldGoldBlockItemsCarried = agent.getDiscValForAttribute(NameSpace.ATAMTGOLDORE);
				agent.setValue(NameSpace.ATAMTGOLDORE, oldGoldBlockItemsCarried+1);
				state.removeObject(object);
			}
			//A gold block
			else {
				blockToItem(object);
			}
		}
		
		
		
	}
	
	private static void blockToItem(ObjectInstance block) {
		block.setValue(NameSpace.ATDEST, 0);
		block.setValue(NameSpace.ATFLOATS, 0);
		block.setValue(NameSpace.ATDESTWHENWALKED, 1);
		block.setValue(NameSpace.ATCOLLIDES, 0);
	}
	/**
	 * 
	 * @param state
	 * @return true if there is a block which collides below the agent
	 */
	public static boolean blockBelowAgent(State state) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		return !emptySpaceAt(agentX, agentY, agentZ-2, state);
	}
	
	public static boolean agentCanJump(State state, int rows, int cols, int height) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		boolean canJumpOff = blockBelowAgent(state) || agentZ - 2 < 0;
		boolean wontHitHead = emptySpaceAt(agentX, agentY, agentZ+1, state) && withinMapAt(agentX, agentY, agentZ+1, cols, rows, height);
		return canJumpOff && wontHitHead;
	}
	
	public static boolean agentLookingInDirectionOfBlock(State state, String objectLookingToward) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		List<ObjectInstance> allObjectsOfInterest = state.getObjectsOfTrueClass(objectLookingToward);
		
		int agentRotDir = agent.getDiscValForAttribute(NameSpace.ATROTDIR);
		
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		
		
		switch(agentRotDir){
		case 0:
			for (ObjectInstance object : allObjectsOfInterest){
				int objectY = object.getDiscValForAttribute(NameSpace.ATY);
				if (objectY < agentY) return true;
			}
			break;
		case 1:
			for (ObjectInstance object : allObjectsOfInterest){
				int objectX = object.getDiscValForAttribute(NameSpace.ATX);
				if (objectX > agentX) return true;
			}
			break;
		case 2:
			for (ObjectInstance object : allObjectsOfInterest){	
				int objectY = object.getDiscValForAttribute(NameSpace.ATY);
				if (objectY > agentY) return true;
			}
			break;
		case 3:
			for (ObjectInstance object : allObjectsOfInterest){
				int objectX = object.getDiscValForAttribute(NameSpace.ATX);
				if (objectX < agentX) return true;
			}
			break;
		}		
		return false;
	}
	
	
}


