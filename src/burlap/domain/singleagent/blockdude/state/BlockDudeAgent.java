package burlap.domain.singleagent.blockdude.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.blockdude.BlockDude.*;

/**
 * @author James MacGlashan.
 */
public class BlockDudeAgent implements ObjectInstance {

	public int x;
	public int y;
	public int dir;
	public boolean holding;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_DIR, VAR_HOLD);

	public BlockDudeAgent() {
	}

	public BlockDudeAgent(int x, int y, int dir, boolean holding) {
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.holding = holding;
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
		throw new RuntimeException("Blockdude agent must always be called agent");
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
		else if(variableKey.equals(VAR_DIR)){
			return dir;
		}
		else if(variableKey.equals(VAR_HOLD)){
			return holding;
		}
		throw new RuntimeException("Unknown key " + variableKey);
	}

	@Override
	public BlockDudeAgent copy() {
		return new BlockDudeAgent(x, y, dir, holding);
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
