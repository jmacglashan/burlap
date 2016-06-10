package burlap.domain.singleagent.gridworld.state;

import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.gridworld.GridWorldDomain.VAR_TYPE;
import static burlap.domain.singleagent.gridworld.GridWorldDomain.VAR_X;
import static burlap.domain.singleagent.gridworld.GridWorldDomain.VAR_Y;

/**
 * ObjectInstance for locations in a {@link GridWorldDomain}. Variable keys are string "x", "y", and "loc" of type int.
 * @author James MacGlashan.
 */
@DeepCopyState
public class GridLocation implements ObjectInstance {

	public int x;
	public int y;
	public int type;

	protected String name;

	private final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_TYPE);


	public GridLocation() {
	}

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
		if(key.equals(VAR_X)){
			return x;
		}
		else if(key.equals(VAR_Y)){
			return y;
		}
		else if(key.equals(VAR_TYPE)){
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
