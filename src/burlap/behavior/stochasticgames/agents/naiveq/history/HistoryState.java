package burlap.behavior.stochasticgames.agents.naiveq.history;

import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class HistoryState implements State{

	protected State curState;
	protected JointAction[] history;
	protected int sIndex = 0;

	public HistoryState(State curState, int historySize) {
		this.curState = curState;
		this.history = new JointAction[historySize];
		for(int i = 0; i < history.length; i++){
			history[i] = new JointAction();
		}
	}

	public HistoryState(State curState, JointAction[] history, int sIndex) {
		this.curState = curState;
		this.history = history;
		this.sIndex = sIndex;
	}

	@Override
	public List<Object> variableKeys() {
		List<Object> keys = new ArrayList<Object>(curState.variableKeys());
		for(int i = 0; i < history.length; i++){
			keys.add("_H"+(i+1));
		}
		return keys;
	}

	@Override
	public Object get(Object variableKey) {

		int ind = this.keyIndex(variableKey);
		if(ind == -1){
			return curState.get(variableKey);
		}

		return history[ind];
	}

	@Override
	public State copy() {
		return new HistoryState(curState, Arrays.copyOf(history, history.length), this.sIndex);
	}


	public HistoryState incrementWithChange(State newState, JointAction lastAction){
		JointAction[] nHistory = Arrays.copyOf(history, history.length);
		nHistory[sIndex] = lastAction;
		int nStart = (sIndex + 1) % history.length;
		return new HistoryState(newState, nHistory, nStart);
	}

	protected int keyIndex(Object key){
		if(!(key instanceof String)){
			return -1;
		}
		if(!((String) key).substring(0, 2).equals("_H")){
			return -1;
		}
		int ind = Integer.parseInt(((String) key).substring(2));
		if(ind <= 0){
			throw new RuntimeException("Steps back must be greater than zero, but given " + ind);
		}
		if(ind > history.length){
			throw new RuntimeException("Requested history for " + ind + "steps back, but only storing " + history.length + " steps.");
		}
		return ind;
	}

	protected int arrayIndexForStepsBack(int ind){
		return (sIndex - ind) % this.history.length;
	}
}
