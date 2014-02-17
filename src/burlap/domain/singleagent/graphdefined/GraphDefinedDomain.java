package burlap.domain.singleagent.graphdefined;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;


/**
 * A domain generator for generating domains that are represented as graphs. The graph transitions may be stochastic.
 * Input to the generator requires the number of graph nodes to be specified and the transition dynamics must be specified for
 * each node in the graph.
 * @author James MacGlashan
 *
 */
public class GraphDefinedDomain implements DomainGenerator {

	
	/**
	 * Constant for the name of the graph node attribute
	 */
	public static final String												ATTNODE = "node";
	
	/**
	 * Constant for the name of the agent class
	 */
	public static final String												CLASSAGENT = "agent";
	
	/**
	 * Constant for the base name of each action
	 */
	public static final String												BASEACTIONNAME = "action";
	
	
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
	protected Map<Integer, Map<Integer, Set<NodeTransitionProbibility>>>	transitionDynamics;
	

	/**
	 * Initializes the generator to create a domain with the given number of state nodes in it.
	 * @param numNodes the number of state nodes in the domain.
	 */
	public GraphDefinedDomain(int numNodes) {
		this.numNodes = numNodes;
		this.maxActions = 0;
		this.transitionDynamics = new HashMap<Integer, Map<Integer,Set<NodeTransitionProbibility>>>();
		
		for(int i = 0; i < this.numNodes; i++){
			this.transitionDynamics.put(i, new HashMap<Integer,Set<NodeTransitionProbibility>>());
		}
		
	}
	
	
	/**
	 * Sets the probability <code>p</code> for transitioning to state node <code>tNode</code> after taking action number <code>action</code> in state node <code>srcNode</code>.
	 * Note that this method also defines from which nodes an action can be executed. If this method is never called for source node i and action j, then
	 * it will be assumed that action j cannot be executed in state node i.
	 * @param srcNode the source node number from which an action will be taken
	 * @param action the action to be taken
	 * @param tNode the resulting state from taking the action in the given source node
	 * @param p the probability of this transition occurring for the given action and source node.
	 */
	public void setTransition(int srcNode, int action, int tNode, double p){
		
		if(action >= this.maxActions){
			this.maxActions = action+1;
		}
		
		NodeTransitionProbibility ntp = new NodeTransitionProbibility(tNode, p);
		Map<Integer,Set<NodeTransitionProbibility>> actionMap = this.transitionDynamics.get(srcNode);
		Set <NodeTransitionProbibility> nts = actionMap.get(action);
		if(nts == null){
			nts = new HashSet<GraphDefinedDomain.NodeTransitionProbibility>();
			actionMap.put(action, nts);
		}
		nts.add(ntp);

	}
	

	@Override
	public Domain generateDomain() {
		
		Domain domain = new SADomain();
		
		Attribute na = new Attribute(domain, ATTNODE, Attribute.AttributeType.DISC);
		na.setDiscValuesForRange(0, this.numNodes-1, 1);
		
		ObjectClass aclass = new ObjectClass(domain, CLASSAGENT);
		aclass.addAttribute(na);
		
		for(int i = 0; i < this.maxActions; i++){
			new GraphAction(domain, i);
		}
		
		
		return domain;
	}
	
	
	
	/**
	 * Returns a new state in which the agent is in the specified source node number.
	 * @param d the domain object for the graph domain
	 * @param sNode the state node number in which the agent will be.
	 * @return a new state object where the agent is in the specified state node number.
	 */
	public static State getState(Domain d, int sNode){
		State s = new State();
		
		ObjectInstance o = new ObjectInstance(d.getObjectClass(CLASSAGENT), CLASSAGENT);
		o.setValue(ATTNODE, sNode);
		
		s.addObject(o);
		
		return s;
	}
	
	
	/**
	 * Returns the state node number where the agent of the provided state is
	 * @param s the state object to query
	 * @return the state node number where the agent of the provided state is
	 */
	public static int getNodeId(State s){
		return s.getFirstObjectOfClass(CLASSAGENT).getDiscValForAttribute(ATTNODE);
	}
	
	
	
