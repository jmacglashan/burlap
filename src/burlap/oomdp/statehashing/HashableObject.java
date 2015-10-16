package burlap.oomdp.statehashing;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.values.Value;

public class HashableObject implements ObjectInstance {
	public ObjectInstance source;
	public HashableObject(ObjectInstance source) {
		this.source = source;
	}

	@Override
	public ObjectInstance copy() {
		return new HashableObject(this.source);
	}

	@Override
	public ObjectInstance setName(String name) {
		return this.source.setName(name);
	}

	@Override
	public <T> ObjectInstance setValue(String attName, T v) {
		return this.source.setValue(attName, v);
	}

	@Override
	public ObjectInstance setValue(String attName, String v) {
		return this.setValue(attName, v);
	}

	@Override
	public ObjectInstance setValue(String attName, double v) {
		return this.setValue(attName, v);
	}

	@Override
	public ObjectInstance setValue(String attName, int v) {
		return this.setValue(attName, v);
	}

	@Override
	public ObjectInstance setValue(String attName, boolean v) {
		return this.setValue(attName, v);
	}

	@Override
	public ObjectInstance setValue(String attName, int[] v) {
		return this.setValue(attName, v);
	}

	@Override
	public ObjectInstance setValue(String attName, double[] v) {
		return this.setValue(attName, v);
	}

	@Override
	public ObjectInstance addRelationalTarget(String attName, String target) {
		return this.source.addRelationalTarget(attName, target);
	}

	@Override
	public ObjectInstance addAllRelationalTargets(String attName,
			Collection<String> targets) {
		return this.source.addAllRelationalTargets(attName, targets);
	}

	@Override
	public ObjectInstance clearRelationalTargets(String attName) {
		return this.source.clearRelationalTargets(attName);
	}

	@Override
	public ObjectInstance removeRelationalTarget(String attName, String target) {
		return this.source.removeRelationalTarget(attName, target);
	}

	@Override
	public String getName() {
		return this.source.getName();
	}

	@Override
	public ObjectClass getObjectClass() {
		return this.source.getObjectClass();
	}

	@Override
	public String getClassName() {
		return this.source.getClassName();
	}

	@Override
	public Value getValueForAttribute(String attName) {
		return this.source.getValueForAttribute(attName);
	}

	@Override
	public double getRealValForAttribute(String attName) {
		return this.source.getRealValForAttribute(attName);
	}

	@Override
	public double getNumericValForAttribute(String attName) {
		return this.getNumericValForAttribute(attName);
	}

	@Override
	public String getStringValForAttribute(String attName) {
		return this.getStringValForAttribute(attName);
	}

	@Override
	public int getIntValForAttribute(String attName) {
		return this.getIntValForAttribute(attName);
	}

	@Override
	public Set<String> getAllRelationalTargets(String attName) {
		return this.getAllRelationalTargets(attName);
	}

	@Override
	public boolean getBooleanValForAttribute(String attName) {
		return this.getBooleanValForAttribute(attName);
	}

	@Override
	public int[] getIntArrayValForAttribute(String attName) {
		return this.getIntArrayValForAttribute(attName);
	}

	@Override
	public double[] getDoubleArrayValForAttribute(String attName) {
		return this.getDoubleArrayValForAttribute(attName);
	}

	@Override
	public List<Value> getValues() {
		return this.source.getValues();
	}

	@Override
	public List<String> unsetAttributes() {
		return this.source.unsetAttributes();
	}

	@Override
	public String getObjectDescription() {
		return this.source.getObjectDescription();
	}

	@Override
	public StringBuilder buildObjectDescription(StringBuilder builder) {
		return this.source.buildObjectDescription(builder);
	}

	@Override
	public String getObjectDescriptionWithNullForUnsetAttributes() {
		return this.source.getObjectDescriptionWithNullForUnsetAttributes();
	}

	@Override
	public double[] getFeatureVec() {
		return this.source.getFeatureVec();
	}

	@Override
	public double[] getNormalizedFeatureVec() {
		return this.source.getNormalizedFeatureVec();
	}

	@Override
	public boolean valueEquals(ObjectInstance obj) {
		return this.source.valueEquals(obj);
	}
	
	public static abstract class CachedHashableObject extends HashableObject {
		protected int								hashCode;
		protected boolean							needToRecomputeHashCode;



		/**
		 * Initializes the StateHashTuple with the given {@link burlap.oomdp.core.states.State} object.
		 * @param s the state object this object will wrap
		 */
		public CachedHashableObject(ObjectInstance source){
			super(source);
			needToRecomputeHashCode = true;
		}


		/**
		 * This method computes the hashCode for this {@link HashableState}
		 * @return the hashcode for this state
		 */
		public abstract int computeHashCode();


		@Override
		public int hashCode(){
			if(needToRecomputeHashCode){
				this.hashCode = this.computeHashCode();
				this.needToRecomputeHashCode = false;
			}
			return hashCode;
		}
	}

}
