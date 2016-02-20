package burlap.domain.singleagent.blocksworld;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.*;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.ObjectParameterizedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;
import burlap.oomdp.singleagent.explorer.VisualExplorer;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a domain generator for the classic relational blocks world domain. There exists a single table and any number of blocks that can be stacked
 * on each other. Blocks can be specified to have the color red, green, or blue. Because this is a relational domain, when performing planning,
 * the {@link burlap.oomdp.statehashing.HashableStateFactory} should be object identifier dependent. For example, if using a
 * {@link burlap.oomdp.statehashing.SimpleHashableStateFactory}, in its constructor specify identifierIndependent=false,
 * @author James MacGlashan
 *
 */
public class BlocksWorld implements DomainGenerator {

	/**
	 * Constant for the relational "on" attribute name.
	 */
	public static final String							ATTONBLOCK = "on";
	/**
	 * Constant for the binary "on table" attribute name.
	 */
	public static final String							ATTONTABLE = "onTable";
	
	/**
	 * Constant for the binary "clear" attribute name.
	 */
	public static final String							ATTCLEAR = "clear";
	
	/**
	 * Constant for the categorical "color" attribute name.
	 */
	public static final String							ATTCOLOR = "color";
	
	
	/**
	 * Constant for the color value "red"
	 */
	public static final String							COLORRED = "red";
	
	/**
	 * Constant for the color value "green"
	 */
	public static final String							COLORGREEN = "green";
	
	/**
	 * Constant for the color value "blue"
	 */
	public static final String							COLORBLUE = "blue";
	
	
	/**
	 * Constant for the name of the block class.
	 */
	public static final String							CLASSBLOCK = "block";
	
	/**
	 * Constant for the stack action name
	 */
	public static final String							ACTIONSTACK = "stack";
	
	/**
	 * Constant for the unstack action name
	 */
	public static final String							ACTIONUNSTACK = "unstack";
	
	
	/**
	 * Constant for the propositional function "on" name
	 */
	public static final String							PFONBLOCK = "on";
	
	/**
	 * Constant for the propositional function "on table" name
	 */
	public static final String							PFONTABLE = "onTable";
	
	/**
	 * Constant for the propositional function "clear" name
	 */
	public static final String							PFCLEAR = "clear";
	
	
	//probability of dropping moved block onto table?
	//probability of knocking over stack when placing block on it?
	//move stack action?
	
	
	
	@Override
	public Domain generateDomain() {
		
		Domain domain = new SADomain();
		domain.setObjectIdentiferDependence(true);
		
		List <String> colNames = new ArrayList<String>();
		colNames.add(COLORRED);
		colNames.add(COLORGREEN);
		colNames.add(COLORBLUE);
		
		Attribute attonblock = new Attribute(domain, ATTONBLOCK, Attribute.AttributeType.RELATIONAL);
		
		Attribute attontable = new Attribute(domain, ATTONTABLE, Attribute.AttributeType.BOOLEAN);
		
		Attribute attclear = new Attribute(domain, ATTCLEAR, Attribute.AttributeType.BOOLEAN);
		
		Attribute attcolor = new Attribute(domain, ATTCOLOR, Attribute.AttributeType.DISC);
		attcolor.setDiscValues(colNames);
		
		
		ObjectClass blockClass = new ObjectClass(domain, CLASSBLOCK);
		blockClass.addAttribute(attonblock);
		blockClass.addAttribute(attontable);
		blockClass.addAttribute(attclear);
		blockClass.addAttribute(attcolor);
		
		
		new StackAction(ACTIONSTACK, domain);
		new UnstackAction(ACTIONUNSTACK, domain);
		
		
		new OnBlockPF(PFONBLOCK, domain);
		new OnTablePF(PFONTABLE, domain);
		new ClearPF(PFCLEAR, domain);
		for(String col : colNames){
			new ColorPF(col, domain);
		}
		
		return domain;
	}
	
	
	/**
	 * Creates a new state with nBlocks block objects in it. Values for created objects are unset, so be sure to set them after call thing method.
	 * @param d the blocks world domain
	 * @param nBlocks the number of block objects to create
	 * @return a new state with nBlocks block objects
	 */
	public static State getNewState(Domain d, int nBlocks){
		State s = new MutableState();
		ObjectClass oc = d.getObjectClass(CLASSBLOCK);
		for(int i = 0; i < nBlocks; i++){
			ObjectInstance o = new MutableObjectInstance(oc, CLASSBLOCK+i);
			setBlock(o, "", 1, 1, COLORRED);
			s.addObject(o);
		}
		return s;
	}
	
	
	/**
	 * Use this method to quickly set the color of a block
	 * @param s the state in which the block object exists
	 * @param blockInd the index of the block object whose color value should be set 
	 * @param color the categorical color value (either "red", "green", or "blue")
	 */
	public static void setBlockColor(State s, int blockInd, String color){
		ObjectInstance b = s.getObjectsOfClass(CLASSBLOCK).get(blockInd);
		b.setValue(ATTCOLOR, color);
	}
	
