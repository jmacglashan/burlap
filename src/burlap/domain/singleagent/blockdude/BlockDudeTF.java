package burlap.domain.singleagent.blockdude;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
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

		ObjectInstance agent = s.getFirstObjectOfClass(BlockDude.CLASSAGENT);
		List<ObjectInstance> exits = s.getObjectsOfClass(BlockDude.CLASSEXIT);

		int ax = agent.getIntValForAttribute(BlockDude.ATTX);
		int ay = agent.getIntValForAttribute(BlockDude.ATTY);

		for(ObjectInstance e : exits){
			int ex = e.getIntValForAttribute(BlockDude.ATTX);
			if(ex == ax){
				int ey = e.getIntValForAttribute(BlockDude.ATTY);
				if(ey == ay){
					return true;
				}
			}
		}

		return false;
	}
}
