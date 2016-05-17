package burlap.statehashing;


import burlap.mdp.core.state.State;

import java.util.List;


/**
 * This class provides a hash value for {@link State} objects. This is useful for tabular
 * planning and learning algorithms that make use of hash-backed sets or maps for fast retrieval. You can
 * access the state it hashes from the public data member {@link #s}. If the {@link State}
 * delegate {@link #s} is a {@link burlap.statehashing.HashableState} itself, and you wish
 * to get the underlying {@link State}, then you should use the
 * {@link #getSourceState()} method, which will recursively descend and return the base source {@link State}.
 * <p>
 * Implementing this class requires implementing
 * the {@link #hashCode()} and {@link #equals(Object)} method.
 * <p>
 * Note that this class implements the {@link State} interface; however,
 * because the purpose of this class is to used with hashed data structures, it is not recommended that
 * you modify the state.
 *
 * @author James MacGlashan
 *
 */
public abstract class HashableState implements State{

	/**
	 * The source {@link State} to be hashed and evaluated by the {@link #hashCode()} and {@link #equals(Object)} method.
	 */
	public State s;

	public HashableState(State s){
		this.s = s;
	}

	/**
	 * Returns the underlying source state is hashed. If the delegate {@link State}
	 * of this {@link burlap.statehashing.HashableState} is also a {@link burlap.statehashing.HashableState},
	 * then it recursively returns its {@link #getSourceState()}.
	 * @return The underlying source {@link State} that this object hashes and evaluates.
	 */
	public State getSourceState(){
		if(this.s instanceof HashableState){
			return ((HashableState)this.s).getSourceState();
		}
		return this.s;
	}


	@Override
	public List<Object> variableKeys() {
		return s.variableKeys();
	}

	@Override
	public Object get(Object variableKey) {
		return s.get(variableKey);
	}


	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);




	/**
	 * A hash code cached abstract implementation of {@link HashableState}.
	 * Once a hash code is computed, it is saved so that it does not need to be used again. Implementing
	 * this class only requires implementing {@link #computeHashCode()}.
	 */
	public static abstract class CachedHashableState extends HashableState {

		protected int								hashCode;
		protected boolean							needToRecomputeHashCode;



		/**
		 * Initializes the StateHashTuple with the given {@link State} object.
		 * @param s the state object this object will wrap
		 */
		public CachedHashableState(State s){
			super(s);
			needToRecomputeHashCode = true;
		}


		/**
		 * This method computes the hashCode for this {@link HashableState}
		 * @return the hashcode for this state
		 */
		public abstract int computeHashCode();


		@Override
		public int hashCode(){
			if(needToRecomputeHashCode){
				this.hashCode = this.computeHashCode();
				this.needToRecomputeHashCode = false;
			}
			return hashCode;
		}

	}


}



