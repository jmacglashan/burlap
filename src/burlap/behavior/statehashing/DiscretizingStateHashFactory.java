package burlap.behavior.statehashing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.Value;


/**
 * This hashing factory is used for comparing states with continuous attributes as if they were discretized, thereby allowing discrete state planning/learning
 * algorithms to be used on domains with continuous attributes. Note that the domain may also include discrete attributes, which will be treated normally in state
 * comparisons. 
 * <p/>
 * By default, all continuous attributes will be discretized to their corresponding integer value when making state comparisons and performing hashing.
 * However, attributes may also be floored to some other multiple specified by the client. For instnace, if 0.5 is set as the multiple, then,
 * values will be discretized to 0.5 intervals; e.g., 0.49 would become 0, 0.51 would become 0.5, 9.73 would become 9.5, and 102.3 would become 102. Note using a 
 * multiple value of 1.0 is equivlent to floor values to their corresponding int value. Large multiple values result in course discretization and small
 * multiple values result in a fine discretization.
 * <p/>
 * The multiple used can be specified for individual attributes so that different attributes have different degrees of discretization. To set
 * multiple used for an specific attribute, use the {@link #addFloorDiscretizingMultipleFor(String, double)} method. When a continuous attribute is to be
 * hashed or compared, it is first checked if there has been a specific multiple value set for it. If so, that multiple is used for discretization. If not,
 * the default multiple is used. By defualt, the default multiple is 1.0 (integer floor discretization), but the default multiple may also be set
 * using the {@link #setDefaultFloorDiscretizingMultiple(double)} method.
 * 
 * @author James MacGlashan
 *
 */
public class DiscretizingStateHashFactory implements StateHashFactory {

	
	/**
	 * The multiples to use for specific attributes
	 */
	protected Map<String, Double> attributeWiseMultiples = new HashMap<String, Double>();
	
	/**
	 * The default multiple to use for any continuous attributes that have not been specifically set. The default is 1.0, whihc
	 * corresponds to integer floor discretization.
	 */
	protected double defaultMultiple = 1.;
	
	
	/**
	 * Initializes. the default multiple is set to 1.0 which corresponds to integer floor discretization.
	 */
	public DiscretizingStateHashFactory(){
		
	}
	
	
	/**
	 * Initializes with a specified default multiple to use for discretization. Any continuous attributes that do not
	 * have a specific multiple set for them will use the default.
	 * @param defaultMultiple the default multiple to be used by discretization.
	 */
	public DiscretizingStateHashFactory(double defaultMultiple){
		this.defaultMultiple = defaultMultiple;
	}
	
	
	/**
	 * Sets the multiple to use for discretization for the attribute with the specified name. See the documentation
	 * of this class for more information on how the multiple works. In short, continuous values will be floored
	 * to the greatest value that is a multiple of the multiple given and less than or equal to the true value.
	 * @param attributeName the name of the attribute whose discretization multiple is being set.
	 * @param nearestMultipleValue the multiple to which values are floored.
	 */
	public void addFloorDiscretizingMultipleFor(String attributeName, double nearestMultipleValue){
		this.attributeWiseMultiples.put(attributeName, nearestMultipleValue);
	}
	
	
	/**
	 * Sets the default multiple to use for continuous attributes that do not have specific multiples set
	 * for them. See the documentation
	 * of this class for more information on how the multiple works. In short, continuous values will be floored
	 * to the greatest value that is a multiple of the multiple given and less than or equal to the true value.
	 * @param defaultMultiple the default multiple to which values are floored
	 */
	public void setDefaultFloorDiscretizingMultiple(double defaultMultiple){
		this.defaultMultiple = defaultMultiple;
	}
	
	
	@Override
	public StateHashTuple hashState(State s) {
		return new DiscretizedStateHashTuple(s);
	}
	
	
	/**
	 * Returns int result of num / mult; that is, (int)(num / mult).
	 * @param mult the multiple
	 * @param num the number
	 * @return the int result of num / mult
	 */
	protected static int intMultiples(double mult, double num){
		int div = (int)(num / mult);
		return div;
	}
	
	
	/**
	 * The StateHashTuple that is generated by objects of the {@link DiscretizingStateHashFactory} class.
	 * @author James MacGlashan
	 *
	 */
	public class DiscretizedStateHashTuple extends StateHashTuple{

		public DiscretizedStateHashTuple(State s) {
			super(s);
		}

