package burlap.behavior.singleagent.learning.actorcritic;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public interface Critic {
	
	public void addNonDomainReferencedAction(Action a);
	
	public void initializeEpisode(State s);
	public void endEpisode();

	public CritiqueResult critiqueAndUpdate(State s, GroundedAction ga, State sprime);
	
}
