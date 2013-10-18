package burlap.behavior.statehashing;


import burlap.oomdp.core.State;

public abstract class StateHashTuple {

	public State								s;
	protected int								hashCode;
	protected boolean							needToRecomputeHashCode;
	
	
	
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
