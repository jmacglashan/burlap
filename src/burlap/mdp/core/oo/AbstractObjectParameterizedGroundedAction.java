package burlap.mdp.core.oo;

import burlap.mdp.core.state.State;
import burlap.mdp.core.AbstractGroundedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An interface extension to the {@link AbstractGroundedAction} interface for grounded actions whose
 * parameter included references to OO-MDP {@link ObjectInstance}s. This is a special
 * interface because grounded actions that have parameters references to OO-MDO {@link ObjectInstance}s
 * may require special care by a planning or learning algorithm since the names of object references can change between states
 * that are otherwise equal (that is, states that are object identifier independent).
 * <p>
 * This interface also includes the inner class {@link AbstractObjectParameterizedGroundedAction.Helper}
 * which provides the static method {@link AbstractObjectParameterizedGroundedAction.Helper#translateParameters(AbstractGroundedAction, State, State)}
 * that can be used to reparameterize a {@link AbstractObjectParameterizedGroundedAction}'s object references
 * to equivalent {@link ObjectInstance} objects in separate state with different names. See its
 * method documentation for more information.
 * @author James MacGlashan.
 */
public interface AbstractObjectParameterizedGroundedAction extends AbstractGroundedAction{

	/**
	 * Returns the parameters of this {@link AbstractGroundedAction} that correspond to OO-MDP objects.
	 * @return the parameters of this {@link AbstractGroundedAction} that correspond to OO-MDP objects.
	 */
	String [] getObjectParameters();

	/**
	 * Sets the object parameters for this {@link AbstractGroundedAction}.
	 * @param params the object parameters to use.
	 */
	void setObjectParameters(String [] params);

	/**
	 * Returns true if this {@link AbstractGroundedAction} is for a domain in which states are identifier independent; false if dependent
	 * @return true if this {@link AbstractGroundedAction} is for a domain in which states are identifier independent; false if dependent
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
					if(matchedObjects.contains(cand.name())) {
						continue;
					}
					if(o.equals(cand)) {
						nparams[i] = o.name();
						matchedObjects.add(o.name());
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
