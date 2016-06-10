package burlap.domain.singleagent.pomdp.tiger;

import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.pomdp.tiger.TigerDomain.*;

/**
 * @author James MacGlashan.
 */
public class TigerModel implements FullModel {

	/**
	 * the reward for opening the correct door
	 */
	public double correctDoor = 10.;

	/**
	 * The reward for opening the wrong door
	 */
	public double wrongDoor = -100.;

	/**
	 * The reward for listening
	 */
	public double listen = -1.;

	/**
	 * The reward for do nothing.
	 */
	public double nothing = 0.;

	protected StateGenerator sg = TigerDomain.randomSideStateGenerator(0.5);

	public TigerModel(double correctDoor, double wrongDoor, double listen, double nothing) {
		this.correctDoor = correctDoor;
		this.wrongDoor = wrongDoor;
		this.listen = listen;
		this.nothing = nothing;
	}

	@Override
	public List<TransitionProb> transitions(State s, Action a) {
		String aname = a.actionName();
		if(aname.equals(ACTION_LEFT)){

			TigerState ts = (TigerState)s;
			double r = ts.door.equals(VAL_LEFT) ? wrongDoor : correctDoor;
			return Arrays.asList(
					new TransitionProb(0.5, new EnvironmentOutcome(s, a, new TigerState(VAL_LEFT), r, false)),
					new TransitionProb(0.5, new EnvironmentOutcome(s, a, new TigerState(VAL_RIGHT), r, false)));


		}
		else if(aname.equals(ACTION_RIGHT)){
			TigerState ts = (TigerState)s;
			double r = ts.door.equals(VAL_LEFT) ? correctDoor : wrongDoor;
			return Arrays.asList(
					new TransitionProb(0.5, new EnvironmentOutcome(s, a, new TigerState(VAL_LEFT), r, false)),
					new TransitionProb(0.5, new EnvironmentOutcome(s, a, new TigerState(VAL_RIGHT), r, false)));
		}
		else if(aname.equals(ACTION_LISTEN)){
			return Arrays.asList(new TransitionProb(1., new EnvironmentOutcome(s, a, s, listen, false)));
		}
		else if(aname.equals(ACTION_DO_NOTHING)){
			return Arrays.asList(new TransitionProb(1., new EnvironmentOutcome(s, a, s, nothing, false)));
		}

		throw new RuntimeException("Unknown action " + a.toString());
	}

	@Override
	public EnvironmentOutcome sample(State s, Action a) {

		String aname = a.actionName();
		if(aname.equals(ACTION_LEFT)){

			TigerState ts = (TigerState)s;
			if(ts.door.equals(VAL_LEFT)){
				return new EnvironmentOutcome(s, a, sg.generateState(), wrongDoor, false);
			}
			else{
				return new EnvironmentOutcome(s, a, sg.generateState(), correctDoor, false);
			}

		}
		else if(aname.equals(ACTION_RIGHT)){
			TigerState ts = (TigerState)s;
			if(ts.door.equals(VAL_LEFT)){
				return new EnvironmentOutcome(s, a, sg.generateState(), correctDoor, false);
			}
			else{
				return new EnvironmentOutcome(s, a, sg.generateState(), wrongDoor, false);
			}
		}
		else if(aname.equals(ACTION_LISTEN)){
			return new EnvironmentOutcome(s, a, s, listen, false);
		}
		else if(aname.equals(ACTION_DO_NOTHING)){
			return new EnvironmentOutcome(s, a, s, nothing, false);
		}

		throw new RuntimeException("Unknown action " + a.toString());
	}



	@Override
	public boolean terminal(State s) {
		return false;
	}
}
