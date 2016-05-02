package burlap.domain.singleagent.mountaincar;

import burlap.oomdp.core.state.MutableState;
import burlap.oomdp.core.state.State;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.mountaincar.MountainCar.ATT_V;
import static burlap.domain.singleagent.mountaincar.MountainCar.ATT_X;

/**
 * @author James MacGlashan.
 */
public class MCState implements MutableState {

	public double x;
	public double v;

	private static final List<Object> keys = Arrays.<Object>asList(ATT_X, ATT_V);

	public MCState() {
	}


	public MCState(double x, double v) {
		this.x = x;
		this.v = v;
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		if(variableKey instanceof String){
			if(variableKey.equals(ATT_X)){
				this.x = ((Number)value).doubleValue();
				return this;
			}
			else if(variableKey.equals(ATT_V)){
				this.v = ((Number)value).doubleValue();
				return this;
			}
			else{
				throw new RuntimeException("Unknown key " + variableKey);
			}
		}
		else if(variableKey instanceof Integer){
			if((Integer)variableKey == 0){
				this.x = ((Number)value).doubleValue();
				return this;
			}
			else if((Integer)variableKey == 1){
				this.v = ((Number)value).doubleValue();
				return this;
			}
			else{
				throw new RuntimeException("Unknown key " + variableKey);
			}
		}

		throw new RuntimeException("Unknown key " + variableKey);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {

		if(variableKey instanceof String){
			if(variableKey.equals(ATT_X)){
				return x;
			}
			else if(variableKey.equals(ATT_V)){
				return v;
			}
			else{
				throw new RuntimeException("Unknown key " + variableKey);
			}
		}
		else if(variableKey instanceof Integer){
			if((Integer)variableKey == 0){
				return x;
			}
			else if((Integer)variableKey == 1){
				return v;
			}
			else{
				throw new RuntimeException("Unknown key " + variableKey);
			}
		}

		throw new RuntimeException("Unknown key " + variableKey);
	}

	@Override
	public State copy() {
		return new MCState(x, v);
	}
}
