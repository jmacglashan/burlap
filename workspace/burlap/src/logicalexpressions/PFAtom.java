package logicalexpressions;

import java.util.Map;

import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.State;

public class PFAtom extends LogicalExpression {

	protected GroundedProp gp;
	
	public PFAtom(GroundedProp gp){
		this.gp = gp;
		String [] gpParamClasses = this.gp.pf.getParameterClasses();
		for(int i = 0; i < this.gp.params.length; i++){
			String varName = this.gp.params[i];
			String varType = gpParamClasses[i];
			if(!this.variablesAndTypes.containsKey(varName)){
				this.addVariable(varName, varType);
			}
		}
		this.setName(this.gp.pf.getName());
	}
	
	public PFAtom() {
		// Blank constructor for use in parsing from knowledge base.
	}
	
	public GroundedProp getGroundedProp(){
		return this.gp;
	}
	
	public void setGroundedProp(GroundedProp gp){
		this.gp = gp;
	}
	
	@Override
	public LogicalExpression duplicate() {
		GroundedProp ngp = new GroundedProp(this.gp.pf, this.gp.params);
		PFAtom natom = new PFAtom(ngp);
		return natom;
	}

	@Override
	public boolean evaluateIn(State s) {
		return this.gp.isTrue(s);
	}

	@Override
	protected void remapVariablesInThisExpression(Map<String, String> fromToVariableMap) {
		for(int i = 0; i < this.gp.params.length; i++){
			this.gp.params[i] = fromToVariableMap.get(this.gp.params[i]);
		}
	}
	
	public String toString() {
		return this.gp.pf.getClassName();
	}

}
