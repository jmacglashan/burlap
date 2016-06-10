package burlap.domain.singleagent.blockdude.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.blockdude.BlockDude.*;

/**
 * @author James MacGlashan.
 */
@DeepCopyState
public class BlockDudeCell implements ObjectInstance {

	public int x;
	public int y;

	protected String className;
	protected String name;

	private final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y);


	public static BlockDudeCell exit(int x, int y){
		return new BlockDudeCell(x, y, CLASS_EXIT, CLASS_EXIT);
	}

	public static BlockDudeCell block(String name, int x, int y){
		return new BlockDudeCell(x, y, CLASS_BLOCK, name);
	}

	public BlockDudeCell() {
	}

	public BlockDudeCell(String className, String name) {
		this.className = className;
		this.name = name;
	}

	public BlockDudeCell(int x, int y, String className, String name) {
		this.x = x;
		this.y = y;
		this.className = className;
		this.name = name;
	}

	public void setXY(int x, int y){
		this.x = x;
		this.y = y;
	}

	@Override
	public String className() {
		return className;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new BlockDudeCell(x, y, className, objectName);
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
		throw new RuntimeException("Unknown key " + variableKey);
	}

	@Override
	public BlockDudeCell copy() {
		return new BlockDudeCell(x, y, className, name);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
