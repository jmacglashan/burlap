package burlap.domain.singleagent.gridworld;

import burlap.oomdp.core.oo.state.OOStateUtilities;
import burlap.oomdp.core.oo.state.ObjectInstance;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.gridworld.GridWorldDomain.ATT_TYPE;
import static burlap.domain.singleagent.gridworld.GridWorldDomain.ATT_X;
import static burlap.domain.singleagent.gridworld.GridWorldDomain.ATT_Y;

/**
 * ObjectInstance for locations in a {@link GridWorldDomain}. Variable keys are string "x", "y", and "loc" of type int.
 * @author James MacGlashan.
 */
public class GridLocation implements ObjectInstance {

	public int x;
	public int y;
	public int type;

	protected String name;

	private final static List<Object> keys = Arrays.<Object>asList(ATT_X, ATT_Y, ATT_TYPE);



	public GridLocation(int x, int y, String name) {
		this.x = x;
		this.y = y;
		this.name = name;
	}


	public GridLocation(int x, int y, int type, String name) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.name = name;
	}

	@Override
	public String className() {
		return GridWorldDomain.CLASS_LOCATION;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public GridLocation copyWithName(String objectName) {
		GridLocation nloc = this.copy();
		nloc.name = objectName;
		return nloc;
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if(!(variableKey instanceof String)){
			throw new RuntimeException("GridAgent variable key must be a string");
		}

		String key = (String)variableKey;
		if(key.equals(ATT_X)){
			return x;
		}
		else if(key.equals(ATT_Y)){
			return y;
		}
		else if(key.equals(ATT_TYPE)){
			return type;
		}

		throw new RuntimeException("Unknown key " + key);
	}


	@Override
	public GridLocation copy() {
		return new GridLocation(x, y, type, name);
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
