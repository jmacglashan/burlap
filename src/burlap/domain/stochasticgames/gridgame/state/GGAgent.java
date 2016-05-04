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
public class GGAgent implements ObjectInstance, MutableState {

	public int x;
	public int y;
	public int player;

	protected String name;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_PN);


	public GGAgent() {
	}

	public GGAgent(int x, int y, int player, String name) {
		this.x = x;
		this.y = y;
		this.player = player;
		this.name = name;
	}

	@Override
	public String className() {
		return CLASS_AGENT;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new GGAgent(x, y, player, objectName);
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
		else if(variableKey.equals(VAR_PN)){
			this.player = i;
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
		else if(variableKey.equals(VAR_PN)){
			return player;
		}
		else{
			throw new UnknownKeyException(variableKey);
		}
	}

	@Override
	public State copy() {
		return new GGAgent(x, y, player, name);
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