		@Override
		public void computeHashCode() {
			
			List <String> objectClasses = this.getOrderedClasses();
			int totalVol = 1;
			hashCode = 0;
			for(String oclass : objectClasses){
				List <ObjectInstance> obs = s.getObjectsOfTrueClass(oclass);
				ObjectClass oc = obs.get(0).getObjectClass();
				int vol = this.computeVolumeForClass(oc);
				
				//to ensure object order invariance, the hash values must first be sorted by their object-wise hashcode
				int [] obHashCodes = new int[obs.size()];
				for(int i = 0; i < obs.size(); i++){
					obHashCodes[i] = this.getIndexValue(obs.get(i), oc);
				}
				Arrays.sort(obHashCodes);
				
				//multiply in reverse (for smaller total hash codes)
				for(int i = obHashCodes.length-1; i >= 0; i--){
					hashCode += obHashCodes[i]*totalVol;
					totalVol *= vol;
				}
				
			}
			
			needToRecomputeHashCode = false;
			
		}
		
		
		//this method will assume that attributes are all discrete
		private int getIndexValue(ObjectInstance o, ObjectClass oc){
			
			List <Attribute> attributes = this.getAttributesForClass(oc);
			int index = 0;
			int vol = 1;
			for(Attribute att : attributes){
				
				if(att.type==AttributeType.DISC || att.type == AttributeType.BOOLEAN){
					index += o.getDiscValForAttribute(att.name)*vol;
					vol *= att.discValues.size();
				}
				else if(att.type==AttributeType.INT){
					index += o.getDiscValForAttribute(att.name)*vol;
					vol *= 31;
				}
				else if(att.type==AttributeType.STRING){
					index += o.getStringValForAttribute(att.name).hashCode()*vol;
					vol *= 31;
				}
				else if(att.type==AttributeType.INTARRAY){
					index += this.intArrayCode(o.getIntArrayValForAttribute(att.name))*vol;
					vol *= 31;
				}
				else if(att.type==AttributeType.REAL || att.type == AttributeType.REALUNBOUND){
					index += this.getDiscrteizedValue(att, o.getRealValForAttribute(att.name))*vol;
					vol *= 31;
				}
				else if(att.type==AttributeType.DOUBLEARRAY){
					double [] dblArray = o.getDoubleArrayValForAttribute(att.name);
					int [] intArray = new int[dblArray.length];
					for(int i = 0; i < intArray.length; i++){
						intArray[i] = this.getDiscrteizedValue(att, dblArray[i]);
					}
					index += this.intArrayCode(o.getIntArrayValForAttribute(att.name))*vol;
					vol *= 31;
				}
				else{
					throw new RuntimeException("DiscretizingStateHashFactory cannot compute values for relational attributes");
				}
				
			}
			
			return index;
			
		}
		
		/**
		 * Returns an int value for an int array
		 * @param array the int array
		 * @return an int value for it
		 */
		protected int intArrayCode(int [] array){
			int sum = 0;
			for(int i : array){
				sum *= 31;
				sum += i;
			}
			return sum;
		}
		
		protected int getDiscrteizedValue(Attribute att, double val){
			Double mult = attributeWiseMultiples.get(att.name);
			if(mult != null){
				return intMultiples(mult, val);
			}
			return intMultiples(defaultMultiple, val);
		}
		
		//this method will assume that attributes are all discrete
		private int computeVolumeForClass(ObjectClass oclass){
			
			List <Attribute> attributes = this.getAttributesForClass(oclass);
			int vol = 1;
			for(Attribute att : attributes){
				if(att.type==AttributeType.DISC || att.type == AttributeType.BOOLEAN){
					vol *= att.discValues.size();
				}
				else if(att.type==AttributeType.INT || att.type == AttributeType.STRING){
					vol *= 31;
				}
				else if(att.type==AttributeType.REAL || att.type == AttributeType.REALUNBOUND){
					vol *= 31;
				}
				else if(att.type == AttributeType.INTARRAY || att.type == AttributeType.DOUBLEARRAY){
					vol *= 31;
				}
				else{
					throw new RuntimeException("DiscretizingStateHashFactory cannot compute values for relational attributes");
				}
			}
			
			return vol;
		}
		
		private List <Attribute> getAttributesForClass(ObjectClass oc){
			
			//then default to using all attributes for all object classes; maybe make this more general later
			return oc.attributeList;
		}
		
		private List <String> getOrderedClasses(){
			List <String> objectClasses = new ArrayList<String>(s.getObjectClassesPresent());
			Collections.sort(objectClasses);
			return objectClasses;
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
			
			Set <String> classesToCheck = new HashSet<String>();
			for(ObjectInstance o : that.s.getAllObjects()){
				classesToCheck.add(o.getObjectClass().name);
			}
			for(ObjectInstance o : this.s.getAllObjects()){
				classesToCheck.add(o.getObjectClass().name);
			}
			
			Set<String> matchedObjects = new HashSet<String>();
			for(String cname : classesToCheck){
				
				
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
						if(this.objectsMatch(o, oo)){
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
		
		
		/**
		 * Compares the values for each attribute of the objects, assuming they are the same class. To return true,
		 * all discrete values much be the same and all continuous values must discretize to the same value.
		 * @param o one object to compare
		 * @param oo another object to compare that is assumed to be the same class
		 * @return whether o and oo are equivelent after discretization of their continuous attribute values.
		 */
		protected boolean objectsMatch(ObjectInstance o, ObjectInstance oo){
			
			for(Attribute att : o.getObjectClass().attributeList){
				if(att.type == AttributeType.BOOLEAN || att.type == AttributeType.DISC || att.type == AttributeType.INT 
						|| att.type == AttributeType.STRING || att.type == AttributeType.INTARRAY){
					Value ov = o.getValueForAttribute(att.name);
					Value oov = oo.getValueForAttribute(att.name);
					if(!ov.equals(oov)){
						return false;
					}
				}
				else if(att.type == AttributeType.REAL || att.type == AttributeType.REALUNBOUND){
					
					Double Mult = attributeWiseMultiples.get(att.name);
					double mult = defaultMultiple;
					if(Mult != null){
						mult = Mult;
					}
					
					
					double v = o.getRealValForAttribute(att.name);
					double ov = o.getRealValForAttribute(att.name);
					
					int iv = intMultiples(mult, v);
					int oiv = intMultiples(mult, ov);
					
					if(iv != oiv){
						return false;
					}
					
				}
				else if(att.type == AttributeType.DOUBLEARRAY){
					double [] dblArray1 = o.getDoubleArrayValForAttribute(att.name);
					double [] dblArray2 = oo.getDoubleArrayValForAttribute(att.name);
					if(dblArray1.length != dblArray2.length){
						return false;
					}
					for(int i = 0; i < dblArray1.length; i++){
						if(this.getDiscrteizedValue(att, dblArray1[i]) != this.getDiscrteizedValue(att, dblArray2[i])){
							return false;
						}
					}
				}
			}
			
			return true;
		}
		
		
	}
	
	
	

}