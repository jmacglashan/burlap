package minecraft.WorldGenerator.WorldTypes;

import minecraft.NameSpace;

public class PlaneGoldSmeltWorld extends MinecraftWorld {

	/**
	 * @param numLava
	 */
	public PlaneGoldSmeltWorld(int numLava) {
		this.numLava = numLava;
	}
	
	@Override
	public int getGoal() {
		return NameSpace.INTGOLDBARGOAL;
	}

	@Override
	public String getName() {
		return "PlaneGoldSmeltWorld";
	}

}
