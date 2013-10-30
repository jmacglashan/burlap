package burlap.oomdp.core;

import java.util.*;

import javax.management.RuntimeErrorException;

import burlap.oomdp.core.values.DiscreteValue;
import burlap.oomdp.core.values.MultiTargetRelationalValue;
import burlap.oomdp.core.values.RealValue;
import burlap.oomdp.core.values.RelationalValue;

/**
 * The attribute class defines attributes that define OO-MDP object classes. There are different types of attributes
 * to support different types of value. Currently, there are attributes to support discrete values, real values, and
 * relational values. Discrete values are represented by an integer, but these integers can be specified to correspond
 * to categorical values represented as string. Discrete attributes require a finite domain (that is, it must be specified
 * how many different discrete values there are). There are two different kinds of real attributed; bounded and unbounded.
 * A a bounded real attribute should have a lower limit value and an upper limit value. Unbounded real attributes may span any
 * range. Both attributes' values are represented by double data types. There are also two kinds of relational
 * attributes: single target and multi-target. A single target relational attribute can only be linked to one other
 * object, whereas a multi-target relational attribute can be linked to set of other objects. In both cases, the relational
 * attribute can be unset to any target. The single-target relational attribute values are represented by strings the specify
 * the name reference of the object to which they are connected. Multi-target relational attribute values are represented
 * by an ordered set of string values indicating the targets to which the attribute is connected.
 * 
 * Attributes may also be specified as "hidden," which means that they should not be used by the agent planning/learning algorithms when resolving
 * the state. Hidden attributes may be useful for defining POMDP domains or in facilitating the generation of values
 * for observable attributes.
 *  
 * @author James MacGlashan
 * 
 */
public class Attribute {
	
	
	/**
	 * And enumeration type to indicate the various types of attributes supported. There is an special value
	 * for an unspecified TYPE, but in general this should not be used unless creation of values for it
	 * is handled with care.
	 * The integer correspondence of attributes types are:
	 * -1: NOTYPE (no type)
	 * 0: DISC (discrete)
	 * 1: REAL (bounded real)
	 * 2: REALUNBOUND (unbounded real)
	 * 3: RELATIONAL (single-target relational)
	 * 4: MULTITARGETRELATIONAL (multi-target relation)
	 * @author James MacGlashan
	 *
	 */
	public enum AttributeType{
		NOTYPE(-1),
		DISC(0),
		REAL(1),
		REALUNBOUND(2),
		RELATIONAL(3),
		MULTITARGETRELATIONAL(4);
		
		private final int intVal;
		
		AttributeType(int i){
			this.intVal = i;
		}
		
		public int toInt(){
			return intVal;
		}
		
		public static AttributeType fromInt(int i){
			switch(i){
				case 0:
					return DISC;
				case 1:
					return REAL;
				case 2:
					return REALUNBOUND;
				case 3:
					return RELATIONAL;
				case 4:
					return MULTITARGETRELATIONAL;
				default:
					return NOTYPE;
			}
		}
	}

	public String						name;				//name of the attribute
	public AttributeType				type;				//type of values attribute holds
	public Domain						domain;				//domain that holds this attribute
	public double						lowerLim;			//lowest value for a bounded real attribute
	public double						upperLim;			//highest value for a bounded real attribute
	public Map <String, Integer>		discValuesHash;		//maps names of discrete values to int values 
	public List <String>				discValues;			//list of discrete value names by their int value
	public boolean						hidden;				//whether this value is part of the state representation or is hidden from the agent
	
	/**
	 * 
	 * @param domain the domain that the attribute exists in
	 * @param name the name of the attribute
	 */
	public Attribute(Domain domain, String name){
		
		this.domain = domain;
		this.name = name;
		
		this.type = AttributeType.NOTYPE;
		this.discValuesHash = new HashMap <String, Integer>(0);
		this.discValues = new ArrayList <String>(0);
		
		this.lowerLim = 0.0;
		this.upperLim = 0.0;
		
		this.hidden = false;
		
		
		this.domain.addAttribute(this);
		
	}
	
	/**
	 * 
	 * @param domain the domain that the attribute exists in
	 * @param name the name of the attribute
	 * @param type the type of the attribute (discrete or real)
	 */
	public Attribute(Domain domain, String name, AttributeType type){
		
		this.domain = domain;
		this.name = name;
		
		this.type = type;
		this.discValuesHash = new HashMap <String, Integer>(0);
		this.discValues = new ArrayList <String>(0);
		
		this.lowerLim = 0.0;
		this.upperLim = 0.0;
		
		this.hidden = false;
		
		
		this.domain.addAttribute(this);
		
	}
	
