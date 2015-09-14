package burlap.oomdp.singleagent.common;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * A {@link burlap.oomdp.singleagent.GroundedAction} implementation for actions that have no parameters.
 * @author James MacGlashan.
 */
public class SimpleGroundedAction extends GroundedAction{

	public SimpleGroundedAction(Action action) {
		super(action);
	}

	@Override
	public void initParamsWithStringRep(String[] params) {
		//do nothing
	}

	@Override
	public String[] getParametersAsString() {
		return new String[0];
	}

	@Override
	public GroundedAction copy() {
		return new SimpleGroundedAction(this.action);
	}
}
