package minecraft.WorldGenerator.WorldTypes;

import minecraft.NameSpace;

public class PlaneWallWorld extends MinecraftWorld{
	
	/**
	 * @param numWalls
	 */
	public PlaneWallWorld(int numWalls, int numLava) {
		this.numWalls = numWalls;
		this.numLava = numLava;
	}
	
	@Override
	public int getGoal() {
		return NameSpace.INTXYZGOAL;
	}

	@Override
	public String getName() {
		return "PlaneWallWorld";
	}

}
