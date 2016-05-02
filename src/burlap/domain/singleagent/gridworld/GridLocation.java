package burlap.domain.singleagent.gridworld;

import burlap.oomdp.core.oo.state.ObjectInstance;

import java.util.Arrays;
import java.util.List;

/**
 * ObjectInstance for locations in a {@link GridWorldDomain}. Variable keys are string "x", "y", and "loc" of type int.
 * @author James MacGlashan.
 */
public class GridLocation implements ObjectInstance {

	public int x;
	public int y;
	public int type;

	protected String name;

	private final static List<Object> keys = Arrays.<Object>asList("x", "y", "type");



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
		if(key.equals("x")){
			return x;
		}
		else if(key.equals("y")){
			return y;
		}
		else if(key.equals("type")){
			return type;
		}

		throw new RuntimeException("Unknown key " + key);
	}


	@Override
	public GridLocation copy() {
		return new GridLocation(x, y, type, name);
	}

}
