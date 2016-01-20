package burlap.behavior.singleagent.planning;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.QValue;
import burlap.domain.singleagent.minecraft.Affordance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * An interface for planning classes that compute Q-values.
 * @author James MacGlashan
 *
 */
public interface QComputablePlanner {

	public List <QValue> getQs(State s);
	public QValue getQ(State s, GroundedAction a);
	public List<QValue> getAffordanceQs(State s, ArrayList<Affordance> kb);


}
