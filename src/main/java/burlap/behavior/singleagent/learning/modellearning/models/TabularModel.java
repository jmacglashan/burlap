package burlap.behavior.singleagent.learning.modellearning.models;

import burlap.behavior.singleagent.learning.modellearning.KWIKModel;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.*;


/**
 * A tabular model using frequencies to model the transition dynamics.
 * 
 * 
 * @author James MacGlashan; adapted from code provided by Takehiro Oyakawa and Chan Trau
 *
 */
public class TabularModel implements KWIKModel {

	/**
	 * The source actual domain object for which actions will be modeled.
	 */
	protected SADomain sourceDomain;
	
	/**
	 * The hashing factory to use for indexing states
	 */
	protected HashableStateFactory hashingFactory;
	
	/**
	 * A mapping from (hashed) states to state nodes that store transition statistics
	 */
	protected Map<HashableState, StateNode> 	stateNodes;
	
	/**
	 * The set of states marked as terminal states.
	 */
	protected Set<HashableState> 				terminalStates;
	
	/**
	 * The number of transitions necessary to be confident in a model's prediction.
	 */
	protected int								nConfident;

	
	/**
	 * Initializes.
	 * @param sourceDomain the source domain whose actions will be modeled.
	 * @param hashingFactory the hashing factory to index states
	 * @param nConfident the number of observed transitions to be confident in the model's prediction.
	 */
	public TabularModel(SADomain sourceDomain, HashableStateFactory hashingFactory, int nConfident){
		this.sourceDomain = sourceDomain;
		this.hashingFactory = hashingFactory;
		this.stateNodes = new HashMap<HashableState, TabularModel.StateNode>();
		this.terminalStates = new HashSet<HashableState>();
		this.nConfident = nConfident;

	}


	@Override
	public boolean transitionIsModeled(State s, Action ga) {
		
		StateActionNode san = this.getStateActionNode(this.hashingFactory.hashState(s), ga);
		if(san == null){
			return false;
		}
		if(san.nTries < this.nConfident){
			return false;
		}
		
		return true;
	}


	@Override
	public List<TransitionProb> transitions(State s, Action a) {

		List<TransitionProb> transitions = new ArrayList<TransitionProb>();
		StateActionNode san = this.getStateActionNode(this.hashingFactory.hashState(s), a);
		if(san == null){
			//assume transition to self if we haven't modeled this at all
			TransitionProb tp = new TransitionProb(1., new EnvironmentOutcome(s, a, s, 0., false));
			transitions.add(tp);
		}
		else{
			double r = san.sumR / san.nTries;
			for(OutcomeState os : san.outcomes.values()){
				State sp = os.osh.s();
				double p = (double)os.nTimes / (double)san.nTries;
				EnvironmentOutcome eo = new EnvironmentOutcome(s, a, sp, r, this.terminalStates.contains(sp));
				TransitionProb tp = new TransitionProb(p, eo);
				transitions.add(tp);
			}
		}

		return transitions;
	}

	@Override
	public EnvironmentOutcome sample(State s, Action a) {
		return FullModel.Helper.sampleByEnumeration(this, s, a);
	}

	@Override
	public boolean terminal(State s) {
		return this.terminalStates.contains(this.hashingFactory.hashState(s));
	}

	@Override
	public void updateModel(EnvironmentOutcome eo) {
		
		HashableState sh = this.hashingFactory.hashState(eo.o);
		HashableState shp = this.hashingFactory.hashState(eo.op);
		
		if(eo.terminated){
			this.terminalStates.add(shp);
		}
		
		StateActionNode san = this.getOrCreateActionNode(sh, eo.a);
		san.update(eo.r, shp);

	}
	
	/**
	 * Returns the {@link TabularModel.StateActionNode} object associated with the given hashed state and action.
	 * If there is not an associated {@link TabularModel.StateActionNode} object, then null is returned.
	 * @param sh the hashed state
	 * @param a the action
	 * @return the associated {@link TabularModel.StateActionNode} or null if it does not exist.
	 */
	protected StateActionNode getStateActionNode(HashableState sh, Action a){

		StateNode sn = this.stateNodes.get(sh);
		if(sn == null){
			return null;
		}
		return sn.actionNode(a);
	}
	
