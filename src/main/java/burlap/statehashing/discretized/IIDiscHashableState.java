package burlap.statehashing.discretized;

import burlap.mdp.core.state.State;
import burlap.statehashing.simple.IISimpleHashableState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author James MacGlashan.
 */
public class IIDiscHashableState extends IISimpleHashableState {

	public DiscConfig config = new DiscConfig(1.);

	public IIDiscHashableState() {
	}

	public IIDiscHashableState(DiscConfig config) {
		this.config = config;
	}

	public IIDiscHashableState(State s, DiscConfig config) {
		super(s);
		this.config = config;
	}

	@Override
	protected void appendHashCodeForValue(HashCodeBuilder hashCodeBuilder, Object key, Object value) {

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
