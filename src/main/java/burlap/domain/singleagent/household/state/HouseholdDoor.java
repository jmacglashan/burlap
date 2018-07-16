package household.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import household.Household;
import utilities.MutableObject;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class HouseholdDoor extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
	    Household.ATT_X,
	    Household.ATT_Y,
	    Household.ATT_LEFT,
	    Household.ATT_RIGHT,
	    Household.ATT_BOTTOM,
	    Household.ATT_TOP,
	    Household.ATT_LOCKED,
	    Household.ATT_CLOSED,
	    Household.ATT_SHAPE,
	    Household.ATT_COLOR
    );

    public HouseholdDoor(String name,
			 int left,
			 int right,
			 int bottom,
			 int top,
			 Boolean locked,
			 Boolean closed,
			 String shape,
			 String color) {
	this(name,
	     (Object) left,
	     (Object) right,
	     (Object) bottom,
	     (Object) top,
	     (Object) locked,
	     (Object) closed,
	     shape,
	     color);
    }

    public HouseholdDoor(String name,
			 int left,
			 int right,
			 int bottom,
			 int top,
			 String lockableState,
			 Boolean closed) {
	this(name,
	     (Object) left,
	     (Object) right,
	     (Object) bottom,
	     (Object) top,
	     (Object) lockableState,
	     (Object) closed,
	     Household.SHAPE_DOOR,
	     Household.COLOR_GRAY);
    }

    private HouseholdDoor(String name,
			  Object left,
			  Object right,
			  Object bottom,
			  Object top,
			  Object locked,
			  Object closed,
			  String shape,
			  String color) {
	this.set(Household.ATT_X, left);
	this.set(Household.ATT_Y, bottom);
	this.set(Household.ATT_LEFT, left);
	this.set(Household.ATT_RIGHT, right);
	this.set(Household.ATT_TOP, top);
	this.set(Household.ATT_BOTTOM, bottom);
	this.set(Household.ATT_LOCKED, locked);
	this.set(Household.ATT_CLOSED, closed);
	this.set(Household.ATT_SHAPE, shape);
	this.set(Household.ATT_COLOR, color);
	this.setName(name);
    }

    @Override
    public String className() {
	return Household.CLASS_DOOR;
    }

    @Override
    public ObjectInstance copyWithName(String name) {
	return new HouseholdDoor(name,
				 get(Household.ATT_LEFT),
				 get(Household.ATT_RIGHT),
				 get(Household.ATT_BOTTOM),
				 get(Household.ATT_TOP),
				 get(Household.ATT_LOCKED),
				 get(Household.ATT_CLOSED),
				 get(Household.ATT_SHAPE),
				 get(Household.ATT_COLOR));
    }

    @Override
    public List<Object> variableKeys() {
	return keys;
    }
}
