package burlap.behavior.singleagent.learning.actorcritic.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.learning.actorcritic.Actor;
import burlap.behavior.singleagent.learning.actorcritic.CritiqueResult;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.datastructures.BoltzmannDistribution;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * And Actor component whose policy is defined by a Boltzmann distribution over action preferences. This actor stores
 * state-action preferences tabularly and therefore requires a {@link burlap.behavior.statehashing.StateHashFactory} to perform lookups.
 * @author James MacGlashan
 *
 */
public class BoltzmannActor extends Actor {

	/**
	 * The domain in which this agent will act
	 */
	protected Domain								domain;
	
	/**
	 * The actions the agent can perform
	 */
	protected List<Action>							actions;
	
	/**
	 * The hashing factory used to hash states and evaluate state equality
	 */
	protected StateHashFactory						hashingFactory;
	
	/**
	 * The learning rate used to update action preferences in response to critiques.
	 */
	protected LearningRate							learningRate;
	
	/**
	 * A map from (hashed) states to Policy nodes; the latter of which contains the action preferences
	 * for each applicable action in the state.
	 */
	protected Map<StateHashTuple, PolicyNode>		preferences;
	
	
	/**
	 * Indicates whether the actions that this agent can perform are parameterized
	 */
	protected boolean								containsParameterizedActions = false;
	
	
	
	/**
	 * Initializes the Actor
	 * @param domain the domain in which the agent will act
	 * @param hashingFactory the state hashing factory to use for state hashing and equality checks
	 * @param learningRate the learning rate that affects how quickly the agent adjusts its action preferences.
	 */
	public BoltzmannActor(Domain domain, StateHashFactory hashingFactory, double learningRate) {
		this.domain = domain;
		this.actions = new ArrayList<Action>(domain.getActions());
		this.hashingFactory = hashingFactory;
		this.learningRate = new ConstantLR(learningRate);
		
		this.preferences = new HashMap<StateHashTuple, BoltzmannActor.PolicyNode>();
		
		
		for(Action a : actions){
			if(a.getParameterClasses().length > 0){
				containsParameterizedActions = true;
				break;
			}
		}
		
	}
	
	
	/**
	 * Sets the learning rate function to use.
	 * @param lr the learning rate function to use.
	 */
	public void setLearningRate(LearningRate lr){
		this.learningRate = lr;
	}

	@Override
	public void updateFromCritqique(CritiqueResult critqiue) {
		
		StateHashTuple sh = this.hashingFactory.hashState(critqiue.getS());
		PolicyNode node = this.getNode(sh);
		
		double learningRate = this.learningRate.pollLearningRate(sh.s, critqiue.getA());
		
		ActionPreference pref = this.getMatchingPreference(sh, critqiue.getA(), node);
		pref.preference += learningRate * critqiue.getCritique();
		

	}

	@Override
	public void addNonDomainReferencedAction(Action a) {
		
		if(!actions.contains(a)){
			this.actions.add(a);
			if(a.getParameterClasses().length > 0){
				containsParameterizedActions = true;
			}
		}
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		return this.sampleFromActionDistribution(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		
		StateHashTuple sh = this.hashingFactory.hashState(s);
		PolicyNode node = this.getNode(sh);
		
		double [] prefs = new double[node.preferences.size()];
		for(int i = 0; i < node.preferences.size(); i++){
			prefs[i] = node.preferences.get(i).preference;
		}
		
		BoltzmannDistribution bd = new BoltzmannDistribution(prefs);
		double [] probsArray = bd.getProbabilities();
		
		List <ActionProb> probs = new ArrayList<ActionProb>(probsArray.length);
		for(int i = 0; i < probsArray.length; i++){
			ActionPreference ap = node.preferences.get(i);
			probs.add(new ActionProb(ap.ga, probsArray[i]));
		}
		
		if(this.containsParameterizedActions && !this.domain.isObjectIdentifierDependent()){
			//then convert back to this states space
			Map <String, String> matching = node.sh.s.getObjectMatchingTo(s, false);
			
			List <ActionProb> translated = new ArrayList<ActionProb>(probs.size());
			for(ActionProb ap : probs){
				if(ap.ga.params.length == 0){
					translated.add(ap);
				}
				else{
					ActionProb tap = new ActionProb(this.translateAction((GroundedAction)ap.ga, matching), ap.pSelection);
					translated.add(tap);
				}
			}
			
			return translated;
			
		}
		
		
		return probs;
	}
	
	
	/**
	 * Returns the policy node that stores the action preferences for state.
	 * @param sh The (hashed) state of the {@link PolicyNode} to return
	 * @return the {@link PolicyNode} object for the given input state.
	 */
	protected PolicyNode getNode(StateHashTuple sh){
		
		//List <GroundedAction> gas = sh.s.getAllGroundedActionsFor(this.actions);
		List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, sh.s);
		
		PolicyNode node = this.preferences.get(sh);
		if(node == null){
			node = new PolicyNode(sh);
			for(GroundedAction ga : gas){
				node.addPreference(new ActionPreference(ga, 0.0));
			}
			this.preferences.put(sh, node);
		}
		
		return node;
		
	}

