package minecraft.MinecraftDomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import minecraft.MapIO;
import minecraft.NameSpace;
import minecraft.MinecraftDomain.Actions.DestroyBlockAction;
import minecraft.MinecraftDomain.Actions.JumpAction;
import minecraft.MinecraftDomain.Actions.MovementAction;
import minecraft.MinecraftDomain.Actions.PlaceBlockAction;
import minecraft.MinecraftDomain.Actions.RotateAction;
import minecraft.MinecraftDomain.Actions.RotateVertAction;
import minecraft.MinecraftDomain.Actions.StochasticAgentAction;
import minecraft.MinecraftDomain.Actions.UseBlockAction;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentCanJumpPF;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentHasAtLeastXGoldBarPF;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentHasAtLeastXGoldOrePF;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentInLavaPF;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentInMidAirPF;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentCanWalkPF;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentLookingAtBlockPF;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentLookingInDirectionOfBlock;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentNotLookingInDirectionOfBlock;
import minecraft.MinecraftDomain.PropositionalFunctions.AlwaysTruePF;
import minecraft.MinecraftDomain.PropositionalFunctions.AtGoalPF;
import minecraft.MinecraftDomain.PropositionalFunctions.HurdleInFrontOfAgent;
import minecraft.MinecraftDomain.PropositionalFunctions.BlockInFrontOfAgentPF;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentWalkIntoEndOfMapPF;
import minecraft.MinecraftDomain.PropositionalFunctions.TrenchInFrontOfAgent;
import minecraft.MinecraftDomain.PropositionalFunctions.AgentLookingAtWallPF;
import minecraft.MinecraftDomain.PropositionalFunctions.TowerInMapPF;
import minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing.AgentAdjacentToTrenchPF;
import minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing.BlockAtPF;
import minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing.EmptyCellInAgentWalkDir;
import minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing.EmptyCellInFrontOfAgentPF;
import minecraft.MinecraftDomain.PropositionalFunctions.ThisIsDumbDontLookAtThisThing.EmptySpacePF;
import minecraft.MinecraftStateGenerator.MinecraftStateGenerator;
import minecraft.MinecraftStateGenerator.Exceptions.StateCreationException;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;

/**
 * Class to implement minecraft in the burlap domain
 * @author Dhershkowitz
 *
 */
public class MinecraftDomainGenerator implements DomainGenerator{
	//------------CLASS VARIABLES------------
	/**
	 * The rows of the minecraft world
	 */
	public int rows;
	
	/**
	 * The cols of minecraft world
	 */
	public int cols;
	
	/**
	 * The height of the minecraft world
	 */
	public int height;
	
	/**
	 * Mapping of string keys to int values for things like the number of blocks that the agent 
	 * has (ultimately derived from the first line of the ascii file).
	 */
	private HashMap<String, Integer> headerInfo;
	
	/**
	 * boolean indicating whether or not the domain's actions should be stochastic or deterministic
	 */
	private boolean stochasticActions = false;
	
	//------------CONSTRUCTOR------------
	/**
	 * Constructs a burlap domain for minecraft given a map and a hashmap of header information
	 * @param mapAsCharArray the map of the world where each block is a character in row-col-height major order
	 * @param headerInfo a hashmap of other relevant information (from string to int values). E.g. number of blocks agent has.
	 */
	public MinecraftDomainGenerator(char [][][] mapAsCharArray, HashMap<String, Integer> headerInfo){
		this.rows = mapAsCharArray.length;
		this.cols = mapAsCharArray[0].length;
		this.height = mapAsCharArray[0][0].length;
		this.headerInfo = headerInfo;
	}

