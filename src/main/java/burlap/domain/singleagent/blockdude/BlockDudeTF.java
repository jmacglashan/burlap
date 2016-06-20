package burlap.domain.singleagent.blockdude;

import burlap.domain.singleagent.blockdude.state.BlockDudeState;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;

/**
 * A {@link burlap.mdp.core.TerminalFunction} for {@link burlap.domain.singleagent.blockdude.BlockDude}. Returns true
 * when the agent is at an exit. If there are multiple exits, then returns true when the agent is at any exit.
 * @author James MacGlashan.
 */
public class BlockDudeTF implements TerminalFunction {

	@Override
	public boolean isTerminal(State s) {

		BlockDudeState bs = (BlockDudeState)s;

		int ax = bs.agent.x;
		int ay = bs.agent.y;

		int ex = bs.exit.x;
		int ey = bs.exit.y;

		return ex == ax && ey == ay;

	}
}
