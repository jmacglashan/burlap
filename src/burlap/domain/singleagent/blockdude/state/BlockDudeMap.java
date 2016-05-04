package burlap.domain.singleagent.blockdude.state;

import burlap.oomdp.core.oo.state.OOStateUtilities;
import burlap.oomdp.core.oo.state.ObjectInstance;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.blockdude.BlockDude.CLASS_MAP;
import static burlap.domain.singleagent.blockdude.BlockDude.VAR_MAP;

/**
 * @author James MacGlashan.
 */
public class BlockDudeMap implements ObjectInstance {

	public int [][] map;

	private final List<Object> keys = Arrays.<Object>asList(VAR_MAP);

	public BlockDudeMap() {
	}

	public BlockDudeMap(int w, int h) {
		this.map = new int[w][h];
	}

	public BlockDudeMap(int[][] map) {
		this.map = map;
	}

	@Override
	public String className() {
		return CLASS_MAP;
	}

	@Override
	public String name() {
		return CLASS_MAP;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		throw new RuntimeException("The map must always be named map");
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		return map;
	}

	@Override
	public BlockDudeMap copy() {
		return new BlockDudeMap(map);
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
