package burlap.domain.singleagent.blocksworld;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.explorer.VisualExplorer;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import burlap.shell.EnvironmentShell;

import java.awt.*;
import java.util.List;

/**
 * This is a domain generator for the classic relational blocks world domain. There exists a single table and any number of blocks that can be stacked
 * on each other. Blocks can be specified to have the color red, green, or blue. Because this is a relational domain, when performing planning,
 * the {@link burlap.mdp.statehashing.HashableStateFactory} should be object identifier dependent. For example, if using a
 * {@link burlap.mdp.statehashing.SimpleHashableStateFactory}, in its constructor specify identifierIndependent=false,
 * @author James MacGlashan
 *
 */
public class BlocksWorld implements DomainGenerator {

	/**
	 * Constant for the relational "on" variable key.
	 */
	public static final String VAR_ON = "on";

	/**
	 * Constant for the binary "clear" variable key.
	 */
	public static final String VAR_CLEAR = "clear";
	
	/**
	 * Constant for the categorical "color" variable key.
	 */
	public static final String VAR_COLOR = "color";


	/**
	 * Value for being on the table
	 */
	public static final String TABLE_VAL = "__table__";
	

	
	/**
	 * Constant for the name of the block class.
	 */
	public static final String CLASS_BLOCK = "block";
	
	/**
	 * Constant for the stack action name
	 */
	public static final String ACTION_STACK = "stack";
	
	/**
	 * Constant for the unstack action name
	 */
	public static final String ACTION_UNSTACK = "unstack";
	
	
	/**
	 * Constant for the propositional function "on" name
	 */
	public static final String PF_ON_BLOCK = "on";
	
	/**
	 * Constant for the propositional function "on table" name
	 */
	public static final String PF_ON_TABLE = "onTable";
	
	/**
	 * Constant for the propositional function "clear" name
	 */
	public static final String PF_CLEAR = "clear";


	protected NamedColor[]  colors = new NamedColor[]{new NamedColor("red", Color.red), new NamedColor("blue", Color.blue), new NamedColor("green", Color.green)};
	
	@Override
	public Domain generateDomain() {

		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(CLASS_BLOCK, BlocksWorldBlock.class);
		
		
		new StackActionType(ACTION_STACK, domain);
		new UnstackActionType(ACTION_UNSTACK, domain);
		
		
		new OnBlockPF(PF_ON_BLOCK, domain);
		new OnTablePF(PF_ON_TABLE, domain);
		new ClearPF(PF_CLEAR, domain);
		for(NamedColor col : colors){
			new ColorPF(col, domain);
		}
		
		return domain;
	}

	public void setColorsForPFs(NamedColor...colors){
		this.colors = colors;
	}
	
	/**
	 * Creates a new state with nBlocks block objects in it.
	 * @param nBlocks the number of block objects to create
	 * @return a new state with nBlocks block objects
	 */
	public static State getNewState(int nBlocks){
		BlocksWorldState s = new BlocksWorldState();
		for(int i = 0; i < nBlocks; i++){
			s.addObject(new BlocksWorldBlock("block" + i));
		}
		return s;
	}

	
	
	/**
	 * Action class for stacking one block onto another. The both blocks must be clear for one to be stacked on the other.
	 * @author James MacGlashan
	 *
	 */
	public class StackActionType extends ObjectParameterizedActionType implements FullActionModel{

		public StackActionType(String name, Domain domain){
			super(name, domain, new String[]{CLASS_BLOCK, CLASS_BLOCK});
		}
		
		@Override
		public boolean applicableInState(State st, GroundedAction groundedAction){

			String [] params = ((SAObjectParameterizedAction)groundedAction).getObjectParameters();

			//block must be clear
			BlocksWorldState s = (BlocksWorldState)st;
			BlocksWorldBlock src = (BlocksWorldBlock)s.object(params[0]);
			BlocksWorldBlock target = (BlocksWorldBlock)s.object(params[1]);
			
			if(!src.clear || !target.clear){
				return false;
			}
			
			
			return true; 
		}
		
		@Override
		protected State sampleHelper(State st, GroundedAction groundedAction) {

			String [] params = ((SAObjectParameterizedAction)groundedAction).getObjectParameters();

			BlocksWorldState s = (BlocksWorldState)st;
			BlocksWorldBlock src = (BlocksWorldBlock)s.object(params[0]);
			BlocksWorldBlock target = (BlocksWorldBlock)s.object(params[1]);
			
			String srcOnName = src.on;

			BlocksWorldBlock nsrc = src.copy();
			nsrc.on = target.name;
			BlocksWorldBlock ntarget = target.copy();
			target.clear = false;

			s.addObject(nsrc).addObject(ntarget);
			
			if(!srcOnName.equals(TABLE_VAL)){
				BlocksWorldBlock oldTarget = (BlocksWorldBlock)s.object(srcOnName).copy();
				oldTarget.clear = true;
				s.addObject(oldTarget);
			}
			
			return st;
		}

		@Override
		public List<TransitionProbability> transitions(State s, GroundedAction groundedAction){
			return this.deterministicTransition(s, groundedAction);
		}

		@Override
		public boolean parametersAreObjectIdentifierIndependent() {
			return false;
		}

