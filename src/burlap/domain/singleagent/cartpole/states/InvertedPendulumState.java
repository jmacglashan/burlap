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
public class InvertedPendulumState implements MutableState {

	public double angle;
	public double angleV;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_ANGLE, VAR_ANGLEV);

	public InvertedPendulumState() {
	}

	public InvertedPendulumState(double angle, double angleV) {
		this.angle = angle;
		this.angleV = angleV;
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		if(!(variableKey instanceof String)){
			throw new RuntimeException("Variable key must be a String");
		}

		double d = StateUtilities.stringOrNumber(value).doubleValue();

		if(variableKey.equals(VAR_ANGLE)){
			angle = d;
		}
		else if(variableKey.equals(VAR_ANGLEV)){
			angleV = d;
		}
		else {
			throw new RuntimeException("Unknown key " + variableKey);
		}

		return this;
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if(!(variableKey instanceof String)){
			throw new RuntimeException("Variable key must be a String");
		}
		else if(variableKey.equals(VAR_ANGLE)){
			return angle;
		}
		else if(variableKey.equals(VAR_ANGLEV)){
			return angleV;
		}
		throw new RuntimeException("Unknown key " + variableKey);
	}

	@Override
	public State copy() {
		return new InvertedPendulumState(angle, angleV);
	}

	@Override
	public String toString() {
		return StateUtilities.stateToString(this);
	}
}
