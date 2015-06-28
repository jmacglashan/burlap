package burlap.behavior.singleagent.planning.stochastic.montecarlo.uct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QFunction;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCTActionNode.UCTActionConstructor;
import burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCTStateNode.UCTStateConstructor;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * An implementation of UCT [1]. This class can be augmented with a goal state specification (using a {@link burlap.behavior.singleagent.planning.StateConditionTest})
 * that will cause the planning algorithm to terminate early once it has found a path to the goal. This may be useful if randomly finding the goal state is rare.
 * <br/><br/>
 * The class also implements the {@link burlap.behavior.singleagent.planning.QFunction} interface. However, it will only return the Q-value
 * for a state if that state is the root node of the tree. If it is not the root node of the tree, then it will automatically reset the planning results
 * and replan from that state as the root node and then return the result. This allows the client to use a {@link burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy}
 * with this planner in which it replans with each step in the world, thereby forcing the Q-values for every state to be for the same horizon.
 * Replanning fresh after each step in the world is the standard UCT approach. If you instead want a policy that walks
 * through the tree it generated from some source state,
 * (so that each step computes a Q-value for a shorter horizon than the step before), you can use the
 * {@link burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCTTreeWalkPolicy}. The TreeWalkPolicy
 * will be more computationally efficient than replanning at each step, but may have degrading performance after each step since
 * each step has a shorter horizon from which to plan and may not have as many samples from which it estimated its Q-value.
 * <br/>
 * <br/>
 * 1. Kocsis, Levente, and Csaba Szepesvari. "Bandit based monte-carlo planning." ECML (2006). 282-293.
 * 
 * @author James MacGlashan
 *
 */
public class UCT extends OOMDPPlanner implements QFunction {

	protected List<Map<StateHashTuple, UCTStateNode>> 			stateDepthIndex;
	protected Map <StateHashTuple, List <UCTStateNode>>			statesToStateNodes;
	protected UCTStateNode										root;
	protected int												maxHorizon;
	protected int												maxRollOutsFromRoot;
	protected int												numRollOutsFromRoot;
	protected double											explorationBias;
	
	protected UCTStateConstructor								stateNodeConstructor;
	protected UCTActionConstructor								actionNodeConstructor;
	
	protected StateConditionTest								goalCondition;
	protected boolean											foundGoal;
	protected boolean											foundGoalOnRollout;
	
	protected Set<StateHashTuple>								uniqueStatesInTree;
	
	protected int												treeSize;
	protected int												numVisits;
	
	protected Random											rand;
	
	
	
	/**
	 * Initializes UCT
	 * @param domain the domain in which to plan
	 * @param rf the reward function to use
	 * @param tf the terminal function to use
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory
	 * @param horizon the planning horizon
	 * @param nRollouts the number of rollouts to perform 
	 * @param explorationBias the exploration bias constant (suggested >2)
	 */
	public UCT(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, int horizon, int nRollouts, int explorationBias){
		
		stateNodeConstructor = new UCTStateConstructor();
		actionNodeConstructor = new UCTActionConstructor();
		
		
		this.UCTInit(domain, rf, tf, gamma, hashingFactory, horizon, nRollouts, explorationBias);
		
		
	}
	
