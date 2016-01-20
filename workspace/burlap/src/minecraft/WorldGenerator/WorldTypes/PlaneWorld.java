package minecraft.WorldGenerator.WorldTypes;

import minecraft.NameSpace;

public class PlaneWorld extends MinecraftWorld {

	public PlaneWorld(int numLava) {
		this.numLava = numLava;
	}
	
	@Override
	public int getGoal() {
		return NameSpace.INTXYZGOAL;
	}

	@Override
	public String getName() {
		return "PlaneWorld";
	}

}
