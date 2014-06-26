package burlap.oomdp.logicalexpressions;

import java.util.Map;

import burlap.oomdp.core.State;

public class Negation extends LogicalExpression {

	public Negation(LogicalExpression childExpression) {
		this.childExpressions.add(childExpression);
	}
	
	
	@Override
	public LogicalExpression duplicate() {
		return new Negation(this.childExpressions.get(0));
	}

	@Override
	public boolean evaluateIn(State s) {
		return (!this.childExpressions.get(0).evaluateIn(s));
	}

	@Override
	protected void remapVariablesInThisExpression(Map<String, String> fromToVariableMap) {
		// Nothing necessary, not an atomic expression
	}

}
