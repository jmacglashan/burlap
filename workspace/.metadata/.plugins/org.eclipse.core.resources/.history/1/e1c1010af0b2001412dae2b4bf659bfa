package minecraft;

import java.util.HashMap;
import minecraft.WorldGenerator.WorldGenerator;
import minecraft.MinecraftDomain.MinecraftDomainGenerator;
import minecraft.MinecraftStateGenerator.MinecraftStateGenerator;
import minecraft.MinecraftStateGenerator.Exceptions.StateCreationException;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


public class MinecraftStateParser implements StateParser {

	private Domain				domain;
	
	public MinecraftStateParser(Domain domain){
		this.domain = domain;
	}
	
	@Override
	public String stateToString(State s) {
		
		StringBuffer sbuf = new StringBuffer(256);
		
		ObjectInstance agent = s.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
//		ObjectInstance goal = s.getObjectsOfTrueClass(NameSpace.CLASSGOAL).get(0);
		
		String xa = NameSpace.ATX;
		String ya = NameSpace.ATY;
		String za = NameSpace.ATZ;
		int numberOfBlocks = agent.getDiscValForAttribute(NameSpace.ATPLACEBLOCKS);
		sbuf.append(agent.getDiscValForAttribute(xa)).append(",").append(agent.getDiscValForAttribute(ya)).append(",").append(agent.getDiscValForAttribute(za)).append(" ");
		sbuf.append(numberOfBlocks);
//		sbuf.append(goal.getDiscValForAttribute(xa)).append(",").append(goal.getDiscValForAttribute(ya)).append(",").append(goal.getDiscValForAttribute(za)).append(" ");
		
		return sbuf.toString();
	}
	
	private static char objectToChar(ObjectInstance object) {
		String objectClassName = object.getObjectClass().name;
		
		if (objectClassName == NameSpace.CLASSAGENT) return NameSpace.CHARAGENT;
		if (objectClassName == NameSpace.CLASSAGENTFEET) return NameSpace.CHARAGENTFEET;
		if (objectClassName == NameSpace.CLASSDIRTBLOCKPICKUPABLE) return NameSpace.CHARDIRTBLOCKPICKUPABLE;
		if (objectClassName == NameSpace.CLASSDIRTBLOCKNOTPICKUPABLE) return NameSpace.CHARDIRTBLOCKNOTPICKUPABLE;
		if (objectClassName == NameSpace.CLASSFURNACE) return NameSpace.CHARFURNACE;
		if (objectClassName == NameSpace.CLASSGOAL) return NameSpace.CHARGOAL;
		if (objectClassName == NameSpace.CLASSGOLDBLOCK) return NameSpace.CHARGOLDBLOCK;
		if (objectClassName == NameSpace.CLASSINDWALL) return NameSpace.CHARINDBLOCK;
		
		return NameSpace.CHARUNIDENTIFIED;
	}
	/**
	 * 
	 * @param state
	 * @param rows
	 * @param cols
	 * @param height
	 * @return
	 */
	public static char[][][] stateToCharArray(State state, int rows, int cols, int height) {
		char [][][] toReturn = new char[rows][cols][height];
		//Set all chars to empty by default
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				for (int currHeight = 0; currHeight < height; currHeight++) {
					toReturn[row][col][currHeight] = NameSpace.CHAREMPTY;
				}
			}	
		}
		
		//Add in chars for objects
		for (ObjectInstance object : state.getAllObjects()) {
			//A spatial object
			if (object.getObjectClass().hasAttribute(NameSpace.ATX)) {
				int x = object.getDiscValForAttribute(NameSpace.ATX);
				int y = object.getDiscValForAttribute(NameSpace.ATY);
				int z = object.getDiscValForAttribute(NameSpace.ATZ);

				char currChar = objectToChar(object);
				char oldChar = toReturn[y][x][z];
				if (oldChar != NameSpace.CHARAGENT) {
					toReturn[y][x][z] = currChar;
				}
			}
		}
		
		return toReturn;
	}

	@Override
	public State stringToState(String str) {
		String[] splitOnFirstNewLine = str.split("\n",1);
		assert(splitOnFirstNewLine.length == 2);
		String stateInfoString = splitOnFirstNewLine[0];
		String mapString = splitOnFirstNewLine[1];
		
		HashMap<String, Integer> header = MapIO.processHeader(stateInfoString);
		char[][][] mapAsCharArray = MapIO.processMapString(mapString);
		
		try {
			return MinecraftStateGenerator.createInitialState(mapAsCharArray, header, domain);
		} catch (StateCreationException e) {
			e.printStackTrace();
		}
		return null;
				
	}
	
	

}

