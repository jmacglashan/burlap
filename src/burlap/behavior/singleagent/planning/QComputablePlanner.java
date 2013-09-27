package burlap.behavior.singleagent.planning;

import java.util.List;

import burlap.behavior.singleagent.QValue;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


public interface QComputablePlanner {

	public List <QValue> getQs(State s);
	public QValue getQ(State s, GroundedAction a);

}
