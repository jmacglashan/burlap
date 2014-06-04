package burlap.oomdp.logicalexpressions;

import java.util.Map;

import burlap.oomdp.core.State;

public class Negation extends LogicalExpression {

	
	
	@Override
	public LogicalExpression duplicate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean evaluateIn(State s) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void remapVariablesInThisExpression(
			Map<String, String> fromToVariableMap) {
		// TODO Auto-generated method stub

	}

}