	/**
	 * Use this method to quickly set the various values of a block
	 * @param s the state in which the block object should be set
	 * @param blockInd the index of the block object whose values should be set
	 * @param onBlock the relational on block value
	 * @param onTable the binary on table value
	 * @param clear the binary clear value
	 * @param color the categorical color value (either "red", "green", or "blue")
	 */
	public static void setBlock(State s, int blockInd, String onBlock, int onTable, int clear, String color){
		setBlock(s.getAllObjects().get(blockInd), onBlock, onTable, clear, color);
	}
	
	
	/**
	 * Use this method to quickly set the various values of a block
	 * @param s the state in which the block object should be set
	 * @param blockName the name identifier of the block object whose values are to be set.
	 * @param onBlock the relational on block value
	 * @param onTable the binary on table value
	 * @param clear the binary clear value
	 * @param color the categorical color value (either "red", "green", or "blue")
	 */
	public static void setBlock(State s, String blockName, String onBlock, int onTable, int clear, String color){
		setBlock(s.getObject(blockName), onBlock, onTable, clear, color);
	}
	
	
	/**
	 * Use this method to quickly set the various values of a block
	 * @param block the block object instance to whose values should be set
	 * @param onBlock the relational on block value
	 * @param onTable the binary on table value
	 * @param clear the binary clear value
	 * @param color the categorical color value (either "red", "green", or "blue")
	 */
	public static void setBlock(ObjectInstance block, String onBlock, int onTable, int clear, String color){
		block.setValue(ATTONBLOCK, onBlock);
		block.setValue(ATTONTABLE, onTable);
		block.setValue(ATTCLEAR, clear);
		block.setValue(ATTCOLOR, color);
	}
	
	
	
	/**
	 * Action class for stacking one block onto another. The both blocks must be clear for one to be stacked on the other.
	 * @author James MacGlashan
	 *
	 */
	public class StackAction extends ObjectParameterizedAction implements FullActionModel{

		public StackAction(String name, Domain domain){
			super(name, domain, new String[]{CLASSBLOCK, CLASSBLOCK});
		}
		
		@Override
		public boolean applicableInState(State st, GroundedAction groundedAction){

			String [] params = ((AbstractObjectParameterizedGroundedAction)groundedAction).getObjectParameters();

			//blocks must be clear
			
			ObjectInstance src = st.getObject(params[0]);
			
			if(src.getIntValForAttribute(ATTCLEAR) == 0){
				return false;
			}
			
			ObjectInstance target = st.getObject(params[1]);
			if(target.getIntValForAttribute(ATTCLEAR) == 0){
				return false;
			}
			
			
			return true; 
		}
		
