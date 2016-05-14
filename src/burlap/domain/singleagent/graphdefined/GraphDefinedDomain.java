package burlap.domain.singleagent.graphdefined;

import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.ActionType;
import burlap.mdp.singleagent.RewardFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.shell.EnvironmentShell;

import java.util.*;


/**
 * A domain generator for generating domains that are represented as graphs. The graph transitions may be stochastic.
 * Edges are added/modified for state-action pairs with the {@link #setTransition(int, int, int, double)} method and
 * transitions can be cleared/removed with the methods {@link #clearStateTransitionsFrom(int)},
 * {@link #clearStateActionTransitions(int, int)}, and {@link #removeEdge(int, int, int)}.
 * <p>
 * A constructed graph's transition dynamics can be validated as proper (all edges from a state-action pair sum to 1)
 * using the method {@link #isValidMDPGraph()} and if they are false, a string reporting which state-actions do not
 * sum to 1 (and their various edges) can be returned with the {@link #invalidMDPReport()} method.
 * <p>
 * Modifying the transition dynamics of a graph will not affect the transition dynamics of previously generated
 * {@link burlap.mdp.core.Domain}, allowing you to reuse the same generator without affected previous domains.
 * @author James MacGlashan
 *
 */
public class GraphDefinedDomain implements DomainGenerator {

	
	/**
	 * the variable key for the single graph node values of a state
	 */
	public static final String VAR = "node";

	
	/**
	 * Constant for the base name of each action
	 */
	public static final String BASE_ACTION_NAME = "action";
	
	
	/**
	 * The number of state nodes in the graph
	 */
	protected int															numNodes;
	
	/**
	 * The maximum number of actions available from any given state node.
	 */
	protected int															maxActions;
	
	/**
	 * The state-action stochastic transition dynamics from each state node.
	 */
	protected Map<Integer, Map<Integer, Set<NodeTransitionProbability>>>	transitionDynamics;

	protected RewardFunction rf = new NullRewardFunction();
	protected TerminalFunction tf = new NullTermination();


	/**
	 * Initializes the generator. States and transition dynamics will be constructed lazily with calls
	 * to the {@link #setTransition(int, int, int, double)} method.
	 */
	public GraphDefinedDomain() {
		this.numNodes = 0;
		this.maxActions = 0;
		this.transitionDynamics = new HashMap<Integer, Map<Integer,Set<NodeTransitionProbability>>>();

	}


	/**
	 * Initializes the generator to create a domain with the given number of state nodes in it.
	 * @param numNodes the number of state nodes in the domain.
	 */
	public GraphDefinedDomain(int numNodes) {
		this.numNodes = numNodes;
		this.maxActions = 0;
		this.transitionDynamics = new HashMap<Integer, Map<Integer,Set<NodeTransitionProbability>>>(numNodes);
		
		for(int i = 0; i < this.numNodes; i++){
			this.transitionDynamics.put(i, new HashMap<Integer,Set<NodeTransitionProbability>>());
		}
		
	}

	/**
	 * Returns the number of state nodes specified in this domain
	 * @return the number of state nodes specified in this domain
	 */
	public int getNumNodes() {
		return numNodes;
	}

	public RewardFunction getRf() {
		return rf;
	}

	public void setRf(RewardFunction rf) {
		this.rf = rf;
	}

	public TerminalFunction getTf() {
		return tf;
	}

	public void setTf(TerminalFunction tf) {
		this.tf = tf;
	}

