package burlap.oomdp.statehashing;


import burlap.oomdp.core.states.State;




/**
 * This class provides a hash value for {@link burlap.oomdp.core.states.State} objects. This is useful for tabular
 * planning and learning algorithms that make use of hash-backed sets or maps for fast retrieval. You can
 * access the state it hashes from the public data member s.
 * <p/>
 * By default, equality checks use the standard {@link burlap.oomdp.core.states.State} object equality check. If you need
 * to handle this specially, (such as providing state abstraction), then the equals method should be overridden.
 * @author James MacGlashan
 *
 */
public abstract class HashableState {

	/**
	 * The state to be hashed
	 */
	public State s;

	public HashableState(State s){
		this.s = s;
	}

	@Override
	public abstract int hashCode();

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(!(obj instanceof HashableState)){
			return false;
		}
		HashableState o = (HashableState)obj;
		return s.equals(o.s);
	}


	/**
	 * A hash code cached abstract implementation of {@link HashableState}.
	 * Once a hash code is computed, it is saved so that it does not need to be used again. Implementing
	 * this class only requires implementing {@link #computeHashCode()}.
	 */
	public static abstract class CachedHashableState extends HashableState {

		protected int								hashCode;
		protected boolean							needToRecomputeHashCode;



		/**
		 * Initializes the StateHashTuple with the given {@link burlap.oomdp.core.states.State} object.
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
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			if(!(other instanceof HashableState)){
				return false;
			}
			HashableState o = (HashableState)other;
			return s.equals(o.s);

		}

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



