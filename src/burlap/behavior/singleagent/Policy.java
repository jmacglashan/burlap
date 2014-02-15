package burlap.behavior.singleagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.options.Option;
import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.minecraft.Affordance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.NullAction;


/**
 * This abstract class is used to store a policy for a domain that can be queried and perform common operations with the policy.
 * @author James MacGlashan
 *
 */
public abstract class Policy {

	protected boolean evaluateDecomposesOptions = true;
	protected boolean annotateOptionDecomposition = true;
	
	/**
	 * This method will return an action sampled by the policy for the given state. If the defined policy is
	 * stochastic, then multiple calls to this method for the same state may return different actions. The sampling
	 * should be with respect to defined action distribution that is returned by getActionDistributionForState
	 * @param s the state for which an action should be returned
	 * @return a sample action from the action distribution; null if the policy is undefined for s
	 */
	public abstract GroundedAction getAction(State s);
	public abstract GroundedAction getAffordanceAction(State s, ArrayList<Affordance> kb);
	
	
	
	/**
	 * This method will return action probability distribution defined by the policy. The action distribution is represented
	 * by a list of ActionProb objects, each which specifies a grounded action and a probability of that grounded action being
	 * taken. The returned list does not have to include actions with probability 0.
	 * @param s the state for which an action distribution should be returned
	 * @return a list of possible actions taken by the policy and their probability. 
	 */
	public abstract List<ActionProb> getActionDistributionForState(State s); //returns null when policy is undefined for s
	
	/**
	 * Indicates whether the policy is stochastic or deterministic.
	 * @return true when the policy is stochastic; false when it is deterministic.
	 */
	public abstract boolean isStochastic();
	
	public abstract boolean isDefinedFor(State s);
	
	/**
	 * Will return the probability of this policy taking action ga in state s
	 * @param s the state in which the action would be taken
	 * @param ga the action being queried
	 * @return the probability of this policy taking action ga in state s
	 */
	public double getProbOfAction(State s, GroundedAction ga){
		List <ActionProb> probs = this.getActionDistributionForState(s);
		if(probs == null || probs.size() == 0){
			throw new PolicyUndefinedException();
		}
		for(ActionProb ap : probs){
			if(ap.ga.equals(ga)){
				return ap.pSelection;
			}
		}
		return 0.;
	}
	
	
	public static double getProbOfActionGivenDistribution(State s, GroundedAction ga, List<ActionProb> distribution){
		if(distribution == null || distribution.size() == 0){
			throw new RuntimeException("Distribution is null or empty, cannot return probability for given action.");
		}
		for(ActionProb ap : distribution){
			if(ap.ga.equals(ga)){
				return ap.pSelection;
			}
		}
		return 0.;
	}
	
	/**
	 * A helper method for defining deterministic policies. This method relies on the getAction method being
	 * implemented and will return a list of ActionProb objects with a single instance: the result of
	 * the getAction method with assigned probability 1. This method simplifies the definition of
	 * deterministic policies because the getActionDistributionForState method can just retunr this method.
	 * @param s the state for which the action distribution should be returned.
	 * @return a deterministic action distribution for the action returned by the getAction method.
	 */
	protected List <ActionProb> getDeterministicPolicy(State s){
		GroundedAction ga = this.getAction(s);
		if(ga == null){
			throw new PolicyUndefinedException();
		}
		ActionProb ap = new ActionProb(ga, 1.);
		List <ActionProb> aps = new ArrayList<Policy.ActionProb>();
		aps.add(ap);
		return aps;
	}
	
