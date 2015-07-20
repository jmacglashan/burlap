package burlap.domain.singleagent.blockdude;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;

/**
 * A class for generating the initial states for {@link burlap.domain.singleagent.blockdude.BlockDude} from levels
 * in the classic Block Dude game. Currently this class will
 * provide the initial states for the first three levels of Block Dude.
 * @author James MacGlashan.
 */
public class BlockDudeLevelConstructor {


	/**
	 * Returns the initial {@link burlap.oomdp.core.State} of the first level.
	 * @param domain the domain to which the state will belong.
	 * @return the initial {@link burlap.oomdp.core.State} of the first level.
	 */
	public static State getLevel1(Domain domain){

		int [][] map = new int[25][3];
		addFloor(map);

		map[3][1] = 1;
		map[3][2] = 1;

		map[7][1] = 1;

		map[11][1] = 1;
		map[11][2] = 1;


		State s = BlockDude.getUninitializedState(domain, 2);
		BlockDude.setAgent(s, 15, 1, 1, false);
		BlockDude.setExit(s, 0, 1);
		BlockDude.setBlock(s, 0, 9, 1);
		BlockDude.setBlock(s, 1, 13, 1);
		BlockDude.setBrickMap(s, map);

		return s;
	}


	/**
	 * Returns the initial {@link burlap.oomdp.core.State} of the second level.
	 * @param domain the domain to which the state will belong.
	 * @return the initial {@link burlap.oomdp.core.State} of the second level.
	 */
	public static State getLevel2(Domain domain){

		int [][] map = new int[25][6];

		wallSegment(map, 2, 5, 0);
		floorSegment(map, 0, 4, 2);
		wallSegment(map, 0, 2, 4);
		floorSegment(map, 4, 8, 0);
		wallSegment(map, 0, 2, 8);
		floorSegment(map, 8, 24, 2);
		wallSegment(map, 2, 4, 12);

		State s = BlockDude.getUninitializedState(domain, 5);
		BlockDude.setAgent(s, 17, 3, 0, false);
		BlockDude.setExit(s, 0, 6);

		BlockDude.setBlock(s, 0, 7, 1);
		BlockDude.setBlock(s, 1, 13, 3);
		BlockDude.setBlock(s, 2, 15, 3);
		BlockDude.setBlock(s, 3, 15, 4);
		BlockDude.setBlock(s, 4, 16, 3);

		BlockDude.setBrickMap(s, map);

		return s;
	}


	/**
	 * Returns the initial {@link burlap.oomdp.core.State} of the third level.
	 * @param domain the domain to which the state will belong.
	 * @return the initial {@link burlap.oomdp.core.State} of the third level.
	 */
	public static State getLevel3(Domain domain){

		int [][] map = new int[25][9];

		floorSegment(map, 0, 1, 0);
		wallSegment(map, 0, 4, 1);
		floorSegment(map, 1, 3, 4);
		wallSegment(map, 0, 4, 3);
		floorSegment(map, 3, 8, 1);
		map[7][2] = 1;
		wallSegment(map, 0, 3, 8);
		floorSegment(map, 8, 10, 0);
		map[10][1] = 1;
		wallSegment(map, 1, 3, 11);
		map[12][4] = 1;
		floorSegment(map, 11, 15, 3);
		map[15][4] = 1;
		map[16][4] = 1;
		wallSegment(map, 5, 8, 17);

		State s = BlockDude.getUninitializedState(domain, 6);
		BlockDude.setAgent(s, 8, 4, 0, false);
		BlockDude.setExit(s, 0, 1);

		BlockDude.setBlock(s, 0, 4, 2);
		BlockDude.setBlock(s, 1, 5, 2);
		BlockDude.setBlock(s, 2, 13, 4);
		BlockDude.setBlock(s, 3, 15, 5);
		BlockDude.setBlock(s, 4, 16, 5);
		BlockDude.setBlock(s, 5, 16, 6);

		BlockDude.setBrickMap(s, map);

		return s;
	}

	public static void addFloor(int [][] map){
		for(int i = 0; i < map.length; i++){
			map[i][0] = 1;
		}
	}

	public static void floorSegment(int [][] map, int x0, int xf, int y){
		for(int i = x0; i <= xf; i++){
			map[i][y] = 1;
		}
	}

	public static void wallSegment(int [][] map, int y0, int yf, int x){
		for(int i = y0; i <= yf; i++){
			map[x][i] = 1;
		}
	}

}
