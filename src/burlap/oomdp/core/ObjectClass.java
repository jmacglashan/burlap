package burlap.oomdp.core;

import java.util.*;

public class ObjectClass {
	
	public String							name;							//name of the object class
	public Domain							domain;							//back pointer to host domain
	public Map <String, Integer>			attributeIndex;					//map from attribute name to feature vector index
	public Map <String, Attribute>			attributeMap;					//map from attribute name to the defining attribute
	public List <Attribute>					attributeList;					//definitions of object attributes
	public List <Integer>					observableAttributeIndices;		//feature vector index of only attributes that are observable to the world
	public boolean							hidden;							//whether this is a hidden object class for facilitating transition dynamics and observable objects
	
	
	public ObjectClass(Domain domain, String name){
		
		this.name = name;
		this.domain = domain;
		this.attributeIndex = new HashMap <String, Integer>();
		this.attributeMap = new HashMap <String, Attribute>();
		this.attributeList = new ArrayList <Attribute>();
		this.observableAttributeIndices = new ArrayList <Integer>();
		this.hidden = false;
		
		this.domain.addObjectClass(this);
		
		
	}
	
	public ObjectClass(Domain domain, String name, boolean hidden){
		
		this.name = name;
		this.domain = domain;
		this.attributeIndex = new HashMap <String, Integer>();
		this.attributeMap = new HashMap <String, Attribute>();
		this.attributeList = new ArrayList <Attribute>();
		this.observableAttributeIndices = new ArrayList <Integer>();
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
	
	
	public void setAttributes(List <Attribute> atts){
		
		attributeList.clear();
		observableAttributeIndices.clear();
		attributeMap.clear();
		attributeIndex.clear();
		
		for(Attribute att: atts){
			this.addAttribute(att);
		}
		
	}
	
	
	public void addAttribute(Attribute att){
		
		//only add if it is new
		if(this.hasAttribute(att)){
			return ;
		}
		
		int ind = attributeList.size();
		
		attributeList.add(att);
		attributeMap.put(att.name, att);
		attributeIndex.put(att.name, ind);
		
		if(!att.hidden){
			observableAttributeIndices.add(ind);
		}
		
	}
	
	
	public boolean hasAttribute(Attribute att){
		return this.hasAttribute(att.name);
	}
	
	public boolean hasAttribute(String attName){	
		return attributeMap.containsKey(attName);
	}
	
	public int attributeIndex(String attName){
		Integer ind = attributeIndex.get(attName);
		if(ind != null){
			return ind;
		}
		return -1;
	}
	
	public int numAttributes(){
		return attributeList.size();
	}
	
	
	
	
}
