package burlap.domain.singleagent.pomdp.tiger;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.pomdp.observations.DiscreteObservationFunction;
import burlap.mdp.singleagent.pomdp.observations.ObservationProbability;
import burlap.mdp.singleagent.pomdp.observations.ObservationUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the Tiger domain observation function
 */
public class TigerObservations implements DiscreteObservationFunction {

	protected double listenAccuracy;
	protected boolean includeDoNothing;

	public TigerObservations(double listenAccuracy, boolean includeDoNothing){
		this.listenAccuracy = listenAccuracy;
	}

	@Override
	public List<State> allObservations() {

		List<State> result = new ArrayList<State>(3);

		result.add(this.observationLeft());
		result.add(this.observationRight());
		result.add(this.observationReset());
		if(includeDoNothing){
			result.add(this.observationNothing());
		}

		return result;
	}

	@Override
	public State sample(State state, Action action){
		//override for faster sampling
		if(action.actionName().equals(TigerDomain.ACTION_LEFT) || action.actionName().equals(TigerDomain.ACTION_RIGHT)){
			return this.observationReset();
		}
		else if(action.actionName().equals(TigerDomain.ACTION_LISTEN)){
			String tigerVal = (String)state.get(TigerDomain.VAR_DOOR);
			double r = RandomFactory.getMapped(0).nextDouble();
			if(r < this.listenAccuracy){
				if(tigerVal.equals(TigerDomain.VAL_LEFT)){
					return this.observationLeft();
				}
				else{
					return this.observationRight();
				}
			}
			else{
				//then nosiy listen; reverse direction
				if(tigerVal.equals(TigerDomain.VAL_LEFT)){
					return this.observationRight();
				}
				else{
					return this.observationLeft();
				}
			}
		}
		else if(action.actionName().equals(TigerDomain.ACTION_DO_NOTHING)){
			return this.observationNothing();
		}

		throw new RuntimeException("Unknown action " + action.actionName() + "; cannot return observation sample.");
	}

	@Override
	public double probability(State observation, State state,
							  Action action) {


		String oVal = (String)observation.get(TigerDomain.VAR_HEAR);
		String tigerVal = (String)state.get(TigerDomain.VAR_DOOR);

		if(action.actionName().equals(TigerDomain.ACTION_LEFT) || action.actionName().equals(TigerDomain.ACTION_RIGHT)){
			if(oVal.equals(TigerDomain.DOOR_RESET)){
				return 1.;
			}
			return 0.;
		}

		if(action.actionName().equals(TigerDomain.ACTION_LISTEN)){
			if(tigerVal.equals(TigerDomain.VAL_LEFT)){
				if(oVal.equals(TigerDomain.HEAR_LEFT)){
					return this.listenAccuracy;
				}
				else if(oVal.equals(TigerDomain.HEAR_RIGHT)){
					return 1.-this.listenAccuracy;
				}
				else{
					return 0.;
				}
			}
			else{
				if(oVal.equals(TigerDomain.HEAR_LEFT)){
					return 1.-this.listenAccuracy;
				}
				else if(oVal.equals(TigerDomain.HEAR_RIGHT)){
					return this.listenAccuracy;
				}
				else{
					return 0.;
				}
			}
		}

		//otherwise we're in the noop
		if(action.actionName().equals(TigerDomain.ACTION_DO_NOTHING)){
			if(oVal.equals(TigerDomain.HEAR_NOTHING)){
				return 1.;
			}
			else{
				return 0.;
			}
		}

		throw new RuntimeException("Unknown action " + action.actionName() + "; cannot return observation probability.");
	}

	@Override
	public List<ObservationProbability> probabilities(State state, Action action) {
		return ObservationUtilities.probabilitiesByEnumeration(this, state, action);
	}

	/**
	 * Returns the observation of hearing the tiger behind the left door
	 * @return a {@link State} specifying the observation of hearing the tiger behind the left door
	 */
	protected State observationLeft(){
		return new TigerObservation(TigerDomain.HEAR_LEFT);
	}


	/**
	 * Returns the observation of hearing the tiger behind the right door
	 * @return a {@link State} specifying the observation of hearing the tiger behind the right door
	 */
	protected State observationRight(){
		return new TigerObservation(TigerDomain.HEAR_RIGHT);
	}


	/**
	 * Returns the observation of approaching a new pair of doors
	 * @return a {@link State} specifying the observation of approaching a new pair of doors
	 */
	protected State observationReset(){
		return new TigerObservation(TigerDomain.DOOR_RESET);
	}


	/**
	 * Returns the observation of hearing nothing; occurs when the do nothing action is selected
	 * @return a {@link State} specifying the observation of hearing nothing; occurs when the do nothing action is selected
	 */
	protected State observationNothing(){
		return new TigerObservation(TigerDomain.HEAR_NOTHING);
	}


}
