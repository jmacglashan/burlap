package burlap.domain.singleagent.blocksworld;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.blocksworld.BlocksWorld.*;

/**
 * @author James MacGlashan.
 */
public class BlocksWorldBlock implements ObjectInstance {

	public String on = TABLE_VAL;
	public boolean clear = true;
	public Color color = Color.red;

	protected String name = "block";

	private final List<Object> keys = Arrays.<Object>asList(ATT_ON, ATT_CLEAR, ATT_COLOR);

	public BlocksWorldBlock() {
	}

	public BlocksWorldBlock(String name) {
		this.name = name;
	}

	public BlocksWorldBlock(String on, boolean clear, Color color, String name) {
		this.on = on;
		this.clear = clear;
		this.color = color;
		this.name = name;
	}

	@Override
	public String className() {
		return CLASS_BLOCK;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new BlocksWorldBlock(on, clear, color, objectName);
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if(!(variableKey instanceof String)){
			throw new RuntimeException("Key must be a string");
		}
		String key = (String)variableKey;
		if(key.equals(ATT_ON)){
			return on;
		}
		else if(key.equals(ATT_CLEAR)){
			return clear;
		}
		else if(key.equals(ATT_COLOR)){
			return color;
		}
		throw new RuntimeException("Unknown key " + key);
	}

	@Override
	public BlocksWorldBlock copy() {
		return new BlocksWorldBlock(on, clear, color, name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean onTable(){
		return this.on.equals(TABLE_VAL);
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
