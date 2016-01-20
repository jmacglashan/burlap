package minecraft.WorldGenerator.WorldTypes;

import minecraft.NameSpace;

public abstract class MinecraftWorld {
	//Default values
	protected int numWalls = 0;
	protected int numTrenches = 0;
	protected char wallOf = NameSpace.CHARDIRTBLOCKNOTPICKUPABLE;
	protected char floorOf = NameSpace.CHARINDBLOCK;
	protected boolean trenchStraightAndBetweenAgentAndGoal = true;
	protected boolean wallsStraightAndBetweenAgentAndGoal = true;
	protected int floorDepth = 1;
	protected Integer goldOreDepth = 0;//Agent's feet is the origin
	protected int numPlaceBlocks = 0;//Num blocks agent can place
	protected int goalShelfHeight = 0;
	protected int numLava = 0;
	
	//Things that definetely need to be overridden
	public abstract int getGoal();
	public abstract String getName();

	
	
	//Defaulted world features getters:
	public int getNumLava() {
		return this.numLava;
	}
	
	public int getGoalShelfHeight() {
		return this.goalShelfHeight;
	}
	
	public Integer getDepthOfGoldOre() {
		return this.goldOreDepth;
	}
	
	public boolean getTrenchStraightAndBetweenAgentAndGoal() {
		 return this.trenchStraightAndBetweenAgentAndGoal;
	}
	public int getNumWalls() {
		return this.numWalls;
	}
	
	public char getWallOf() {
		return this.wallOf;
	}
	
	public char getFloorOf() {
		return this.floorOf;
	}
	
	public int getNumTrenches() {
		return this.numTrenches;
	}
	
	public int getFloorDepth() {
		return this.floorDepth;
	}
	
	public boolean getwallsStraightAndBetweenAgentAndGoal() {
		return wallsStraightAndBetweenAgentAndGoal;
	}
	
	public int getNumPlaceBlocks() {
		return this.numPlaceBlocks;
	}
	
}