	/**
	 * A class for specifying transition probabilities to result node states.
	 * @author James MacGlashan
	 *
	 */
	public class NodeTransitionProbibility{
		
		/**
		 * The resulting state
		 */
		public int 		transitionTo;
		
		/**
		 * The probability of transitioning to the resulting state
		 */
		public double 	probabiltiy;
		
		
		/**
		 * Initializes transition probability
		 * @param transitionTo the resulting state
		 * @param probability the probability of transitioning to the resulting state
		 */
		public NodeTransitionProbibility(int transitionTo, double probability){
			this.transitionTo = transitionTo;
			this.probabiltiy = probability;
		}
		
		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			
			if(!(other instanceof NodeTransitionProbibility)){
				return false;
			}
			
			NodeTransitionProbibility that = (NodeTransitionProbibility)other;
			return transitionTo == that.transitionTo;
		}
		
		@Override
		public int hashCode(){
			return transitionTo;
		}
		
		
	}
	
	
	
	/**
	 * An action class for defining actions that can be taken from state nodes. The action can only be taken
	 * in states for which transition dynamics for the given action are defined.
	 * @author James MacGlashan
	 *
	 */
	class GraphAction extends Action{

		/**
		 * Random object for sampling the stochastic graph transitions
		 */
		protected Random rand;
		
		/**
		 * The action number of this action
		 */
		protected int aId;
		
		
		/**
		 * Initializes a graph action object for the given domain and for the action of the given number.
		 * The name of this action will be the constant BASEACTIONNAMEi where i is the action number specified.
		 * @param domain the domain of the action
		 * @param aId the action identifier number
		 */
		public GraphAction(Domain domain, int aId){
			super(BASEACTIONNAME+aId, domain, "");
			this.aId = aId;
			rand = RandomFactory.getMapped(0);
		}
		
		
		@Override
		public boolean applicableInState(State st, String [] params){
			
			ObjectInstance o = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int n = o.getDiscValForAttribute(ATTNODE);
			
			Map<Integer, Set<NodeTransitionProbibility>> actionMap = transitionDynamics.get(n);
			Set<NodeTransitionProbibility> transitions = actionMap.get(aId);
			if(transitions == null){
				return false;
			}
			
			return true;
		}
		
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			
			ObjectInstance o = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int n = o.getDiscValForAttribute(ATTNODE);
			
			Map<Integer, Set<NodeTransitionProbibility>> actionMap = transitionDynamics.get(n);
			Set<NodeTransitionProbibility> transitions = actionMap.get(aId);
			
			double roll = rand.nextDouble();
			double sumP = 0.;
			int selection = 0;
			for(NodeTransitionProbibility ntp : transitions){
				sumP += ntp.probabiltiy;
				if(roll < sumP){
					selection = ntp.transitionTo;
					break;
				}
			}
			
			o.setValue(ATTNODE, selection);
			
			return st;
		}
		
		
		@Override
		public List<TransitionProbability> getTransitions(State st, String [] params){
			
			List <TransitionProbability> result = new ArrayList<TransitionProbability>();
			
			ObjectInstance o = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int n = o.getDiscValForAttribute(ATTNODE);
			
			Map<Integer, Set<NodeTransitionProbibility>> actionMap = transitionDynamics.get(n);
			Set<NodeTransitionProbibility> transitions = actionMap.get(aId);
			
			for(NodeTransitionProbibility ntp : transitions){
				
				State ns = st.copy();
				ObjectInstance no = ns.getObjectsOfTrueClass(CLASSAGENT).get(0);
				no.setValue(ATTNODE, ntp.transitionTo);
				
				TransitionProbability tp = new TransitionProbability(ns, ntp.probabiltiy);
				result.add(tp);
				
			}
			
			
			return result;
			
		}
		
		
		
		
	}
	
	

}
