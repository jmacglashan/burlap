package burlap.behavior.singleagent.vfa.common;

import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.vardomain.StateDomain;
import burlap.mdp.core.state.vardomain.VariableDomain;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is will construct a double array from states by iterating over numeric state variables and setting values
 * in the double array to the variables normalized value. To set a normalized value, you must specify the domain of
 * each variable key that is used in the produced vector. Set these with the {@link #variableDomain(Object, VariableDomain)}
 * method. If you do not set a range for a variable, then that variable will not have a corresponding entry in the
 * constructed double array. If a state implements {@link StateDomain}, then you can give an example state to the
 * {@link #useAllDomains(StateDomain)} method, and it will automatically define the ranges for all variables that have
 * corresponding {@link VariableDomain} entries defined in teh {@link StateDomain}.
 * @author James MacGlashan.
 */
public class NormalizedVariablesVectorGenerator implements StateToFeatureVectorGenerator{

	protected Map<Object, VariableDomain> ranges = new HashMap<Object, VariableDomain>();

	/**
	 * Sets the variable range for the given variable.
	 * @param key the variable key
	 * @param range the range of the variable.
	 * @return this object, so a builder design pattern may be used.
	 */
	public NormalizedVariablesVectorGenerator variableDomain(Object key, VariableDomain range){
		ranges.put(key, range);
		return this;
	}

	/**
	 * Goes through the state and sets the ranges for all variables that have a {@link VariableDomain} set. If
	 * the {@link StateDomain#domain(Object)} method returns null for a state variable key, then a range will not be set
	 * and the variable will be skipped (unless a range is then manually added with the {@link #variableDomain(Object, VariableDomain)}
	 * method).
	 * @param state the {@link StateDomain} to query for the the state ranges.
	 * @return this object, so a builder design pattern may be used
	 */
	public NormalizedVariablesVectorGenerator useAllDomains(StateDomain state){
		for(Object key : state.variableKeys()){
			VariableDomain range = state.domain(key);
			if(range != null){
				this.ranges.put(key, range);
			}
		}
		return this;
	}

	@Override
	public double[] generateFeatureVectorFrom(State s) {
		double [] vals = new double[ranges.size()];
		int i = 0;
		for(Map.Entry<Object, VariableDomain> e : ranges.entrySet()){

			double d = ((Number)s.get(e.getKey())).doubleValue();
			VariableDomain r = e.getValue();

			double norm = r.norm(d);
			vals[i] = d;

			i++;
		}

		return vals;
	}
}
