package burlap.domain.singleagent.blocksworld;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static burlap.domain.singleagent.blocksworld.BlocksWorld.*;

/**
 * @author James MacGlashan.
 */
public class BlocksWorldState implements MutableOOState{

	protected Map<String, BlocksWorldBlock> blocks = new HashMap<String, BlocksWorldBlock>();

	public BlocksWorldState() {

	}

	public BlocksWorldState(Map<String, BlocksWorldBlock> blocks) {
		this.blocks = blocks;
	}


	@Override
	public MutableOOState addObject(ObjectInstance o) {
		if(!(o instanceof BlocksWorldBlock)){
			throw new RuntimeException("Can only add BlocksWorldBlock ObjectInstances");
		}
		this.blocks.put(o.name(), (BlocksWorldBlock)o);
		return this;
	}

	@Override
	public MutableOOState removeObject(String oname) {
		this.blocks.remove(oname);
		return this;
	}

	@Override
	public MutableOOState renameObject(String objectName, String newName) {
		BlocksWorldBlock ob = this.blocks.get(objectName);
		if(ob != null){
			this.blocks.remove(objectName);
			this.blocks.put(newName, (BlocksWorldBlock)ob.copyWithName(newName));
		}
		return this;
	}

	@Override
	public int numObjects() {
		return this.blocks.size();
	}

	@Override
	public ObjectInstance object(String oname) {
		return this.blocks.get(oname);
	}

	@Override
	public List<ObjectInstance> objects() {
		return new ArrayList<ObjectInstance>(this.blocks.values());
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(!oclass.equals(CLASS_BLOCK)){
			throw new RuntimeException("Unsupported object class " + oclass);
		}
		return new ArrayList<ObjectInstance>(this.blocks.values());
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		BlocksWorldBlock ob = this.blocks.get(key.obName);
		if(ob != null){
			ob = ob.copy();
			if(key.obVarKey.equals(VAR_ON)){
				ob.on = (String)value;
			}
			else if(key.obVarKey.equals(VAR_CLEAR)){
				ob.clear = (Boolean)value;
			}
			else if(key.obVarKey.equals(VAR_COLOR)){
				ob.color = (Color)value;
			}
			this.blocks.put(ob.name(), ob);
		}
		return this;
	}

	@Override
	public List<Object> variableKeys() {
		return OOStateUtilities.flatStateKeys(this);
	}

	@Override
	public Object get(Object variableKey) {
		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		ObjectInstance ob = this.blocks.get(key.obName);
		if(ob == null){
			throw new RuntimeException("Unknown object " + ob.name());
		}
		return ob.get(key.obVarKey);
	}

	@Override
	public State copy() {
		return new BlocksWorldState(new HashMap<String, BlocksWorldBlock>(blocks));
	}

	@Override
	public String toString() {
		return OOStateUtilities.ooStateToString(this);
	}
}
