package burlap.domain.singleagent.cartpole.states;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.cartpole.CartPoleDomain.*;

/**
 * @author James MacGlashan.
 */
public class CartPoleState extends InvertedPendulumState {

	public double x;
	public double v;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_V, VAR_ANGLE, VAR_ANGLEV);

	public CartPoleState() {
	}

	public CartPoleState(double x, double v, double angle, double angleV) {
		this.x = x;
		this.v = v;
		this.angle = angle;
		this.angleV = angleV;
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		double d = ((Number)value).doubleValue();

		if(variableKey.equals(VAR_X)){
			this.x = d;
		}
		else if(variableKey.equals(VAR_V)){
			this.v = d;
		}
		return super.set(variableKey, value);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if(variableKey.equals(VAR_X)){
			return x;
		}
		else if(variableKey.equals(VAR_V)){
			return v;
		}
		return super.get(variableKey);
	}

	@Override
	public State copy() {
		return new CartPoleState(x, v, angle, angleV);
	}

	@Override
	public String toString() {
		return StateUtilities.stateToString(this);
	}
}
