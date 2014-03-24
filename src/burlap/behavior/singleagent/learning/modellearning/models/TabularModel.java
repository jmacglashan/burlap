package burlap.behavior.singleagent.learning.modellearning.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * A tabular model using frequencies to model the transition dynamics.
 * 
 * 
 * @author James MacGlashan; adapted from code provided by Takehiro Oyakawa and Chan Trau
 *
 */
public class TabularModel extends Model {

	/**
	 * The source actual domain object for which actions will be modeled.
	 */
	protected Domain							sourceDomain;
	
	/**
	 * The hashing factory to use for indexing states
	 */
	protected StateHashFactory					hashingFactory;
	
	/**
	 * A mapping from (hashed) states to state nodes that store transition statistics
	 */
	protected Map<StateHashTuple, StateNode> 	stateNodes;
	
	/**
	 * The set of states marked as terminal states.
	 */
	protected Set<StateHashTuple> 				terminalStates;
	
	/**
	 * The number of transitions necessary to be confident in a model's prediction.
	 */
	protected int								nConfident;
	
	/**
	 * The modeled terminal funciton.
	 */
	protected TerminalFunction					modeledTF;
	
	/**
	 * The modeled reward function.
	 */
	protected RewardFunction					modeledRF;
	
	/**
	 * Initializes.
	 * @param sourceDomain the source domain whose actions will be modeled.
	 * @param hashingFactory the hashing factory to index states
	 * @param nConfident the number of observed transitions to be confidnent in the model's prediction.
	 */
	public TabularModel(Domain sourceDomain, StateHashFactory hashingFactory, int nConfident){
		this.sourceDomain = sourceDomain;
		this.hashingFactory = hashingFactory;
		this.stateNodes = new HashMap<StateHashTuple, TabularModel.StateNode>();
		this.terminalStates = new HashSet<StateHashTuple>();
		this.nConfident = nConfident;
		
		this.modeledTF = new TerminalFunction() {
			
			@Override
			public boolean isTerminal(State s) {
				return terminalStates.contains(TabularModel.this.hashingFactory.hashState(s));
			}
		};
		
		
		this.modeledRF = new RewardFunction() {
			
			@Override
			public double reward(State s, GroundedAction a, State sprime) {
				StateActionNode san = TabularModel.this.getStateActionNode(TabularModel.this.hashingFactory.hashState(s), a);
				if(san == null){
					return 0;
				}
				if(san.nTries == 0){
					return 0.;
				}
				return san.sumR / (double)san.nTries;
			}
		};
	}
	
	@Override
	public RewardFunction getModelRF() {
		return this.modeledRF;
	}

	@Override
	public TerminalFunction getModelTF() {
		return this.modeledTF;
	}

