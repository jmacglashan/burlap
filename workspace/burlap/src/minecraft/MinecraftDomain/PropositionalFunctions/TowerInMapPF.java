package minecraft.MinecraftDomain.PropositionalFunctions;

import java.util.HashMap;

import minecraft.MinecraftStateParser;
import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class TowerInMapPF extends PropositionalFunction{
	private int towerHeight;
	private char towerOf;
	private int rows;
	private int cols;
	private int height;
	
	public TowerInMapPF(String name, Domain domain, String[] parameterClasses, int heightOfTower, char towerOf, int rows, int cols, int height) {
		super(name, domain, parameterClasses);
		this.towerHeight = heightOfTower;
		this.towerOf = towerOf;
		this.rows = rows;
		this.height = height;
		this.cols = cols;
	}
	@Override
	public boolean isTrue(State state, String[] params) {
		char[][][] stateAsCharArray = MinecraftStateParser.stateToCharArray(state, rows, cols, height);
		return towerInCharArray(stateAsCharArray);
	}
	
	private boolean towerInCharArray(char[][][] charArray) {
		for (int row = 0; row < this.rows; row++) {
			for (int col = 0; col < this.cols; col++) {
				int topOfTower = columnAtThisPosition(charArray, col, row);
				int heightOfFirstObstruction = heightUntilBuried(charArray, col, row, false);
				int heightOfTower = -heightOfFirstObstruction + topOfTower;
				if (heightOfTower >= this.towerHeight) return true;
			}
		}
		return false; 
	}
	private int columnAtThisPosition(char[][][] charArray, int x, int y) {		
		
		for (int currHeight = 0; currHeight < this.height; currHeight++) {
			char currCharacter = charArray[y][x][currHeight];
			if (currCharacter != this.towerOf) return currHeight-1;
		}
		return this.height-1;
	}
	
	private int heightUntilBuried(char[][][] charArray, int col, int row, boolean outOfBoundsIsObstruction) {
		int height = charArray[0][0].length;
		int heightWhereObstructionStarts = -1;
		for (int currHeight = height-1; currHeight >= 0; currHeight--) {
			boolean somethingObstructing = false;
			for (int xAway = -1; xAway < 2; xAway++) {
				for (int yAway = -1; yAway < 2; yAway++) {
					//Break if ontop of tower
					if (xAway == 0 && yAway == 0) continue;
					
					int newY = row+yAway;
					int newX = col+xAway;
					//Continue if out of bounds
					if (!Helpers.withinMapAt(newX, newY, 0, this.cols, this.rows, this.height)) {
						if (outOfBoundsIsObstruction) somethingObstructing = true;
						continue;
					}
					//If not empty then cause break
					char currentChar = charArray[newY][newX][currHeight];
					if (currentChar != NameSpace.CHAREMPTY && currentChar != NameSpace.CHARAGENT && currentChar != NameSpace.CHARAGENTFEET) {
						somethingObstructing = true;	
					}
				}
			}
			if (somethingObstructing) {
				heightWhereObstructionStarts = currHeight;
				break;
			}
		}
		
		return heightWhereObstructionStarts;

	}

}
