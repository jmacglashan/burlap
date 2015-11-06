package burlap.oomdp.core;

import java.util.*;


/**
 * Object classes are part of the OO-MDP definition and define the kinds of objects that can exist in an OO-MDP state.
 * Object classes have a name to identify them and a set of attributes that define them. Object classes may also be
 * specified as "hidden" which means that they should not be used by the agent planning/learning algorithms when resolving
 * the state. Hidden object classes may be useful for defining POMDP domains or in facilitating the generation of values
 * for observable object classes.
 * @author James MacGlashan
 *
 */
public class ObjectClass {
	
	public String							name;							//name of the object class
	public Domain							domain;							//back pointer to host domain
	public Map <String, Integer>			attributeIndex;					//map from attribute name to feature vector index
	public Map <String, Attribute>			attributeMap;					//map from attribute name to the defining attribute
	public List <Attribute>					attributeList;					//definitions of object attributes
	public boolean							hidden;							//whether this is a hidden object class from the agent, but defines the state space
	
	
	/**
	 * Initializes the attribute indexing data structures, connects this object class
	 * to the specified domain, and automatically connects the domain to this object class.
	 * @param domain the domain to which this object class belongs
	 * @param name the name identifier for this object class
	 */
	public ObjectClass(Domain domain, String name){
		
		this.name = name;
		this.domain = domain;
		this.attributeIndex = new HashMap <String, Integer>();
		this.attributeMap = new HashMap <String, Attribute>();
		this.attributeList = new ArrayList <Attribute>();
		this.hidden = false;
		
		this.domain.addObjectClass(this);
		
		
	}
	
	/**
	 * Initializes the attribute indexing data structures, connects this object class
	 * to the specified domain, and automatically connects the domain to this object class.
	 * @param domain the domain to which this object class belongs
	 * @param name the name identifier for this object class
	 * @param hidden whether this object class is hidden or not
	 */
	public ObjectClass(Domain domain, String name, boolean hidden){
		
		this.name = name;
		this.domain = domain;
		this.attributeIndex = new HashMap <String, Integer>();
		this.attributeMap = new HashMap <String, Attribute>();
		this.attributeList = new ArrayList <Attribute>();
		this.hidden = hidden;
		this.domain.addObjectClass(this);
		
		
	}
	
	
	/**
	 * Will create and return a new ObjectClass object with copies of this object class' attributes
	 * @param newDomain the domain to which the new object class should be attached
	 * @return the new ObjectClass object
	 */
	public ObjectClass copy(Domain newDomain){
		ObjectClass noc = new ObjectClass(newDomain, name);
		for(Attribute att : attributeList){
			noc.addAttribute(att.copy(newDomain));
		}
		
		return noc;
	}
	
	
	/**
	 * Returns the attribute with the given name
	 * @param name the name of the attribute to return
	 * @return the attribute with the given name, or null, if this object class is not defined by an attribute with the given name.
	 */
	public Attribute getAttribute(String name){
		return this.attributeMap.get(name);
	}
	
	/**
	 * Sets the attributes used to define this object class
	 * @param atts the attributes to define this object class
	 */
	public void setAttributes(List <Attribute> atts){
		
		attributeList.clear();
		attributeMap.clear();
		attributeIndex.clear();
		
		for(Attribute att: atts){
			this.addAttribute(att);
		}
		
	}
	
	
	/**
	 * Adds an attribute to define this object class
	 * @param att the attribute to add
	 */
	public void addAttribute(Attribute att){
		
		//only add if it is new
		if(this.hasAttribute(att)){
			return ;
		}
		
		int ind = attributeList.size();
		
		attributeList.add(att);
		attributeMap.put(att.name, att);
		attributeIndex.put(att.name, ind);

		
	}
	
	
	/**
	 * Return whether this object class is defined by the given attribute
	 * @param att the attribute to test
	 * @return true if this object class is defined by attribute att; false otherwise.
	 */
	public boolean hasAttribute(Attribute att){
		return this.hasAttribute(att.name);
	}
	
	/**
	 * Return whether this object class is defined by the attribute with the given name
	 * @param attName the name of the attribute to test
	 * @return true if this object class is defined by an attribute with the name attName; false otherwise
	 */
	public boolean hasAttribute(String attName){	
		return attributeMap.containsKey(attName);
	}
	
	/**
	 * Returns the internally stored index of the attribute with the given name. A runtime exception is thrown
	 * if this object class is not defined by an attribute named attName
	 * @param attName the name of the attribute for which to get the index
	 * @return the index of the attribute with name attName
	 */
	public int attributeIndex(String attName){
		return attributeIndex.get(attName);
//		if(ind != null){
//			return ind;
//		}
		//throw new RuntimeException("The attribute " + attName + " is not defined for this object class (" + this.name + ")");
	}
	
	
	/**
	 * Returns the number of attributes that define this object class.
	 * @return the number of attributes that define this object class.
	 */
	public int numAttributes(){
		return attributeList.size();
	}

	
	
	
	
}
