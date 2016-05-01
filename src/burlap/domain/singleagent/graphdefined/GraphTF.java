package burlap.domain.singleagent.graphdefined;

import burlap.oomdp.core.state.State;
import burlap.oomdp.core.TerminalFunction;

import java.util.HashSet;
import java.util.Set;

/**
 * A {@link burlap.oomdp.core.TerminalFunction} for instances of {@link burlap.domain.singleagent.graphdefined.GraphDefinedDomain}.
 * Lets the user specify the integer node ids of the terminal states in the graph.
 * @author James MacGlashan.
 */
public class GraphTF implements TerminalFunction {

	/**
	 * The set of nodes ids in the graph that are terminal states
	 */
	protected Set<Integer> terminalStates;


	/**
	 * Initializes setting all states with the provide integer node ids to be terminal states
	 * @param nodes the state node ids that are terminal states
	 */
	public GraphTF(int...nodes){
		this.terminalStates = new HashSet<Integer>(nodes.length);
		for(int n : nodes){
			this.terminalStates.add(n);
		}
	}

	@Override
	public boolean isTerminal(State s) {

		int sid = GraphDefinedDomain.getNodeId(s);
		return this.terminalStates.contains(sid);
	}

	public Set<Integer> getTerminalStates() {
		return terminalStates;
	}

	public void setTerminalStates(Set<Integer> terminalStates) {
		this.terminalStates = terminalStates;
	}

	/**
	 * Adds additional terminal states
	 * @param nodes the additional state node ids that are to be marked as terminal states
	 */
	public void addTerminals(int...nodes){
		for(int n : nodes){
			this.terminalStates.add(n);
		}
	}

	/**
	 * Removes nodes as being marked as terminal states
	 * @param nodes the nodes to remove as terminal states
	 */
	public void removeTerminals(int...nodes){
		for(int n : nodes){
			this.terminalStates.remove(n);
		}
	}
}
