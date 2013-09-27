package burlap.behavior.singleagent.planning.stochastic.montecarlo.uct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCTActionNode.UCTActionConstructor;
import burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCTStateNode.UCTStateConstructor;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;


public class UCT extends OOMDPPlanner {

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
	
	
	
	public UCT(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, int horizon, int nRollouts, int explorationBias){
		
		stateNodeConstructor = new UCTStateConstructor();
		actionNodeConstructor = new UCTActionConstructor();
		
		
		this.UCTInit(domain, rf, tf, gamma, hashingFactory, horizon, nRollouts, explorationBias);
		
		
	}
	
	public void UCTInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, int horizon, int nRollouts, int explorationBias){
		
		this.PlannerInit(domain, rf, tf, gamma, hashingFactory);
		this.maxHorizon = horizon;
		this.maxRollOutsFromRoot = nRollouts;
		this.explorationBias = explorationBias;
		
		goalCondition = null;
		
		rand = RandomFactory.getMapped(589449);
		
	}
	
	
	public UCTStateNode getRoot(){
		return root;
	}
	
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
	
	
	protected void initializeRollOut(){
		foundGoalOnRollout = false;
	}
	
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
			//no action to perform!
			return ((maxHorizon - depth))*-1.; //TODO: this should be generalized to RMIN and return Min
		}
		
		
		
		//sample the action
		StateHashTuple shprime = this.stateHash(anode.action.executeIn(node.state.s));
		double r = rf.reward(node.state.s, anode.action, shprime.s);
		int depthChange = 1;
		if(!anode.action.action.isPrimitive()){
			Option o = (Option)anode.action.action;
			depthChange = o.getLastNumSteps();
		}
		
		if(numRollOutsFromRoot % 100 == 0){
			//System.out.print(anode.action.toString() + " ");
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
		
		if(foundGoalOnRollout && anode != this.bestReturnAction(node)){
			DPrint.cl(debugCode, "Not propagated correctly...");
		}
		
		return sampledReturn;
	}
	
	
	
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
	
	
	protected double computeUCTQ(UCTStateNode snode, UCTActionNode anode){
		return anode.averageReturn() + this.explorationQBoost(snode.n, anode.n);
	}
	
	
	protected double explorationQBoost(int ns, int na){
		return explorationBias * Math.sqrt(Math.log(ns) / (double)na);
	}
	
	
	
	protected UCTStateNode queryTreeIndex(StateHashTuple sh, int d){
		
		if(d >= stateDepthIndex.size()){
			return null;
		}
		
		return stateDepthIndex.get(d).get(sh);
		
	}
	
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
	
	protected boolean containsActionPreference(UCTStateNode snode){
		
		
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
