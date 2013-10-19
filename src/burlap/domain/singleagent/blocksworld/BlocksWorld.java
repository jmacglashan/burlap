package burlap.domain.singleagent.blocksworld;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;
import burlap.oomdp.singleagent.explorer.VisualExplorer;

public class BlocksWorld implements DomainGenerator {

	public static final String							ATTONBLOCK = "on";
	public static final String							ATTONTABLE = "onTable";
	public static final String							ATTCLEAR = "clear";
	public static final String							ATTCOLOR = "color";
	
	public static final String							COLORRED = "red";
	public static final String							COLORGREEN = "green";
	public static final String							COLORBLUE = "blue";
	
	public static final String							CLASSBLOCK = "block";
	
	public static final String							ACTIONSTACK = "stack";
	public static final String							ACTIONUNSTACK = "unstack";
	
	public static final String							PFONBLOCK = "on";
	public static final String							PFONTABLE = "onTable";
	public static final String							PFCLEAR = "clear";
	
	
	//probability of dropping moved block onto table?
	//probability of knocking over stack when placing block on it?
	//move stack action?
	
	
	public BlocksWorld(){
		
	}
	
	@Override
	public Domain generateDomain() {
		
		Domain domain = new SADomain();
		
		List <String> colNames = new ArrayList<String>();
		colNames.add(COLORRED);
		colNames.add(COLORGREEN);
		colNames.add(COLORBLUE);
		
		Attribute attonblock = new Attribute(domain, ATTONBLOCK, Attribute.AttributeType.RELATIONAL);
		
		Attribute attontable = new Attribute(domain, ATTONTABLE, Attribute.AttributeType.DISC);
		attontable.setDiscValuesForRange(0, 1, 1); //binary
		
		Attribute attclear = new Attribute(domain, ATTCLEAR, Attribute.AttributeType.DISC);
		attclear.setDiscValuesForRange(0, 1, 1); //binary
		
		Attribute attcolor = new Attribute(domain, ATTCOLOR, Attribute.AttributeType.DISC);
		attcolor.setDiscValues(colNames);
		
		
		ObjectClass blockClass = new ObjectClass(domain, CLASSBLOCK);
		blockClass.addAttribute(attonblock);
		blockClass.addAttribute(attontable);
		blockClass.addAttribute(attclear);
		blockClass.addAttribute(attcolor);
		
		
		Action stack= new StackAction(ACTIONSTACK, domain);
		Action unstack = new UnstackAction(ACTIONUNSTACK, domain);
		
		
		PropositionalFunction onBlockPF = new OnBlockPF(PFONBLOCK, domain);
		PropositionalFunction onTablePF = new OnTablePF(PFONTABLE, domain);
		PropositionalFunction clearPF = new ClearPF(PFCLEAR, domain);
		for(String col : colNames){
			PropositionalFunction cpf = new ColorPF(col, domain);
		}
		
		return domain;
	}
	
	
	public static State getNewState(Domain d, int nBlocks){
		State s = new State();
		ObjectClass oc = d.getObjectClass(CLASSBLOCK);
		for(int i = 0; i < nBlocks; i++){
			ObjectInstance o = new ObjectInstance(oc, CLASSBLOCK+i);
			setBlock(o, "", 1, 1, COLORRED);
			s.addObject(o);
		}
		return s;
	}
	
	
	public static void setBlock(State s, int blockInd, String onBlock, int onTable, int clear, String color){
		setBlock(s.getObservableObjectAt(blockInd), onBlock, onTable, clear, color);
	}
	
	public static void setBlock(State s, String blockName, String onBlock, int onTable, int clear, String color){
		setBlock(s.getObject(blockName), onBlock, onTable, clear, color);
	}
	
	public static void setBlock(ObjectInstance block, String onBlock, int onTable, int clear, String color){
		block.setValue(ATTONBLOCK, onBlock);
		block.setValue(ATTONTABLE, onTable);
		block.setValue(ATTCLEAR, clear);
		block.setValue(ATTCOLOR, color);
	}
	
	
	public class StackAction extends Action{

		public StackAction(String name, Domain domain){
			super(name, domain, new String[]{CLASSBLOCK, CLASSBLOCK});
		}
		
		public boolean applicableInState(State st, String [] params){

			//blocks must be clear
			
			ObjectInstance src = st.getObject(params[0]);
			
			if(src.getDiscValForAttribute(ATTCLEAR) == 0){
				return false;
			}
			
			ObjectInstance target = st.getObject(params[1]);
			if(target.getDiscValForAttribute(ATTCLEAR) == 0){
				return false;
			}
			
			
			return true; 
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
		
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
		
		
	}
	
	public class UnstackAction extends Action{
		
		public UnstackAction(String name, Domain domain){
			super(name, domain, new String[]{CLASSBLOCK});
		}
		
		public boolean applicableInState(State st, String [] params){

			//block must be clear
			
			ObjectInstance src = st.getObject(params[0]);
			
			if(src.getDiscValForAttribute(ATTCLEAR) == 0){
				return false;
			}
			
			return true; 
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
		
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
		
	}
	
	
	public class OnBlockPF extends PropositionalFunction{

		public OnBlockPF(String name, Domain domain) {
			super(name, domain, new String[]{CLASSBLOCK,CLASSBLOCK});
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance src = st.getObject(params[0]);
			ObjectInstance target = st.getObject(params[1]);
			if(src.getStringValForAttribute(ATTONBLOCK).equals(target.getName())){
				return true;
			}
			return false;
		}
		
	}
	
	public class OnTablePF extends PropositionalFunction{
		
		public OnTablePF(String name, Domain domain) {
			super(name, domain, new String[]{CLASSBLOCK});
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance src = st.getObject(params[0]);
			if(src.getDiscValForAttribute(ATTONTABLE) == 1){
				return true;
			}
			return false;
		}
		
	}
	
	public class ClearPF extends PropositionalFunction{
		
		public ClearPF(String name, Domain domain) {
			super(name, domain, new String[]{CLASSBLOCK});
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance src = st.getObject(params[0]);
			if(src.getDiscValForAttribute(ATTCLEAR) == 1){
				return true;
			}
			return false;
		}
		
	}
	
	public class ColorPF extends PropositionalFunction{
		
		public ColorPF(String name, Domain domain) {
			super(name, domain, new String[]{CLASSBLOCK});
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance src = st.getObject(params[0]);
			if(src.getStringValForAttribute(ATTCOLOR).equals(this.name)){
				return true;
			}
			return false;
		}
		
	}
	
	
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
			
			TerminalExplorer exp = new TerminalExplorer(domain);
			exp.addActionShortHand("s", ACTIONSTACK);
			exp.addActionShortHand("u", ACTIONUNSTACK);
			
			exp.exploreFromState(s);
			
		}
		else if(expMode == 1){
			VisualExplorer exp = new VisualExplorer(domain, BlocksWorldVisualizer.getVisualizer(24), s);
			
			
			exp.initGUI();
		}
		
	}
	

}
