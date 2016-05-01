package burlap.behavior.singleagent.planning.stochastic.valueiteration;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import burlap.behavior.singleagent.planning.stochastic.ActionTransitions;
import burlap.behavior.singleagent.planning.stochastic.HashedTransitionProbability;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.HashableState;
import burlap.datastructures.HashIndexedHeap;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * An implementation of Prioritized Sweeping as DP planning algorithm as described by Li and Littman [1]. This class will perform Bellman updates
 * on states according to their position in a Priority queue. The priority of any state is updated with respect to the change in the Bellman error
 * of a state to which it transitions. This means that there is greater memory utilization in this algorithm than standard VI because the backwards transition dynamics must be stored.
 * The priority queue takes C*lg(N) time to manage at each step, where C is the number of backpointers per state,
 * but if large gains can be achieved by the ordeing of the states, then this cost may be worth it.
 * 
 * 
 * 1. Li, Lihong, Michael L. Littman, and L. Littman. Prioritized sweeping converges to the optimal value function. Tech. Rep. DCS-TR-631, 2008.
 * @author James MacGlashan
 *
 */
public class PrioritizedSweeping extends ValueIteration{

	/**
	 * The priority queue of states
	 */
	protected HashIndexedHeap<BPTRNode> priorityNodes;
	
	/**
	 * THe maximum number Bellman backups permitted
	 */
	protected int maxBackups;
	
	
	/**
	 * Initializes
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param maxDelta when the maximum change in the value function is smaller than this value, VI will terminate.
	 * @param maxBackups the maximum number of Bellman backups. If set to -1, then there is no hard limit.
	 */
	public PrioritizedSweeping(Domain domain, RewardFunction rf,
			TerminalFunction tf, double gamma, HashableStateFactory hashingFactory,
			double maxDelta, int maxBackups) {
		super(domain, rf, tf, gamma, hashingFactory, maxDelta, 0);
		this.priorityNodes = new HashIndexedHeap<PrioritizedSweeping.BPTRNode>(new BPTRNodeComparator());
		this.maxBackups = maxBackups;
	}



	@Override
	public void runVI(){
		
		if(!this.foundReachableStates){
			throw new RuntimeException("Cannot run VI until the reachable states have been found. Use the planFromState or performReachabilityFrom method at least once before calling runVI.");
		}
		
		DPrint.cl(this.debugCode, "Beginning Planning.");
		
		double lastDelta = Double.POSITIVE_INFINITY;
		int numBackups = 0;
		while(lastDelta > this.maxDelta && (numBackups < this.maxBackups || this.maxBackups == -1)){
			
			BPTRNode node = this.priorityNodes.poll();
			lastDelta = node.priority;
			
			double oldV = this.value(node.sh);
			double newV = this.performBellmanUpdateOn(node.sh);
			double delta = Math.abs(newV-oldV);
			
			//update this nodes priority
			node.priority = delta* node.maxSelfTransitionProb;
			this.priorityNodes.insert(node);
			
			//update priority of nodes that transition to it
			for(BPTR bptr : node.backPointers){
				bptr.backNode.priority = Math.max(bptr.backNode.priority, bptr.forwardMaxProbability*delta);
				this.priorityNodes.refreshPriority(bptr.backNode);
			}
			
			lastDelta = Math.max(lastDelta, delta);
			numBackups++;
			
		}
		
		DPrint.cl(this.debugCode, "Finished planning with " + numBackups + " Bellman backups");
		
	}
	
