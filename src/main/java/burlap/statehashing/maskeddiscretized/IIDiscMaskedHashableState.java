package burlap.statehashing.maskeddiscretized;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.statehashing.simple.IISimpleHashableState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * @author James MacGlashan.
 */
public class IIDiscMaskedHashableState extends IISimpleHashableState {

	public DiscMaskedConfig config;

	public IIDiscMaskedHashableState() {
	}

	public IIDiscMaskedHashableState(DiscMaskedConfig config) {
		this.config = config;
	}

	public IIDiscMaskedHashableState(State s, DiscMaskedConfig config) {
		super(s);
		this.config = config;
	}

	@Override
	protected int computeOOHashCode(OOState s) {
		List<Integer> hashCodes = new ArrayList<Integer>(s.numObjects());
		List<ObjectInstance> objects = s.objects();
		for(int i = 0; i < s.numObjects(); i++){
			ObjectInstance o = objects.get(i);
			if(!config.maskedObjectClasses.contains(o.className())) {
				int oHash = this.computeFlatHashCode(o);
				int classNameHash = o.className().hashCode();
				int totalHash = oHash + 31 * classNameHash;
				hashCodes.add(totalHash);
			}
		}

		//sort for invariance to order
		Collections.sort(hashCodes);

		//combine
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
		for(int hashCode : hashCodes){
			hashCodeBuilder.append(hashCode);
		}

		return hashCodeBuilder.toHashCode();
	}

	@Override
	protected boolean ooStatesEqual(OOState s1, OOState s2) {
		if(s1 == s2){
			return true;
		}

		Set<String> matchedObjects = new HashSet<String>();
		for(Map.Entry<String, List<ObjectInstance>> e1 : OOStateUtilities.objectsByClass(s1).entrySet()){

			String oclass = e1.getKey();

			if(config.maskedObjectClasses.contains(oclass)){
				continue;
			}

			List<ObjectInstance> objects = e1.getValue();

			List<ObjectInstance> oobjects = s2.objectsOfClass(oclass);
			if(objects.size() != oobjects.size()){
				return false;
			}

			for(ObjectInstance o : objects){
				boolean foundMatch = false;
				for(ObjectInstance oo : oobjects){
					String ooname = oo.name();
					if(matchedObjects.contains(ooname)){
						continue;
					}
					if(flatStatesEqual(o, oo)){
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


	@Override
	protected void appendHashCodeForValue(HashCodeBuilder hashCodeBuilder, Object key, Object value) {

		if(config.maskedVariables.contains(key)){
			return ; //no need to incorporate hash codes for masked values
		}

		Double mult = config.keyWiseMultiples.get(key);
		if(mult == null){
			mult = config.defaultMultiple;
		}

		if(value instanceof Double || value instanceof Float){
			hashCodeBuilder.append(intMultiples(mult, ((Number)value).doubleValue()));
		}
		else if(value.getClass().isArray()){
			if(value instanceof double[]){
				double [] vals = (double[])value;
				for(int i = 0; i < vals.length; i++){
					hashCodeBuilder.append(intMultiples(mult, vals[i]));
				}
			}
			else if(value instanceof float[]){
				float [] vals = (float[])value;
				for(int i = 0; i < vals.length; i++){
					hashCodeBuilder.append(intMultiples(mult, vals[i]));
				}
			}
			else{
				super.appendHashCodeForValue(hashCodeBuilder, key, value);
			}
		}
		else{
			super.appendHashCodeForValue(hashCodeBuilder, key, value);
		}
	}

	@Override
	protected boolean valuesEqual(Object key, Object v1, Object v2) {
		if(config.maskedVariables.contains(key)){
			return true;
		}

		Double mult = config.keyWiseMultiples.get(key);
		if(mult == null){
			mult = config.defaultMultiple;
		}

		if(v1 instanceof Double || v1 instanceof Float){

			Double dv1 = ((Number)v1).doubleValue();
			Double dv2 = ((Number)v2).doubleValue();

			return intMultiples(mult, dv1) == intMultiples(mult, dv2);

		}
		else if(v1.getClass().isArray()){
			if(v1 instanceof double[]){
				double [] vals1 = (double[])v1;
				double [] vals2 = (double[])v2;
				if(vals1.length != vals2.length){
					return false;
				}
				for(int i = 0; i < vals1.length; i++){
					if(intMultiples(mult, vals1[i]) != intMultiples(mult, vals2[i])){
						return false;
					}
				}
				return true;
			}
			else if(v1 instanceof float[]){
				float [] vals1 = (float[])v1;
				float [] vals2 = (float[])v2;
				if(vals1.length != vals2.length){
					return false;
				}
				for(int i = 0; i < vals1.length; i++){
					if(intMultiples(mult, vals1[i]) != intMultiples(mult, vals2[i])){
						return false;
					}
				}
				return true;
			}
			else{
				return super.valuesEqual(key, v1, v2);
			}
		}
		else{
			return super.valuesEqual(key, v1, v2);
		}
	}


	/**
	 * Returns int result of num / mult; that is, (int)(num / mult).
	 * @param mult the multiple
	 * @param num the number
	 * @return the int result of num / mult
	 */
	protected static int intMultiples(double mult, double num){
		int div = (int)Math.floor(num / mult);
		return div;
	}


}
