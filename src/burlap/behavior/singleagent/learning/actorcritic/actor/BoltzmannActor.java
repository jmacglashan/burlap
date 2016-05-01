package burlap.behavior.singleagent.learning.actorcritic.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.learning.actorcritic.Actor;
import burlap.behavior.singleagent.learning.actorcritic.CritiqueResult;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.HashableState;
import burlap.datastructures.BoltzmannDistribution;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * And Actor component whose policy is defined by a Boltzmann distribution over action preferences. This actor stores
 * state-action preferences tabularly and therefore requires a {@link burlap.oomdp.statehashing.HashableStateFactory} to perform lookups.
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
	protected HashableStateFactory hashingFactory;
	
	/**
	 * The learning rate used to update action preferences in response to critiques.
	 */
	protected LearningRate							learningRate;
	
	/**
	 * A map from (hashed) states to Policy nodes; the latter of which contains the action preferences
	 * for each applicable action in the state.
	 */
	protected Map<HashableState, PolicyNode>		preferences;
	
	
	/**
	 * Indicates whether the actions that this agent can perform are parameterized
	 */
	protected boolean								containsParameterizedActions = false;
	
	/**
	 * The total number of learning steps performed by this agent.
	 */
	protected int													totalNumberOfSteps = 0;
	
	
	
	/**
	 * Initializes the Actor
	 * @param domain the domain in which the agent will act
	 * @param hashingFactory the state hashing factory to use for state hashing and equality checks
	 * @param learningRate the learning rate that affects how quickly the agent adjusts its action preferences.
	 */
	public BoltzmannActor(Domain domain, HashableStateFactory hashingFactory, double learningRate) {
		this.domain = domain;
		this.actions = new ArrayList<Action>(domain.getActions());
		this.hashingFactory = hashingFactory;
		this.learningRate = new ConstantLR(learningRate);
		
		this.preferences = new HashMap<HashableState, BoltzmannActor.PolicyNode>();
		
		
		for(Action a : actions){
			if(a.isParameterized()){
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
		
		HashableState sh = this.hashingFactory.hashState(critqiue.getS());
		PolicyNode node = this.getNode(sh);
		
		double learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, sh.s, critqiue.getA());
		
		ActionPreference pref = this.getMatchingPreference(sh, critqiue.getA(), node);
		pref.preference += learningRate * critqiue.getCritique();
		
		this.totalNumberOfSteps++;
		

	}

	@Override
	public void addNonDomainReferencedAction(Action a) {
		
		if(!actions.contains(a)){
			this.actions.add(a);
			if(a.isParameterized()){
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
		
		HashableState sh = this.hashingFactory.hashState(s);
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
				ActionProb tap = new ActionProb(((GroundedAction)ap.ga).translateParameters(node.sh.s, s), ap.pSelection);
				translated.add(tap);
			}

			return translated;
			
		}
		
		
		return probs;
	}
	
	
	/**
	 * Returns the policy node that stores the action preferences for state.
	 * @param sh The (hashed) state of the {@link BoltzmannActor.PolicyNode} to return
	 * @return the {@link BoltzmannActor.PolicyNode} object for the given input state.
	 */
	protected PolicyNode getNode(HashableState sh){
		
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
	 * Returns the stored {@link BoltzmannActor.ActionPreference} that is stored in a policy node. If actions are parameterized and the domain is not name dependent,
	 * then a matching between the input state and stored state is first found and used to match the input action parameters to the stored action parameters.
	 * @param sh the input state on which the input action was applied
	 * @param ga the input action for which the {@link BoltzmannActor.ActionPreference} object should be returned.
	 * @param node the {@link BoltzmannActor.PolicyNode} object that contains the Action preference.
	 * @return the {@link BoltzmannActor.ActionPreference} object for the given action stored in the given {@link BoltzmannActor.PolicyNode}; null if it does not exist.
	 */
	protected ActionPreference getMatchingPreference(HashableState sh, GroundedAction ga, PolicyNode node){
		
		GroundedAction translatedAction = ga.translateParameters(sh.s, node.sh.s);
		
		for(ActionPreference p : node.preferences){
			if(p.ga.equals(translatedAction)){
				return p;
			}
		}
		
		return null;
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
		public HashableState sh;
		
		/**
		 * The action preferences for actions applicable in sh.
		 */
		public List <ActionPreference>	preferences;
		
		
		/**
		 * Initializes with an empty list of action preferences for the given input state sh.
		 * @param sh the input state for which this PolicyNode will be created.
		 */
		public PolicyNode(HashableState sh){
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