	/**
	 * This method will return an action sampled by the policy for the given state. If the defined policy is
	 * stochastic, then multiple calls to this method for the same state may return different actions. The sampling
	 * should be with respect to defined action distribution that is returned by getActionDistributionForState
	 * @param s the state for which an action should be returned
	 * @return a sample action from the action distribution; null if the policy is undefined for s
	 */
	public GroundedAction getAffordanceAction(State s) {
		return null;
	}
	
	
	/**
	 * This is a helper method for stochastic policies. If the policy is stochastic, then rather than
	 * having the subclass policy define both the getAction method and getActionDistribution method,
	 * the subclass needs to only define the getActionDistribution method and the getAction method can simply
	 * call this method to return an action.
	 * @param s
	 * @return a GroundedAction to take
	 */
	protected GroundedAction sampleFromActionDistribution(State s){
		Random rand = RandomFactory.getMapped(0);
		double roll = rand.nextDouble();
		List <ActionProb> probs = this.getActionDistributionForState(s);
		if(probs == null || probs.size() == 0){
			throw new PolicyUndefinedException();
		}
		double sump = 0.;
		for(ActionProb ap : probs){
			sump += ap.pSelection;
			if(roll < sump){
				return ap.ga;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Sets whether the primitive actions taken during an options will be included as steps in produced EpisodeAnalysis objects.
	 * The default value is true. If this is set to false, then EpisodeAnalysis objects returned from evaluating a policy will record options
	 * as a single "action" and the steps taken by the option will be hidden. 
	 * @param toggle whether to decompose options into the primitive actions taken by them or not.
	 */
	public void evaluateMethodsShouldDecomposeOption(boolean toggle){
		this.evaluateDecomposesOptions = toggle;
	}
	
	/**
	 * Sets whether options that are decomposed into primitives will have the option that produced them and listed.
	 * The default value is true. If option decomposition is not enabled, changing this value will do nothing. When it
	 * is enabled and this is set to true, primitive actions taken by an option in EpisodeAnalysis objects will be
	 * recorded with a special action name that indicates which option was called to produce the primitive action
	 * as well as which step of the option the primitive action is. When set to false, recorded names of primitives
	 * will be only the primitive aciton's name it will be unclear which option was taken to generate it.
	 * @param toggle whether to annotate the primitive actions of options with the calling option's name.
	 */
	public void evaluateMethodsShouldAnnotateOptionDecomposition(boolean toggle){
		this.annotateOptionDecomposition = toggle;
	}
	
	
	/**
	 * This method will return the an episode that results from following this policy from state s. The episode will terminate
	 * when the policy reaches a terminal state defined by tf.
	 * @param s the state from which to roll out the policy
	 * @param rf the reward function used to track rewards accumulated during the episode
	 * @param tf the terminal function defining when the policy should stop being followed.
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public EpisodeAnalysis evaluateBehavior(State s, RewardFunction rf, TerminalFunction tf){
		EpisodeAnalysis res = new EpisodeAnalysis();
		res.addState(s); //add initial state
		
		State cur = s;
		while(!tf.isTerminal(cur)){
			cur = this.followAndRecordPolicy(res, cur, rf);
		}
		
		return res;
	}
	
	
	
	/**
	 * This method will return the an episode that results from following this policy from state s. The episode will terminate
	 * when the policy reaches a terminal state defined by tf or when the number of steps surpasses maxSteps.
	 * @param s the state from which to roll out the policy
	 * @param rf the reward function used to track rewards accumulated during the episode
	 * @param tf the terminal function defining when the policy should stop being followed.
	 * @param maxSteps the maximum number of steps to take before terminating the policy rollout.
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public EpisodeAnalysis evaluateBehavior(State s, RewardFunction rf, TerminalFunction tf, int maxSteps){
		EpisodeAnalysis res = new EpisodeAnalysis();
		res.addState(s); //add initial state
		
		State cur = s;
		int nSteps = 0;
		while(!tf.isTerminal(cur) && nSteps < maxSteps){
			
			cur = this.followAndRecordPolicy(res, cur, rf);
			
			nSteps = res.numTimeSteps();
			
		}
		
		return res;
	}
	
	/**
	 * This method will return the an episode that results from following this policy from state s. The episode will terminate
	 * when the policy reaches a terminal state defined by tf or when the number of steps surpasses maxSteps.
	 * @param s the state from which to roll out the policy
	 * @param rf the reward function used to track rewards accumulated during the episode
	 * @param tf the terminal function defining when the policy should stop being followed.
	 * @param maxSteps the maximum number of steps to take before terminating the policy rollout.
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public EpisodeAnalysis evaluateAffordanceBehavior(State s, RewardFunction rf, TerminalFunction tf, ArrayList<Affordance> kb){
		EpisodeAnalysis res = new EpisodeAnalysis();
		res.addState(s); //add initial state
		
		State cur = s;
		while(!tf.isTerminal(cur)){
			
			cur = this.followAndRecordAffordancePolicy(res, cur, rf, kb);
			
		}
		
		return res;
	}
	
	/**
	 * This method will return the an episode that results from following this policy from state s. The episode will terminate
	 * when the number of steps taken is >= numSteps.
	 * @param s the state from which to roll out the policy
	 * @param rf the reward function used to track rewards accumulated during the episode
	 * @param numSteps the number of steps to take before terminating the policy rollout
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public EpisodeAnalysis evaluateBehavior(State s, RewardFunction rf, int numSteps){
		EpisodeAnalysis res = new EpisodeAnalysis();
		res.addState(s);
		
		State cur = s;
		int nSteps = 0;
		while(nSteps < numSteps){
			
			cur = this.followAndRecordPolicy(res, cur, rf);
			
			nSteps = res.numTimeSteps();
			
		}
		
		return res;
	}
	
	
	private State followAndRecordPolicy(EpisodeAnalysis ea, State cur, RewardFunction rf){
		
		State next = null;
		
		//follow policy
		GroundedAction ga = this.getAction(cur);
		if(ga == null){
			throw new PolicyUndefinedException();
		}
		if(ga.action.isPrimitive() || !this.evaluateDecomposesOptions){
			next = ga.executeIn(cur);
			double r = rf.reward(cur, ga, next);
			
			//record result
			ea.recordTransitionTo(next, ga, r);
		}
		else{
			//then we need to decompose the option
			Option o = (Option)ga.action;
			o.initiateInState(cur, ga.params);
			int ns = 0;
			do{
				//do step of option
				GroundedAction cga = o.oneStepActionSelection(cur, ga.params);
				next = cga.executeIn(cur);
				double r = rf.reward(cur, cga, next);
				
				if(annotateOptionDecomposition){
					//setup a null action to record the option and primitive action taken
					NullAction annotatedPrimitive = new NullAction(o.getName() + "(" + ns + ")-" + cga.action.getName());
					GroundedAction annotatedPrimitiveGA = new GroundedAction(annotatedPrimitive, cga.params);
					
					//record it
					ea.recordTransitionTo(next, annotatedPrimitiveGA, r);
				}
				else{
					//otherwise just record the primitive that was taken
					ea.recordTransitionTo(next, cga, r);
				}
				
				cur = next;
				ns++;
				
				
			}while(o.continueFromState(cur, ga.params));
			
		}
		
		//return outcome state
		return next;
	}
	
private State followAndRecordAffordancePolicy(EpisodeAnalysis ea, State cur, RewardFunction rf, ArrayList<Affordance> kb){
		
		State next = null;
		
		//follow policy
		GroundedAction ga = this.getAffordanceAction(cur, kb);
		if(ga == null){
			throw new PolicyUndefinedException();
		}
		if(ga.action.isPrimitive() || !this.evaluateDecomposesOptions){
			next = ga.executeIn(cur);
			double r = rf.reward(cur, ga, next);
			
			//record result
			ea.recordTransitionTo(next, ga, r);
		}
		else{
			//then we need to decompose the option
			Option o = (Option)ga.action;
			o.initiateInState(cur, ga.params);
			int ns = 0;
			do{
				//do step of option
				GroundedAction cga = o.oneStepActionSelection(cur, ga.params);
				next = cga.executeIn(cur);
				double r = rf.reward(cur, cga, next);
				
				if(annotateOptionDecomposition){
					//setup a null action to record the option and primitive action taken
					NullAction annotatedPrimitive = new NullAction(o.getName() + "(" + ns + ")-" + cga.action.getName());
					GroundedAction annotatedPrimitiveGA = new GroundedAction(annotatedPrimitive, cga.params);
					
					//record it
					ea.recordTransitionTo(next, annotatedPrimitiveGA, r);
				}
				else{
					//otherwise just record the primitive that was taken
					ea.recordTransitionTo(next, cga, r);
				}
				
				cur = next;
				ns++;
				
				
			}while(o.continueFromState(cur, ga.params));
			
		}
		
		//return outcome state
		return next;
	}

	/**
	 * Class for storing an action and probability tuple. The probability represents the probability that the action will be selected.
	 * @author James MacGlashan
	 *
	 */
	public static class ActionProb{
		
		/**
		 * The action to be considered.
		 */
		public GroundedAction ga;
		
		/**
		 * The probability of the action being selected.
		 */
		public double pSelection;
		
		
		/**
		 * Initializes the action, probability tuple.
		 * @param ga the action to be considered
		 * @param p the probability of the action being selected.
		 */
		public ActionProb(GroundedAction ga, double p){
			this.ga = ga;
			this.pSelection = p;
		}
		
	}
	
	
	/**
	 * RuntimeException to be thrown when a Policy is queried for a state in which the policy is undefined.
	 * @author James MacGlashan
	 *
	 */
	public static class PolicyUndefinedException extends RuntimeException{

		private static final long serialVersionUID = 1L;
		
		public PolicyUndefinedException(){
			super("Policy is undefined for provided state");
		}
		
	}
	
}
