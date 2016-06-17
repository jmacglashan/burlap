package burlap.domain.singleagent.blocksworld;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.List;

import static burlap.domain.singleagent.blocksworld.BlocksWorld.*;

/**
 * @author James MacGlashan.
 */
public class BWModel implements FullStateModel {

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		return FullStateModel.Helper.deterministicTransition(this, s, a);
	}

	@Override
	public State sample(State s, Action a) {

		BlocksWorldState bs = (BlocksWorldState)s.copy();

		if(a.actionName().equals(ACTION_STACK)){
			return stack(bs, (ObjectParameterizedAction)a);
		}
		else if(a.actionName().equals(ACTION_UNSTACK)){
			return unstack(bs, (ObjectParameterizedAction)a);
		}

		throw new RuntimeException("Unknown action " + a.toString());
	}


	protected State stack(BlocksWorldState s, ObjectParameterizedAction a){

		String [] params = a.getObjectParameters();

		BlocksWorldBlock src = (BlocksWorldBlock)s.object(params[0]);
		BlocksWorldBlock target = (BlocksWorldBlock)s.object(params[1]);

		String srcOnName = src.on;

		BlocksWorldBlock nsrc = src.copy();
		nsrc.on = target.name;
		BlocksWorldBlock ntarget = target.copy();
		target.clear = false;

		s.addObject(nsrc).addObject(ntarget);

		if(!srcOnName.equals(TABLE_VAL)){
			BlocksWorldBlock oldTarget = (BlocksWorldBlock)s.object(srcOnName).copy();
			oldTarget.clear = true;
			s.addObject(oldTarget);
		}

		return s;

	}

	protected State unstack(BlocksWorldState s, ObjectParameterizedAction a){

		String [] params = a.getObjectParameters();

		BlocksWorldBlock src = (BlocksWorldBlock)s.object(params[0]);

		String srcOnName = src.on;

		s.set(src.name() + ":" + VAR_ON, TABLE_VAL);
		if(!srcOnName.equals(TABLE_VAL)) {
			s.set(srcOnName + ":" + VAR_CLEAR, true);
		}


		return s;

	}

}
