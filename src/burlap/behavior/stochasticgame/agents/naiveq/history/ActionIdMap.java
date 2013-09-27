package burlap.behavior.stochasticgame.agents.naiveq.history;

import burlap.oomdp.stocashticgames.GroundedSingleAction;

public interface ActionIdMap {
	public int getActionId(GroundedSingleAction gsa);
	public int getActionId(String actionName, String [] params);
	public int maxValue();
	public GroundedSingleAction getActionForId(int id);
}
