package burlap.behavior.singleagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.options.Option;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
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
	public abstract AbstractGroundedAction getAction(State s);
	
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


	/**
	 * Specifies whether this policy is defined for the input state.
	 * @param s the input state to test for whether this policy is defined
	 * @return true if this policy is defined for {@link burlap.oomdp.core.State} s, false otherwise.
	 */
	public abstract boolean isDefinedFor(State s);
	
	/**
	 * Will return the probability of this policy taking action ga in state s
	 * @param s the state in which the action would be taken
	 * @param ga the action being queried
	 * @return the probability of this policy taking action ga in state s
	 */
	public double getProbOfAction(State s, AbstractGroundedAction ga){
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
	


	/**
	 * Don't use this, the input state is not necessary; instead use {@link #getProbOfActionGivenDistribution(burlap.oomdp.core.AbstractGroundedAction, java.util.List)}.
	 */
	@Deprecated
	public static double getProbOfActionGivenDistribution(State s, AbstractGroundedAction ga, List<ActionProb> distribution){
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
	 * Searches the input distribution for the occurrence of the input action and returns its probability.
	 * @param ga the {@link burlap.oomdp.core.AbstractGroundedAction} for which its probability in specified distribution should be returned.
	 * @param distribution the probability distribution over actions.
	 * @return the probability of selecting action ga according to the probability specified in distribution.
	 */
	public static double getProbOfActionGivenDistribution(AbstractGroundedAction ga, List<ActionProb> distribution){
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
		AbstractGroundedAction ga = this.getAction(s);
		if(ga == null){
			throw new PolicyUndefinedException();
		}
		ActionProb ap = new ActionProb(ga, 1.);
		List <ActionProb> aps = new ArrayList<Policy.ActionProb>();
		aps.add(ap);
		return aps;
	}
	
	
	
	/**
	 * This is a helper method for stochastic policies. If the policy is stochastic, then rather than
	 * having the subclass policy define both the getAction method and getActionDistribution method,
	 * the subclass needs to only define the getActionDistribution method and the getAction method can simply
	 * call this method to return an action.
	 * @param s
	 * @return an {@link AbstractGroundedAction} to take
	 */
	protected AbstractGroundedAction sampleFromActionDistribution(State s){
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
		
		throw new RuntimeException("Tried to sample policy action distribution, but it did not sum to 1.");
		
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
		AbstractGroundedAction aga = this.getAction(cur);
		if(aga == null){
			throw new PolicyUndefinedException();
		}
		if(!(aga instanceof GroundedAction)){
			throw new RuntimeException("cannot folow policy for non-single agent actions");
		}
		GroundedAction ga = (GroundedAction)aga;
		
		if(ga.action.isPrimitive() || !this.evaluateDecomposesOptions){
			next = ga.executeIn(cur);
			double r = rf.reward(cur, ga, next);
			
			//record result
			ea.recordTransitionTo(ga, next, r);
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
					ea.recordTransitionTo(annotatedPrimitiveGA, next, r);
				}
				else{
					//otherwise just record the primitive that was taken
					ea.recordTransitionTo(cga, next, r);
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
		public AbstractGroundedAction ga;
		
		/**
		 * The probability of the action being selected.
		 */
		public double pSelection;
		
		
		/**
		 * Initializes the action, probability tuple.
		 * @param ga the action to be considered
		 * @param p the probability of the action being selected.
		 */
		public ActionProb(AbstractGroundedAction ga, double p){
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


	/**
	 * A uniform random policy for single agent domains. You may set the actions between which it randomly
	 * selects by providing a domain (from which the domains primitive actions are copied into an internal list)
	 * or from a list of Action objects (from which the action references are copied into an internal list).
	 * You may also add additional actions with the {@link #addAction(burlap.oomdp.singleagent.Action)} method
	 * or remove or clear the actions.
	 * <br/>
	 * Upon action selection, all applicable grounded actions for the state are generated and an action is selected
	 * uniformly randomly from them. The policy is not defined if there are no applicable actions.
	 */
	public static class RandomPolicy extends Policy{


		/**
		 * The actions from which selection is performed
		 */
		protected List<Action> actions;

		/**
		 * The random factory used to randomly select actions.
		 */
		protected Random rand = RandomFactory.getMapped(0);


		/**
		 * Initializes by copying all the primitive actions references defined for the domain into an internal action
		 * list for this policy.
		 * @param domain the domain containing all the primitive actions.
		 */
		public RandomPolicy(Domain domain){
			this.actions = new ArrayList<Action>(domain.getActions());
		}

		/**
		 * Initializes by copying all the actions references defined in the provided list into an internal action
		 * list for this policy.
		 * @param acitons the actions to select between.
		 */
		public RandomPolicy(List<Action> actions){
			this.actions = new ArrayList<Action>(actions);
		}


		/**
		 * Adds an aciton to consider in selection.
		 * @param action an action to consider in selection
		 */
		public void addAction(Action action){
			this.actions.add(action);
		}


		/**
		 * Clears the action list used in action selection. Note that if no actions are added to this policy after
		 * calling this method then the policy will be undefined everywhere.
		 */
		public void clearActions(){
			this.actions.clear();
		}


		/**
		 * Removes an action from consideration.
		 * @param actionName the name of the action to remove.
		 */
		public void removeAction(String actionName){
			Action toRemove = null;
			for(Action a : this.actions){
				if(a.getName().equals(actionName)){
					toRemove = a;
					break;
				}
			}
			if(toRemove != null){
				this.actions.remove(toRemove);
			}
		}

		/**
		 * Returns of the list of actions that can be randomly selected.
		 * @return the list of actions that can be randomly selected.
		 */
		public List<Action> getSelectionActions(){
			return this.actions;
		}


		/**
		 * Returns the random generator used for action selection.
		 * @return the random generator used for action selection.
		 */
		public Random getRandomGenerator(){
			return this.rand;
		}


		/**
		 * Sets the random generator used for action selection.
		 * @param rand the random generator used for action selection.
		 */
		public void setRandomGenerator(Random rand){
			this.rand = rand;
		}


		@Override
		public AbstractGroundedAction getAction(State s) {
			List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, s);
			if(gas.size() == 0){
				throw new PolicyUndefinedException();
			}
			GroundedAction selection = gas.get(this.rand.nextInt(this.actions.size()));
			return selection;
		}

		@Override
		public List<ActionProb> getActionDistributionForState(State s) {
			List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, s);
			if(gas.size() == 0){
				throw new PolicyUndefinedException();
			}
			double p = 1./gas.size();
			List<ActionProb> aps = new ArrayList<ActionProb>(gas.size());
			for(GroundedAction ga : gas){
				ActionProb ap = new ActionProb(ga, p);
				aps.add(ap);
			}
			return aps;
		}

		@Override
		public boolean isStochastic() {
			return true;
		}

		@Override
		public boolean isDefinedFor(State s) {
			return Action.getAllApplicableGroundedActionsFromActionList(this.actions, s).size() > 0;
		}
	}
	
}
