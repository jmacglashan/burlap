package household.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import household.Household;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class HouseholdRoom extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
	   Household.ATT_LEFT,
	   Household.ATT_RIGHT,
	   Household.ATT_BOTTOM,
	   Household.ATT_TOP,
           Household.ATT_COLOR,
	   Household.ATT_SHAPE
    );

    public HouseholdRoom(String name,
			 int left,
			 int right,
			 int bottom,
			 int top,
			 String color,
			 String shape) {
	this.set(Household.ATT_LEFT, left);
	this.set(Household.ATT_RIGHT, right);
	this.set(Household.ATT_BOTTOM, bottom);
	this.set(Household.ATT_TOP, top);
	this.set(Household.ATT_COLOR, color);
	this.set(Household.ATT_SHAPE, shape);
	this.setName(name);
    }

    @Override
    public string className() {
	return Household.CLASS_ROOM;
    }

    @Override
    public List<Object> variableKeys() {
	return keys;
    }

    @Override
    public HouseholdRoom copyWithName(String objectName) {
	return new HouseholdRoom(objectName,
				 (int) get(Household.ATT_LEFT),
				 (int) get(Household.ATT_RIGHT),
				 (int) get(Household.ATT_BOTTOM),
				 (int) get(Household.ATT_TOP),
				 (String) get(Household.ATT_SHAPE),
				 (String) get(Household.ATT_COLOR));
    }
}
