package household;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.state.State;
import household.state.HouseholdState;

public class HouseholdGoal implements StateConditionTest {

    public HouseholdGoalDescription[] goals = {};

    public HouseholdGoal(HouseholdGoalDescription[] goals) {
	this.goals = goals;
    }

    public HouseholdGoalDescription[] getGoals() {
	return goals;
    }

    public void setGoals(HouseholdGoalDescription[] goals) {
	this.goals = goals;
    }

    @Override
    public boolean satisfies(State s) {
	for(int i = 0; i < goals.length; i++) {
	    GroundedProp gp = new GroundedProp(
					       goals[i].getPf(),
					       goals[i].getParams());
	    if(!gp.isTrue((HouseholdState) s)) {
		return false;
	    }
	}
	return true;
    }

    public String toString() {
	String out = "";
	for(HouseholdGoalDescription desc : goals) {
	    out += desc.toString();
	}
	return out;
    }
}
