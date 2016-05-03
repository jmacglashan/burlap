package burlap.domain.singleagent.blockdude;

import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.TerminalFunction;

import java.util.List;

/**
 * A {@link burlap.oomdp.core.TerminalFunction} for {@link burlap.domain.singleagent.blockdude.BlockDude}. Returns true
 * when the agent is at an exit. If there are multiple exits, then returns true when the agent is at any exit.
 * @author James MacGlashan.
 */
public class BlockDudeTF implements TerminalFunction {

	@Override
	public boolean isTerminal(State s) {

		OldObjectInstance agent = s.getFirstObjectOfClass(BlockDude.CLASS_AGENT);
		List<OldObjectInstance> exits = s.getObjectsOfClass(BlockDude.CLASS_EXIT);

		int ax = agent.getIntValForAttribute(BlockDude.VAR_X);
		int ay = agent.getIntValForAttribute(BlockDude.VAR_Y);

		for(OldObjectInstance e : exits){
			int ex = e.getIntValForAttribute(BlockDude.VAR_X);
			if(ex == ax){
				int ey = e.getIntValForAttribute(BlockDude.VAR_Y);
				if(ey == ay){
					return true;
				}
			}
		}

		return false;
	}
}
