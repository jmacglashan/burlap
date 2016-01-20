package minecraft;

import java.util.List;

import minecraft.MinecraftDomain.PropositionalFunctions.AgentHasAtLeastXGoldBarPF;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentHasAtLeastXGoldOrePF;
import minecraft.MinecraftDomain.PropositionalFunctions.AtGoalPF;
import minecraft.MinecraftDomain.PropositionalFunctions.TowerInMapPF;
import affordances.AffordanceLearner;
import burlap.oomdp.logicalexpressions.LogicalExpression;

public class NameSpace {
	//-------------ATTRIBUTE STRINGS-------------
	public static final String							ATX = "x";
	public static final String							ATY = "y";
	public static final String							ATZ = "z";
	public static final String							ATCOLLIDES = "collides";
	public static final String 							ATROTDIR = "rotationalDirection";
	public static final String							ATDEST = "destroyable";
	public static final String							ATFLOATS = "floats";
	public static final String							ATPLACEBLOCKS = "placeableBlocks";
	public static final String							ATVERTDIR = "verticalDirection";
	public static final String							ATDESTWHENWALKED = "destroyedByAgentWhenWalkedOn";
	public static final String							ATAMTGOLDORE = "amountOfGoldOre";
	public static final String							ATAMTGOLDBAR = "amountOfGoldBar";
	public static final String							ATTRENCHVECTOR = "trenchVector";
	
	//-------------BURLAP OBJECTCLASS STRINGS-------------
	public static final String							CLASSAGENT = "agent";
	public static final String							CLASSAGENTFEET = "agentsFeet";
	public static final String							CLASSGOAL = "goal";
	public static final String							CLASSDIRTBLOCKPICKUPABLE = "dirtBlockPickupable";
	public static final String							CLASSDIRTBLOCKNOTPICKUPABLE = "dirtBlockNotPickupable";
	public static final String							CLASSGOLDBLOCK = "goldBlock";
	public static final String							CLASSINDWALL = "indestrucibleWall";
	public static final String							CLASSFURNACE = "furnace";
	public static final String							CLASSTRENCH = "trench";
	public static final String							CLASSLAVA = "lava";

	
	//-------------ASCII MAP-------------
	//Map chars
	public static final char CHARGOAL = 'G';
	public static final char CHARAGENT = 'A';
	public static final char CHARDIRTBLOCKPICKUPABLE = '.';
	public static final char CHARDIRTBLOCKNOTPICKUPABLE = ',';
	public static final char CHAREMPTY = 'e';
	public static final char CHARAGENTFEET = 'F';
	public static final char CHARINDBLOCK = 'w';
	public static final char CHARGOLDBLOCK = 'g';
	public static final char CHARFURNACE = 'f';
	public static final char CHAROUTOFBOUNDS = 'o';
	public static final char CHARUNIDENTIFIED = 'u';
	public static final char CHARLAVA = 'l';
	
	//Header chars
	public static final char CHARPLACEABLEBLOCKS = 'B';
	public static final char CHARSTARTINGGOLDORE = 'g';
	public static final char CHARSTARTINGGOLDBAR = 'b';
	public static final char CHARGOALDESCRIPTOR = 'G';
	
	//Ints for goals
	public static final int INTXYZGOAL = 0;
	public static final int INTGOLDOREGOAL = 1;
	public static final int INTGOLDBARGOAL = 2;
	public static final int INTTOWERGOAL = 3;
	
	
	//-------------MAP SEPARATORS-------------
	public static String 							planeSeparator = "\n~\n";
	public static String 							rowSeparator = "\n";	
	
	//-------------ACTIONS STRINGS-------------
	public static final String						ACTIONMOVE = "moveAction";
	public static final String						ACTIONROTATEC = "rotateClockwise";
	public static final String 						ACTIONROTATECC = "rotateCounterClockwise";
	public static final String 						ACTIONDESTBLOCK = "destroyBlock";
	public static final String						ACTIONJUMP = "jump";
	public static final String						ACTIONPLACEBLOCK = "placeBlock";
	public static final String						ACTIONLOOKUP = "lookup";
	public static final String						ACTIONLOOKDOWN = "lookdown";
	public static final String						ACTIONUSEBLOCK = "useBlock";
	
	//-------------MACRO-ACTIONS STRINGS-------------
	public static final String						MACROACTIONSPRINT = "sprintMacroAction";
	public static final String						MACROACTIONTURNAROUND = "turnAroundMacroAction";
	public static final String						MACROACTIONLOOKDOWNALOT = "lookDownAlotMacroAction";
	public static final String						MACROACTIONLOOKUPALOT = "lookUpAlotMacroAction";
	public static final String						MACROACTIONBUILDTRENCH = "buildTrenchMacroAction";
	public static final String						MACROACTIONJUMPBLOCK = "jumpThenPlaceBlockMacroAction";
	public static final String						MACROACTIONDESTROYWALL = "destroyWallMacroAction";
	public static final String						MACROACTIONDIGDOWN = "digDownMacroAction";
	
	//-------------OPTIONS STRINGS-------------
	public static final String						OPTBUILDTRENCH = "trenchBuildOption";
	public static final String						OPTWALKUNTILCANT = "walkUntilCantOption";
	public static final String						OPTLOOKALLTHEWAYDOWN = "lookAllTheWayDownOption";
	public static final String						OPTDESTROYWALL = "destroyWallOption";
	public static final String						OPTJUMPBLOCK = "jumpThenPlaceBlockOption";
	public static final String						OPTDIGDOWN = "digDownOption";
	
