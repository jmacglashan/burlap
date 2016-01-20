package burlap.behavior.statehashing;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;



/**
 * Like the {@link DiscreteStateHashFactory} (which this class extends) this class computes unique hash codes for states
 * based on attributes that the client specifies to use. However, unlike the superclass, this class also performs
 * state equality checks using only the attributes specified for each class. This is often a useful abstraction
 * for components like options whose policies are invariant to attributes specifying properties like ultimate task goal locations.
 * @author James MacGlashan
 *
 */
public class DiscreteMaskHashingFactory extends DiscreteStateHashFactory {

	//this is here for future implementation so that the client can specify different attributes for hashing versus equality checks
	//that functionality is not yet implemented, however, so it can be ignored.
	protected Map<String, List<Attribute>>	attributesForEquality; 
	
	
	/**
	 * Initializes this hashing factory to compute hash codes and equality checks with all attributes of all object classes.
	 */
	public DiscreteMaskHashingFactory() {
		super();
		attributesForEquality = null;
	}

	/**
	 * Initializes this hashing factory to hash and equality check on only the attributes for the specified classes in the provided map
	 * @param attributesForHashCode a map from object class names to the attributes that should be used in the hash calculation and equality check for those object classes.
	 */
	public DiscreteMaskHashingFactory(Map<String, List<Attribute>> attributesForHashCode) {
		super(attributesForHashCode);
		attributesForEquality = null;
	}
	
	@Override
	public StateHashTuple hashState(State s){
		return new DiscreteMaskHashTuple(s);
	}
	
	
	public class DiscreteMaskHashTuple extends DiscreteStateHashTuple{

		public DiscreteMaskHashTuple(State s) {
			super(s);
		}
		

		
		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			if(!(other instanceof StateHashTuple)){
				return false;
			}
			
			
			
			StateHashTuple that = (StateHashTuple)other;
			
			
			Collection<String> classesToCheck = null;
			Map <String, List<Attribute>> attMap = null;
			if(attributesForEquality != null){
				classesToCheck = attributesForEquality.keySet();
				attMap = attributesForEquality;
			}
			else if(attributesForHashCode != null){
				classesToCheck = attributesForHashCode.keySet();
				attMap = attributesForHashCode;
			}
			else{
				//then the user has specified no special attributes to use for equality as we must use it all
				return this.s.equals(that.s);
			}
			
			Set<String> matchedObjects = new HashSet<String>();
			for(String cname : classesToCheck){
				
				List <Attribute> attsToCheck = attMap.get(cname);
				
				List <ObjectInstance> theseObjects = this.s.getObjectsOfTrueClass(cname);
				List <ObjectInstance> thoseObjects = that.s.getObjectsOfTrueClass(cname);
				
				if(theseObjects.size() != thoseObjects.size()){
					return false;
				}
				
				for(ObjectInstance o : theseObjects){
					
					boolean foundMatch = false;
					for(ObjectInstance oo : thoseObjects){
						String ooname = oo.getName();
						if(matchedObjects.contains(ooname)){
							continue;
						}
						if(this.objectsMatch(o, oo, attsToCheck)){
							foundMatch = true;
							matchedObjects.add(ooname);
							break;
						}
					}
					if(!foundMatch){
						return false;
					}
					
				}
				
			}
			
			
			
			return true;
			
		}
		
		
		
		protected boolean objectsMatch(ObjectInstance o, ObjectInstance oo, List <Attribute> attsToCheck){
			
			for(Attribute att : attsToCheck){
				Value ov = o.getValueForAttribute(att.name);
				Value oov = oo.getValueForAttribute(att.name);
				if(!ov.equals(oov)){
					return false;
				}
			}
			
			return true;
		}
		
		
		
	}

}
