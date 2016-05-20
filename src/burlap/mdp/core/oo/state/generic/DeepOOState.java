package burlap.mdp.core.oo.state.generic;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.oo.state.exceptions.UnknownObjectException;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.annotations.DeepCopyState;

/**
 * An alternative implementation of {@link GenericOOState} in which the {@link #copy()} operations performs a deep copy
 * ({@link DeepCopyState}) of all {@link ObjectInstance} objects, thereby allows safe modification of any of its
 * {@link ObjectInstance} objects without using the {@link #touch(String)} method. As a result, this implementation
 * will cause greater memory overhead in your algorithms, but requires less management on a client that is manipulating
 * the states (e.g., defining transition dynamics).
 * @author James MacGlashan.
 */
@DeepCopyState
public class DeepOOState extends GenericOOState {
	public DeepOOState() {
	}

	public DeepOOState(OOState srcOOState) {
		super();
		for(ObjectInstance o : srcOOState.objects()){
			this.addObject((ObjectInstance)o.copy());
		}
	}

	public DeepOOState(ObjectInstance... objects) {
		for(ObjectInstance o : objects){
			this.addObject((ObjectInstance)o.copy());
		}
	}

	@Override
	public MutableState set(Object variableKey, Object value) {

		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		ObjectInstance ob = this.object(key.obName);
		if(ob == null){
			throw new UnknownObjectException(key.obName);
		}
		if(!(ob instanceof MutableState)){
			throw new RuntimeException("Cannot set value for object " + ob.name() + " because it does not implement MutableState");
		}
		((MutableState)ob).set(key.obVarKey, value);

		return this;
	}

	@Override
	public DeepOOState copy() {
		return new DeepOOState(this);
	}
}
