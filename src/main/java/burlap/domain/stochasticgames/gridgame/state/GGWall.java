package burlap.domain.stochasticgames.gridgame.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.stochasticgames.gridgame.GridGame.*;

/**
 * @author James MacGlashan.
 */
@DeepCopyState
public abstract class GGWall implements ObjectInstance, MutableState {

	public int e1;
	public int e2;
	public int pos;
	public int type;

	protected String name;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_E1, VAR_E2, VAR_POS, VAR_WT);


	public GGWall() {
	}

	public GGWall(int e1, int e2, int pos, int type, String name) {
		this.e1 = e1;
		this.e2 = e2;
		this.pos = pos;
		this.type = type;
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}


	@Override
	public MutableState set(Object variableKey, Object value) {

		int i = StateUtilities.stringOrNumber(value).intValue();
		if(variableKey.equals(VAR_E1)){
			this.e1 = i;
		}
		else if(variableKey.equals(VAR_E2)){
			this.e2 = i;
		}
		else if(variableKey.equals(VAR_POS)){
			this.pos = i;
		}
		else if(variableKey.equals(VAR_WT)){
			this.type = i;
		}
		else{
			throw new UnknownKeyException(variableKey);
		}

		return this;
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if(variableKey.equals(VAR_E1)){
			return this.e1;
		}
		else if(variableKey.equals(VAR_E2)){
			return this.e2;
		}
		else if(variableKey.equals(VAR_POS)){
			return this.pos;
		}
		else if(variableKey.equals(VAR_WT)){
			return this.type;
		}
		else{
			throw new UnknownKeyException(variableKey);
		}
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@DeepCopyState
	public static class GGHorizontalWall extends GGWall{

		public GGHorizontalWall() {
		}

		public GGHorizontalWall(int e1, int e2, int pos, int type, String name) {
			super(e1, e2, pos, type, name);
		}

		@Override
		public String className() {
			return CLASS_DIM_H_WALL;
		}

		@Override
		public GGHorizontalWall copyWithName(String objectName) {
			return new GGHorizontalWall(e1, e2, pos, type, objectName);
		}

		@Override
		public GGHorizontalWall copy() {
			return new GGHorizontalWall(e1, e2, pos, type, name);
		}
	}


	@DeepCopyState
	public static class GGVerticalWall extends GGWall{

		public GGVerticalWall() {
		}

		public GGVerticalWall(int e1, int e2, int pos, int type, String name) {
			super(e1, e2, pos, type, name);
		}

		@Override
		public String className() {
			return CLASS_DIM_V_WALL;
		}

		@Override
		public GGVerticalWall copyWithName(String objectName) {
			return new GGVerticalWall(e1, e2, pos, type, objectName);
		}

		@Override
		public GGVerticalWall copy() {
			return new GGVerticalWall(e1, e2, pos, type, name);
		}
	}
}
