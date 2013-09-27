package burlap.behavior.singleagent.learning.actorcritic;


import burlap.behavior.singleagent.Policy;
import burlap.oomdp.singleagent.Action;

public abstract class Actor extends Policy {

	public abstract void updateFromCritqique(CritiqueResult critqiue);
	public abstract void addNonDomainReferencedAction(Action a);

}