	@Override
	public boolean performReachabilityFrom(State si){
		
		HashableState sih = this.stateHash(si);
		//if this is not a new state and we are not required to perform a new reachability analysis, then this method does not need to do anything.
		if(mapToStateIndex.containsKey(sih) && this.foundReachableStates){
			return false; //no need for additional reachability testing
		}
		
		DPrint.cl(this.debugCode, "Starting reachability analysis");
		
		//add to the open list
		BPTRNode inode = this.getNodeFor(sih);
		LinkedList <BPTRNode> openList = new LinkedList<BPTRNode>();
		Set <BPTRNode> openedSet = new HashSet<BPTRNode>();
		openList.offer(inode);
		openedSet.add(inode);
		
		
		while(!openList.isEmpty()){
			BPTRNode node = openList.poll();
			
			
			//skip this if it's already been expanded
			if(mapToStateIndex.containsKey(node.sh)){
				continue;
			}
			
			mapToStateIndex.put(node.sh, node.sh);
			
			//do not need to expand from terminal states if set to prune
			if(this.tf.isTerminal(node.sh.s) && stopReachabilityFromTerminalStates){
				continue;
			}
			
			
			//get the transition dynamics for each action and queue up new states
			List <ActionTransitions> transitions = this.getActionsTransitions(node.sh);
			for(ActionTransitions at : transitions){
				for(HashedTransitionProbability tp : at.transitions){
					HashableState tsh = tp.sh;
					BPTRNode tnode = this.getNodeFor(tsh);
					tnode.addBackTransition(node);
					if(!openedSet.contains(tsh) && !transitionDynamics.containsKey(tsh)){
						openedSet.add(tnode);
						openList.offer(tnode);
					}
				}
				
			}
			
			
		}
		
		DPrint.cl(this.debugCode, "Finished reachability analysis; # states: " + mapToStateIndex.size());
		
		this.foundReachableStates = true;
		this.hasRunVI = false;
		
		return true;
		
	}
	
	
	/**
	 * Returns or creates, stores, and returns a priority back pointer node for the given hased state 
	 * @param sh the hashed state for which its node should be returned.
	 * @return a priority back pointer node for the given hased state 
	 */
	protected BPTRNode getNodeFor(HashableState sh){
		
		BPTRNode node = new BPTRNode(sh);
		BPTRNode stored = this.priorityNodes.containsInstance(node);
		if(stored != null){
			node = stored;
		}
		else{
			this.priorityNodes.insert(node);
		}
		
		return node;
	}
	
	
	
	/**
	 * A node for state thar contains a list of its back pointers, their max probability of transition to this state, and the priority of this nodes state.
	 * @author James MacGlashan
	 *
	 */
	protected class BPTRNode{
		
		public HashableState sh;
		public List<BPTR> backPointers;
		public double maxSelfTransitionProb = 0.;
		public double priority = Double.MAX_VALUE;
		
		/**
		 * Creates a back pointer for the given state with no back pointers and a priority of Double.MAX_VALUE (ensures one sweep of the state space to start)
		 * @param sh the hased state for which this node will correspond
		 */
		public BPTRNode(HashableState sh){
			this.sh = sh;
			this.backPointers = new LinkedList<PrioritizedSweeping.BPTR>();
		}
		
		
		/**
		 * Adds a backpointer transition
		 * @param bptr the node that can transition to this node
		 */
		public void addBackTransition(BPTRNode bptr){
			if(!bptr.sh.equals(this.sh)){
				//make sure we don't already have this node
				boolean hasNode = false;
				for(BPTR b : this.backPointers){
					if(b.backNode == bptr){
						hasNode = true;
						break;
					}
				}
				if(!hasNode){
					this.backPointers.add(new BPTR(bptr, this.sh));
				}
			}
			else if(this.maxSelfTransitionProb == 0.){
				BPTR tmp = new BPTR(bptr, this.sh);
				this.maxSelfTransitionProb = tmp.forwardMaxProbability;
			}
			
		}
		
		@Override
		public int hashCode(){
			return this.sh.hashCode();
		}
		
		@Override
		public boolean equals(Object other){
		    if (other == null || this.getClass() != other.getClass()) {
                return false;   
            }
			BPTRNode o = (BPTRNode)other;
			return this.sh.equals(o.sh);
		}
		
	}
	
	
	/**
	 * A back pointer and its max action probability of transition.
	 * @author James MacGlashan
	 *
	 */
	protected class BPTR{
		
		/**
		 * The back pointer node
		 */
		public BPTRNode backNode;
		
		/**
		 * The maximum probability tha the back node will transition to the implicitly defined forward state node.
		 */
		public double forwardMaxProbability;
		
		
		/**
		 * Stores back pointer information.
		 * @param backNode the backwards node
		 * @param forwardState the state to which the back node transitions
		 */
		public BPTR(BPTRNode backNode, HashableState forwardState){
			this.backNode = backNode;
			List<GroundedAction> actions = PrioritizedSweeping.this.getAllGroundedActions(backNode.sh.s);
			double maxProb = 0.;
			//find action with maximum transition probability
			for(GroundedAction ga : actions){
				//search for match
				List<TransitionProbability> tps = ga.getTransitions(backNode.sh.s);
				for(TransitionProbability tp : tps){
					HashableState tpsh = PrioritizedSweeping.this.hashingFactory.hashState(tp.s);
					if(tpsh.equals(forwardState)){
						maxProb = Math.max(maxProb, tp.p);
						break;
					}
				}
			}
			this.forwardMaxProbability = maxProb;
		}
		
	}
	
	
	/**
	 * Comparator for the the priority of BPTRNodes
	 * @author James MacGlashan
	 *
	 */
	protected static class BPTRNodeComparator implements Comparator<BPTRNode>{

		@Override
		public int compare(BPTRNode o1, BPTRNode o2) {
			return Double.compare(o1.priority, o2.priority);
		}
		
	}
	
	
}
