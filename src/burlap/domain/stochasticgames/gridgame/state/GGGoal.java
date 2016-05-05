package burlap.domain.stochasticgames.gridgame.state;

import burlap.oomdp.core.oo.state.OOStateUtilities;
import burlap.oomdp.core.oo.state.ObjectInstance;
import burlap.oomdp.core.state.MutableState;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.state.UnknownKeyException;
import burlap.oomdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.stochasticgames.gridgame.GridGame.*;

/**
 * @author James MacGlashan.
 */
@DeepCopyState
public class GGGoal implements ObjectInstance, MutableState{

	public int x;
	public int y;
	public int type;

	protected String name;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_GT);


	public GGGoal() {
	}

	public GGGoal(int x, int y, int type, String name) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.name = name;
	}

	@Override
	public String className() {
		return CLASS_GOAL;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new GGGoal(x, y, type, objectName);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		int i = (Integer)value;

		if(variableKey.equals(VAR_X)){
			this.x = i;
		}
		else if(variableKey.equals(VAR_Y)){
			this.y = i;
		}
		else if(variableKey.equals(VAR_GT)){
			this.type = i;
		}
		else{
			throw new UnknownKeyException(variableKey);
		}

		return this;
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if(variableKey.equals(VAR_X)){
			return x;
		}
		else if(variableKey.equals(VAR_Y)){
			return y;
		}
		else if(variableKey.equals(VAR_GT)){
			return type;
		}
		else{
			throw new UnknownKeyException(variableKey);
		}
	}

	@Override
	public State copy() {
		return new GGGoal(x, y, type, name);
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
