package minecraft.WorldGenerator.WorldTypes;

import minecraft.NameSpace;

public class DeepTrenchWorld extends MinecraftWorld {
	/**
	 * 
	 * @param numTrenches
	 * @param numLava
	 */
	public DeepTrenchWorld(int numTrenches, int numLava) {
		this.numTrenches = numTrenches;
		this.floorDepth = 3;
		this.numPlaceBlocks = numTrenches;
		this.numLava = numLava;
	}
	
	@Override
	public int getGoal() {
		return NameSpace.INTXYZGOAL;
	}
	


	@Override
	public String getName() {
		return "DeepTrenchWorld";
	}
}