	/**
	 * Sets the probability <code>p</code> for transitioning to state node <code>tNode</code> after taking action number <code>action</code> in state node <code>srcNode</code>.
	 * Note that this method also defines from which nodes an action can be executed. If this method is never called for source node i and action j, then
	 * it will be assumed that action j cannot be executed in state node i. The the specified source node or target node
	 * is greater than this domain generator's previously specified number of nodes, then space will be added for them
	 * in the transition dynamics.
	 * @param srcNode the source node number from which an action will be taken
	 * @param action the action to be taken
	 * @param tNode the resulting state from taking the action in the given source node
	 * @param p the probability of this transition occurring for the given action and source node.
	 */
	public void setTransition(int srcNode, int action, int tNode, double p){
		
		if(action >= this.maxActions){
			this.maxActions = action+1;
		}

		if(srcNode >= this.numNodes){
			for(int i = this.numNodes; i <= srcNode; i++){
				this.transitionDynamics.put(i, new HashMap<Integer,Set<NodeTransitionProbability>>());
			}
			this.numNodes = srcNode+1;
		}

		if(tNode >= this.numNodes){
			for(int i = this.numNodes; i <= tNode; i++){
				this.transitionDynamics.put(i, new HashMap<Integer,Set<NodeTransitionProbability>>());
			}
			this.numNodes = tNode+1;
		}

		Map<Integer,Set<NodeTransitionProbability>> actionMap = this.transitionDynamics.get(srcNode);
		Set <NodeTransitionProbability> nts = actionMap.get(action);
		if(nts == null){
			nts = new HashSet<NodeTransitionProbability>();
			actionMap.put(action, nts);
		}
		NodeTransitionProbability ntp = this.getNodeTransitionTo(nts, tNode);
		if(ntp == null){
			ntp = new NodeTransitionProbability(tNode, p);
			nts.add(ntp);
		}
		else{
			ntp.probability = p;
		}
	}


	/**
	 * Checks whether the the probability of all state-action outcomes sum to 1 in this graph. Returns true
	 * if they do, false otherwise. If this method returns false, you can use the
	 *  method to get a string reporting where errors are.
	 * @return true if all probability distributions sum to 1; false otherwise
	 */
	public boolean isValidMDPGraph(){
		return isValidMDPGraph(this.transitionDynamics);
	}

	/**
	 * Checks whether the the probability of all state-action outcomes sum to 1 in he provided transition dynamics. Returns true
	 * if they do, false otherwise. If this method returns false, you can use the
	 *  method to get a string reporting where errors are.
	 * @param transitionDynamics the graph transition dynamics to check
	 * @return true if all probability distributions sum to 1; false otherwise
	 */
	public static boolean isValidMDPGraph(Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> transitionDynamics){
		for(Map.Entry<Integer, Map<Integer, Set<NodeTransitionProbability>>> e : transitionDynamics.entrySet()){
			Map<Integer, Set<NodeTransitionProbability>> at = e.getValue();
			for(Map.Entry<Integer, Set<NodeTransitionProbability>> e2 : at.entrySet()){
				if(!e2.getValue().isEmpty()) {
					double sum = 0.;
					for(NodeTransitionProbability ntp : e2.getValue()) {
						sum += ntp.probability;
					}
					if(Math.abs(1. - sum) > 1e-15){
						return false;
					}
				}

			}
		}


		return true;
	}


	/**
	 * Returns a string that lists the state-action paris that have improper transition dynamics (transitions that don't sum to 1).
	 * If there are no improper dynamics, then the returned string is empty. The output form is:
	 * <p>
	 * (s, a): sumProbability<p>
	 * &nbsp;&nbsp;&nbsp;&nbsp;(s, a)-&gt;s_1' p_1<p>
	 * &nbsp;&nbsp;&nbsp;&nbsp;(s, a)-&gt;s_2' p_2<p>
	 * ...
	 * <p>
	 * @return a string that lists the state-action paris that have improper transition dynamics or an empty string if all transitions are proper.
	 */
	public String invalidMDPReport(){
		return invalidMDPReport(this.transitionDynamics);
	}