	//------------DOMAIN GENERATION------------
	/** 
	 * @param object burlap object to add attributes to
	 * @param xAtt the x position attribute
	 * @param yAtt the y position attribute
	 * @param zAtt the z position attribute
	 * @param collAtt the x position attribute
	 * @param shouldCollide a boolean of if the spatial object should collide with the agent
	 */
	private void addSpatialAttributes(ObjectClass object, Attribute xAt, Attribute yAt, Attribute zAt, Attribute collAt, Attribute floatsAt, Attribute destroyWhenWalkedAt, Attribute destAt) {
		object.addAttribute(xAt);
		object.addAttribute(yAt);
		object.addAttribute(zAt);
		object.addAttribute(collAt);
		object.addAttribute(floatsAt);
		object.addAttribute(destroyWhenWalkedAt);
		object.addAttribute(destAt);
		
	}
	
	

	@Override
	public Domain generateDomain() {
		
		Domain domain = new SADomain();
		
		//ATTRIBUTES
		
		//x Position Attribute
		Attribute xAtt = new Attribute(domain, NameSpace.ATX, Attribute.AttributeType.INT);
		
		//y Position Attribute
		Attribute yAtt = new Attribute(domain, NameSpace.ATY, Attribute.AttributeType.INT);
		
		//z Position Attribute
		Attribute zAtt = new Attribute(domain, NameSpace.ATZ, Attribute.AttributeType.INT);

		//Rotational direction for agent
		Attribute rotDirAt = new Attribute(domain, NameSpace.ATROTDIR, Attribute.AttributeType.INT);
		
		//Agent's vertical direction attribute
		Attribute vertDirAt = new Attribute(domain, NameSpace.ATVERTDIR, Attribute.AttributeType.INT);
		
		//Collidable attribute
		Attribute collAt = new Attribute(domain, NameSpace.ATCOLLIDES, Attribute.AttributeType.INT);
		
		//Destroyable attribute
		Attribute destAt = new Attribute(domain, NameSpace.ATDEST, Attribute.AttributeType.INT);
		
		//Floats attribute
		Attribute floatsAt = new Attribute(domain, NameSpace.ATFLOATS, Attribute.AttributeType.INT);
		
		//Placeable blocks attribute
		Attribute blocksToPlaceAt = new Attribute(domain, NameSpace.ATPLACEBLOCKS, Attribute.AttributeType.INT);
		
		//Destroyed by agent when walked on
		Attribute destroyWhenWalkedAt = new Attribute(domain, NameSpace.ATDESTWHENWALKED, Attribute.AttributeType.INT);
		
		//Amount of gold ore of agent attribute
		Attribute amountOfGoldOre = new Attribute(domain, NameSpace.ATAMTGOLDORE, Attribute.AttributeType.INT);
		
		//Amount of gold bars of agent attribute
		Attribute amountOfGoldBar = new Attribute(domain, NameSpace.ATAMTGOLDBAR, Attribute.AttributeType.INT);
		
		//Trench vector, indicating trench direction and length
		Attribute trenchVector = new Attribute(domain, NameSpace.ATTRENCHVECTOR, Attribute.AttributeType.INTARRAY);
		trenchVector.setLims(0, Math.max(this.cols, Math.max(this.rows, this.height)));
		
		
		//BURLAP OBJECT CLASSES
		
		//Burlap object for the agent
		ObjectClass agentClass = new ObjectClass(domain, NameSpace.CLASSAGENT);
		addSpatialAttributes(agentClass, xAtt, yAtt, zAtt, collAt, floatsAt, destroyWhenWalkedAt, destAt);
		agentClass.addAttribute(rotDirAt);
		agentClass.addAttribute(vertDirAt);
		agentClass.addAttribute(blocksToPlaceAt);
		agentClass.addAttribute(amountOfGoldOre);
		agentClass.addAttribute(amountOfGoldBar);
		
		//Burlap object for agent's feet
		ObjectClass agentFeetClass = new ObjectClass(domain, NameSpace.CLASSAGENTFEET);
		addSpatialAttributes(agentFeetClass, xAtt, yAtt, zAtt, collAt, floatsAt, destroyWhenWalkedAt, destAt);
		
		//Burlap object for xyz goal
		ObjectClass goalClass = new ObjectClass(domain, NameSpace.CLASSGOAL);
		addSpatialAttributes(goalClass, xAtt, yAtt, zAtt, collAt, floatsAt, destroyWhenWalkedAt, destAt);

		//Burlap object for pickupable dirt blocks
		ObjectClass pickupableDirtBlock = new ObjectClass(domain, NameSpace.CLASSDIRTBLOCKPICKUPABLE);
		addSpatialAttributes(pickupableDirtBlock, xAtt, yAtt, zAtt, collAt, floatsAt, destroyWhenWalkedAt, destAt);
		
		//Burlap object for not pickupable dirt blocks
		ObjectClass notPickupableDirtBloc = new ObjectClass(domain, NameSpace.CLASSDIRTBLOCKNOTPICKUPABLE);
		addSpatialAttributes(notPickupableDirtBloc, xAtt, yAtt, zAtt, collAt, floatsAt, destroyWhenWalkedAt, destAt);
		
		//Burlap object for gold blocks
		ObjectClass goldBlockClass = new ObjectClass(domain, NameSpace.CLASSGOLDBLOCK);
		addSpatialAttributes(goldBlockClass, xAtt, yAtt, zAtt, collAt, floatsAt, destroyWhenWalkedAt, destAt);
		
		//Burlap object for indestructable walls
		ObjectClass indWallClass = new ObjectClass(domain, NameSpace.CLASSINDWALL);
		addSpatialAttributes(indWallClass, xAtt, yAtt, zAtt, collAt, floatsAt, destroyWhenWalkedAt, destAt);
		
		//Burlap object for furnace
		ObjectClass furnaceClass = new ObjectClass(domain, NameSpace.CLASSFURNACE);
		addSpatialAttributes(furnaceClass, xAtt, yAtt, zAtt, collAt, floatsAt, destroyWhenWalkedAt, destAt);
		
		//Burlap object for lava
		ObjectClass lavaClass = new ObjectClass(domain, NameSpace.CLASSLAVA);
		addSpatialAttributes(lavaClass, xAtt, yAtt, zAtt, collAt, floatsAt, destroyWhenWalkedAt, destAt);
		
		//Burlap object for Trench (high level)
		ObjectClass trenchClass = new ObjectClass(domain, NameSpace.CLASSTRENCH);
		trenchClass.addAttribute(xAtt);
		trenchClass.addAttribute(yAtt);
		trenchClass.addAttribute(zAtt);
		trenchClass.addAttribute(trenchVector);
		
		//ACTIONS
		
		//Set up actions
		StochasticAgentAction move = new MovementAction(NameSpace.ACTIONMOVE, domain, rows, cols, height);
		
		StochasticAgentAction turnRight = new RotateAction(NameSpace.ACTIONROTATEC, domain, 1, rows, cols, height);
		StochasticAgentAction turnLeft = new RotateAction(NameSpace.ACTIONROTATECC, domain, NameSpace.RotDirection.size-1, rows, cols, height); 
		StochasticAgentAction lookDown = new RotateVertAction(NameSpace.ACTIONLOOKDOWN, domain, rows, cols, height, -1);
		StochasticAgentAction lookUp = new RotateVertAction(NameSpace.ACTIONLOOKUP, domain, rows, cols, height, 1);

		new DestroyBlockAction(NameSpace.ACTIONDESTBLOCK, domain, rows, cols, height);
		new JumpAction(NameSpace.ACTIONJUMP, domain, rows, cols, height, 1);
		new PlaceBlockAction(NameSpace.ACTIONPLACEBLOCK, domain, rows, cols, height);
		new UseBlockAction(NameSpace.ACTIONUSEBLOCK, domain, rows, cols, height);
		
		//Set up non-determinism
		List<StochasticAgentAction> actions = new ArrayList<StochasticAgentAction>();
		actions.add(move);
		actions.add(turnRight);
		actions.add(turnLeft);
		actions.add(lookDown);
		actions.add(lookUp);
		
		if(stochasticActions) { 
			// Stochastic
			move.addResultingActionsWithWeights(actions, new double[]{0.95, 0.025, 0.025, 0, 0});
			turnRight.addResultingActionsWithWeights(actions, new double[]{0.025, .95, 0.025, 0, 0});
			turnLeft.addResultingActionsWithWeights(actions, new double[]{0.025, 0.025, 0.95, 0, 0});
		}
		else {
			// Deterministic
			move.addResultingActionsWithWeights(actions, new double[]{1, 0 , 0, 0, 0});
			turnRight.addResultingActionsWithWeights(actions, new double[]{0, 1 , 0, 0, 0});
			turnLeft.addResultingActionsWithWeights(actions, new double[]{0, 0 , 1, 0, 0});

		}

		
		lookDown.addResultingActionsWithWeights(actions, new double[]{0, 0, 0, 1, 0});
		lookUp.addResultingActionsWithWeights(actions, new double[]{0, 0, 0, 0, 1});
		
		//PROPOSITIONAL FUNCTIONS
		
		//Set up propositional functions
		new AtGoalPF(NameSpace.PFATGOAL, domain, new String[]{NameSpace.CLASSAGENT, NameSpace.CLASSGOAL});
		new EmptySpacePF(NameSpace.PFEMPSPACE, domain, new String[]{}, 0, 0, 0);
		new BlockAtPF(NameSpace.PFBLOCKAT, domain, new String[]{}, 0, 0, 0);
		new AgentHasAtLeastXGoldOrePF(NameSpace.PFATLEASTXGOLDORE, domain, new String[]{NameSpace.CLASSAGENT}, 1);
		new AgentHasAtLeastXGoldBarPF(NameSpace.PFATLEASTXGOLDBAR, domain, new String[]{NameSpace.CLASSAGENT}, 1);
		new BlockInFrontOfAgentPF(NameSpace.PFINDBLOCKINFRONT, domain, new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSINDWALL);
		new AgentWalkIntoEndOfMapPF(NameSpace.PFENDOFMAPINFRONT, domain, new String[]{NameSpace.CLASSAGENT}, rows, cols, height);
		new EmptyCellInFrontOfAgentPF(NameSpace.PFEMPTYCELLINFRONT, domain, new String[]{NameSpace.CLASSAGENT}, rows, cols, height);
		new AgentInMidAirPF(NameSpace.PFAGENTINMIDAIR, domain, new String[]{NameSpace.CLASSAGENT}, rows, cols, height);
		new TowerInMapPF(NameSpace.PFTOWER, domain, new String[]{NameSpace.CLASSAGENT}, 2, NameSpace.CHARDIRTBLOCKNOTPICKUPABLE, rows, cols, height);
		new AgentInLavaPF(NameSpace.PFAGENTINLAVA, domain, new String[]{NameSpace.CLASSAGENT});
		
		new AgentLookingInDirectionOfBlock(NameSpace.PFAGENTLOOKTOWARDGOAL, domain, new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSGOAL);
		new AgentLookingInDirectionOfBlock(NameSpace.PFAGENTLOOKTOWARDGOLD, domain, new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSGOLDBLOCK);
		new AgentLookingInDirectionOfBlock(NameSpace.PFAGENTLOOKTOWARDFURNACE, domain, new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSFURNACE);
		new AgentNotLookingInDirectionOfBlock(NameSpace.PFAGENTNOTLOOKTOWARDGOAL, domain, new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSGOAL);
		new AgentNotLookingInDirectionOfBlock(NameSpace.PFAGENTNOTLOOKTOWARDGOLD, domain, new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSGOLDBLOCK);
		new AgentNotLookingInDirectionOfBlock(NameSpace.PFAGENTNOTLOOKTOWARDFURNACE, domain, new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSFURNACE);
		
		new AgentCanJumpPF(NameSpace.PFAGENTCANJUMP, domain, new String[]{NameSpace.CLASSAGENT}, rows, cols, height);
		new TrenchInFrontOfAgent(NameSpace.PFTRENCHINFRONTAGENT, domain, new String[]{NameSpace.CLASSAGENT}, rows, cols, height);
		new AlwaysTruePF(NameSpace.PFALWAYSTRUE, domain, new String[]{NameSpace.CLASSAGENT});
		
		// Dave's jenky hard coded prop funcs
//		new AgentAdjacentToTrenchPF(NameSpace.PFAGENTADJTRENCH, domain, new String[]{NameSpace.CLASSAGENT, NameSpace.CLASSTRENCH});
		new AgentCanWalkPF(NameSpace.PFAGENTCANWALK, domain, new String[]{NameSpace.CLASSAGENT});
		new EmptyCellInAgentWalkDir(NameSpace.PFEMPTYCELLINWALK, domain, new String[]{NameSpace.CLASSAGENT});
		new HurdleInFrontOfAgent(NameSpace.PFHURDLEINFRONTAGENT, domain, new String[]{NameSpace.CLASSAGENT}, rows, cols, height);
		new AgentLookingAtBlockPF(NameSpace.PFAGENTLOOKLAVA, domain, new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSLAVA);
		new AgentLookingAtBlockPF(NameSpace.PFAGENTLOOKGOLD, domain, new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSGOLDBLOCK);
		new AgentLookingAtBlockPF(NameSpace.PFAGENTLOOKINDBLOCK, domain,  new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSINDWALL);
		new AgentLookingAtBlockPF(NameSpace.PFAGENTLOOKDESTBLOCK, domain,  new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSDIRTBLOCKNOTPICKUPABLE);
		new BlockInFrontOfAgentPF(NameSpace.PFLAVAFRONTAGENT, domain,  new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSLAVA);
		new BlockInFrontOfAgentPF(NameSpace.PFFURNACEINFRONT, domain,  new String[]{NameSpace.CLASSAGENT}, NameSpace.CLASSFURNACE);
		new AgentLookingAtWallPF(NameSpace.PFAGENTLOOKWALLOBJ, domain, new String[]{NameSpace.CLASSAGENT});
		return domain;
	}
	
