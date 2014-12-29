package burlap.behavior.singleagent.pomdp;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class POMDPEpisodeAnalysis extends EpisodeAnalysis {
	public List<State>							observationSequence;
	
	public POMDPEpisodeAnalysis(){
		super();
		this.initializePOMDPRelatedDataStructures();
	}

	private void initializePOMDPRelatedDataStructures() {
		this.observationSequence = new ArrayList<State>();
		
	} 
	
	@Deprecated
	@Override
	public void recordTransitionTo(State next, GroundedAction usingAction, double r){
		stateSequence.add(next);
		actionSequence.add(usingAction);
		rewardSequence.add(r);
		observationSequence.add(null);
		System.out.println("POMDPEpisodeAnalysis, recordTransitionTo: Current Observation not provided");
	}
	
	@Deprecated
	public void recordTransitionTo(State next, GroundedAction usingAction, double r, State observation){
		stateSequence.add(next);
		actionSequence.add(usingAction);
		rewardSequence.add(r);
		observationSequence.add(observation);
	}
	
	
	/**
	 * Records an transition event where the agent applied the usingAction action in the last
	 * state in this object's state sequence, transitioned to state nextState, and received reward r,. 
	 * @param usingAction the action the agent used that caused the transition
	 * @param nextState the next state to which the agent transitioned
	 * @param r the reward the agent received for this transition.
	 */
	@Override
	public void recordTransitionTo(GroundedAction usingAction, State nextState, double r){
		stateSequence.add(nextState);
		actionSequence.add(usingAction);
		rewardSequence.add(r);
		observationSequence.add(null);
		System.out.println("POMDPEpisodeAnalysis, recordTransitionTo: Current Observation not provided");
	}
	
	public void recordTransitionTo(GroundedAction usingAction, State nextState, double r, State observation){
		stateSequence.add(nextState);
		actionSequence.add(usingAction);
		rewardSequence.add(r);
		observationSequence.add(observation);
	}
	
	public State getObservation(int t){
		return observationSequence.get(t);
	}
	
	public void appendAndMergeEpisodeAnalysis(EpisodeAnalysis e){
		for(int i = 0; i < e.numTimeSteps()-1; i++){
			this.recordTransitionTo(e.getAction(i), e.getState(i+1), e.getReward(i+1));
		}
		System.out.println("POMDPEpisodeAnalysis, appendAndMergeEpisodeAnalysis: Observations not provided");
	}
	
	public void appendAndMergeEpisodeAnalysis(POMDPEpisodeAnalysis e){
		for(int i = 0; i < e.numTimeSteps()-1; i++){
			this.recordTransitionTo(e.getAction(i), e.getState(i+1), e.getReward(i+1), e.getObservation(i+1));
		}
	}
	
	/**
	 * Converts this episode into a string representation.
	 * @param sp the state parser to use to convert state objects to string representations.
	 * @return a string representation of this episode.
	 */
	@Override
	public String parseIntoString(StateParser sp){
		/*
		StringBuffer sbuf = new StringBuffer(256);
		
		for(int i = 0; i < stateSequence.size(); i++){
			
			sbuf.append("#EL#\n").append(sp.stateToString(stateSequence.get(i))).append("\n#ES#\n");
			if(i < stateSequence.size()-1){
				sbuf.append(getSpaceDelimGAString(actionSequence.get(i))).append("\n").append(rewardSequence.get(i)).append("\n");
			}
			
		}
		
		
		return sbuf.toString();
		*/
		//TODO: fill in the parse to string
		System.out.println("POMDPEpisodeAnalysis, parseIntoString: This method does not work");
		return null;
		
	}
	
public static EpisodeAnalysis parseStringIntoEA(String str, Domain d, StateParser sp){
	//TODO: fill in the parse to EA method
	System.out.println("POMDPEpisodeAnalysis, parseStringIntoEA: This method does not work");
	/*
		EpisodeAnalysis ea = new EpisodeAnalysis();
		
		String [] elComps = str.split("#EL#\n");
		
		//System.out.println("--" + elComps[0] + "--");
		
		for(int i = 1; i < elComps.length; i++){
			
			String spToken = "\n#ES#";
			if(!elComps[i].endsWith(spToken)){
				spToken += "\n";
			}
			
			String [] parts = elComps[i].split(spToken);
			
			State s = sp.stringToState(parts[0]);
			if(i < elComps.length-1){
				String [] ars = parts[1].split("\n");
				ea.recordTransitionTo(getGAFromSpaceDelimGASTring(d, ars[0]), s, Double.parseDouble(ars[1]));
			}
			else{
				ea.addState(s);
			}
		}
		
		
		return ea;
		*/
		return null;
	}
	
	
	

}
