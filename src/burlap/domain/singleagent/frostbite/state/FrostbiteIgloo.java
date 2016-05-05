package burlap.domain.singleagent.frostbite.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.frostbite.FrostbiteDomain.CLASS_IGLOO;
import static burlap.domain.singleagent.frostbite.FrostbiteDomain.VAR_BUILDING;

/**
 * @author James MacGlashan.
 */
@DeepCopyState
public class FrostbiteIgloo implements ObjectInstance {

	public int height;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_BUILDING);

	public FrostbiteIgloo() {
	}

	public FrostbiteIgloo(int height) {
		this.height = height;
	}

	@Override
	public String className() {
		return CLASS_IGLOO;
	}

	@Override
	public String name() {
		return CLASS_IGLOO;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		if(!objectName.equals(CLASS_IGLOO))
			throw new RuntimeException("Igloo object must be named " + CLASS_IGLOO);
		return copy();
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if(variableKey.equals(VAR_BUILDING)){
			return height;
		}

		throw new UnknownKeyException(variableKey);
	}

	@Override
	public FrostbiteIgloo copy() {
		return new FrostbiteIgloo(height);
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
