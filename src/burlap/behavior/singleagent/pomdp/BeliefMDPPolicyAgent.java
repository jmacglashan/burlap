package burlap.behavior.singleagent.pomdp;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.pomdp.BeliefAgent;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.PODomain;

public class BeliefMDPPolicyAgent extends BeliefAgent {

	protected PODomain	 	domain;
	protected Policy		policy;
	protected SADomain		beliefDomain;
	
	public BeliefMDPPolicyAgent(PODomain domain, Policy policy){
		this.domain = domain;
		this.policy = policy;
		
		BeliefMDPGenerator bdgen = new BeliefMDPGenerator(this.domain);
		this.beliefDomain = (SADomain)bdgen.generateDomain();
	}
	
	
	@Override
	public GroundedAction getAction(BeliefState curBelief) {
		
		//convert to belief state
		State s = BeliefMDPGenerator.getBeliefMDPState(this.beliefDomain, curBelief);
		GroundedAction ga = (GroundedAction)this.policy.getAction(s);
		//System.out.println(s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getStringValForAttribute(BeliefMDPGenerator.ATTBELIEF) + ": " + ga.toString());
		return ga;
	}
	
	

}
