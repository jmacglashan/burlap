package burlap.domain.singleagent.minecraft;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.List;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;


import burlap.oomdp.core.State;

/**
 * @author dabel, gabrielbm
 * Reads in an ascii map of a 10x10 minecraft map and initializes a minecraft state.
 * Here is an example map file:
 * g
 * +++++++++ 
 * 
 * a
 * This will create a goal at (0,0,1), a wall from (0,1,2) to (8,1,2), and an agent
 * at (0,3,2).
 */
public class MCStateGenerator {
	
	private String fpath;
	
//	Symbols for parsing file
	private static final char gSym = 'g';
	private static final char bAddSym = '+';
	private static final char aSym = 'a';
	private static final char bRmSym = '-';
	private static final char dummySym = '.';
	private static final char wallSym = '=';
	
	/**
	 * @param path the file path for the map file.
	 */
	public MCStateGenerator(String path) {
		// TODO Auto-generated constructor stub
		
		// Convert relative path to absolute.
		String root = System.getProperty("user.dir");
		String abspath = root + "/maps/" + path;
		
		this.fpath = abspath;
	}
	
	/**
	 * This is the main method for the MCStateGenerator class.
	 * A new state is created, and we create an empty 10x10 floor.
	 * Next, the map file is read and adjustments are made to the empty 10x10 floor
	 * as necessary.
	 * @param d the uninitialized domain.
	 * @return the initialized State object.
	 */
	public State getCleanState(Domain d) {

		State s = new State();
		int nrow = 0;
		
		try {
			Scanner scnr = new Scanner(new File(this.fpath));
			while (scnr.hasNextLine()) {
				processRow(s, d, scnr.nextLine(), nrow);
				nrow++;
			}
			scnr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return s;
				
	}
	
	/**
	 * Here we read each ascii character in the row and make adjustments to the
	 * 10x10 empty state.
	 * @param s the state we are building
	 * @param d the uninitialized domain
	 * @param row the current map row in ascii format
	 * @param nrow the row number
	 */
	public static void processRow(State s, Domain d, String row, int nrow) {
		char ch;
		int ncol = 0;
		row = row.replace(" ", "");
		
		while (ncol < row.length()) {
			ch = row.charAt(ncol);
			if (ch != ' ') {
				addBlock(s, d, ncol, nrow, 0);
			}
			switch (ch) {
			case bAddSym:
				addBlock(s, d, ncol, nrow, 1);
				ncol++;
				break;
			case aSym:
				addAgent(s, d, ncol, nrow, 1);
				ncol++;
				break;
			case gSym:
				addGoal(s, d, ncol, nrow, 1);
				ncol++;
				break;
			case bRmSym:
				removeBlock(s, d, ncol, nrow, 0);
				ncol++;
				break;
			case dummySym:
				addBlock(s, d, ncol, nrow, 0);
				ncol++;
				break;
			case wallSym:
				addWall(s, d, ncol, nrow, 1);
				ncol++;
				break;
			default:
				continue;
			}
		}
	}
	
	
	private static void addWall(State s, Domain d, int x, int y, int z) {
		addBlock(s, d, x, y, 0); // Add a block under the wall
		ObjectInstance wall = new ObjectInstance(d.getObjectClass("block"), "block"+x+y+z);
		wall.setValue("x", x);
		wall.setValue("y", y);
		wall.setValue("z", z);
		wall.setValue("attDestroyable", 0); // Walls cannot be destroyed
		s.addObject(wall);
	}
	
	private static void addBlock(State s, Domain d, int x, int y, int z) {
		ObjectInstance block = new ObjectInstance(d.getObjectClass("block"), "block"+x+y+z);
		block.setValue("x", x);
		block.setValue("y", y);
		block.setValue("z", z);
		block.setValue("attDestroyable", 1); // By default blocks can be destroyed
		s.addObject(block);
	}
	
	private static void removeBlock(State s, Domain d, int x, int y, int z) {
		ObjectInstance block = s.getObject("block" + Integer.toString(x) + Integer.toString(y) + Integer.toString(z));
		s.removeObject(block);
	}
	
	private static void addAgent(State s, Domain d, int x, int y, int z) {
		ObjectInstance agent = new ObjectInstance(d.getObjectClass("agent"), "agent0");
		agent.setValue("bNum", 1);  // Expliticly set the number of blocks agent can carry to 1
		addBlock(s, d, x, y, z - 1); // Agent needs to be on top of a block
		addObject(agent, s, d, x, y, z);
	}

	private static void addGoal(State s, Domain d, int x, int y, int z) {
		ObjectInstance goal = new ObjectInstance(d.getObjectClass("goal"), "goal0");
		addBlock(s, d, x, y, z - 1); // Goal needs to be on top of a block
		addObject(goal, s, d, x, y, z);
	}
	
	private static void addObject(ObjectInstance obj, State s, Domain d, int x, int y, int z) {
		obj.setValue("x", x);
		obj.setValue("y", y);
		obj.setValue("z", z);
		s.addObject(obj);
	}
	

	public static void main(String[] args) {
		MinecraftDomain mcdg = new MinecraftDomain();
		Domain domain = mcdg.generateDomain();
		
		MCStateGenerator mcsg = new MCStateGenerator("/Users/dabel/Projects/workspace/burlap/src/burlap/domain/singleagent/minecraft/test.txt");
		mcsg.getCleanState(domain);
		System.out.println(domain.toString());

	}

}
