package burlap.oomdp.singleagent.pomdp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public abstract class ObservationFunction {

	protected PODomain domain;
	
	public ObservationFunction(PODomain domain){
		this.domain = domain;
		this.domain.setObservationFunction(this);
	}
	
	public abstract List<State> getAllPossibleObservations();
	
	
	
	public abstract double getObservationProbability(State observation, State state, GroundedAction action);
	
	public abstract boolean isTerminalObservation(State observation);
	
	public List<ObservationProbability> getObservationProbabilities(State state, GroundedAction action){
		List<State> possibleObservations = this.getAllPossibleObservations();
		List<ObservationProbability> probs = new ArrayList<ObservationFunction.ObservationProbability>(possibleObservations.size());
		
		for(State obs : possibleObservations){
			double p = this.getObservationProbability(obs, state, action);
			if(p != 0){
				probs.add(new ObservationProbability(obs, p));
			}
		}
		
		return probs;
	}
	
	public State sampleObservation(State state, GroundedAction action){
		List<ObservationProbability> obProbs = this.getObservationProbabilities(state, action);
		Random rand = RandomFactory.getMapped(0);
		double r = rand.nextDouble();
		double sumProb = 0.;
		for(ObservationProbability op : obProbs){
			sumProb += op.p;
			if(r < sumProb){
				return op.observation;
			}
		}
		
		throw new RuntimeException("Could not sample observaiton because observation probabilities did not sum to 1; they summed to " + sumProb);
	}
	
	
	public class ObservationProbability{
		public State observation;
		public double p;
		
		public ObservationProbability(State observation, double p){
			this.observation = observation;
			this.p = p;
		}
	}
	
}
