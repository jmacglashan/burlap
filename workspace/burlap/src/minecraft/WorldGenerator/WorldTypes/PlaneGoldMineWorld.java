package minecraft.WorldGenerator.WorldTypes;

import minecraft.NameSpace;

public class PlaneGoldMineWorld extends MinecraftWorld {
	
	/**
	 * @param numLava
	 */
	public PlaneGoldMineWorld(int numLava) {
		this.numLava = numLava;
		this.goldOreDepth = -2;
		this.floorDepth = 2;
		this.floorOf = NameSpace.CHARDIRTBLOCKNOTPICKUPABLE;
		
	}

	@Override
	public int getGoal() {
		return NameSpace.INTGOLDOREGOAL;
	}

	@Override
	public String getName() {
		return "PlaneGoldMineWorld";
	}
}
