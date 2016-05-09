package burlap.domain.singleagent.gridworld.state;

import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.gridworld.GridWorldDomain.VAR_X;
import static burlap.domain.singleagent.gridworld.GridWorldDomain.VAR_Y;

/**
 * Object instance for the agent in a {@link GridWorldDomain}. Variable keys are string "x" and "y" of type int.
 * @author James MacGlashan.
 */
@DeepCopyState
public class GridAgent implements ObjectInstance {

	public int x;
	public int y;

	protected String name;

	private final static List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y);

	public GridAgent() {
		this.name = "agent";
	}

	public GridAgent(int x, int y) {
		this();
		this.x = x;
		this.y = y;
	}

	public GridAgent(int x, int y, String name) {
		this.x = x;
		this.y = y;
		this.name = name;
	}

	@Override
	public String className() {
		return GridWorldDomain.CLASS_AGENT;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public GridAgent copyWithName(String objectName) {
		GridAgent nagent = this.copy();
		nagent.name = objectName;
		return nagent;
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

		throw new RuntimeException("Unknown key " + key);
	}


	@Override
	public GridAgent copy() {
		return new GridAgent(x, y, name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
