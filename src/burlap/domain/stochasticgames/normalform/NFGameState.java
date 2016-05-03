package burlap.domain.stochasticgames.normalform;

import burlap.oomdp.core.state.MutableState;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.state.StateUtilities;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.SGStateGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class NFGameState implements MutableState, SGStateGenerator{

	public String [] players;

	/**
	 * Default constructor for serialization
	 */
	public NFGameState() {
	}

	public NFGameState(int numPlayers){
		this.players = new String[numPlayers];
		for(int i = 0; i < numPlayers; i++){
			players[i] = "player" + i;
		}
	}

	public NFGameState(String[] players) {
		this.players = players;
	}

	public NFGameState(List<SGAgent> agents){
		this.players = new String[agents.size()];
		for(int i = 0; i < players.length; i++){
			players[i] = agents.get(i).getAgentName();
		}
	}

	@Override
	public State generateState(List<SGAgent> agents) {
		return new NFGameState(agents);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		int ind = genInd(variableKey);
		this.players[ind] = (String)value;

		return this;
	}

	@Override
	public List<Object> variableKeys() {
		List<Object> keys = new ArrayList<Object>(players.length);
		for(int i = 0; i < players.length; i++){
			keys.add(i);
		}
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		int ind = this.genInd(variableKey);
		return players[ind];
	}

	@Override
	public State copy() {
		return new NFGameState(Arrays.copyOf(this.players, this.players.length));
	}

	@Override
	public String toString() {
		return StateUtilities.stateToString(this);
	}

	public int playerIndex(String playerName){
		for(int i = 0 ; i < this.players.length; i++){
			if(playerName.equals(this.players[i])){
				return i;
			}
		}
		return -1;
	}

	protected int genInd(Object variableKey){
		int ind;
		if(variableKey instanceof String){
			ind = Integer.parseInt((String)variableKey);
		}
		else if(!(variableKey instanceof Integer)){
			throw new RuntimeException("Variable key must be a string or int.");
		}
		else {
			ind = (Integer) variableKey;
		}
		return ind;
	}
}
