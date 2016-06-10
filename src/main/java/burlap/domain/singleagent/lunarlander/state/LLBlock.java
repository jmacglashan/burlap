package burlap.domain.singleagent.lunarlander.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.lunarlander.LunarLanderDomain.*;

/**
 * @author James MacGlashan.
 */
public abstract class LLBlock implements ObjectInstance {

	public double left;
	public double right;
	public double bottom;
	public double top;

	protected String name;


	private static final List<Object> keys = Arrays.<Object>asList(VAR_LEFT, VAR_RIGHT, VAR_BOTTOM, VAR_TOP);

	public LLBlock() {
	}

	public LLBlock(double left, double right, double bottom, double top, String name) {
		this.left = left;
		this.right = right;
		this.bottom = bottom;
		this.top = top;
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public LLBlock copyWithName(String objectName) {
		LLBlock block = this.copy();
		block.name = objectName;
		return block;
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {

		if(variableKey.equals(VAR_LEFT)){
			return left;
		}
		else if(variableKey.equals(VAR_RIGHT)){
			return right;
		}
		else if(variableKey.equals(VAR_BOTTOM)){
			return bottom;
		}
		else if(variableKey.equals(VAR_TOP)){
			return top;
		}

		throw new UnknownKeyException(variableKey);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	abstract public LLBlock copy();


	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}

	@DeepCopyState
	public static class LLPad extends LLBlock{

		public LLPad() {
		}

		public LLPad(double left, double right, double bottom, double top, String name) {
			super(left, right, bottom, top, name);
		}

		@Override
		public String className() {
			return CLASS_PAD;
		}

		@Override
		public LLPad copy() {
			return new LLPad(left, right, bottom, top, name);
		}
	}


	@DeepCopyState
	public static class LLObstacle extends LLBlock{

		public LLObstacle() {
		}

		public LLObstacle(double left, double right, double bottom, double top, String name) {
			super(left, right, bottom, top, name);
		}

		@Override
		public String className() {
			return CLASS_OBSTACLE;
		}

		@Override
		public LLObstacle copy() {
			return new LLObstacle(left, right, bottom, top, name);
		}
	}

}
