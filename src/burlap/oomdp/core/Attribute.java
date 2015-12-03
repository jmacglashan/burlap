package burlap.oomdp.core;

import burlap.oomdp.core.values.*;

import javax.management.RuntimeErrorException;
import java.util.*;

/**
 * The attribute class defines attributes that define OO-MDP object classes. There are different types of attributes
 * to support different types of value. Currently, there are attributes to support discrete values, real values, string values, and
 * relational values. There are two different kinds of discrete values, int and disc. While both are internally stored as integers,
 * the int value may be unbounded in range, whereas disc values have a finite domain and also refer to specific categorical values 
 * represented as string. Discrete attributes require a finite domain (that is, it must be specified
 * how many different discrete values there are). There are two different kinds of real attributes; bounded and unbounded.
 * A a bounded real attribute should have a lower limit value and an upper limit value. Unbounded real attributes may span any
 * range. Both attributes' values are represented by double data types. There are also two kinds of relational
 * attributes: single target and multi-target. A single target relational attribute can only be linked to one other
 * object, whereas a multi-target relational attribute can be linked to a set of other objects. In both cases, the relational
 * attribute can be unset to any target. The single-target relational attribute values are represented by strings that specify
 * the name reference of the object to which they are connected. Multi-target relational attribute values are represented
 * by an ordered set of string values indicating the targets to which the attribute is connected.
 *
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
	 * 5: INT (discrete int, but not categorical)
	 * 6: BOOLEAN (0, or 1, discrete value for false, true)
	 * 7: STRING (a string)
	 * 8: INTARRAY (an attribute whose value is an int[] type)
	 * 9: DOUBLEARRAY (an attribute whose value is a double[] type)
	 * @author James MacGlashan
	 *
	 */
	public enum AttributeType{
		NOTYPE(-1),
		DISC(0),
		REAL(1),
		REALUNBOUND(2),
		RELATIONAL(3),
		MULTITARGETRELATIONAL(4),
		INT(5),
		BOOLEAN(6),
		STRING(7),
		INTARRAY(8),
		DOUBLEARRAY(9);
		
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
				case 5:
					return INT;
				case 6:
					return BOOLEAN;
				case 7: 
					return STRING;
				case 8:
					return INTARRAY;
				case 9:
					return DOUBLEARRAY;
				default:
					return NOTYPE;
			}
		}
	}

	/**
	 * name of the attribute
	 */
	public String						name;
	
	/**
	 * type of values attribute holds
	 */
	public AttributeType				type;
	
	/**
	 * domain that holds this attribute
	 */
	public Domain						domain;
	
	/**
	 * lowest value for a non-relational attribute
	 */
	public double						lowerLim;
	
	/**
	 * highest value for a non-relational attribute
	 */
	public double						upperLim;
	
	/**
	 * maps categorical names of discrete values to int values
	 */
	public Map <String, Integer>		discValuesHash;
	
	/**
	 * The possible categorical values for a discrete or boolean attribute.
	 */
	public List <String>				discValues;

	
	/**
	 * Constructs an attribute with an unspecified type that will need to be specified later.
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
		
		
		this.domain.addAttribute(this);
		
	}
	
	/**
	 * Constructs with a given attribute type. If the attribute type is set to boolean, the categorical values will automatically
	 * be initialized to "false" and "true" with a range from 0 to 1. If the attribute type is relational,
	 * then the domain is automatically set to object identifier dependent.
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

		
		if(this.type == AttributeType.BOOLEAN){
			this.discValues.add("false");
			this.discValues.add("true");
			this.discValuesHash.put("false", 0);
			this.discValuesHash.put("true", 1);
			
			this.lowerLim = 0.;
			this.upperLim = 1.;
		}
		
		if(this.type == AttributeType.REALUNBOUND){
			this.lowerLim = Double.NEGATIVE_INFINITY;
			this.upperLim = Double.POSITIVE_INFINITY;
		}
		
		if(this.type == AttributeType.INT){
			this.lowerLim = Integer.MIN_VALUE;
			this.upperLim = Integer.MAX_VALUE;
		}
		
		if(this.type == AttributeType.RELATIONAL || this.type == AttributeType.MULTITARGETRELATIONAL){
			this.domain.setObjectIdentiferDependence(true);
		}
		
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
	 * Sets the type for this attribute.
	 * If the attribute type is set to boolean, the categorical values will automatically
	 * be initialized to "false" and "true" with a range from 0 to 1. If the attribute type is relational,
	 * then the domain is automatically set to object identifier dependent.
	 * @param type the attribute type to which this attribute should be set
	 */
	public void setType(AttributeType type){
		this.type = type;
		
		if(this.type == AttributeType.BOOLEAN){
			this.discValues.add("false");
			this.discValues.add("true");
			this.discValuesHash.put("false", 0);
			this.discValuesHash.put("true", 1);
			
			this.lowerLim = 0.;
			this.upperLim = 1.;
		}
		
		if(this.type == AttributeType.REALUNBOUND){
			this.lowerLim = Double.NEGATIVE_INFINITY;
			this.upperLim = Double.POSITIVE_INFINITY;
		}
		
		if(this.type == AttributeType.INT){
			this.lowerLim = Integer.MIN_VALUE;
			this.upperLim = Integer.MAX_VALUE;
		}
		
		if(this.type == AttributeType.RELATIONAL || this.type == AttributeType.MULTITARGETRELATIONAL){
			this.domain.setObjectIdentiferDependence(true);
		}
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
	 * Sets the possible range of discrete (@link {@link AttributeType#DISC}) values for the attribute. The categorical values
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
		if(this.type == Attribute.AttributeType.DISC || this.type == AttributeType.BOOLEAN){
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
		else if(this.type == AttributeType.INT){
			return new IntValue(this);
		}
		else if(this.type == AttributeType.STRING){
			return new StringValue(this);
		}
		else if(this.type == AttributeType.INTARRAY){
			return new IntArrayValue(this);
		}
		else if(this.type == AttributeType.DOUBLEARRAY){
			return new DoubleArrayValue(this);
		}
		
		throw new RuntimeErrorException(new Error("Unknown attribute type; cannot construct a corresponding Value for it."));
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj) {
			return true;
		}
		Attribute op = (Attribute)obj;
		if(op.name.equals(name))
			return true;
		return false;
	}
	
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}
	
	
	
	
}
