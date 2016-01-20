package minecraft.WorldGenerator.WorldTypes;

import minecraft.NameSpace;

public class PlaneTowerWorld extends MinecraftWorld{
	
	/**
	 * 
	 * @param numPlaceBlocks
	 * @param numLava
	 */
	public PlaneTowerWorld(int numPlaceBlocks, int numLava) {
		this.numPlaceBlocks = numPlaceBlocks;
		this.numLava = numLava;
	}
	
	@Override
	public int getGoal() {
		return NameSpace.INTTOWERGOAL;
	}

	@Override
	public String getName() {
		return "PlaneTowerWorld";
	}
	

}
