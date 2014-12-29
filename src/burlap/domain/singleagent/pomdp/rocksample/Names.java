package burlap.domain.singleagent.pomdp.rocksample;

public class Names {
	
	// attributes
	public static final String ATTR_X = "attr.x";
	public static final String ATTR_Y = "attr.y";
	public static final String ATTR_VALUE = "attr.Value";
	public static final String ATTR_COLLECTED = "attr.collected";
	public static final String ATTR_DIRECTION = "attr.Direction";
	public static final String ATTR_ROCK_NUMBER = "attr.RockNumber";
	public static final String ATTR_WALL_DIRECTION= "attr.wallDirection";// not sure how to use this right
	public static final String ATTR_COMPLETE = "attr.complete";
	public static final String ATTR_GRID_SIZE = "attr.gridSize";
	public static final String ATTR_NUMBER_OF_ROCKS = "attr.numberOfRocks";
	
	public static final String ATTR_OBS = "attr.obs";
	
	//classes
	public static final String CLASS_ROCK = "class.rock";
	public static final String CLASS_WALL = "class.wall";
	public static final String CLASS_AGENT = "class.agent";
	public static final String CLASS_DIRECTION = "class.direction";
	public static final String CLASS_GRID = "class.grid";
	
	public static final String CLASS_OBSERVATION = "class.observation";
	
	// objects
//	public static final String Good = "Good";
	public static final String OBJ_AGENT = "obj.Agent";
	public static final String OBJ_ROCK = "obj.Rock";
	public static final String OBJ_EAST = "obj.East";
	public static final String OBJ_WEST = "obj.West";
	public static final String OBJ_NORTH = "obj.North";
	public static final String OBJ_SOUTH = "obj.South";
	public static final String OBJ_WALL_EAST = "obj.wallEast";
	public static final String OBJ_WALL_WEST = "obj.wallWest";
	public static final String OBJ_WALL_NORTH = "obj.wallNorth";
	public static final String OBJ_WALL_SOUTH = "obj.wallSouth";
	public static final String OBJ_GRID = "obj.grid";
	
	// obs obj
	public static final String OBJ_OBS = "obj.obs";
	
	
	//actions 
	public static final String ACTION_MOVE = "act.move";
	public static final String ACTION_SAMPLE = "act.sample";
	public static final String ACTION_CHECK = "act.check";
	
	// params - these are as good as string objects that actions or objects can point to 
	public static final String DIR_WEST = "dir.west";
	public static final String DIR_EAST = "dir.east";
	public static final String DIR_NORTH = "dir.north";
	public static final String DIR_SOUTH = "dir.south";
	
	//observations
	public static final String OBS_GOOD = "obs.good";
	public static final String OBS_BAD = "obs.bad";
	public static final String OBS_NULL = "obs.null";
	public static final String OBS_COMPLETE = "obs.complete";

}