	/**
	 * Returns a string that lists the state-action paris that have improper transition dynamics (transitions that don't sum to 1).
	 * If there are no improper dynamics, then the returned string is empty. The output form is:
	 * <p>
	 * (s, a): sumProbability<p>
	 * &nbsp;&nbsp;&nbsp;&nbsp;(s, a)-&gt;s_1' p_1<p>
	 * &nbsp;&nbsp;&nbsp;&nbsp;(s, a)-&gt;s_2' p_2<p>
	 * ...
	 * <p>
	 * @param transitionDynamics the transition dynamics of the MDP/Graph
	 * @return a string that lists the state-action paris that have improper transition dynamics or an empty string if all transitions are proper.
	 */
	public static String invalidMDPReport(Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> transitionDynamics){

		StringBuilder sb = new StringBuilder();

		for(Map.Entry<Integer, Map<Integer, Set<NodeTransitionProbability>>> e : transitionDynamics.entrySet()){
			Map<Integer, Set<NodeTransitionProbability>> at = e.getValue();
			for(Map.Entry<Integer, Set<NodeTransitionProbability>> e2 : at.entrySet()){
				if(!e2.getValue().isEmpty()) {

					double sum = 0.;
					StringBuilder sb2 = new StringBuilder();
					for(NodeTransitionProbability ntp : e2.getValue()) {
						sb2.append("    (").append(e.getKey()).append(", ").append(e2.getKey()).append(")->").append(ntp.transitionTo).append(" ").append(ntp.probability).append("\n");
						sum += ntp.probability;
					}

					if(Math.abs(1. - sum) > 1e-15){
						sb.append("    (").append(e.getKey()).append(", ").append(e2.getKey()).append("): ").append(sum).append("\n").append(sb2.toString()).append("\n");
					}
				}

			}
		}


		return sb.toString().trim();
	}

	/**
	 * Clears all transitions from a given state node
	 * @param srcNode the state node from which transitions will be cleared
	 */
	public void clearStateTransitionsFrom(int srcNode){
		Map<Integer,Set<NodeTransitionProbability>> actionMap = this.transitionDynamics.get(srcNode);
		if(actionMap != null){
			actionMap.clear();
		}
	}


	/**
	 * Clears all (stochastic) edges for a given state-action pair.
	 * @param srcNode The state node of the pair to clear
	 * @param action the action of the pair to clear
	 */
	public void clearStateActionTransitions(int srcNode, int action){
		Map<Integer,Set<NodeTransitionProbability>> actionMap = this.transitionDynamics.get(srcNode);
		if(actionMap == null){
			return;
		}

		Set<NodeTransitionProbability> nts = actionMap.get(action);
		if(nts != null){
			nts.clear();
		}
	}

	/**
	 * Removes a given edge from the transition dynamics.
	 * @param srcNode the source state node of the edge
	 * @param action the action in the state node of the edge
	 * @param tNode the target state node of the dge
	 */
	public void removeEdge(int srcNode, int action, int tNode){
		Map<Integer,Set<NodeTransitionProbability>> actionMap = this.transitionDynamics.get(srcNode);
		if(actionMap == null){
			return;
		}
		Set<NodeTransitionProbability> nts = actionMap.get(action);
		if(nts == null){
			return;
		}

		NodeTransitionProbability curEdge = this.getNodeTransitionTo(nts, tNode);
		if(curEdge != null){
			nts.remove(curEdge);
		}

	}

	/**
	 * Returns the {@link burlap.domain.singleagent.graphdefined.GraphDefinedDomain.NodeTransitionProbability} object
	 * in the provided set that corresponds to a transition to state tNode or null if it doesn't exist.
	 * @param nts the set of possible node transitions
	 * @param tNode the query transition node id
	 * @return a {@link burlap.domain.singleagent.graphdefined.GraphDefinedDomain.NodeTransitionProbability} or null if the transition to the state does not already exist.
	 */
	protected NodeTransitionProbability getNodeTransitionTo(Set <NodeTransitionProbability> nts, int tNode){
		for(NodeTransitionProbability ntp : nts){
			if(ntp.transitionTo == tNode){
				return ntp;
			}
		}
		return null;
	}


