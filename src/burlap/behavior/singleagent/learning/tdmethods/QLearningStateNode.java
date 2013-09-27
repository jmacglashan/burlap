package burlap.behavior.singleagent.learning.tdmethods;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.singleagent.GroundedAction;


public class QLearningStateNode {

	public StateHashTuple			s;
	public List<QValue>				qEntry;
	
	
	public QLearningStateNode(StateHashTuple s) {
		this.s = s;
		qEntry = new ArrayList<QValue>();
	}

	public void addQValue(GroundedAction a, double q){
		QValue qv = new QValue(s.s, a, q);
		qEntry.add(qv);
	}
	
	
}
