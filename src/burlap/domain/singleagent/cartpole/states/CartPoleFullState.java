package burlap.domain.singleagent.cartpole.states;

import burlap.oomdp.core.state.MutableState;
import burlap.oomdp.core.state.State;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.cartpole.CartPoleDomain.*;

/**
 * @author James MacGlashan.
 */
public class CartPoleFullState extends CartPoleState {

	public double normSign = 1.;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_V, VAR_ANGLE, VAR_ANGLEV, VAR_NORM_SGN);

	public CartPoleFullState() {
		super();
	}

	public CartPoleFullState(double x, double v, double angle, double angleV, double normSign) {
		super(x, v, angle, angleV);
		this.normSign = normSign;
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		if(variableKey.equals(VAR_NORM_SGN)){
			this.normSign = ((Number)value).doubleValue();
			return this;
		}
		return super.set(variableKey, value);
	}

	@Override
	public Object get(Object variableKey) {
		if(variableKey.equals(VAR_NORM_SGN)){
			return this.normSign;
		}
		return super.get(variableKey);
	}

	@Override
	public State copy() {
		return new CartPoleFullState(x, v, angle, angleV, normSign);
	}
}