	protected void UCTInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, int horizon, int nRollouts, int explorationBias){
		
		this.plannerInit(domain, rf, tf, gamma, hashingFactory);
		this.maxHorizon = horizon;
		this.maxRollOutsFromRoot = nRollouts;
		this.explorationBias = explorationBias;
		
		goalCondition = null;
		
		rand = RandomFactory.getMapped(589449);
		
	}
	
	
	/**
	 * Returns the root node of the UCT tree.
	 * @return the root node of the UCT tree.
	 */
	public UCTStateNode getRoot(){
		return root;
	}
	
	
	/**
	 * Tells the planner to stop planning if a goal state is ever found.
	 * @param gc a {@link burlap.behavior.singleagent.planning.StateConditionTest} object used to specify goal states (whereever it evaluates as true).
	 */
	public void useGoalConditionStopCriteria(StateConditionTest gc){
		this.goalCondition = gc;
	}
	
	
	@Override
	public void planFromState(State initialState) {
		
		foundGoal = false;
		
		treeSize = 1;
		numVisits = 0;
		
		StateHashTuple shi = this.stateHash(initialState);
		root = stateNodeConstructor.generate(shi, 0, actions, actionNodeConstructor);
		
		uniqueStatesInTree = new HashSet<StateHashTuple>();
		
		stateDepthIndex = new ArrayList<Map<StateHashTuple,UCTStateNode>>();
		statesToStateNodes = new HashMap<StateHashTuple, List<UCTStateNode>>();
		Map <StateHashTuple, UCTStateNode> depth0Map = new HashMap<StateHashTuple, UCTStateNode>();
		depth0Map.put(shi, root);
		stateDepthIndex.add(depth0Map);
		
		
		int lastNumUnique = 0;
		
		numRollOutsFromRoot = 0;
		while(!this.stopPlanning()){
			
			this.initializeRollOut();
			
			this.treeRollOut(root, 0, maxHorizon);
			
			numRollOutsFromRoot++;
			
			int nu = uniqueStatesInTree.size();
			if(nu - lastNumUnique > 0){
				DPrint.cl(debugCode, "" + numRollOutsFromRoot + "; unique states: " + nu  + "; tree size: " + treeSize + "; total visits: " + numVisits);
				lastNumUnique = nu;
			}
			
			
			
			//System.out.println("\nRollouts: " + numRollOutsFromRoot + "; Best Action Expected Return: " + this.bestReturnAction(root).averageReturn());
		}
		DPrint.cl(debugCode, "\nRollouts: " + numRollOutsFromRoot + "; Best Action Expected Return: " + this.bestReturnAction(root).averageReturn());

	}

	@Override
	public List<QValue> getQs(State s) {

		//if we haven't done any planning, then do so now
		if(this.root == null){
			this.planFromState(s);
		}

		//if the root node isn't the query state, then replan
		StateHashTuple sh = this.hashingFactory.hashState(s);
		if(!sh.equals(this.root.state)){
			this.resetPlannerResults();
			this.planFromState(s);
		}

		//compute the Q-values
		List <QValue> qs = new ArrayList<QValue>(this.root.actionNodes.size());
		for(UCTActionNode act : this.root.actionNodes){
			qs.add(new QValue(s, act.action, act.averageReturn()));
		}

		return qs;
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {

		//if we haven't done any planning, then do so now
		if(this.root == null){
			this.planFromState(s);
		}

		//if the root node isn't the query state, then replan
		StateHashTuple sh = this.hashingFactory.hashState(s);
		if(!sh.equals(this.root.state)){
			this.resetPlannerResults();
			this.planFromState(s);
		}

		GroundedAction ga = (GroundedAction)a;
		for(UCTActionNode act : this.root.actionNodes){
			if(act.action.equals(ga)){
				return new QValue(s, ga, act.averageReturn());
			}
		}

		throw new RuntimeException("UCT does not know about action: " + a.toString() + "; cannot return Q-value for it");
	}

	@Override
	public void resetPlannerResults(){
		this.mapToStateIndex.clear();
		this.stateDepthIndex.clear();
		this.statesToStateNodes.clear();
		this.root = null;
		this.numRollOutsFromRoot = 0;
	}
	
	/*
	 * Initializes data members; should be called before {@link treeRollOut(UCTStateNode, int, int)}
	 */
	protected void initializeRollOut(){
		foundGoalOnRollout = false;
	}
	
	
	/**
	 * Performs a rollout in the UCT tree from the given node, keeping track of how many new nodes can be added to the tree.
	 * @param node the node from which to rollout
	 * @param depth the depth of the node
	 * @param childrenLeftToAdd the number of new subsequent nodes that can be connected to the tree
	 * @return the sample return from rolling out from this node
	 */
	public double treeRollOut(UCTStateNode node, int depth, int childrenLeftToAdd){
		
		numVisits++;
		
		if(depth == maxHorizon){
			return 0.;
		}
		
		if(tf.isTerminal(node.state.s)){
			if(goalCondition != null){
				if(goalCondition.satisfies(node.state.s)){
					foundGoal = true;
					foundGoalOnRollout = true;
				}
			}
			DPrint.cl(debugCode, numRollOutsFromRoot + " Hit terminal at depth: " + depth);
			return 0.;
		}
		
		
		
		UCTActionNode anode = this.selectActionNode(node);
		
		if(anode == null){
			//no actions can be performed in this state
			return 0.;
			//return ((maxHorizon - depth))*-1.;
		}
		
		
		
		//sample the action
		StateHashTuple shprime = this.stateHash(anode.action.executeIn(node.state.s));
		double r = rf.reward(node.state.s, anode.action, shprime.s);
		int depthChange = 1;
		if(!anode.action.action.isPrimitive()){
			Option o = (Option)anode.action.action;
			depthChange = o.getLastNumSteps();
		}
		
		UCTStateNode snprime = this.queryTreeIndex(shprime, depth+depthChange);
		
		double sampledReturn = 0.;
		
		boolean shouldConnectNode = false;
		double futureReturn = 0.;
		if(snprime != null){
			
			//then this state already exists in the tree
			
			if(!anode.referencesSuccessor(snprime)){ 
				//then this successor has not been generated by this state-action pair before and should be indexed
				anode.addSuccessor(snprime);
			}
			
			futureReturn = this.treeRollOut(snprime, depth + depthChange, childrenLeftToAdd);
			sampledReturn = r + Math.pow(gamma, depthChange) * futureReturn;
			
		}
		else{
			
			//this state is not in the tree at this depth so create it
			snprime = stateNodeConstructor.generate(shprime, depth+1, actions, actionNodeConstructor);
			
			//store it in the tree depending on how many new nodes have already been stored in this roll out
			if(childrenLeftToAdd > 0){
				shouldConnectNode = true;
			}
			
			//and do an exploratory sample from it
			futureReturn = this.treeRollOut(snprime, depth + depthChange, childrenLeftToAdd-1);
			sampledReturn = r + gamma * futureReturn;
			
			
		}
		
		node.n++;
		anode.update(sampledReturn);
		
		if(shouldConnectNode || foundGoalOnRollout){
			this.addNodeToIndexTree(snprime);
			anode.addSuccessor(snprime);
			uniqueStatesInTree.add(snprime.state);
		}
		
		
		return sampledReturn;
	}
	
	
	
	/**
	 * Returns true if rollouts and planning should cease. Planning will stop
	 * if the planner is told to terminate upon finding a goal and one was found, or if
	 * the maximum number of rollouts have already been performed.
	 * @return true if rollouts and planning should cease; false otherwise.
	 */
	public boolean stopPlanning(){
		if(foundGoal){
			return true;
		}
		if(maxRollOutsFromRoot == -1){
			return false;
		}
		if(numRollOutsFromRoot < maxRollOutsFromRoot){
			return false;
		}
		return true;
	}
	
	

	
	
	
	/**
	 * Selections which action to take. Unexplored actions from the node are selected first.
	 * If all actions have been explored, then the action with the highest upper confidence Q-value
	 * is selected, ties are broken randomly.
	 * @param snode the UCT node from which to select an action.
	 * @return the {@link UCTActionNode} to be taken.
	 */
	protected UCTActionNode selectActionNode(UCTStateNode snode){
		
		List <UCTActionNode> candidates = new ArrayList<UCTActionNode>();
		
		boolean untriedNodes = false;
		double maxUCTQ = Double.NEGATIVE_INFINITY;
		
		for(UCTActionNode an : snode.actionNodes){
			
			if(!untriedNodes){
				if(an.n == 0){
					untriedNodes = true;
					candidates.clear();
					candidates.add(an);
				}
				else{
					double UCTQ = this.computeUCTQ(snode, an);
					if(UCTQ > maxUCTQ){
						candidates.clear();
						candidates.add(an);
						maxUCTQ = UCTQ;
					}
					else if(UCTQ == maxUCTQ){
						candidates.add(an);
					}
				}
			}
			else if(an.n == 0){
				candidates.add(an);
			}
			
		}
		
		
		//only one thing to do
		if(candidates.size() == 1){
			return candidates.get(0);
		}
		
		//if there are untried actions, try the most interesting first
		if(untriedNodes){
			List <UCTActionNode> candidates2 = new ArrayList<UCTActionNode>(candidates.size());
			for(UCTActionNode anode : candidates){
				StateHashTuple sample = this.stateHash(anode.action.executeIn(snode.state.s));
				if(!uniqueStatesInTree.contains(sample)){
					candidates2.add(anode);
				}
			}
			if(candidates2.size() > 0){
				candidates = candidates2;
			}
		}
		
		return candidates.get(rand.nextInt(candidates.size()));
		
	}
	
	
	
	/**
	 * Returns the upper confidence Q-value for a given state node and action node.
	 * @param snode the state node
	 * @param anode the action node
	 * @return the upper confidence Q-value
	 */
	protected double computeUCTQ(UCTStateNode snode, UCTActionNode anode){
		return anode.averageReturn() + this.explorationQBoost(snode.n, anode.n);
	}
	
	
	/**
	 * Returns the extra value added to the average sample Q-value that is sued to produce the upper confidence Q-value.
	 * @param ns the number of times the state node has been visited
	 * @param na the number of times the action node has been visited
	 * @return the extra value added to the average sample Q-value that is sued to produce the upper confidence Q-value.
	 */
	protected double explorationQBoost(int ns, int na){
		return explorationBias * Math.sqrt(Math.log(ns) / (double)na);
	}
	
	
	/**
	 * Returns the {@link UCTStateNode} for the given (hashed) state at the given depth.
	 * @param sh the state whose node should be returned
	 * @param d the depth of the state
	 * @return the corresponding {@link UCTStateNode}
	 */
	protected UCTStateNode queryTreeIndex(StateHashTuple sh, int d){
		
		if(d >= stateDepthIndex.size()){
			return null;
		}
		
		return stateDepthIndex.get(d).get(sh);
		
	}
	
	/**
	 * Adds a {@link UCTStateNode} to the UCT tree
	 * @param snode the {@link UCTStateNode} to add
	 */
	protected void addNodeToIndexTree(UCTStateNode snode){
		
		while(stateDepthIndex.size() <= snode.depth){
			stateDepthIndex.add(new HashMap<StateHashTuple, UCTStateNode>());
		}
		
		stateDepthIndex.get(snode.depth).put(snode.state, snode);
		
		List <UCTStateNode> depthNodes = statesToStateNodes.get(snode.state);
		if(depthNodes == null){
			depthNodes = new ArrayList<UCTStateNode>();
			statesToStateNodes.put(snode.state, depthNodes);
		}
		
		depthNodes.add(snode);
		
		treeSize++;
		
	}
	
	
	/**
	 * Returns the {@link UCTActionNode} with the highest average sample Q-value. Ties are broken by returning the first {@link UCTActionNode} with the highest value. 
	 * @param snode the {@link UCTStateNode} to query
	 * @return the {@link UCTActionNode} with the highest average sample Q-value
	 */
	protected UCTActionNode bestReturnAction(UCTStateNode snode){
		
		double maxQ = Double.NEGATIVE_INFINITY;
		UCTActionNode choice = null;
		
		for(UCTActionNode anode : snode.actionNodes){
			
			//only select nodes that have been visited
			if(anode.n > 0 && anode.averageReturn() > maxQ){
				maxQ = anode.averageReturn();
				choice = anode;
			}
			
		}
		
		return choice;
		
	}
	
	
	/**
	 * Returns true if the sample returns for any actions are different
	 * @param snode the node to check for an action preference
	 * @return true if the sample returns for any actions are different; false otherwise or if there is only one action to take.
	 */
	protected boolean containsActionPreference(UCTStateNode snode){

		if(snode == null){
			return false;
		}
		
		UCTActionNode lastNode = null;
		boolean multipleChoices = false;
		for(UCTActionNode anode : snode.actionNodes){
			
			//only select nodes that have been visited
			if(anode.n > 0){
				if(lastNode != null){
					if(anode.averageReturn() != lastNode.averageReturn()){
						return true;
					}
					multipleChoices = true;
				}
				
				lastNode = anode;
			}
			
		}
		
		if(multipleChoices){
			return false;
		}
		
		return true; //there was only once choice so it's not ill defined
		
	}
	

}
