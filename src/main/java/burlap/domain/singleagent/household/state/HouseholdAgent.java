package household.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import household.Household;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class HouseholdAgent extends MutableObject {
    private final static List<Object> keys = Arrays.<Object>asList(
	    Household.ATT_X,
	    Household.ATT_Y,
	    Household.ATT_DIR
    );
	    
    public HouseholdAgent(String name, int x, int y, String direction) {
	this(name, (Object) x, (Object) y, direction;
    }

    private HouseholdAgent(String name, Object x, Object y, String direction) {
	this.set(Household.ATT_X, x);
	this.set(Household.ATT_Y, y);
	this.set(Household.ATT_DIR, direction);
	this.setName(name);
    }

    @Override
    public String className() {
	return Household.CLASS_AGENT;
    }

    @Override
    public HouseholdAgent copy() {
	return (HouseholdAgent) copyWithName(name());
    }

    @Override ObjectInstance copyWithName(String objectName) {
	return new HouseholdAgent(
				  objectName,
				  get(Household.ATT_X),
				  get(Household.ATT_Y),
				  get(Household.ATT_DIR)
				  );
    }

    @Override
    public List<Object> variableKeys() {
	return keys;
    }
}
