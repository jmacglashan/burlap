package burlap.oomdp.singleagent.pomdp;

import burlap.oomdp.core.State;

/**
 * Wrapper around a State instance with a BeliefStatistic instance, 
 * allowing a belief statistic to be a used as a state in planners.
 * @author Nakul Gopalan & James MacGlashan 
 *
 */
public class POStateWrapper extends State {
	protected BeliefStatistic bs;

	
	public POStateWrapper(){
		super();
	}
	
	public POStateWrapper(BeliefStatistic setBS){
		super();
		this.bs=setBS;
	}
	
	public void setBS(BeliefStatistic setBS){
		this.bs=setBS;
	}
	
	public BeliefStatistic getBeliefStatistic(){
		return this.bs;
	}
	
	
}
