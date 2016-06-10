package burlap.domain.singleagent.frostbite.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.oo.state.exceptions.UnknownClassException;
import burlap.mdp.core.oo.state.exceptions.UnknownObjectException;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.ShallowCopyState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.frostbite.FrostbiteDomain.*;

/**
 * @author James MacGlashan.
 */
@ShallowCopyState
public class FrostbiteState implements MutableOOState {

	public FrostbiteAgent agent;
	public FrostbiteIgloo igloo;
	public List<FrostbitePlatform> platforms;


	public FrostbiteState() {
		this(5, 4, 4);
	}

	public FrostbiteState(int scale, int numPlatformRow){
		this(scale, numPlatformRow, 4);
	}

	public FrostbiteState(int scale, int numberPlatformRow, int numberPlatformCol) {

		int platformSize = 15 * scale;
		int agentSize = 8 * scale;
		int gameHeight = 130 * scale;
		int gameIceHeight = gameHeight / 4;
		int jumpSize = 22 * scale;
		int gameWidth = 160 * scale;
		int spaceBetweenPlatforms = 26 * scale;

		this.agent = new FrostbiteAgent(platformSize / 2 + agentSize / 2, gameIceHeight - jumpSize / 2);
		this.igloo = new FrostbiteIgloo();

		platforms = new ArrayList<FrostbitePlatform>(numberPlatformCol*numberPlatformRow);
		int ind = 0;
		for(int row = 0; row < numberPlatformRow; row++){
			for(int i = 0; i < numberPlatformCol; i++){
				FrostbitePlatform p = new FrostbitePlatform(
						spaceBetweenPlatforms * i + ((row % 2 == 0) ? 0 : gameWidth / 3),
						gameIceHeight + jumpSize / 2 - platformSize / 2 + agentSize / 2 + jumpSize * row,
						platformSize,
						false,
						"p"+ind
				);
				platforms.add(p);
				ind++;
			}
		}

	}

	public FrostbiteState(FrostbiteAgent agent, FrostbiteIgloo igloo, List<FrostbitePlatform> platforms) {
		this.agent = agent;
		this.igloo = igloo;
		this.platforms = platforms;
	}

