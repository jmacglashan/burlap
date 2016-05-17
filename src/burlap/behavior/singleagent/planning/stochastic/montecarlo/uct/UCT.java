package burlap.behavior.singleagent.planning.stochastic.montecarlo.uct;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.options.EnvironmentOptionOutcome;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCTActionNode.UCTActionConstructor;
import burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCTStateNode.UCTStateConstructor;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.*;

/**
 * An implementation of UCT [1]. This class can be augmented with a goal state specification (using a {@link burlap.mdp.auxiliary.stateconditiontest.StateConditionTest})
 * that will cause the planning algorithm to terminate early once it has found a path to the goal. This may be useful if randomly finding the goal state is rare.
 * <p>
 * The class also implements the {@link burlap.behavior.valuefunction.QFunction} interface. However, it will only return the Q-value
 * for a state if that state is the root node of the tree. If it is not the root node of the tree, then it will automatically reset the planning results
 * and replan from that state as the root node and then return the result. This allows the client to use a {@link burlap.behavior.policy.GreedyQPolicy}
 * with this valueFunction in which it replans with each step in the world, thereby forcing the Q-values for every state to be for the same horizon.
 * Replanning fresh after each step in the world is the standard UCT approach. If you instead want a policy that walks
 * through the tree it generated from some source state,
 * (so that each step computes a Q-value for a shorter horizon than the step before), you can use the
 * {@link burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCTTreeWalkPolicy}. The TreeWalkPolicy
 * will be more computationally efficient than replanning at each step, but may have degrading performance after each step since
 * each step has a shorter horizon from which to plan and may not have as many samples from which it estimated its Q-value.
 * <p>
 * 1. Kocsis, Levente, and Csaba Szepesvari. "Bandit based monte-carlo planning." ECML (2006). 282-293.
 * 
 * @author James MacGlashan
 *
 */
public class UCT extends MDPSolver implements Planner, QFunction {

	protected List<Map<HashableState, UCTStateNode>> 			stateDepthIndex;
	protected Map <HashableState, List <UCTStateNode>>			statesToStateNodes;
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
	
	protected Set<HashableState>								uniqueStatesInTree;
	
	protected int												treeSize;
	protected int												numVisits;
	
	protected Random											rand;
	
	
	
	/**
	 * Initializes UCT
	 * @param domain the domain in which to plan
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory
	 * @param horizon the planning horizon
	 * @param nRollouts the number of rollouts to perform 
	 * @param explorationBias the exploration bias constant (suggested &gt;2)
	 */
	public UCT(SADomain domain, double gamma, HashableStateFactory hashingFactory, int horizon, int nRollouts, int explorationBias){
		
		stateNodeConstructor = new UCTStateConstructor();
		actionNodeConstructor = new UCTActionConstructor();
		
		
		this.UCTInit(domain, gamma, hashingFactory, horizon, nRollouts, explorationBias);
		
		
	}
	
	protected void UCTInit(SADomain domain, double gamma, HashableStateFactory hashingFactory, int horizon, int nRollouts, int explorationBias){
		
		this.solverInit(domain, gamma, hashingFactory);
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
	 * Tells the valueFunction to stop planning if a goal state is ever found.
	 * @param gc a {@link burlap.mdp.auxiliary.stateconditiontest.StateConditionTest} object used to specify goal states (whereever it evaluates as true).
	 */
	public void useGoalConditionStopCriteria(StateConditionTest gc){
		this.goalCondition = gc;
	}


	/**
	 * Plans from the input state and then returns a {@link burlap.behavior.policy.GreedyQPolicy} that greedily
	 * selects the action with the highest Q-value and breaks ties uniformly randomly.
	 * @param initialState the initial state of the planning problem
	 * @return a {@link burlap.behavior.policy.GreedyQPolicy}.
	 */
	@Override
	public GreedyQPolicy planFromState(State initialState) {
		
		foundGoal = false;
		
		treeSize = 1;
		numVisits = 0;
		
		HashableState shi = this.stateHash(initialState);
		root = stateNodeConstructor.generate(shi, 0, actionTypes, actionNodeConstructor);
		
		uniqueStatesInTree = new HashSet<HashableState>();
		
		stateDepthIndex = new ArrayList<Map<HashableState,UCTStateNode>>();
		statesToStateNodes = new HashMap<HashableState, List<UCTStateNode>>();
		Map <HashableState, UCTStateNode> depth0Map = new HashMap<HashableState, UCTStateNode>();
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
				DPrint.cl(debugCode, String.valueOf(numRollOutsFromRoot) + "; unique states: " + nu  + "; tree size: " + treeSize + "; total visits: " + numVisits);
				lastNumUnique = nu;
			}

		}
		DPrint.cl(debugCode, "\nRollouts: " + numRollOutsFromRoot + "; Best Action Expected Return: " + this.bestReturnAction(root).averageReturn());

		return new GreedyQPolicy(this);

	}

	@Override
	public List<QValue> getQs(State s) {

		//if we haven't done any planning, then do so now
		if(this.root == null){
			this.planFromState(s);
		}

		//if the root node isn't the query state, then replan
		HashableState sh = this.hashingFactory.hashState(s);
		if(!sh.equals(this.root.state)){
			this.resetSolver();
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
	public QValue getQ(State s, Action a) {

		//if we haven't done any planning, then do so now
		if(this.root == null){
			this.planFromState(s);
		}

		//if the root node isn't the query state, then replan
		HashableState sh = this.hashingFactory.hashState(s);
		if(!sh.equals(this.root.state)){
			this.resetSolver();
			this.planFromState(s);
		}

		for(UCTActionNode act : this.root.actionNodes){
			if(act.action.equals(a)){
				return new QValue(s, a, act.averageReturn());
			}
		}

		throw new RuntimeException("UCT does not know about action: " + a.toString() + "; cannot return Q-value for it");
	}

	@Override
	public double value(State s) {
		if(model.terminal(s)){
			return 0.;
		}
		return QFunction.QFunctionHelper.getOptimalValue(this, s);
	}

	@Override
	public void resetSolver(){
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
		
		if(model.terminal(node.state.s)){
			if(goalCondition != null && goalCondition.satisfies(node.state.s)){
			    foundGoal = true;
                foundGoalOnRollout = true;
			}
			DPrint.cl(debugCode, numRollOutsFromRoot + " Hit terminal at depth: " + depth);
			return 0.;
		}
		
		
		
		UCTActionNode anode = this.selectActionNode(node);
		
		if(anode == null){
			//no actions can be performed in this state
			return 0.;
		}
		
		
		
		//sample the action
		EnvironmentOutcome eo = model.sample(node.state.s, anode.action);
		HashableState shprime = this.stateHash(eo.op);
		double r = eo.r;
		int depthChange = 1;
		if(anode.action instanceof Option){
			depthChange = ((EnvironmentOptionOutcome)eo).numSteps();
		}
		
		UCTStateNode snprime = this.queryTreeIndex(shprime, depth+depthChange);
		
		double sampledReturn;
		
		boolean shouldConnectNode = false;
		double futureReturn;
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
			snprime = stateNodeConstructor.generate(shprime, depth+1, actionTypes, actionNodeConstructor);
			
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
	 * if the valueFunction is told to terminate upon finding a goal and one was found, or if
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
	protected UCTStateNode queryTreeIndex(HashableState sh, int d){
		
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
			stateDepthIndex.add(new HashMap<HashableState, UCTStateNode>());
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
