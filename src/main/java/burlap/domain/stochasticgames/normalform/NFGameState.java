package burlap.domain.stochasticgames.normalform;

import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.annotations.ShallowCopyState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
@ShallowCopyState
public class NFGameState implements MutableState, StateGenerator {

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


	@Override
	public State generateState() {
		return new NFGameState(players.clone());
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
		return Arrays.toString(this.players);
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
