package household;

// TODO: Make a generic violation class. 

public class HouseholdInterruptViolation extends MutableObject {

    private final static List<Object> keys = Arrays.<Object>asList(
           Household.ATT_RPENALTY,
	   Household.ATT_TPENALTY);

    public HouseholdInterruptViolation(String name, int rpenalty, State tpenalty) {
	this.set(Household.ATT_RPENALTY, rpenalty);
	this.set(Household.ATT_TPENALTY, rpenalty);
	this.setName(name);
    }

    public static Boolean isViolation(State state, Action action, State sprime) {
	// if action is not speak
	// return false
	// else
	// get the people
	// if there are no people
	// return false
	// if there are people
	// if a person is speaking
	// return true
	// else
	// return false
    }

    @Override
    public String className() {
	return Household.CLASS_INTERRUPT_VIOLATION;
    }

    @Override
    public List<Object> variableKeys() {
	return keys;
    }

    @Override
    public Household copyWithName(String objectName) {
	return new HouseholdInterruptViolation(objectName,
					       (int) get(Household.ATT_RPENALTY),
					       (State) get(Household.ATT_TPENALTY)
					       );
    }
}
