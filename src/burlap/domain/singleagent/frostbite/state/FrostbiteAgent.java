package burlap.domain.singleagent.frostbite.state;

import burlap.oomdp.core.oo.state.OOStateUtilities;
import burlap.oomdp.core.oo.state.ObjectInstance;
import burlap.oomdp.core.state.UnknownKeyException;
import burlap.oomdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.frostbite.FrostbiteDomain.*;

/**
 * @author James MacGlashan.
 */
@DeepCopyState
public class FrostbiteAgent implements ObjectInstance {

	public int x;
	public int y;
	public int height;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_HEIGHT);

	public FrostbiteAgent() {
	}

	public FrostbiteAgent(int x, int y, int height) {
		this.x = x;
		this.y = y;
		this.height = height;
	}

	public FrostbiteAgent(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String className() {
		return CLASS_AGENT;
	}

	@Override
	public String name() {
		return CLASS_AGENT;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		throw new RuntimeException("Frostbite agent must be named " + CLASS_AGENT);
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
		else if(variableKey.equals(VAR_HEIGHT)){
			return height;
		}

		throw new UnknownKeyException(variableKey);
	}

	@Override
	public FrostbiteAgent copy() {
		return new FrostbiteAgent(x, y, height);
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
