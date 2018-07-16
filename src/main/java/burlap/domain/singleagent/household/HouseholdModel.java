package Household;


public class HouseholdModel implements FullStateModel {

    public double[][] transitionProbs;

    @Override
    public State sample(State s, Action a) {
	List<StateTransitionProb> stpList = this.stateTransitions(s, a);
	double roll = RandomFactory.getMapped(0).nextDouble();
	double cSum = 0.;
	for(int i = 0; i < stpList.size(); i++) {
	    cSum += stpList.get(i).p;
	    if(roll < cSum) {
		return stpList.get(i).s;
	    }
	}
	throw new RuntimeException("Probabilities don't sum to 1.0:" + cSum);
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
	List<StateTransitionProb> tps = new ArrayList<StateTransitionProb>();
	int action = actionInd(a);
	HouseholdState hS = (Householdstate) s;

	// actions

	return tps;
    }

    public int actionInd(action a) {
	String aname = a.actionName();
	if(aName.startsWith(Household.ACTION_NORTH))
	    return Household.IND_NORTH;
	else if(aName.startsWith(Household.ACTION_EAST))
	    return Household.IND_EAST;
	else if(aName.startsWith(Household.ACTION_SOUTH))
	    return Household.IND_SOUTH;
	else if(aName.startsWith(Household.ACTION_WEST))
	    return Household.IND_WEST;
	else if(aName.startsWith(Household.ACTION_SPEAK))
	    return Household.IND_SPEAK;
	else if(aName.startsWith(Household.ACTION_PICKUP))
	    return Household.IND_PICKUP;
	else if(aName.startsWith(Household.ACTION_PUTDOWN))
	    return Household.IND_PUTDOWN;
	throw new RuntimeException("Invalid action " + aname);
    }
}
    
