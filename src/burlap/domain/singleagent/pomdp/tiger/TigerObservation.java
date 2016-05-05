package burlap.domain.singleagent.pomdp.tiger;

import burlap.mdp.core.state.MutableState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.pomdp.tiger.TigerDomain.*;


/**
 * @author James MacGlashan.
 */
public class TigerObservation implements MutableState {

	public String hear;

	public TigerObservation() {
		hear = HEAR_NOTHING;
	}

	public TigerObservation(String hear) {
		if(!hear.equals(HEAR_LEFT) && !hear.equals(HEAR_RIGHT) && !hear.equals(HEAR_NOTHING) && !hear.equals(DOOR_RESET)){
			throw new RuntimeException("Value must be either " + HEAR_LEFT + ", " + HEAR_RIGHT + ", " + HEAR_NOTHING + ", or " + DOOR_RESET);
		}
		this.hear = hear;
	}

	@Override
	public MutableState set(Object variableKey, Object value) {

		if(!(value instanceof String)){
			throw new RuntimeException("Value must be a String");
		}

		String hear = (String)value;
		if(!hear.equals(HEAR_LEFT) && !hear.equals(HEAR_RIGHT) && !hear.equals(HEAR_NOTHING) && !hear.equals(DOOR_RESET)){
			throw new RuntimeException("Value must be either " + HEAR_LEFT + ", " + HEAR_RIGHT + ", " + HEAR_NOTHING + ", or " + DOOR_RESET);
		}
		this.hear = hear;

		return this;
	}

	@Override
	public List<Object> variableKeys() {
		return Arrays.<Object>asList(VAR_HEAR);
	}

	@Override
	public Object get(Object variableKey) {
		return hear;
	}

	@Override
	public TigerObservation copy() {
		return new TigerObservation(hear);
	}

	@Override
	public String toString() {
		return hear;
	}
}
