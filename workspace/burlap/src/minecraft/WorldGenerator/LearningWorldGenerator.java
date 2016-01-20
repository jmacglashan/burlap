//package minecraft.WorldGenerator;
//
//import burlap.oomdp.logicalexpressions.LogicalExpression;
//
//import java.util.Random;
//
//import minecraft.NameSpace;
//import minecraft.WorldGenerator.Exceptions.WorldNotTallEnoughException;
//
//public class LearningWorldGenerator extends WorldGenerator {
//	private static Random 	r = new Random();
//	private static int 		maxTrenches = 2;
//	private static int 		depthOfDirtFloor = 2;
//	private static double 	probOfTrenchChangeDir = 0; // Straight trenches for now
//	private 	   int	 	numTrenches = 1;
//	
//	public LearningWorldGenerator(int rows, int cols, int height) {
//		super(rows, cols, height, depthOfDirtFloor, probOfTrenchChangeDir);
//	}
//
//	public char[][][] generateMap(LogicalExpression goalDescription) {
//		char[][][] toReturn = new char[this.rows][this.cols][this.height];
//		// Initialize empty
//		this.emptifyCharArray(toReturn);
//		
//		// Add dirt floor
//		this.addFloor(toReturn, NameSpace.CHARINDBLOCK);
//		
//		
//		//Add agent
//		try {
//			this.addAgent(toReturn);
//		} catch (WorldNotTallEnoughException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		// Add trench
//		if(goalDescription.toString().contains("trench")) {
//			//this.addTrenches(this.numTrenches, toReturn, false);
//		}
//		
//		// Add trench
//		if(goalDescription.toString().contains("Gold")) {
//			// Add gold stuff
//			//this.addGoldOre(toReturn);
//			this.addFurnace(toReturn);
//		} else {
//			//Add goal
////			this.addRandomSpatialGoal(toReturn);			
//		}
//				
//		return toReturn;
//	}
//
//}
