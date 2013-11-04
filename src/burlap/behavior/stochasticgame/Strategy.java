package burlap.behavior.stochasticgame;

import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;


public abstract class Strategy {

	public abstract GroundedSingleAction getAction(State s); //returns null when policy is undefined for s
	public abstract List<SingleActionProb> getActionDistributionForState(State s); //returns null when policy is undefined for s
	public abstract boolean isStochastic();
	
	
	public class SingleActionProb{
		public GroundedSingleAction ga;
		public double pSelection;
		
		public SingleActionProb(GroundedSingleAction ga, double p){
			this.ga = ga;
			this.pSelection = p;
		}
		
	}

}
