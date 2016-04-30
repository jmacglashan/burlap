package burlap.oomdp.core;

import burlap.oomdp.core.states.State;
import burlap.oomdp.core.states.oo.OOState;
import burlap.oomdp.core.states.oo.ObjectInstance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An interface extension to the {@link burlap.oomdp.core.AbstractGroundedAction} interface for grounded actions whose
 * parameter included references to OO-MDP {@link burlap.oomdp.core.states.oo.ObjectInstance}s. This is a special
 * interface because grounded actions that have parameters references to OO-MDO {@link burlap.oomdp.core.states.oo.ObjectInstance}s
 * may require special care by a planning or learning algorithm since the names of object references can change between states
 * that are otherwise equal (that is, states that are object identifier independent).
 * <p>
 * This interface also includes the inner class {@link burlap.oomdp.core.AbstractObjectParameterizedGroundedAction.Helper}
 * which provides the static method {@link burlap.oomdp.core.AbstractObjectParameterizedGroundedAction.Helper#translateParameters(AbstractGroundedAction, burlap.oomdp.core.states.State, burlap.oomdp.core.states.State)}
 * that can be used to reparameterize a {@link burlap.oomdp.core.AbstractObjectParameterizedGroundedAction}'s object references
 * to equivalent {@link burlap.oomdp.core.states.oo.ObjectInstance} objects in separate state with different names. See its
 * method documentation for more information.
 * @author James MacGlashan.
 */
public interface AbstractObjectParameterizedGroundedAction extends AbstractGroundedAction{

	/**
	 * Returns the parameters of this {@link burlap.oomdp.core.AbstractGroundedAction} that correspond to OO-MDP objects.
	 * @return the parameters of this {@link burlap.oomdp.core.AbstractGroundedAction} that correspond to OO-MDP objects.
	 */
	String [] getObjectParameters();

	/**
	 * Sets the object parameters for this {@link burlap.oomdp.core.AbstractGroundedAction}.
	 * @param params the object parameters to use.
	 */
	void setObjectParameters(String [] params);

	/**
	 * Returns true if this {@link burlap.oomdp.core.AbstractGroundedAction} is for a domain in which states are identifier independent; false if dependent
	 * @return true if this {@link burlap.oomdp.core.AbstractGroundedAction} is for a domain in which states are identifier independent; false if dependent
	 */
	boolean actionDomainIsObjectIdentifierIndependent();


	public static class Helper {
	    
	    private Helper() {
	        // do nothing
	    }

		/**
		 * This method will translate this object's parameters that were assigned for a given source state, into object parameters in the
		 * target state that are equal. This method is useful if a domain uses parameterized actions and is object identifier invariant.
		 * If the domain of this grounded action's action is object identifier dependent, then no translation will occur
		 * and this object will be returned. This object will also be returned if it is a parameterless action.
		 * @param sourceState the source state from which this objects parameters were bound.
		 * @param targetState a target state with potentially different object identifiers for equivalent values.
		 * @return a grounded action object whose parameters have been translated to the target state object identifiers
		 */
		public static AbstractGroundedAction translateParameters(AbstractGroundedAction groundedAction, State sourceState, State targetState) {

			if(!(groundedAction instanceof AbstractObjectParameterizedGroundedAction)) {
				return groundedAction;
			}

			if(!(sourceState instanceof OOState) || !(targetState instanceof OOState)){
				throw new RuntimeException("Cannot translate object parameters for state that does not implement OOState");
			}

			OOState ooSource = (OOState)sourceState;
			OOState ooTarget = (OOState)targetState;

			String[] params = ((AbstractObjectParameterizedGroundedAction)groundedAction).getObjectParameters();

			if(params.length == 0 || !((AbstractObjectParameterizedGroundedAction)groundedAction).actionDomainIsObjectIdentifierIndependent()) {
				//no need to translate a parameterless action or an action that belongs to a name dependent domain or actions that do not have objects as parameters
				return groundedAction;
			}

			AbstractGroundedAction aga = groundedAction.copy();

			Set<String> matchedObjects = new HashSet<String>();
			String[] nparams = new String[params.length];
			int i = 0;
			for(String oname : params) {
				ObjectInstance o = ooSource.object(oname);
				List<ObjectInstance> cands = ooTarget.objectsOfClass(o.className());
				for(ObjectInstance cand : cands) {
					if(matchedObjects.contains(cand.getName())) {
						continue;
					}
					if(o.equals(cand)) {
						nparams[i] = o.getName();
						matchedObjects.add(o.getName());
						break;
					}
				}

				i++;
			}

			((AbstractObjectParameterizedGroundedAction)aga).setObjectParameters(nparams);

			return aga;
		}


	}

}
