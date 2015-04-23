package burlap.behavior.statehashing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.states.State;


/**
 * This hashing factory provides hashing and state equality checks for domains in which the object name references are important.
 * This is in contrast to the typical OO-MDP state equality checking in which as long as there was a bijection between set of
 * object instances in two state objects in which the matched objects had equivalent value assignments, the states were considered
 * equal. In other words, in typical OO-MDP the ObjectInstance name identifier did not affect state equality. In a NameDependent domain,
 * changing the name of an object instances, changes which state it represents. NameDependent domains are useful for domains with
 * relational attributes in which the specific object reference to which an attribute is linked is important.
 * <p/>
 * Hash codes are currently computed using the name identifier of the object instance as well as the value assignments for all
 * observable object instances.
 * @author James MacGlashan
 *
 */
public class NameDependentStateHashFactory implements StateHashFactory {

	protected List <String>				objectNameOrder;
	protected Set <String>				objectNames;
	
	
	public NameDependentStateHashFactory(){
		objectNameOrder = new ArrayList<String>();
		objectNames = new HashSet<String>();
	}
	
	@Override
	public StateHashTuple hashState(State s) {
		
		if(objectNameOrder.size() != s.getObservableObjects().size()){
			this.addNewObjectNames(s);
		}
		
		
		return new NameDependentStateHashTuple(s);
	}
	
	protected void addNewObjectNames(State s){
		List <ObjectInstance> obs = s.getObservableObjects();
		for(ObjectInstance ob : obs){
			String name = ob.getName();
			if(!objectNames.contains(name)){
				objectNameOrder.add(name);
				objectNames.add(name);
			}
		}
	}
	
	
	public class NameDependentStateHashTuple extends StateHashTuple{

		public NameDependentStateHashTuple(State s) {
			super(s);
		}

		@Override
		public void computeHashCode() {
			
			StringBuffer buf = new StringBuffer();
			
			boolean completeMatch = true;
			for(String oname : NameDependentStateHashFactory.this.objectNameOrder){
				ObjectInstance o = this.s.getObject(oname);
				if(o != null){
					buf.append(o.getObjectDescription());
				}
				else{
					completeMatch = false;
				}
			}
			
			if(!completeMatch){
				int start = NameDependentStateHashFactory.this.objectNameOrder.size();
				NameDependentStateHashFactory.this.addNewObjectNames(this.s);
				for(int i = start; i < NameDependentStateHashFactory.this.objectNameOrder.size(); i++){
					ObjectInstance o = this.s.getObject(NameDependentStateHashFactory.this.objectNameOrder.get(i));
					if(o != null){
						buf.append(o.getObjectDescription());
					}
				}
			}
			
			this.hashCode = buf.toString().hashCode();
			this.needToRecomputeHashCode = false;
			
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
			
			List <ObjectInstance> obs = this.s.getObservableObjects();
			for(ObjectInstance ob : obs){
				String name = ob.getName();
				ObjectInstance oob = o.s.getObject(name);
				if(oob == null){
					return false;
				}
				if(!ob.valueEquals(oob)){
					return false;
				}
				
			}
			
			return true;
			
			
		}
		
		
		
	}
	

}