	//-------------PROPOSITIONAL FUNCTION STRINGS-------------
	public static final String				PFATGOAL = "AtGoal";
	public static final String				PFEMPSPACE = "EmptySpace";
	public static final String				PFBLOCKAT = "BlockAt";
	public static final String				PFATLEASTXGOLDORE = "AgentHasXGoldOre";
	public static final String				PFATLEASTXGOLDBAR = "AgentHasXGoldBlock";
	public static final String				PFINDBLOCKINFRONT = "IndBlockFrontOfAgent";
	public static final String				PFENDOFMAPINFRONT = "EndMapFrontOfAgent";
	public static final String				PFEMPTYCELLINFRONT = "TrenchFrontOfAgent";
	public static final String				PFAGENTINMIDAIR = "AgentInAir";
	public static final String				PFAGENTCANWALK = "AgentCanWalk";
	public static final String				PFEMPTYCELLINWALK = "EmptyCellInWalk";
	public static final String				PFTOWER = "TowerInWorld";
	public static final String				PFAGENTLOOKGOLD = "AgentLookingAtGold";
	public static final String				PFFURNACEINFRONT = "FurnaceInFront";
	public static final String				PFAGENTLOOKWALLOBJ = "AgentLookingAtWallObject";
	public static final String				PFHURDLEINFRONTAGENT = "HurdleInFrontOfAgent";
	public static final String				PFAGENTINLAVA = "AgentInLava";
	public static final String				PFLAVAFRONTAGENT = "LavaFrontOfAgent";
	public static final String				PFAGENTLOOKLAVA = "AgentLookAtLava";
	public static final String				PFAGENTLOOKINDBLOCK = "AgentLookAtIndBlock";
	public static final String				PFAGENTLOOKDESTBLOCK = "AgentLookAtDestBlock";
	public static final String				PFAGENTLOOKFURNACE = "AgentLookAtFurnace";
	public static final String				PFAGENTLOOKTOWARDGOAL = "AgentLookingInDirectionOfGoal";
	public static final String				PFAGENTLOOKTOWARDGOLD = "AgentLookingInDirectionOfGold";
	public static final String				PFAGENTLOOKTOWARDFURNACE = "AgentLookingInDirectionOfFurnace";
	public static final String				PFAGENTNOTLOOKTOWARDGOAL = "AgentNotLookingInDirectionOfGoal";
	public static final String				PFAGENTNOTLOOKTOWARDGOLD = "AgentNotLookingInDirectionOfGold";
	public static final String				PFAGENTNOTLOOKTOWARDFURNACE = "AgentNotLookingInDirectionOfFurnace";
	public static final String				PFTRENCHINFRONTAGENT = "TrenchIsInFrontOfAgent";
	public static final String				PFAGENTCANJUMP = "AgentCanJump";
	public static final String 				PFALWAYSTRUE = "True";

	//-----------PLANNERS-------------
	public static final String				RTDP = "RTDP";
	public static final String				ExpertRTDP = "ERTDP";
	public static final String				LearnedHardRTDP = "LHRTDP";
	public static final String				LearnedSoftRTDP = "LSRTDP";
	public static final String				VI = "VI";
	public static final String				ExpertVI = "EVI";
	public static final String				LearnedHardVI = "LHVI";
	public static final String				LearnedSoftVI = "LSVI";

	//----------DIRECTORIES---------
	// Grid changes
//	public static final String				PATHMAPS = "src/minecraft/maps/";
	public static final String				PATHMAPS = "maps/";
//	public static final String				PATHKB = "src/minecraft/kb/";
	public static final String				PATHKB = "kb/";
//	public static final String				PATHTEMPLATEMAP = "src/minecraft/maps/template.map";
	public static final String				PATHTEMPLATEMAP = "maps/template.map";
	public static final String				PATHRESULTS = "src/tests/results/";
	
	//-------------MISC-------------
	public static final String				DOUBLEFORMAT = "%.3f";

	//-------------ENUMS-------------
	public enum RotDirection {
		NORTH(0), EAST(1), SOUTH(2), WEST(3);
		public static final int size = RotDirection.values().length;
		private final int intVal;
		
		RotDirection(int i) {
			this.intVal = i;
		}
		
		public int toInt() {
			return this.intVal;
		}
		
		public static RotDirection fromInt(int i) {
			switch (i) {
			case 0:
				return NORTH;
			case 1:
				return EAST;
			case 2:
				return SOUTH;
			case 3:
				return WEST;
			
			}
			return null;
		}
		
	}
	
	public enum VertDirection {
		AHEAD(3), DOWNONE(2), DOWNTWO(1), DOWNTHREE(0);
		public static final int size = VertDirection.values().length;
		private final int intVal;
		
		VertDirection(int i) {
			this.intVal = i;
		}
		
		public int toInt() {
			return this.intVal;
		}
		
		public static VertDirection fromInt(int i) {
			switch (i) {
			case 3:
				return AHEAD;
			case 2:
				return DOWNONE;
			case 1:
				return DOWNTWO;
			case 0:
				return DOWNTHREE;
			
			}
			return null;
		}
	}
	
	
}