		@Override
		protected State performActionHelper(State st, GroundedAction groundedAction) {

			String [] params = ((AbstractObjectParameterizedGroundedAction)groundedAction).getObjectParameters();

			ObjectInstance src = st.getObject(params[0]);
			ObjectInstance target = st.getObject(params[1]);
			
			String srcOnName = src.getStringValForAttribute(ATTONBLOCK);
			
			src.setValue(ATTONBLOCK, target.getName());
			src.setValue(ATTONTABLE, 0); //may not have been on table to start, but make sure it's not set now
			
			target.setValue(ATTCLEAR, 0);
			
			if(!srcOnName.equals("")){
				ObjectInstance oldTarget = st.getObject(srcOnName);
				oldTarget.setValue(ATTCLEAR, 1);
			}
			
			return st;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction){
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
	public class UnstackAction extends ObjectParameterizedAction implements FullActionModel{
		
		public UnstackAction(String name, Domain domain){
			super(name, domain, new String[]{CLASSBLOCK});
		}
		
		public boolean applicableInState(State st, GroundedAction groundedAction){

			String [] params = ((AbstractObjectParameterizedGroundedAction)groundedAction).getObjectParameters();

			//block must be clear
			
			ObjectInstance src = st.getObject(params[0]);
			
			if(src.getIntValForAttribute(ATTCLEAR) == 0){
				return false;
			}
			if(src.getIntValForAttribute(ATTONTABLE) == 1){
				return false; //cannot unstack a block already on the table
			}
			
			return true; 
		}
		
		@Override
		protected State performActionHelper(State st, GroundedAction groundedAction) {

			String [] params = ((AbstractObjectParameterizedGroundedAction)groundedAction).getObjectParameters();

			ObjectInstance src = st.getObject(params[0]);
			
			String srcOnName = src.getStringValForAttribute(ATTONBLOCK);
			
			src.clearRelationalTargets(ATTONBLOCK);
			src.setValue(ATTONTABLE, 1);
			
			if(!srcOnName.equals("")){
				ObjectInstance oldTarget = st.getObject(srcOnName);
				oldTarget.setValue(ATTCLEAR, 1);
			}
			
			return st;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction){
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
	public class OnBlockPF extends PropositionalFunction{

		public OnBlockPF(String name, Domain domain) {
			super(name, domain, new String[]{CLASSBLOCK,CLASSBLOCK});
		}

		@Override
		public boolean isTrue(State st, String... params) {
			ObjectInstance src = st.getObject(params[0]);
			ObjectInstance target = st.getObject(params[1]);
			if(src.getStringValForAttribute(ATTONBLOCK).equals(target.getName())){
				return true;
			}
			return false;
		}
		
	}
	
	
	/**
	 * Propositional function class for evaluating whether one block is on a table.
	 * @author James MacGlashan
	 *
	 */
	public class OnTablePF extends PropositionalFunction{
		
		public OnTablePF(String name, Domain domain) {
			super(name, domain, new String[]{CLASSBLOCK});
		}

		@Override
		public boolean isTrue(State st, String... params) {
			ObjectInstance src = st.getObject(params[0]);
			if(src.getIntValForAttribute(ATTONTABLE) == 1){
				return true;
			}
			return false;
		}
		
	}
	
	
	/**
	 * Propositional function class for evaluating whether one block is clear (has no block stacked on top of it).
	 * @author James MacGlashan
	 *
	 */
	public class ClearPF extends PropositionalFunction{
		
		public ClearPF(String name, Domain domain) {
			super(name, domain, new String[]{CLASSBLOCK});
		}

		@Override
		public boolean isTrue(State st, String... params) {
			ObjectInstance src = st.getObject(params[0]);
			if(src.getIntValForAttribute(ATTCLEAR) == 1){
				return true;
			}
			return false;
		}
		
	}
	
	
	/**
	 * Propositional function class for evaluating whether a block is a certain color. This
	 * class will be instantiated for each possible color of a block.
	 * @author James MacGlashan
	 *
	 */
	public class ColorPF extends PropositionalFunction{
		
		public ColorPF(String name, Domain domain) {
			super(name, domain, new String[]{CLASSBLOCK});
		}

		@Override
		public boolean isTrue(State st, String... params) {
			ObjectInstance src = st.getObject(params[0]);
			if(src.getStringValForAttribute(ATTCOLOR).equals(this.name)){
				return true;
			}
			return false;
		}
		
	}
	
	
	/**
	 * Main method for exploring the domain. The initial state will have 3 red blocks starting on the table. By default this method will launch the visual explorer.
	 * Pass a "t" argument to use the terminal explorer.
	 * @param args
	 */
	public static void main(String [] args){
		
		BlocksWorld bw = new BlocksWorld();
		Domain domain = bw.generateDomain();
		
		State s = getNewState(domain, 3);
		
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
			
			TerminalExplorer exp = new TerminalExplorer(domain, s);
			
			exp.explore();
			
		}
		else if(expMode == 1){
			VisualExplorer exp = new VisualExplorer(domain, BlocksWorldVisualizer.getVisualizer(24), s);
			
			
			exp.initGUI();
		}
		
	}
	

}