	@Override
	public boolean isStochastic() {
		return true;
	}
	
	
	@Override
	public boolean isDefinedFor(State s) {
		return true; //can always create equal-probable action preferences for a new state
	}
	
	@Override
	public void resetData() {
		this.preferences.clear();
		this.learningRate.resetDecay();
	}
	
	
	/**
	 * Returns the stored {@link ActionPreference} that is stored in a policy node. If actions are parameterized and the domain is not name dependent,
	 * then a matching between the input state and stored state is first found and used to match the input action parameters to the stored action parameters.
	 * @param sh the input state on which the input action was applied
	 * @param ga the input action for which the {@link ActionPreferece} object should be returned.
	 * @param node the {@link PolicyNode} object that contains the Action preference.
	 * @return the {@link ActionPreferece} object for the given action stored in the given {@link PolicyNode}; null if it does not exist.
	 */
	protected ActionPreference getMatchingPreference(StateHashTuple sh, GroundedAction ga, PolicyNode node){
		
		GroundedAction translatedAction = ga;
		if(ga.params.length > 0  && !this.domain.isObjectIdentifierDependent()){
			Map <String, String> matching = sh.s.getObjectMatchingTo(node.sh.s, false);
			translatedAction = this.translateAction(ga, matching);
		}
		
		for(ActionPreference p : node.preferences){
			if(p.ga.equals(translatedAction)){
				return p;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Takes a parameterized GroundedAction and returns an action with its parameters shifting according to a provided object matching from the state in
	 * which the action was applied and some other state's object name identifiers.
	 * @param a the source action
	 * @param matching a matching from objects in the state in which the source action was applied to corresponding objects in some other state's object name identifiers
	 * @return a GroundedAction whose parameters are translated from the input GroundedAction, to the corresponding object identifiers specified by a matching.
	 */
	protected GroundedAction translateAction(GroundedAction a, Map <String,String> matching){
		String [] newParams = new String[a.params.length];
		for(int i = 0; i < a.params.length; i++){
			newParams[i] = matching.get(a.params[i]);
		}
		return new GroundedAction(a.action, newParams);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * A class for storing action preferences for the possible actions applicable in a given state.
	 * @author James MacGlashan
	 *
	 */
	class PolicyNode{
		
		/**
		 * A hashed state object.
		 */
		public StateHashTuple			sh;
		
		/**
		 * The action preferences for actions applicable in sh.
		 */
		public List <ActionPreference>	preferences;
		
		
		/**
		 * Initializes with an empty list of action preferences for the given input state sh.
		 * @param sh the input state for which this PolicyNode will be created.
		 */
		public PolicyNode(StateHashTuple sh){
			this.sh = sh;
			this.preferences = new ArrayList<BoltzmannActor.ActionPreference>();
		}
		
		/**
		 * Adds an action preference for this state.
		 * @param pr the action preference to add.
		 */
		public void addPreference(ActionPreference pr){
			this.preferences.add(pr);
		}
		
		
	}
	
	/**
	 * Defines an Action preference; a pair consisting of a GroundedAction and a double representing the preference for that action.
	 * @author James MacGlashan
	 *
	 */
	class ActionPreference{
		
		/**
		 * The action being evaluated.
		 */
		public GroundedAction 	ga;
		
		/**
		 * the preference for action ga.
		 */
		public double			preference;
		
		
		/**
		 * Initializes for a given action and preference for that action.
		 * @param ga the action to be evaluated.
		 * @param preference the preference for the action ga.
		 */
		public ActionPreference(GroundedAction ga, double preference){
			this.ga = ga;
			this.preference = preference;
		}
		
	}

	

}