	public static void main(String[] args) {
		String filePath = "src/minecraft/maps/planeMaps/plane1.map";
		MapIO io = new MapIO(filePath);
		
		char[][][] charMap = io.getMapAs3DCharArray();
		HashMap<String, Integer> headerInfo = io.getHeaderHashMap();
		
		DomainGenerator dg = new MinecraftDomainGenerator(charMap, headerInfo);
		Domain d = dg.generateDomain();
		State state = null;
		
		try {
			state = MinecraftStateGenerator.createInitialState(charMap, headerInfo, d);
		} catch (StateCreationException e) {
			e.printStackTrace();
		}
		
		TerminalExplorer exp = new TerminalExplorer(d);
		exp.addActionShortHand("j", NameSpace.ACTIONJUMP);
		exp.addActionShortHand("w", NameSpace.ACTIONMOVE);
		exp.addActionShortHand("rc", NameSpace.ACTIONROTATEC);
		exp.addActionShortHand("ld", NameSpace.ACTIONLOOKDOWN);
		exp.addActionShortHand("lu", NameSpace.ACTIONLOOKUP);
		exp.addActionShortHand("d", NameSpace.ACTIONDESTBLOCK);
		exp.addActionShortHand("u", NameSpace.ACTIONUSEBLOCK);
		exp.addActionShortHand("p", NameSpace.ACTIONPLACEBLOCK);
		
		exp.exploreFromState(state);
	}
}
