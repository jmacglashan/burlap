package burlap.oomdp.core;

import java.util.*;

/**
 * 
 * @author James
 * 
 */
public class Attribute {
	
	public enum AttributeType{
		NOTYPE(-1),
		DISC(0),
		REAL(1),
		REALUNBOUND(2);
		
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
	
	public void setLims(double lower, double upper){
		this.lowerLim = lower;
		this.upperLim = upper;
	}
	
	
	
	public void setType(int itype){
		this.type = AttributeType.fromInt(itype);
	}
	
	public void setType(AttributeType type){
		this.type = type;
	}
	
	
	public void setDiscValues(List <String> vals){
		this.discValues = new ArrayList <String> (vals);
		this.discValuesHash = new HashMap<String, Integer>();
		for(int i = 0; i < discValues.size(); i++){
			this.discValuesHash.put(vals.get(i), new Integer(i));
		}
		
		//set range
		this.lowerLim = 0.0;
		this.upperLim = discValues.size()-1;
	}
	
	/**
	 * Sets the possible range of discrete values for the attribute
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
