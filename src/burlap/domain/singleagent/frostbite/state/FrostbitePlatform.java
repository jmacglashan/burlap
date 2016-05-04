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
public class FrostbitePlatform implements ObjectInstance {

	public int x;
	public int y;
	public int size;
	public boolean activated;

	protected String name;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_SIZE, VAR_ACTIVATED);


	public FrostbitePlatform() {
	}

	public FrostbitePlatform(int x, int y, int size, boolean activated, String name) {
		this.x = x;
		this.y = y;
		this.size = size;
		this.activated = activated;
		this.name = name;
	}

	@Override
	public String className() {
		return CLASS_PLATFORM;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public FrostbitePlatform copyWithName(String objectName) {
		return new FrostbitePlatform(x, y, size, activated, objectName);
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
		else if(variableKey.equals(VAR_SIZE)){
			return size;
		}
		else if(variableKey.equals(VAR_ACTIVATED)){
			return activated;
		}

		throw new UnknownKeyException(variableKey);

	}

	@Override
	public FrostbitePlatform copy() {
		return new FrostbitePlatform(x, y ,size, activated, name);
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
