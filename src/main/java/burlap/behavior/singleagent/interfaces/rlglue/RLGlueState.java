package burlap.behavior.singleagent.interfaces.rlglue;

import burlap.mdp.core.state.State;
import org.rlcommunity.rlglue.codec.types.Observation;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link State} for RLGLue {@link Observation} objects. Each instance contains the inner RLGlue {@link Observation}
 * You can set and get the {@link Observation} with standard methods, thereby allowing serialization. The BURLAP
 * variable keys are specified with the {@link RLGlueVarKey}, which indicates indices for character, integer, and double
 * values that RLGlue supports. You can also get variable values using String keys of the form "cn", "in", "dn" for character
 * integer, and double variables respectively, where n is the index into that vector.
 * @author James MacGlashan.
 */
public class RLGlueState implements State{

	protected Observation obs;

	public RLGlueState() {
	}

	public RLGlueState(Observation obs) {
		this.obs = obs;
	}

	@Override
	public List<Object> variableKeys() {
		List<Object> keys = new ArrayList<Object>(obs.getNumChars() + obs.getNumInts() + obs.getNumDoubles());
		for(int i = 0; i < obs.getNumChars(); i++){
			RLGlueVarKey key = new RLGlueVarKey('c', i);
			keys.add(key);
		}
		for(int i = 0; i < obs.getNumInts(); i++){
			RLGlueVarKey key = new RLGlueVarKey('i', i);
			keys.add(key);
		}
		for(int i = 0; i < obs.getNumDoubles(); i++){
			RLGlueVarKey key = new RLGlueVarKey('d', i);
			keys.add(key);
		}
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		RLGlueVarKey key;
		if(variableKey instanceof RLGlueVarKey){
			key = (RLGlueVarKey)variableKey;
		}
		else if(variableKey instanceof String){
			key = new RLGlueVarKey((String)variableKey);
		}
		else{
			throw new RuntimeException("RLGlueState does not understand key of type " + variableKey.getClass().getName() + "; use a RLGlueVarKey or String rep");
		}

		if(key.type == 'c'){
			return obs.charArray[key.ind];
		}
		else if(key.type == 'i'){
			return obs.intArray[key.ind];
		}
		else if(key.type == 'd'){
			return obs.doubleArray[key.ind];
		}

		return null;
	}

	@Override
	public State copy() {
		return new RLGlueState(obs); //immutable so we can do shallow copy
	}

	public Observation getObs() {
		return obs;
	}

	public void setObs(Observation obs) {
		this.obs = obs;
	}

	public static class RLGlueVarKey{
		public char type;
		public int ind;


		public RLGlueVarKey(char type, int ind) {
			this.type = type;
			this.ind = ind;
		}

		public RLGlueVarKey(String strRep){
			this.type = strRep.charAt(0);
			this.ind = Integer.parseInt(strRep.substring(1));
		}

		@Override
		public String toString() {
			return String.valueOf(type) + ind;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			RLGlueVarKey that = (RLGlueVarKey) o;

			if(type != that.type) return false;
			return ind == that.ind;

		}

		@Override
		public int hashCode() {
			int result = (int) type;
			result = 31 * result + ind;
			return result;
		}
	}
}
