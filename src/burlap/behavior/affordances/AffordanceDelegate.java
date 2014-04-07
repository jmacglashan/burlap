package burlap.behavior.affordances;

import java.util.Collection;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.logicalexpressions.LogicalExpression;

public class AffordanceDelegate {

	 protected Affordance								affordance;
	 protected Collection<AbstractGroundedAction>		listedActionSet;
	 
	 public AffordanceDelegate(Affordance affordance){
		 this.affordance = affordance;
		 this.resampleActionSet();
	 }
	 
	 public void resampleActionSet(){
		 this.listedActionSet = affordance.sampleNewLiftedActionSet();
	 }
	 
	 public void setCurrentGoal(LogicalExpression currentGoal){
		 //TODO: fill this in; should set current goal; check if this affordance does not satisfy; and handle variable bindings with lifted affordance goal if it is satisifed.
	 }
	 
	 /**
	  * Primes this affordance to answer if actions are relevant for the given state (using method {@link #actionIsRelevant(AbstractGroundedAction)})
	  * and return whether this affordance is active for the given state and using the currently set goal as the task (sub)goal.
	  * If this affordance is not active,
	  * then any subsequent queries to {@link #isActionRelevant(AbstractGroundedAction)} will return false.
	  * An affordance is determined to be active if its preconditions are satisifed in s and if the current task goal
	  * entails the affordance lifted goal description.
	  * @param s the state in which to prime the affordance
	  * @return true if this affordance is active, false if it is not.
	  */
	 public boolean primeAndCheckIfActiveInState(State s){
		 //TODO: fill this in
		 return false;
	 }
	 
	 public boolean actionIsRelevant(AbstractGroundedAction action){
		 //TODO: fill this in
		 return false;
	 }
	 
	 
}
