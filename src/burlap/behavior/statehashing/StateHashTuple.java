package burlap.behavior.statehashing;


import burlap.oomdp.core.State;


/**
 * This class provides a hash value for {@link burlap.oomdp.core.State} objects. This is useful for tabular
 * planning and learning algorithms that make use of hash-backed sets or maps for fast retrieval.
 * In general, hash codes should only be computed once, and only once the hashCode method is called.
 * If something about the StateHashTuple changes, then needsToRecomputeHashCode boolean flag should be
 * set to true so that the next time the hashCode is called it is recomputed. Likewise, once the {@link computeHashCode()}
 * method has been called, it should set the needToRecomputeHashCode flag to false.
 * <p/>
 * By default, equality checks use the standard {@link burlap.oomdp.core.State} object equality check. If you need
 * to handle this specially, (such as providing state abstraction), then the equals method should be overridden.
 * @author James MacGlashan
 *
 */
public abstract class StateHashTuple {

	public State								s;
	protected int								hashCode;
	protected boolean							needToRecomputeHashCode;
	
	
	
	/**
	 * Initializes the StateHashTuple with the given {@link burlap.oomdp.core.State} object.
	 * @param s the state object this object will wrap
	 */
	public StateHashTuple(State s){
		this.s = s;
		needToRecomputeHashCode = true;
	}
	
	
	
	public abstract void computeHashCode();
	
	
	@Override
	public boolean equals(Object other){
		if(this == other){
			return true;
		}
		if(!(other instanceof StateHashTuple)){
			return false;
		}
		StateHashTuple o = (StateHashTuple)other;
		return s.equals(o.s);
		
	}
	
	@Override
	public int hashCode(){
		if(needToRecomputeHashCode){
			this.computeHashCode();
		}
		return hashCode;
	}
	
}
