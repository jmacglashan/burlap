package burlap.statehashing.simple;

import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

/**
 * A straightforward factory for creating {@link burlap.statehashing.HashableState} objects from
 * {@link State} instances. It produces either a {@link IISimpleHashableState} or a
 * {@link IDSimpleHashableState}.
 * The general approach is that hash values are computed by iterating through each
 * variable key in the order returned by {@link State#variableKeys()} and the has code for values returned by
 * {@link State#get(Object)} are combined. Similarly, two states are evaluated as equal when the values returned by
 * {@link State#get(Object)} satisfy their implemented {@link Object#equals(Object)} method.
 * <p>
 * This class also automatically provides special treatment for OO-MDP states (states that implement
 * {@link burlap.mdp.core.oo.state.OOState}) by being object identifier independent
 * (the names of objects don't affect the state identity). However, you may disable identifier independence
 * by using the constructor {@link #SimpleHashableStateFactory(boolean)}. If your domain is relational, it may be
 * important to be identifier *dependent* (that is, set the parameter in the constructor to false).
 * @author James MacGlashan.
 */
public class SimpleHashableStateFactory implements HashableStateFactory {

	/**
	 * Whether state evaluations of OO-MDPs are object identifier independent (the names of objects don't matter). By
	 * default it is independent.
	 */
	protected boolean identifierIndependent = true;


	/**
	 * Default constructor: object identifier independent and no hash code caching.
	 */
	public SimpleHashableStateFactory(){

	}

	/**
	 * Initializes with no hash code caching.
	 * @param identifierIndependent if true then state evaluations for {@link burlap.mdp.core.oo.state.OOState}s are object identifier independent; if false then dependent.
	 */
	public SimpleHashableStateFactory(boolean identifierIndependent){
		this.identifierIndependent = identifierIndependent;
	}


	@Override
	public HashableState hashState(State s) {
		if(s instanceof IISimpleHashableState || s instanceof IDSimpleHashableState){
			return (HashableState)s;
		}

		if(identifierIndependent){
			return new IISimpleHashableState(s);
		}
		return new IDSimpleHashableState(s);
	}


	public boolean objectIdentifierIndependent() {
		return this.identifierIndependent;
	}





}