	/**
	 * Returns a deep copy of the transition dynamics
	 * @return a deep copy of the transition dynamics
	 */
	protected Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> copyTransitionDynamics(){

		Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> ct = new HashMap<Integer, Map<Integer, Set<NodeTransitionProbability>>>(this.transitionDynamics.size());

		for(Map.Entry<Integer, Map<Integer, Set<NodeTransitionProbability>>> e : this.transitionDynamics.entrySet()){

			Map<Integer, Set<NodeTransitionProbability>> at = e.getValue();
			Map<Integer, Set<NodeTransitionProbability>> cat = new HashMap<Integer, Set<NodeTransitionProbability>>(at.size());
			for(Map.Entry<Integer, Set<NodeTransitionProbability>> eat : at.entrySet()){
				Set<NodeTransitionProbability> cntp = new HashSet<NodeTransitionProbability>(eat.getValue().size());
				for(NodeTransitionProbability ntpe : eat.getValue()){
					cntp.add(ntpe.copy());
				}
				cat.put(eat.getKey(), cntp);
			}
			ct.put(e.getKey(), cat);

		}

		return ct;

	}
	

	@Override
	public SADomain generateDomain() {
		
		SADomain domain = new SADomain();

		Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> ctd = this.copyTransitionDynamics();

		GraphStateModel stateModel = new GraphStateModel(ctd);
		FactoredModel model = new FactoredModel(stateModel, rf, tf);

		domain.setModel(model);

		for(int i = 0; i < this.maxActions; i++){
			domain.addAction(new GraphActionType(i, ctd));
		}
		
		
		return domain;
	}
	


	/**
	 * A class for specifying transition probabilities to result node states.
	 * @author James MacGlashan
	 *
	 */
	public class NodeTransitionProbability {
		
		/**
		 * The resulting state
		 */
		public int 		transitionTo;
		
		/**
		 * The probability of transitioning to the resulting state
		 */
		public double probability;

		
		
		/**
		 * Initializes transition probability
		 * @param transitionTo the resulting state
		 * @param probability the probability of transitioning to the resulting state
		 */
		public NodeTransitionProbability(int transitionTo, double probability){
			this.transitionTo = transitionTo;
			this.probability = probability;
		}

		public NodeTransitionProbability copy(){
			return new NodeTransitionProbability(this.transitionTo, this.probability);
		}
		
		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			
			if(!(other instanceof NodeTransitionProbability)){
				return false;
			}
			
			NodeTransitionProbability that = (NodeTransitionProbability)other;
			return transitionTo == that.transitionTo;
		}
		
