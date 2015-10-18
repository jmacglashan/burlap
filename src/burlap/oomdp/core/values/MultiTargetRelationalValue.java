package burlap.oomdp.core.values;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Attribute;


/**
 * A multi-target relational value object subclass. Values are stored as an ordered set (TreeSet) of string names
 * of the object instance name identifiers. If the attribute is not linked to any target, the set will be empty.
 * @author James MacGlashan
 *
 */
public class MultiTargetRelationalValue extends OOMDPValue implements Value{

	/**
	 * The set of object targets to which this value points. Object targets are indicated
	 * by their object name identifier.
	 */
	protected final Set <String>		targetObjects;
	
	
	/**
	 * Initializes the value to be associted with the given attribute
	 * @param attribute the attribute with which this value is associated
	 */
	public MultiTargetRelationalValue(Attribute attribute){
		super(attribute);
		this.targetObjects = Collections.unmodifiableSet(new TreeSet<String>());
	}
	
	
	/**
	 * Initializes this value as a copy from the source Value object v.
	 * @param v the source Value to make this object a copy of.
	 */
	public MultiTargetRelationalValue(MultiTargetRelationalValue v){
		super(v);
		MultiTargetRelationalValue rv = (MultiTargetRelationalValue)v;
		this.targetObjects = rv.targetObjects;
	}
	
	public MultiTargetRelationalValue(Attribute attribute, Collection<String> targets) {
		super(attribute);
		this.targetObjects = Collections.unmodifiableSet(new TreeSet<String>(targets));
	}
	
	
	
	@Override
	public Value copy() {
		return new MultiTargetRelationalValue(this);
	}

	@Override
	public boolean valueHasBeenSet() {
		return true;
	}

	@Override
	public Value setValue(String v) {
		if(v.indexOf(';') != -1){
			return new MultiTargetRelationalValue(this.attribute, Arrays.asList(v.split(";")));
		}
		return new MultiTargetRelationalValue(this.attribute, Arrays.asList(v));
	}
	
	@Override
	public Value addRelationalTarget(String t) {
		TreeSet<String> targets = new TreeSet<String>(this.targetObjects);
		targets.add(t);
		return new MultiTargetRelationalValue(this.attribute, targets);
	}
	
	@Override
	public Value addAllRelationalTargets(Collection<String> targets) {
		TreeSet<String> allTargets = new TreeSet<String>(this.targetObjects);
		allTargets.addAll(targets);
		return new MultiTargetRelationalValue(this.attribute, allTargets);
	}
	
	
	@Override
	public Value clearRelationTargets() {
		return new MultiTargetRelationalValue(this.attribute);
	}
	
	@Override
	public Value removeRelationalTarget(String target) {
		TreeSet<String> targets = new TreeSet<String>(this.targetObjects);
		targets.remove(target);
		return new MultiTargetRelationalValue(this.attribute, targets);
	}
	
	@Override
	public Set<String> getAllRelationalTargets() {
		return this.targetObjects;
	}
	

	@Override
	public StringBuilder buildStringVal(StringBuilder builder) {
		boolean didFirst = false;
		for(String t : this.targetObjects){
			if(didFirst){
				builder.append(";");
			}
			builder.append(t);
			didFirst = true;
		}
		return builder;
	}

	@Override
	public double getNumericRepresentation() {
		return 0;
	}
	
	
	@Override
	public boolean equals(Object obj){
		if (this == obj) {
			return true;
		}
		if(!(obj instanceof MultiTargetRelationalValue)){
			return false;
		}
		
		MultiTargetRelationalValue op = (MultiTargetRelationalValue)obj;
		if(!op.attribute.equals(attribute)){
			return false;
		}
		
		if(this.targetObjects.size() != op.targetObjects.size()){
			return false;
		}
		
		for(String t : this.targetObjects){
			if(!op.targetObjects.contains(t)){
				return false;
			}
		}
		
		return true;
		
	}
}