	/**
	 * Returns the {@link TabularModel.StateActionNode} object associated with the given hashed state and action.
	 * If there is not an associated {@link TabularModel.StateActionNode} object, then one will be created.
	 * @param sh the hashed state
	 * @param ga the grounded action
	 * @return the associated {@link TabularModel.StateActionNode}
	 */
	protected StateActionNode getOrCreateActionNode(HashableState sh, Action ga){

		StateNode sn = this.stateNodes.get(sh);
		StateActionNode toReturn = null;
		if(sn == null){
			sn = new StateNode(sh);
			this.stateNodes.put(sh, sn);
			
			//List <GroundedAction> allActions = sh.s.getAllGroundedActionsFor(this.sourceDomain.getActions());
			List<Action> allActions = ActionUtils.allApplicableActionsForTypes(this.sourceDomain.getActionTypes(), sh.s());
			for(Action tga : allActions){
				StateActionNode san = sn.addActionNode(tga);
				if(tga.equals(ga)){
					toReturn = san;
				}
			}
			
		}
		else{
			toReturn = sn.actionNode(ga);
		}
		
		if(toReturn == null){
			throw new RuntimeException("Could not finding matching grounded action in model for action: " + ga.toString());
		}
		
		
		return toReturn;
	}
	
	
	@Override
	public void resetModel(){
		this.stateNodes.clear();
		this.terminalStates.clear();
	}
	
	/**
	 * A class for storing statistics about a state
	 * @author James MacGlashan
	 *
	 */
	class StateNode{
		
		/**
		 * The hashed state this node wraps
		 */
		HashableState sh;
		
		/**
		 * Maps from actions to state-aciton nodes that store statistics about the acitons taken from this state
		 */
		Map <Action, StateActionNode> actionNodes;
		
		
		/**
		 * Initializes
		 * @param sh the hashed state this node wraps.
		 */
		public StateNode(HashableState sh){
			this.sh = sh;
			this.actionNodes = new HashMap<Action, TabularModel.StateActionNode>();
		}
		
		
		/**
		 * Returns a {@link StateActionNode} object for the given grounded action
		 * @param ga the grounded action specifying the action node to return
		 * @return the {@link StateActionNode} object associated with the specified action
		 */
		public StateActionNode actionNode(Action ga){
			return actionNodes.get(ga);
		}
		
		/**
		 * Creates and adds a {@link StateActionNode} for the given grounded action
		 * @param ga the grounded action for which a {@link StateActionNode} should be created.
		 * @return the created {@link StateActionNode} object.
		 */
		public StateActionNode addActionNode(Action ga){
			StateActionNode san = new StateActionNode(ga);
			this.actionNodes.put(ga, san);
			return san;
		}
		
		
	}
	
	
	/**
	 * A class storing statistics information for a given state and action pair. Objects of this class should be associated with a {@link StateNode} object.
	 * @author James MacGlashan
	 *
	 */
	class StateActionNode{
		
		/**
		 * The relevant action
		 */
		Action ga;
		
		/**
		 * The number of times this action has been tried in the associated state
		 */
		int nTries;
		
		/**
		 * The sum reward received over all tries fo this action in the associated stte.
		 */
		double sumR;
		
		/**
		 * A map to the states to which this action from the associated state transition.
		 */
		Map<HashableState, OutcomeState> outcomes;
		
		
		/**
		 * Initializes.
		 * @param ga the action for which this object will record transition statistics
		 */
		public StateActionNode(Action ga){
			this.ga = ga;
			this.sumR = 0.;
			this.nTries = 0;
			
			this.outcomes = new HashMap<HashableState, TabularModel.OutcomeState>();
		}
		
		
		/**
		 * Initializes
		 * @param ga the action for which this object will record transition statistics
		 * @param r the reward received for one observaiton
		 * @param sprime the outcome recieved for one observation
		 */
		public StateActionNode(Action ga, double r, HashableState sprime){
			this.ga = ga;
			this.sumR = r;
			this.nTries = 1;
			
			this.outcomes = new HashMap<HashableState, TabularModel.OutcomeState>();
			this.outcomes.put(sprime, new OutcomeState(sprime));
		}
		
		/**
		 * Updates with a new transition observation
		 * @param r the reward received
		 * @param sprime the outcome state
		 */
		public void update(double r, HashableState sprime){
			this.nTries++;
			this.sumR += r;
			OutcomeState stored = this.outcomes.get(sprime);
			if(stored != null){
				stored.nTimes++;
			}
			else{
				this.outcomes.put(sprime, new OutcomeState(sprime));
			}
		}
		
	}
	
	
	/**
	 * A class for storing how many times an outcome state is observed
	 * @author James MacGlashan
	 *
	 */
	class OutcomeState{
		
		/**
		 * The hased outcome state observed
		 */
		HashableState osh;
		
		/**
		 * The number of times it has been observed
		 */
		int nTimes;
		
		
		/**
		 * Initializes for the given outcome state with an observation count of 1
		 * @param osh the observed hased outcome state
		 */
		public OutcomeState(HashableState osh){
			this.osh = osh;
			nTimes = 1;
		}
		
		@Override
		public int hashCode(){
			return osh.hashCode();
		}
		
		@Override
		public boolean equals(Object o){
			if(!(o instanceof OutcomeState)){
				return false;
			}
			
			OutcomeState oos = (OutcomeState)o;
			return this.osh.equals(oos.osh);
		}
		
		
	}


}