	/**
	 * 
	 * @param domain the domain that the attribute exists in
	 * @param name the name of the attribute
	 * @param type the type of the attribute (discrete or real) in int form
	 */
	public Attribute(Domain domain, String name, int type){
		
		this.domain = domain;
		this.name = name;
		
		this.type = AttributeType.fromInt(type);
		this.discValuesHash = new HashMap <String, Integer>(0);
		this.discValues = new ArrayList <String>(0);
		
		this.lowerLim = 0.0;
		this.upperLim = 0.0;
		
		
		this.domain.addAttribute(this);
		
	}
	
	
	/**
	 * Will create a new Attribute object with the same configuration and name as this one.
	 * @param newDomain the domain to which the new attribute should be attached
	 * @return the new attribute object
	 */
	public Attribute copy(Domain newDomain){
		Attribute nd = new Attribute(newDomain, name, type);
		nd.lowerLim = this.lowerLim;
		nd.upperLim = this.upperLim;
		nd.discValues = new ArrayList<String>(discValues);
		nd.discValuesHash = new HashMap<String, Integer>(discValuesHash);
		nd.hidden = this.hidden;
		
		
		return nd;
	}
	
	/**
	 * Sets the upper and lower bound limits for a bounded real attribute.
	 * @param lower the lower limit
	 * @param upper the upper limit
	 */
	public void setLims(double lower, double upper){
		this.lowerLim = lower;
		this.upperLim = upper;
	}
	
	
	/**
	 * Sets the type for this attribute using the integer representation of types.
	 * See the AttributeType enum for more information on the correspondence of int
	 * values to the AttributeTypes.
	 * @param itype the integer representation of types.
	 */
	public void setType(int itype){
		this.type = AttributeType.fromInt(itype);
	}
	
	
	/**
	 * Sets the type for this attribute.
	 * @param type the attribute type to which this attribute should be set
	 */
	public void setType(AttributeType type){
		this.type = type;
	}
	
	
	/**
	 * Sets a discrete attribute's categorical values
	 * @param vals the list of categorical values for this discrete attribute
	 */
	public void setDiscValues(List <String> vals){
		this.discValues = new ArrayList <String> (vals);
		this.discValuesHash = new HashMap<String, Integer>();
		for(int i = 0; i < discValues.size(); i++){
			this.discValuesHash.put(vals.get(i), i);
		}
		
		//set range
		this.lowerLim = 0.0;
		this.upperLim = discValues.size()-1;
	}
	
	
	/**
	 * Sets a discrete attribute's categorical values.
	 * @param vals an array of categorical values for this discerte attribute
	 */
	public void setDiscValues(String [] vals){
		this.discValues = Arrays.asList(vals);
		this.discValuesHash = new HashMap<String, Integer>();
		for(int i = 0; i < discValues.size(); i++){
			this.discValuesHash.put(discValues.get(i), i);
		}
		
		//set range
		this.lowerLim = 0.0;
		this.upperLim = discValues.size()-1;
	}
	
	/**
	 * Sets the possible range of discrete values for the attribute. The categorical values
	 * will be set to the the string representation of each integer number.
	 * @param low the minimum int value for the attribute
	 * @param high the maximum int value for the attribute
	 * @param step the amount by which the int value will increase
	 */
	public void setDiscValuesForRange(int low, int high, int step){
	
		this.discValues = new ArrayList <String>();
		this.discValuesHash = new HashMap<String, Integer>();
		
		int counter = 0;
		for(int i = low; i <= high; i += step){
		
			String s = Integer.toString(i);
			
			this.discValues.add(s);
			this.discValuesHash.put(s, counter);
			
			counter++;
		}
		
		//set range
		this.lowerLim = 0.0;
		this.upperLim = discValues.size()-1;
	
	}
	
	
	/**
	 * Returns a Value object compatible with this Attributes type (i.e., discrete or real).
	 * This method will not work for NOTYPE attributes.
	 * @return a Value object compatible with this Attributes type (i.e., discrete or real)
	 */
	public Value valueConstructor(){
		if(this.type == Attribute.AttributeType.DISC){
			return new DiscreteValue(this);
		}
		else if(this.type == AttributeType.REAL || this.type == AttributeType.REALUNBOUND){
			return new RealValue(this);
		}
		else if(this.type == AttributeType.RELATIONAL){
			return new RelationalValue(this);
		}
		else if(this.type == AttributeType.MULTITARGETRELATIONAL){
			return new MultiTargetRelationalValue(this);
		}
		
		throw new RuntimeErrorException(new Error("Unknown attribute type; cannot construct a corresponding Value for it."));
	}
	
	
	public boolean equals(Object obj){
		Attribute op = (Attribute)obj;
		if(op.name.equals(name))
			return true;
		return false;
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	
	
	
}