	@Override
	public boolean transitionIsModeled(State s, GroundedAction ga) {
		
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
	public State sampleModelHelper(State s, GroundedAction ga) {
		return this.sampleTransitionFromTransitionProbabilities(s, ga);
	}

	@Override
	public List<TransitionProbability> getTransitionProbabilities(State s, GroundedAction ga) {
		
		List<TransitionProbability> transitions = new ArrayList<TransitionProbability>();
		
		StateActionNode san = this.getStateActionNode(this.hashingFactory.hashState(s), ga);
		if(san == null){
			//assume transition to self if we haven't modeled this at all
			TransitionProbability tp = new TransitionProbability(s, 1.);
			transitions.add(tp);
		}
		else{
			for(OutcomeState os : san.outcomes.values()){
				State sp = os.osh.s;
				double p = (double)os.nTimes / (double)san.nTries;
				TransitionProbability tp = new TransitionProbability(sp, p);
				transitions.add(tp);
			}
		}
		
		return transitions;
	}

	@Override
	public void updateModel(State s, GroundedAction ga, State sprime, double r, boolean sprimeIsTerminal) {
		
		StateHashTuple sh = this.hashingFactory.hashState(s);
		StateHashTuple shp = this.hashingFactory.hashState(sprime);
		
		if(sprimeIsTerminal){
			this.terminalStates.add(shp);
		}
		
		StateActionNode san = this.getOrCreateActionNode(sh, ga);
		san.update(r, shp);

	}
	
	/**
	 * Returns the {@link StateActionNode} object associated with the given hashed state and action.
	 * If there is not an associated {@link StateActionNode} object, then null is returned.
	 * @param sh the hashed state
	 * @param ga the grounded action
	 * @return the associated {@link StateActionNode} or null if it does not exist.
	 */
	protected StateActionNode getStateActionNode(StateHashTuple sh, GroundedAction ga){

		StateNode sn = this.stateNodes.get(sh);
		if(sn == null){
			return null;
		}
		return sn.actionNode((GroundedAction)ga.translateParameters(sh.s, sn.sh.s));
	}
	
	/**
	 * Returns the {@link StateActionNode} object associated with the given hashed state and action.
	 * If there is not an associated {@link StateActionNode} object, then one will be created.
	 * @param sh the hashed state
	 * @param ga the grounded action
	 * @return the associated {@link StateActionNode}
	 */
	protected StateActionNode getOrCreateActionNode(StateHashTuple sh, GroundedAction ga){

		StateNode sn = this.stateNodes.get(sh);
		StateActionNode toReturn = null;
		if(sn == null){
			sn = new StateNode(sh);
			this.stateNodes.put(sh, sn);
			
			List <GroundedAction> allActions = sh.s.getAllGroundedActionsFor(this.sourceDomain.getActions());
			for(GroundedAction tga : allActions){
				StateActionNode san = sn.addActionNode(tga);
				if(tga.equals(ga)){
					toReturn = san;
				}
			}
			
		}
		else{
			toReturn = sn.actionNode((GroundedAction)ga.translateParameters(sh.s, sn.sh.s));
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
		StateHashTuple sh;
		
		/**
		 * Maps from actions to state-aciton nodes that store statistics about the acitons taken from this state
		 */
		Map <GroundedAction, StateActionNode> actionNodes;
		
		
		/**
		 * Initializes
		 * @param sh the hashed state this node wraps.
		 */
		public StateNode(StateHashTuple sh){
			this.sh = sh;
			this.actionNodes = new HashMap<GroundedAction, TabularModel.StateActionNode>();
		}
		
		
		/**
		 * Returns a {@link StateActionNode} object for the given grounded action
		 * @param ga the grounded action specifying the action node to return
		 * @return the {@link StateActionNode} object associated with the specified action
		 */
		public StateActionNode actionNode(GroundedAction ga){
			return actionNodes.get(ga);
		}
		
		/**
		 * Creates and adds a {@link StateActionNode} for the given grounded action
		 * @param ga the grounded action for which a {@link StateActionNode} should be created.
		 * @return the created {@link StateActionNode} object.
		 */
		public StateActionNode addActionNode(GroundedAction ga){
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
		GroundedAction ga;
		
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
		Map<StateHashTuple, OutcomeState> outcomes;
		
		
		/**
		 * Initializes.
		 * @param ga the action for which this object will record transition statistics
		 */
		public StateActionNode(GroundedAction ga){
			this.ga = ga;
			this.sumR = 0.;
			this.nTries = 0;
			
			this.outcomes = new HashMap<StateHashTuple, TabularModel.OutcomeState>();
		}
		
		
		/**
		 * Initializes
		 * @param ga the action for which this object will record transition statistics
		 * @param r the reward received for one observaiton
		 * @param sprime the outcome recieved for one observation
		 */
		public StateActionNode(GroundedAction ga, double r, StateHashTuple sprime){
			this.ga = ga;
			this.sumR = r;
			this.nTries = 1;
			
			this.outcomes = new HashMap<StateHashTuple, TabularModel.OutcomeState>();
			this.outcomes.put(sprime, new OutcomeState(sprime));
		}
		
		/**
		 * Updates with a new transition observation
		 * @param r the reward received
		 * @param sprime the outcome state
		 */
		public void update(double r, StateHashTuple sprime){
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
		StateHashTuple osh;
		
		/**
		 * The number of times it has been observed
		 */
		int nTimes;
		
		
		/**
		 * Initializes for the given outcome state with an observation count of 1
		 * @param osh the observed hased outcome state
		 */
		public OutcomeState(StateHashTuple osh){
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
