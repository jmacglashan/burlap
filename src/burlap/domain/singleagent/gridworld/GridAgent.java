package burlap.domain.singleagent.gridworld;

import burlap.oomdp.core.oo.state.ObjectInstance;

import java.util.Arrays;
import java.util.List;

/**
 * Object instance for the agent in a {@link GridWorldDomain}. Variable keys are string "x" and "y" of type int.
 * @author James MacGlashan.
 */
public class GridAgent implements ObjectInstance {

	public int x;
	public int y;

	protected String name;

	private final static List<Object> keys = Arrays.<Object>asList("x", "y");

	public GridAgent() {
		this.name = "agent";
	}

	public GridAgent(int x, int y) {
		super();
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
		if(key.equals("x")){
			return x;
		}
		else if(key.equals("y")){
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
}
