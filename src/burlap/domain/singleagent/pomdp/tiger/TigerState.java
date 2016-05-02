package burlap.domain.singleagent.pomdp.tiger;

import burlap.oomdp.core.state.MutableState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.pomdp.tiger.TigerDomain.*;

/**
 * @author James MacGlashan.
 */
public class TigerState implements MutableState{
	public String door;

	public TigerState() {
		door = VAL_LEFT;
	}

	public TigerState(String door) {
		if(!door.equals(VAL_LEFT) && !door.equals(VAL_RIGHT)){
			throw new RuntimeException("Value must be either " + VAL_LEFT + " or " + VAL_RIGHT);
		}
		this.door = door;
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		if(!(value instanceof String)){
			throw new RuntimeException("Value must be a String");
		}

		String val = (String)value;
		if(!val.equals(VAL_LEFT) && !val.equals(VAL_RIGHT)){
			throw new RuntimeException("Value must be either " + VAL_LEFT + " or " + VAL_RIGHT);
		}

		this.door = val;

		return this;
	}

	@Override
	public List<Object> variableKeys() {
		return Arrays.<Object>asList(ATT_DOOR);
	}

	@Override
	public Object get(Object variableKey) {
		return door;
	}

	@Override
	public TigerState copy() {
		return new TigerState(door);
	}

	@Override
	public String toString() {
		return door;
	}
}
