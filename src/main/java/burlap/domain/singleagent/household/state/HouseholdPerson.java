package household.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import household.Household;
import utilities.MutableObject;

import java.util.Arrays;
import java.util.List;

public class HouseholdPerson extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
	     Household.ATT_X,
	     Household.ATT_Y,
	     Household.ATT_SHAPE,
             Household.ATT_COLOR,
	     Household.ATT_IN_CONVERSATION					     
    );

    public HouseholdPerson(String name, int x, int y, String shape, String color) {
	this(name, (Object) x, (Object) y, shape, color, false);
    }

    public HouseholdPerson(String name, int x, int y, String shape,
			   String color, boolean inConversation) {
	this(name, (Object) x, (Object) y, shape, color, (Object) inConversation);
    }

    private HouseholdPerson(String name, Object x, Object y, String shape,
			    String color, Object inConversation) {
	this.set(Household.ATT_X, x);
	this.set(Household.ATT_Y, y);
	this.set(Household.ATT_SHAPE, shape);
	this.set(Household.ATT_COLOR, color);
	this.set(Household.ATT_IN_CONVERSATION, inConversation);
	this.setName(name);
    }

    @Override
    public String className() {
	return Household.CLASS_PERSON;
    }

    @Override
    public HouseholdPerson copy() {
	return (HouseholdPerson) copyWithName(name());
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
	return new HouseholdPerson(
				   objectName,
				   get(Household.ATT_X),
				   get(Household.ATT_Y),
				   get(Household.ATT_SHAPE),
				   get(Household.ATT_COLOR),
				   get(Household.ATT_IN_CONVERSATION)
				   );
    }

    @Override
    public List<Object> variableKeys() {
	return keys;
    }
}
