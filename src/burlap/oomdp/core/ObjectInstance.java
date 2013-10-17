package burlap.oomdp.core;

import java.util.*;



public class ObjectInstance {
	
	private ObjectClass					obClass;			//object class to which this object belongs
	private String						name;				//name of the object for disambiguation
	private List <Value>				values;				//the values for each attribute
	
	private LinkedList <String>			pseudoClass;		//list of pseudo classes that have been assigned from parameterization
	
	private double [] 					obsFeatureVec;		//double representation of feature vector for easy manipulation (e.g., kd-trees)
	
	
	
	public ObjectInstance(ObjectClass obClass){
		
		this.obClass = obClass;
		this.name = "noname";
		this.pseudoClass = new LinkedList <String>();
		this.pseudoClass.add(obClass.name); //base pseudo class is the true class 
		
		this.initializeValueObjects();
		
	}
	
	public ObjectInstance(ObjectClass obClass, String name){
		
		this.obClass = obClass;
		this.name = name;
		this.pseudoClass = new LinkedList <String>();
		this.pseudoClass.add(obClass.name); //base pseudo class is the true class
		
		this.initializeValueObjects();
		
	}
	
	public ObjectInstance(ObjectInstance o){
		
		this.obClass = o.obClass;
		this.name = o.name;
		
		this.values = new ArrayList <Value>(obClass.numAttributes());
		for(Value v : o.values){
			values.add(v.copy());
		}
		
		this.pseudoClass = new LinkedList<String>();
		for(String pc : o.pseudoClass){
			pseudoClass.addLast(pc);
		}
		
		this.obsFeatureVec = new double[obClass.observableAttributeIndices.size()];
		for(int i = 0; i < o.obsFeatureVec.length; i++){
			obsFeatureVec[i] = o.obsFeatureVec[i];
		}
		
	}
	
	public ObjectInstance copy(){
		return new ObjectInstance(this);
	}
	
	
	
	public void initializeValueObjects(){
		
		obsFeatureVec = new double[obClass.observableAttributeIndices.size()];
		
		values = new ArrayList <Value>(obClass.numAttributes());
		for(Attribute att : obClass.attributeList){
			values.add(att.valueConstructor());
		}
		
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	
	public void pushPseudoClass(String c){
		pseudoClass.addFirst(c);
	}
	
	public void setValue(String attName, String v){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).setValue(v);
		this.computeRealVals();
	}
	
	public void setValue(String attName, double v){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).setValue(v);
		this.computeRealVals();
	}
	
	public void setValue(String attName, int v){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).setValue(v);
		this.computeRealVals();
	}
	
	public void addRelationalTarget(String attName, String target){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).addRelationalTarget(target);
	}
	
	public void clearRelationalTargets(String attName){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).clearRelationTargets();
	}
	
	public void setValues(List <Double> vs){
		
		for(int i = 0; i < vs.size(); i++){
			values.get(i).setValue(vs.get(i));
		}
		this.computeRealVals();
	}
	
	public void setObservableValues(List <Double> vs){
		
		for(int i = 0; i < vs.size(); i++){
			int ind = obClass.observableAttributeIndices.get(i);
			values.get(ind).setValue(vs.get(i));
		}
		this.computeRealVals();
		
	}
	
	public String getName(){
		return name;
	}
	
	
	public ObjectClass getObjectClass(){
		return obClass;
	}
	
	public String getTrueClassName(){
		return obClass.name;
	}
	
	public String getPseudoClass(){
		return pseudoClass.peek();
	}
	
	public String popPseudoClass(){
		//only pop the class if there are more than one
		//because the first element is the true class and can never be removed
		if(pseudoClass.size() > 1){
			return pseudoClass.poll();
		}
		return null;
	}
	
	public Value getValueForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind);
	}
	
	public double getRealValForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getRealVal();
	}
	
	public String getStringValForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getStringVal();
	}
	
	public int getDiscValForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getDiscVal();
	}
	
	public List <String> getAllRelationalTargets(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getAllRelationalTargets();
	}
	
	public List <Value> getValues(){
		return this.values;
	}
	
	public String getObjectDescription(){
		
		String desc = name + " (" + this.getPseudoClass() + ")\n";
		for(Value v : values){
			desc = desc + "\t" + v.attName() + ":\t" + v.getStringVal() + "\n";
		}
		
		return desc;
	
	}
	
	public String getObservableValueStringRep(){
		String res = String.valueOf(obsFeatureVec[0]);
		for(int i = 1; i < obsFeatureVec.length; i++){
			res = res + "," + String.valueOf(obsFeatureVec[i]);
		}
		return res;
	}
	
	public void computeRealVals(){
		
		for(int i = 0; i < obClass.observableAttributeIndices.size(); i++){
			
			int ind = obClass.observableAttributeIndices.get(i);
			obsFeatureVec[i] = values.get(ind).getNumericRepresentation();
			
		}
		
	}
	
	public double[] getObservableFeatureVec(){
		return obsFeatureVec;
	}
	
	
	
	
	public boolean equals(Object obj){
		ObjectInstance op = (ObjectInstance)obj;
		if(op.name.equals(name))
			return true;
		return false;
	}
	
	public boolean valueEquals(ObjectInstance obj){
	
		if(!obClass.equals(obj.obClass)){
			return false;
		}
	
		for(Value v : values){
		
			Value ov = obj.getValueForAttribute(v.attName());
			if(!v.equals(ov)){
				return false;
			}
		
		}
		
		return true;
	
	}
	
	
	public int hashCode(){
		return name.hashCode();
	}
	
	

}