		@Override
		public int hashCode(){
			return transitionTo;
		}
		
		
	}
	
	public static class GraphStateModel implements FullStateModel{

		/**
		 * The transition dynamics to use
		 */
		protected Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> transitionDynamics;

		protected Random rand = RandomFactory.getMapped(0);

		public GraphStateModel(Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> transitionDynamics) {
			this.transitionDynamics = transitionDynamics;
		}

		public Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> getTransitionDynamics() {
			return transitionDynamics;
		}

		public void setTransitionDynamics(Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> transitionDynamics) {
			this.transitionDynamics = transitionDynamics;
		}

		@Override
		public List<StateTransitionProb> stateTransitions(State s, Action a) {

			int aId = ((GraphActionType.GraphAction)a).aId;

			List <StateTransitionProb> result = new ArrayList<StateTransitionProb>();

			int n = (Integer)s.get(VAR);

			Map<Integer, Set<NodeTransitionProbability>> actionMap = transitionDynamics.get(n);
			Set<NodeTransitionProbability> transitions = actionMap.get(aId);

			for(NodeTransitionProbability ntp : transitions){

				State ns = s.copy();
				((MutableState)ns).set(VAR, ntp.transitionTo);

				StateTransitionProb tp = new StateTransitionProb(ns, ntp.probability);
				result.add(tp);

			}


			return result;

		}

		@Override
		public State sampleStateTransition(State s, Action a) {

			s = s.copy();

			int aId = ((GraphActionType.GraphAction)a).aId;

			int n = (Integer)s.get(VAR);

			Map<Integer, Set<NodeTransitionProbability>> actionMap = transitionDynamics.get(n);
			Set<NodeTransitionProbability> transitions = actionMap.get(aId);

			double roll = rand.nextDouble();
			double sumP = 0.;
			int selection = 0;
			for(NodeTransitionProbability ntp : transitions){
				sumP += ntp.probability;
				if(roll < sumP){
					selection = ntp.transitionTo;
					break;
				}
			}

			((MutableState)s).set(VAR, selection);

			return s;
		}
	}
	
	/**
	 * An action class for defining actions that can be taken from state nodes. The action can only be taken
	 * in states for which transition dynamics for the given action are defined.
	 * @author James MacGlashan
	 *
	 */
	public static class GraphActionType implements ActionType{

		/**
		 * Random object for sampling the stochastic graph transitions
		 */
		protected Random rand;
		
		/**
		 * The action number of this action
		 */
		protected int aId;

		/**
		 * The transition dynamics to use
		 */
		protected Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> transitionDynamics;


		/**
		 * Initializes a graph action object for the given domain and for the action of the given number.
		 * The name of this action will be the constant BASEACTIONNAMEi where i is the action number specified.
		 * @param aId the action identifier number
		 */
		public GraphActionType(int aId, Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> transitionDynamics){
			this.aId = aId;
			rand = RandomFactory.getMapped(0);
			this.transitionDynamics = transitionDynamics;
		}


		@Override
		public String typeName() {
			return BASE_ACTION_NAME + aId;
		}

		@Override
		public Action associatedAction(String strRep) {
			return new GraphAction(aId, transitionDynamics);
		}

		@Override
		public List<Action> allApplicableActions(State s) {
			Action a = associatedAction("");
			if(a.applicableInState(s)){
				return Arrays.asList(a);
			}
			return new ArrayList<Action>();
		}



		public static class GraphAction implements Action{

			public int aId;

			public Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> transitionDynamics;

			public GraphAction() {
			}

			public GraphAction(int aId, Map<Integer, Map<Integer, Set<NodeTransitionProbability>>> transitionDynamics) {
				this.aId = aId;
				this.transitionDynamics = transitionDynamics;
			}

			@Override
			public String actionName() {
				return BASE_ACTION_NAME + aId;
			}

			@Override
			public Action copy() {
				return new GraphAction(aId, transitionDynamics);
			}

			@Override
			public boolean applicableInState(State s) {
				int n = (Integer)s.get("node");

				Map<Integer, Set<NodeTransitionProbability>> actionMap = transitionDynamics.get(n);
				Set<NodeTransitionProbability> transitions = actionMap.get(aId);
				if(transitions == null){
					return false;
				}
				if(transitions.isEmpty()){
					return false;
				}

				return true;
			}

			@Override
			public boolean equals(Object o) {
				if(this == o) return true;
				if(o == null || getClass() != o.getClass()) return false;

				GraphAction that = (GraphAction) o;

				return aId == that.aId;

			}

			@Override
			public int hashCode() {
				return aId;
			}

			@Override
			public String toString() {
				return actionName();
			}
		}
		

	}


	public static void main(String[] args) {

		GraphDefinedDomain gdd = new GraphDefinedDomain(3);
		gdd.setTransition(0, 0, 1, 1.);
		gdd.setTransition(0, 1, 2, 1.);

		gdd.setTransition(1, 0, 1, 1.);
		gdd.setTransition(1, 1, 0, 1.);

		gdd.setTransition(2, 0, 2, 1.);
		gdd.setTransition(2, 1, 0, 1.);

		SADomain domain = gdd.generateDomain();



		State s = new GraphStateNode(0);
		EnvironmentShell shell = new EnvironmentShell(domain, s);
		shell.start();
	}
	
	

}
