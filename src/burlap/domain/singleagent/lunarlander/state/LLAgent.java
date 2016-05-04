package burlap.domain.singleagent.lunarlander.state;

import burlap.oomdp.core.oo.state.OOStateUtilities;
import burlap.oomdp.core.oo.state.ObjectInstance;
import burlap.oomdp.core.state.UnknownKeyException;
import burlap.oomdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.lunarlander.LunarLanderDomain.*;

/**
 * @author James MacGlashan.
 */
@DeepCopyState
public class LLAgent implements ObjectInstance {

	public double x;
	public double y;
	public double vx;
	public double vy;
	public double angle;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_VX, VAR_VY, VAR_ANGLE);

	public LLAgent() {
	}

	public LLAgent(double x, double y, double vx, double vy, double angle) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.angle = angle;
	}

	@Override
	public String className() {
		return CLASS_AGENT;
	}

	@Override
	public String name() {
		return CLASS_AGENT;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		if(!objectName.equals(CLASS_AGENT))
			throw new RuntimeException("Lunar lander agent number must be " + CLASS_AGENT);

		return copy();
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
		else if(variableKey.equals(VAR_Y)){
			return y;
		}
		else if(variableKey.equals(VAR_VX)){
			return vx;
		}
		else if(variableKey.equals(VAR_VY)){
			return vy;
		}
		else if(variableKey.equals(VAR_ANGLE)){
			return angle;
		}
		throw new UnknownKeyException(variableKey);
	}

	@Override
	public LLAgent copy() {
		return new LLAgent(x, y, vx, vy, angle);
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