		@Override
		public boolean isPrimitive() {
			return true;
		}

	}
	
	
	/**
	 * Action class for unstacking a block off of another block and putting it on the table. The block to be unstacked
	 * must be clear and must be on another block to be unstacked.
	 * @author James MacGlashan
	 *
	 */
	public class UnstackActionType extends ObjectParameterizedActionType implements FullActionModel{
		
		public UnstackActionType(String name, Domain domain){
			super(name, domain, new String[]{CLASS_BLOCK});
		}
		
		public boolean applicableInState(State st, GroundedAction groundedAction){

			String [] params = ((SAObjectParameterizedAction)groundedAction).getObjectParameters();

			//block must be clear
			BlocksWorldState s = (BlocksWorldState)st;
			BlocksWorldBlock src = (BlocksWorldBlock)s.object(params[0]);

			if(src.clear || src.onTable()){
				return false;
			}

			
			return true; 
		}
		
		@Override
		protected State sampleHelper(State st, GroundedAction groundedAction) {

			String [] params = ((SAObjectParameterizedAction)groundedAction).getObjectParameters();

			BlocksWorldState s = (BlocksWorldState)st;
			BlocksWorldBlock src = (BlocksWorldBlock)s.object(params[0]);
			
			String srcOnName = src.on;

			s.set(src.name() + ":" + VAR_ON, TABLE_VAL);
			if(!srcOnName.equals(TABLE_VAL)) {
				s.set(srcOnName + ":" + VAR_CLEAR, true);
			}

			
			return st;
		}

		@Override
		public List<TransitionProbability> transitions(State s, GroundedAction groundedAction){
			return this.deterministicTransition(s, groundedAction);
		}

		@Override
		public boolean parametersAreObjectIdentifierIndependent() {
			return false;
		}

		@Override
		public boolean isPrimitive() {
			return true;
		}
	}
	
	
	
	/**
	 * Propositional function class for evaluating whether one block is on another.
	 * @author James MacGlashan
	 *
	 */
	public class OnBlockPF extends PropositionalFunction {

		public OnBlockPF(String name, OODomain domain) {
			super(name, domain, new String[]{CLASS_BLOCK, CLASS_BLOCK});
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			BlocksWorldState s = (BlocksWorldState)st;
			BlocksWorldBlock src = (BlocksWorldBlock)s.object(params[0]);
			BlocksWorldBlock target = (BlocksWorldBlock)s.object(params[1]);
			return src.on.equals(target.name());

		}
		
	}
	
	
	/**
	 * Propositional function class for evaluating whether one block is on a table.
	 * @author James MacGlashan
	 *
	 */
	public class OnTablePF extends PropositionalFunction{
		
		public OnTablePF(String name, OODomain domain) {
			super(name, domain, new String[]{CLASS_BLOCK});
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			BlocksWorldState s = (BlocksWorldState)st;
			BlocksWorldBlock src = (BlocksWorldBlock)s.object(params[0]);
			return src.onTable();
		}
		
	}
	
	
	/**
	 * Propositional function class for evaluating whether one block is clear (has no block stacked on top of it).
	 * @author James MacGlashan
	 *
	 */
	public class ClearPF extends PropositionalFunction{
		
		public ClearPF(String name, OODomain domain) {
			super(name, domain, new String[]{CLASS_BLOCK});
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			BlocksWorldState s = (BlocksWorldState)st;
			BlocksWorldBlock src = (BlocksWorldBlock)s.object(params[0]);
			return src.clear;
		}
		
	}
	
	
	/**
	 * Propositional function class for evaluating whether a block is a certain color. This
	 * class will be instantiated for each possible color of a block.
	 * @author James MacGlashan
	 *
	 */
	public class ColorPF extends PropositionalFunction{

		protected NamedColor color;

		public ColorPF(NamedColor color, OODomain domain) {
			super(color.name, domain, new String[]{CLASS_BLOCK});
			this.color = color;
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			BlocksWorldState s = (BlocksWorldState)st;
			BlocksWorldBlock src = (BlocksWorldBlock)s.object(params[0]);
			return color.equals(src.color);
		}
		
	}

	public static class NamedColor{
		public String name;
		public Color col;

		public NamedColor() {
		}

		public NamedColor(String name, Color col) {
			this.name = name;
			this.col = col;
		}
	}
	
	
	/**
	 * Main method for exploring the domain. The initial state will have 3 red blocks starting on the table. By default this method will launch the visual explorer.
	 * Pass a "t" argument to use the terminal explorer.
	 * @param args process arguments
	 */
	public static void main(String [] args){
		
		BlocksWorld bw = new BlocksWorld();
		Domain domain = bw.generateDomain();
		
		State s = getNewState(3);
		
		int expMode = 1;
		if(args.length > 0){
			if(args[0].equals("v")){
				expMode = 1;
			}
			else if(args[0].equals("t")){
				expMode = 0;
			}
		}
		
		
		if(expMode == 0){

			EnvironmentShell shell = new EnvironmentShell(domain, s);
			shell.start();
			
		}
		else if(expMode == 1){
			VisualExplorer exp = new VisualExplorer(domain, BlocksWorldVisualizer.getVisualizer(24), s);
			
			
			exp.initGUI();
		}
		
	}
	

}
