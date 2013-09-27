package burlap.behavior.statehashing;


import burlap.oomdp.core.State;

public class StateHashTuple {

	public State								s;
	protected int								hashCode;
	protected boolean							needToRecomputeHashCode;
	
	
	
	public StateHashTuple(State s){
		this.s = s;
		needToRecomputeHashCode = true;
	}
	
	
	
	public void computeHashCode(){
		
		this.hashCode = this.s.getCompleteStateDescription().toString().hashCode(); //very basic and does not guarantee object name invariance
		needToRecomputeHashCode = false;
		
	}
	
	
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
