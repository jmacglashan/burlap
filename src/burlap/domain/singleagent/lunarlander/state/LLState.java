package burlap.domain.singleagent.lunarlander.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.oo.state.exceptions.UnknownClassException;
import burlap.mdp.core.oo.state.exceptions.UnknownObjectException;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.ShallowCopyState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.lunarlander.LunarLanderDomain.*;

/**
 * @author James MacGlashan.
 */
@ShallowCopyState
public class LLState implements MutableOOState{

	public LLAgent agent;
	public LLBlock.LLPad pad;
	public List<LLBlock.LLObstacle> obstacles;

	public LLState() {
	}

	public LLState(LLAgent agent, LLBlock.LLPad pad, List<LLBlock.LLObstacle> obstacles) {
		this.agent = agent;
		this.pad = pad;
		this.obstacles = obstacles;
	}

	public LLState(LLAgent agent, LLBlock.LLPad pad, LLBlock.LLObstacle...obstacles) {
		this.agent = agent;
		this.pad = pad;
		this.obstacles = Arrays.asList(obstacles);
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {

		if(o instanceof LLAgent){
			agent = (LLAgent)o;
		}
		else if(o instanceof LLBlock.LLPad){
			pad = (LLBlock.LLPad)o;
		}
		else if(o instanceof LLBlock){
			touchObstacles().add((LLBlock.LLObstacle)o);
		}
		else{
			throw new UnknownClassException(o.className());
		}

		return this;
	}

	@Override
	public MutableOOState removeObject(String oname) {

		if(agent.name().equals(oname)){
			agent = new LLAgent(); //cannot remove, so copy
		}
		else if(pad != null && pad.name().equals(oname)){
			pad = null;
		}
		else{
			int ind = OOStateUtilities.objectIndexWithName(obstacles, oname);
			if(ind != -1){
				touchObstacles().remove(ind);
			}
			else{
				throw new UnknownObjectException(oname);
			}
		}

		return this;
	}

	@Override
	public MutableOOState renameObject(String objectName, String newName) {
		if(agent.name().equals(objectName)){
			throw new RuntimeException("LL Agent name must be " + objectName);
		}
		else if(pad != null && pad.name().equals(objectName)){
			touchPad().name = newName;
		}
		else{
			int ind = OOStateUtilities.objectIndexWithName(obstacles, objectName);
			if(ind != -1){
				LLBlock.LLObstacle ob = obstacles.get(ind);
				touchObstacles().remove(ind);
				obstacles.add(ind, (LLBlock.LLObstacle)ob.copyWithName(newName));
			}
			else{
				throw new UnknownObjectException(objectName);
			}
		}

		return this;
	}

	@Override
	public int numObjects() {
		return pad != null ? 2 + obstacles.size() : 1 + obstacles.size();
	}

	@Override
	public ObjectInstance object(String oname) {
		if(agent.name().equals(oname)){
			return agent;
		}
		else if(pad != null && pad.name().equals(oname)){
			return pad;
		}
		else{
			int ind = OOStateUtilities.objectIndexWithName(obstacles, oname);
			if(ind != -1)
				return obstacles.get(ind);
		}
		throw new UnknownObjectException(oname);
	}

	@Override
	public List<ObjectInstance> objects() {
		List<ObjectInstance> obs = new ArrayList<ObjectInstance>(2+obstacles.size());
		obs.add(agent);
		if(pad != null) obs.add(pad);
		obs.addAll(obstacles);
		return obs;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(CLASS_AGENT)){
			return Arrays.<ObjectInstance>asList(agent);
		}
		else if(oclass.equals(CLASS_PAD)){
			return pad != null ? Arrays.<ObjectInstance>asList(pad) : new ArrayList<ObjectInstance>();
		}
		else if(oclass.equals(CLASS_OBSTACLE)){
			return new ArrayList<ObjectInstance>(obstacles);
		}
		throw new UnknownClassException(oclass);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {

		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		Double d = (Double)value;

		if(agent.name().equals(key.obName)){
			if(key.obVarKey.equals(VAR_X)){
				touchAgent().x = d;
			}
			else if(key.obVarKey.equals(VAR_Y)){
				touchAgent().y = d;
			}
			else if(key.obVarKey.equals(VAR_VX)){
				touchAgent().vx = d;
			}
			else if(key.obVarKey.equals(VAR_VY)){
				touchAgent().vy = d;
			}
			else{
				throw new UnknownKeyException(key.obVarKey);
			}
		}
		else if(pad != null && pad.name().equals(key.obName)){
			if(key.obVarKey.equals(VAR_LEFT)){
				touchPad().left = d;
			}
			else if(key.obVarKey.equals(VAR_RIGHT)){
				touchPad().right = d;
			}
			else if(key.obVarKey.equals(VAR_BOTTOM)){
				touchPad().bottom = d;
			}
			else if(key.obVarKey.equals(VAR_TOP)){
				touchPad().top = d;
			}
			else{
				throw new UnknownKeyException(key.obVarKey);
			}
		}
		else{
			int ind = OOStateUtilities.objectIndexWithName(obstacles, key.obName);
			if(ind != -1){
				if(key.obVarKey.equals(VAR_LEFT)){
					touchObstacle(ind).left = d;
				}
				else if(key.obVarKey.equals(VAR_RIGHT)){
					touchObstacle(ind).right = d;
				}
				else if(key.obVarKey.equals(VAR_BOTTOM)){
					touchObstacle(ind).bottom = d;
				}
				else if(key.obVarKey.equals(VAR_TOP)){
					touchObstacle(ind).top = d;
				}
				else{
					throw new UnknownKeyException(key.obVarKey);
				}
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
		return new LLState(agent, pad, obstacles);
	}


	public LLAgent touchAgent(){
		agent = agent.copy();
		return agent;
	}

	public LLBlock.LLPad touchPad(){
		pad = pad.copy();
		return pad;
	}

	public List<LLBlock.LLObstacle> touchObstacles(){
		obstacles = new ArrayList<LLBlock.LLObstacle>(obstacles);
		return obstacles;
	}

	public LLBlock.LLObstacle touchObstacle(int ind){
		LLBlock.LLObstacle obs = obstacles.get(ind).copy();
		touchObstacles().remove(ind);
		touchObstacles().add(ind, obs);
		return obs;
	}

	public List<LLBlock.LLObstacle> deepTouchObstacles(){
		List<LLBlock.LLObstacle> nobs = new ArrayList<LLBlock.LLObstacle>(obstacles.size());
		for(LLBlock.LLObstacle obs : obstacles){
			nobs.add(obs.copy());
		}
		obstacles = nobs;
		return obstacles;
	}

	@Override
	public String toString() {
		return OOStateUtilities.ooStateToString(this);
	}
}