	public FrostbiteState(FrostbiteAgent agent, FrostbiteIgloo igloo, FrostbitePlatform...platforms) {
		this.agent = agent;
		this.igloo = igloo;
		this.platforms = Arrays.asList(platforms);
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {

		if(o instanceof FrostbiteAgent){
			agent = (FrostbiteAgent)o;
		}
		else if(o instanceof FrostbiteIgloo){
			igloo = (FrostbiteIgloo)o;
		}
		else if(o instanceof FrostbitePlatform){
			this.touchPlatforms().add((FrostbitePlatform)o);
		}
		else{
			throw new RuntimeException("Cannot add object of type " + o.getClass().getName());
		}

		return this;
	}

	@Override
	public MutableOOState removeObject(String oname) {
		if(oname.equals(CLASS_AGENT)){
			agent = new FrostbiteAgent(); //cannot remove, so just reset
		}
		else if(oname.equals(CLASS_IGLOO)){
			igloo = new FrostbiteIgloo(); //cannot remove so just reset
		}
		else{
			int ind = OOStateUtilities.objectIndexWithName(platforms, oname);
			if(ind != -1){
				touchPlatforms().remove(ind);
			}
		}
		return this;
	}

	@Override
	public MutableOOState renameObject(String objectName, String newName) {
		if(objectName.equals(CLASS_AGENT)){
			throw new RuntimeException("Cannot rename agent");
		}
		else if(objectName.equals(CLASS_IGLOO)){
			throw new RuntimeException("Cannot rename igloo");
		}
		else{
			int ind = OOStateUtilities.objectIndexWithName(platforms, objectName);
			if(ind != -1){
				FrostbitePlatform op = platforms.get(ind);
				touchPlatforms().remove(ind);
				touchPlatforms().add(ind, op.copyWithName(newName));
			}
		}
		return this;
	}

	@Override
	public int numObjects() {
		return platforms.size()+2;
	}

	@Override
	public ObjectInstance object(String oname) {
		if(oname.equals(CLASS_AGENT)){
			return agent;
		}
		else if(oname.equals(CLASS_IGLOO)){
			return igloo;
		}
		else{
			int ind = OOStateUtilities.objectIndexWithName(platforms, oname);
			if(ind != -1){
				return platforms.get(ind);
			}
		}
		throw new UnknownObjectException(oname);
	}

	@Override
	public List<ObjectInstance> objects() {
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>(platforms.size()+2);
		objects.add(agent);
		objects.add(igloo);
		objects.addAll(platforms);
		return objects;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(CLASS_AGENT)){
			return Arrays.<ObjectInstance>asList(agent);
		}
		else if(oclass.equals(CLASS_IGLOO)){
			return Arrays.<ObjectInstance>asList(igloo);
		}
		else if(oclass.equals(CLASS_PLATFORM)){
			return new ArrayList<ObjectInstance>(platforms);
		}
		throw new UnknownClassException(oclass);

	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		if(key.obName.equals(agent.name())){
			if(key.obVarKey.equals(VAR_X)){
				touchAgent().x = StateUtilities.stringOrNumber(value).intValue();
			}
			else if(key.obVarKey.equals(VAR_Y)){
				touchAgent().y = StateUtilities.stringOrNumber(value).intValue();
			}
			else if(key.obVarKey.equals(VAR_HEIGHT)){
				touchAgent().height = StateUtilities.stringOrNumber(value).intValue();
			}
			else
				throw new UnknownKeyException(key.obVarKey);
		}
		else if(key.obName.equals(igloo.name())){
			if(key.obVarKey.equals(VAR_BUILDING)){
				touchIgloo().height = (Integer)value;
			}
			else
				throw new UnknownKeyException(key.obVarKey);
		}
		else{
			int ind = OOStateUtilities.objectIndexWithName(platforms, key.obName);
			if(ind != -1){
				if(key.obVarKey.equals(VAR_X)){
					touchPlatform(ind).x = StateUtilities.stringOrNumber(value).intValue();
				}
				else if(key.obVarKey.equals(VAR_Y)){
					touchPlatform(ind).y = StateUtilities.stringOrNumber(value).intValue();
				}
				else if(key.obVarKey.equals(VAR_SIZE)){
					touchPlatform(ind).size = StateUtilities.stringOrNumber(value).intValue();
				}
				else if(key.obVarKey.equals(VAR_ACTIVATED)){
					touchPlatform(ind).activated = StateUtilities.stringOrBoolean(value);
				}
				else
					throw new UnknownKeyException(key.obVarKey);
			}
			else{
				throw new UnknownObjectException(key.obName);
			}
		}
		return this;
	}

	@Override
	public List<Object> variableKeys() {
		return OOStateUtilities.flatStateKeys(this);
	}

	@Override
	public Object get(Object variableKey) {
		return OOStateUtilities.get(this, variableKey);
	}

	@Override
	public State copy() {
		return new FrostbiteState(agent, igloo, platforms);
	}

	public FrostbiteAgent touchAgent(){
		agent = agent.copy();
		return agent;
	}

	public FrostbiteIgloo touchIgloo(){
		igloo = igloo.copy();
		return igloo;
	}

	public List<FrostbitePlatform> touchPlatforms(){
		this.platforms = new ArrayList<FrostbitePlatform>(platforms);
		return this.platforms;
	}

	public FrostbitePlatform touchPlatform(int i){
		FrostbitePlatform p = platforms.get(i).copy();
		touchPlatforms().remove(i);
		platforms.add(p);
		return p;
	}

	public List<FrostbitePlatform> deepTouchPlatforms(){
		List<FrostbitePlatform> nplatforms = new ArrayList<FrostbitePlatform>(platforms.size());
		for(FrostbitePlatform p : platforms){
			nplatforms.add(p.copy());
		}
		this.platforms = nplatforms;
		return this.platforms;
	}

	@Override
	public String toString() {
		return OOStateUtilities.ooStateToString(this);
	}
}
