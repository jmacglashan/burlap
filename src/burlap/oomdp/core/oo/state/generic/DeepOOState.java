package burlap.oomdp.core.oo.state.generic;

import burlap.oomdp.core.oo.state.OOState;
import burlap.oomdp.core.oo.state.OOStateUtilities;
import burlap.oomdp.core.oo.state.OOVariableKey;
import burlap.oomdp.core.oo.state.ObjectInstance;
import burlap.oomdp.core.oo.state.exceptions.UnknownObjectException;
import burlap.oomdp.core.state.MutableState;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.state.annotations.DeepCopyState;

/**
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
	public State copy() {
		return new DeepOOState(this);
	}
}
