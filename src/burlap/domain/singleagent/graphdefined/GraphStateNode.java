package burlap.domain.singleagent.graphdefined;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class GraphStateNode implements MutableState {

	protected int id;

	protected static List<Object> keys;

	public GraphStateNode() {
		if(keys == null) {
			keys = new ArrayList<Object>();
			keys.add("node");
		}
	}

	public GraphStateNode(int id) {
		super();
		this.id = id;
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		if(value instanceof Number) {
			id = ((Number) value).intValue();
		} else if(value instanceof String) {
			try {
				id = Integer.parseInt((String) value);
			} catch(Exception e) {
				throw new RuntimeException("Could not parse string value " + value + " into an int. Cannot set GraphState value.");
			}
		} else {
			throw new RuntimeException("Cannot set graph value to value of type " + value.getClass().getName() + ". Use int or String.");
		}
		return this;
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		return id;
	}

	@Override
	public State copy() {
		return new GraphStateNode(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
