package burlap.domain.singleagent.gridworld.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.annotations.ShallowCopyState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.gridworld.GridWorldDomain.VAR_TYPE;
import static burlap.domain.singleagent.gridworld.GridWorldDomain.VAR_X;
import static burlap.domain.singleagent.gridworld.GridWorldDomain.VAR_Y;

/**
 * @author James MacGlashan.
 */
@ShallowCopyState
public class GridWorldState implements MutableOOState {

	public GridAgent agent;
	public List<GridLocation> locations = new ArrayList<GridLocation>();

	public GridWorldState() {
	}

	public GridWorldState(int x, int y, GridLocation...locations){
		this(new GridAgent(x, y), locations);
	}

	public GridWorldState(GridAgent agent, GridLocation...locations){
		this.agent = agent;
		if(locations.length == 0){
			this.locations = new ArrayList<GridLocation>();
		}
		else {
			this.locations = Arrays.asList(locations);
		}
	}

	public GridWorldState(GridAgent agent, List<GridLocation> locations){
		this.agent = agent;
		this.locations = locations;
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {
		if(!(o instanceof GridLocation)){
			throw new RuntimeException("Can only add GridLocation objects to a GridWorldState.");
		}
		GridLocation loc = (GridLocation)o;

		//copy on write
		touchLocations().add(loc);

		return this;
	}

	@Override
	public MutableOOState removeObject(String oname) {
		if(oname.equals(agent.name())){
			throw new RuntimeException("Cannot remove agent object from state");
		}
		int ind = this.locationInd(oname);
		if(ind == -1){
			throw new RuntimeException("Cannot find object " + oname);
		}

		//copy on write
		touchLocations().remove(ind);

		return this;
	}

	@Override
	public MutableOOState renameObject(String objectName, String newName) {

		if(objectName.equals(agent.name())){
			GridAgent nagent = agent.copyWithName(newName);
			this.agent = nagent;
		}
		else{
			int ind = this.locationInd(objectName);
			if(ind == -1){
				throw new RuntimeException("Cannot find object " + objectName);
			}

			//copy on write
			GridLocation nloc = this.locations.get(ind).copyWithName(newName);
			touchLocations().remove(ind);
			locations.add(ind, nloc);

		}

		return this;
	}

	@Override
	public int numObjects() {
		return 1 + this.locations.size();
	}

	@Override
	public ObjectInstance object(String oname) {
		if(oname.equals(agent.name())){
			return agent;
		}
		int ind = this.locationInd(oname);
		if(ind != -1){
			return locations.get(ind);
		}
		return null;
	}

	@Override
	public List<ObjectInstance> objects() {
		List<ObjectInstance> obs = new ArrayList<ObjectInstance>(1+locations.size());
		obs.add(agent);
		obs.addAll(locations);
		return obs;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals("agent")){
			return Arrays.<ObjectInstance>asList(agent);
		}
		else if(oclass.equals("location")){
			return new ArrayList<ObjectInstance>(locations);
		}
		throw new RuntimeException("Unknown class type " + oclass);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {

		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		int iv = StateUtilities.stringOrNumber(value).intValue();
		if(key.obName.equals(agent.name())){
			if(key.obVarKey.equals(VAR_X)){
				touchAgent().x = iv;
			}
			else if(key.obVarKey.equals(VAR_Y)){
				touchAgent().y = iv;
			}
			else{
				throw new RuntimeException("Unknown variable key " + variableKey);
			}
			return this;
		}
		int ind = locationInd(key.obName);
		if(ind != -1){
			if(key.obVarKey.equals(VAR_X)){
				touchLocation(ind).x = iv;
			}
			else if(key.obVarKey.equals(VAR_Y)){
				touchLocation(ind).y = iv;
			}
			else if(key.obVarKey.equals(VAR_TYPE)){
				touchLocation(ind).type = iv;
			}
			else{
				throw new RuntimeException("Unknown variable key " + variableKey);
			}

			return this;
		}

		throw new RuntimeException("Unknown variable key " + variableKey);
	}

	@Override
	public List<Object> variableKeys() {
		return OOStateUtilities.flatStateKeys(this);
	}

	@Override
	public Object get(Object variableKey) {
		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		if(key.obName.equals(agent.name())){
			return agent.get(key.obVarKey);
		}
		int ind = this.locationInd(key.obName);
		if(ind == -1){
			throw new RuntimeException("Cannot find object " + key.obName);
		}
		return locations.get(ind).get(key.obVarKey);
	}

	@Override
	public State copy() {
		return new GridWorldState(agent, locations);
	}

	protected int locationInd(String oname){
		int ind = -1;
		for(int i = 0; i < locations.size(); i++){
			if(locations.get(i).name().equals(oname)){
				ind = i;
				break;
			}
		}
		return ind;
	}

	@Override
	public String toString() {
		return OOStateUtilities.ooStateToString(this);
	}

	public GridAgent touchAgent(){
		this.agent = agent.copy();
		return agent;
	}

	public List<GridLocation> touchLocations(){
		this.locations = new ArrayList<GridLocation>(locations);
		return locations;
	}

	public List<GridLocation> deepTouchLocations(){
		List<GridLocation> nlocs = new ArrayList<GridLocation>(locations.size());
		for(GridLocation loc : locations){
			nlocs.add(loc.copy());
		}
		locations = nlocs;
		return locations;
	}

	public GridLocation touchLocation(int ind){
		GridLocation n = locations.get(ind).copy();
		touchLocations().remove(ind);
		locations.add(ind, n);
		return n;
	}
}
