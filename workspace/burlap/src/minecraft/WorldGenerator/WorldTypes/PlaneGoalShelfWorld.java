package minecraft.WorldGenerator.WorldTypes;

import minecraft.NameSpace;

public class PlaneGoalShelfWorld extends MinecraftWorld {
	/**
	 * 
	 * @param numPlaceBlocks
	 * @param shelfHeight
	 * @param numLava
	 */
	
	public PlaneGoalShelfWorld(int numPlaceBlocks, int shelfHeight, int numLava) {
		this.numPlaceBlocks = numPlaceBlocks;
		this.goalShelfHeight = shelfHeight;
		this.numLava = numLava;
	}
	
	@Override
	public int getGoal() {
		return NameSpace.INTXYZGOAL;
	}

	
	@Override
	public String getName() {
		return "PlaneGoalShelfWorld";
	}

}
