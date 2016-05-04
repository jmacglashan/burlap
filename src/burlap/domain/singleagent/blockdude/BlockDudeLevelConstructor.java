package burlap.domain.singleagent.blockdude;

import burlap.domain.singleagent.blockdude.states.BlockDudeAgent;
import burlap.domain.singleagent.blockdude.states.BlockDudeCell;
import burlap.domain.singleagent.blockdude.states.BlockDudeMap;
import burlap.domain.singleagent.blockdude.states.BlockDudeState;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.state.State;

/**
 * A class for generating the initial states for {@link burlap.domain.singleagent.blockdude.BlockDude} from levels
 * in the classic Block Dude game. Currently this class will
 * provide the initial states for the first three levels of Block Dude.
 * @author James MacGlashan.
 */
public class BlockDudeLevelConstructor {

    private BlockDudeLevelConstructor() {
        // do nothing
    }

	/**
	 * Returns the initial {@link State} of the first level.
	 * @param domain the domain to which the state will belong.
	 * @return the initial {@link State} of the first level.
	 */
	public static State getLevel1(Domain domain){

		int [][] map = new int[25][3];
		addFloor(map);

		map[3][1] = 1;
		map[3][2] = 1;

		map[7][1] = 1;

		map[11][1] = 1;
		map[11][2] = 1;


		BlockDudeState s = new BlockDudeState(
				new BlockDudeAgent(15, 1, 1, false),
				new BlockDudeMap(map),
				BlockDudeCell.exit(0, 1),
				BlockDudeCell.block("b0", 9, 1),
				BlockDudeCell.block("b1", 13, 1)
		);

		return s;
	}


	/**
	 * Returns the initial {@link State} of the second level.
	 * @param domain the domain to which the state will belong.
	 * @return the initial {@link State} of the second level.
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

		BlockDudeState s = new BlockDudeState(
				new BlockDudeAgent(17, 3, 0, false),
				new BlockDudeMap(map),
				BlockDudeCell.exit(0, 6),
				BlockDudeCell.block("b0", 7, 1),
				BlockDudeCell.block("b1", 13, 3),
				BlockDudeCell.block("b2", 15, 3),
				BlockDudeCell.block("b3", 14, 4),
				BlockDudeCell.block("b4", 16, 3)
		);


		return s;
	}


	/**
	 * Returns the initial {@link State} of the third level.
	 * @param domain the domain to which the state will belong.
	 * @return the initial {@link State} of the third level.
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

		BlockDudeState s = new BlockDudeState(
				new BlockDudeAgent(8, 4, 0, false),
				new BlockDudeMap(map),
				BlockDudeCell.exit(0, 1),
				BlockDudeCell.block("b0", 4, 2),
				BlockDudeCell.block("b1", 5, 2),
				BlockDudeCell.block("b2", 13, 4),
				BlockDudeCell.block("b3", 15, 5),
				BlockDudeCell.block("b4", 16, 5),
				BlockDudeCell.block("b5", 16, 6));

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
