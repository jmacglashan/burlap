package logicalexpressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.State;

public abstract class LogicalExpression {

	protected Map<String, String>			variablesAndTypes = new HashMap<String, String>();
	protected LogicalExpression 			parentExpression = null;
	protected List<LogicalExpression>		childExpressions = new ArrayList<LogicalExpression>();
	protected String						name; // For Debugging purposes
	
	
	/**
	 * Duplicates should not have a parentExpression
	 * @return
	 */
	public abstract LogicalExpression duplicate();
	public abstract boolean evaluateIn(State s);
	protected abstract void remapVariablesInThisExpression(Map<String, String> fromToVariableMap);
	
	
	public LogicalExpression duplicateWithVariableRemap(Map<String, String> fromToVariableMap){
		LogicalExpression copy = this.duplicate();
		copy.remapVariables(fromToVariableMap);
		return copy;
	}
	
	public Map<String, String> getVariableAndTypes(){
		return this.variablesAndTypes;
	}
	
	public LogicalExpression getParentExpression(){
		return this.parentExpression;
	}
	
	public void setParentExpression(LogicalExpression parentExpression){
		this.parentExpression = parentExpression;
		for(Map.Entry<String, String> vt : this.variablesAndTypes.entrySet()){
			this.parentExpression.addVariable(vt.getKey(), vt.getValue());
		}
	}
	
	public void addVariable(String variableName, String variableType){
		
		if(this.variablesAndTypes.containsKey(variableName)){
			throw new VariableAlreadyInUseException(variableName);
		}
		
		this.variablesAndTypes.put(variableName, variableType);
		if(this.parentExpression != null){
			this.parentExpression.addVariable(variableName, variableType);
		}
	}
	
	
	public void remapVariables(Map<String, String> fromToVariableMap){
		this.remapVariablesInVariableAndTypeMap(fromToVariableMap);
		this.remapVariablesInThisExpression(fromToVariableMap);
		this.remapVariablesUpStream(fromToVariableMap);
		this.remapVariablesDownStream(fromToVariableMap);
	}
	
	
	protected void remapVariablesInVariableAndTypeMap(Map<String, String> fromToVariableMap){
		//to protect against variable mapping name swaps create a copy
		Map<String, String> newVT = new HashMap<String, String>();
		for(Map.Entry<String, String> nn : fromToVariableMap.entrySet()){
			String from = nn.getKey();
			String to = nn.getValue();
			String type = this.variablesAndTypes.get(from);
			newVT.put(to, type);
		}
		
		for(Map.Entry<String, String> on : this.variablesAndTypes.entrySet()){
			String oldName = on.getKey();
			if(!fromToVariableMap.containsKey(oldName)){
				newVT.put(oldName, on.getValue());
			}
		}
		
		this.variablesAndTypes = newVT;
		
	}
	
	
	protected void remapVariablesDownStream(Map<String, String> fromToVariableMap){
		for(LogicalExpression exp : this.childExpressions){
			exp.remapVariablesInVariableAndTypeMap(fromToVariableMap);
			exp.remapVariablesInThisExpression(fromToVariableMap);
			exp.remapVariablesDownStream(fromToVariableMap);
		}
	}
	
	protected void remapVariablesUpStream(Map<String, String> fromToVariableMap){
		if(this.parentExpression != null){
			this.parentExpression.remapVariablesInVariableAndTypeMap(fromToVariableMap);
			this.parentExpression.remapVariablesInThisExpression(fromToVariableMap);
			this.parentExpression.remapVariablesUpStream(fromToVariableMap);
		}
	}
	
	
	
	public class VariableAlreadyInUseException extends RuntimeException{

		private static final long serialVersionUID = 4641273304404441272L;
		public final String variableName;
		
		public VariableAlreadyInUseException(String variableName){
			super("The variable name " + variableName + "is already in use in this expression");
			this.variableName = variableName;
		}
		
	}
	
	
	// For debugging purposes
	public void setName(String name) {
		this.name = name;
	}
	
	// For debugging purposes
	public String toString() {
		String result = "";
		for(LogicalExpression child : this.childExpressions) {
			result += child.toString();
		}
		return result;
	}
	
	public void load(String logicalExpressionString) {
		
	}
	
}
