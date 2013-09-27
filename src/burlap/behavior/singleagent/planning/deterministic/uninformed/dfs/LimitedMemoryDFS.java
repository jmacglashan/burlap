package burlap.behavior.singleagent.planning.deterministic.uninformed.dfs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.SearchNode;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class LimitedMemoryDFS extends DFS {

	protected int									memorySize;
	protected LinkedList<StateHashTuple>			memoryQueue;
	protected Map <StateHashTuple, Integer>			memoryStateDepth;
	
	
	
	public LimitedMemoryDFS(Domain domain, StateConditionTest gc, StateHashFactory hashingFactory, int maxDepth, 
			boolean maintainClosed, boolean optionsFirst, int memorySize) {
		super(domain, gc, hashingFactory, maxDepth, maintainClosed,
				optionsFirst);
		
		this.memorySize = memorySize;
		
	}
	
	
	
	@Override
	public void planFromState(State initialState){
		
		memoryQueue = new LinkedList<StateHashTuple>();
		memoryStateDepth = new HashMap<StateHashTuple, Integer>();
		
		super.planFromState(initialState);
		
	}
	
	
	protected SearchNode dfs(SearchNode n, int depth, Set<StateHashTuple> statesOnPath){
		
		numVisted++;
		
		if(gc.satisfies(n.s.s)){
			//found goal!
			return n;
		}
		
		if(maxDepth != -1 && depth > maxDepth){
			return null; //back track
		}
		
		//otherwise we need to generate successors and search them
		
		statesOnPath.add(n.s);
		
		if(memoryQueue.size() >= memorySize){
			StateHashTuple mempop = memoryQueue.poll();
			memoryStateDepth.remove(mempop);
			
		}
		
		memoryQueue.offer(n.s);
		memoryStateDepth.put(n.s, depth);
		
		
		//shuffle actions for a random walk, but keep options as priority if set that way
		List<GroundedAction> gas = this.getAllGroundedActions(n.s.s);
		if(optionsFirst){
			int no = this.numOptionsInGAs(gas);
			this.shuffleGroundedActions(gas, 0, no);
			this.shuffleGroundedActions(gas, no, gas.size());
		}
		else{
			this.shuffleGroundedActions(gas, 0, gas.size());
		}
		
		//generate a search successors from the order of grounded actions
		for(GroundedAction ga : gas){
			StateHashTuple shp = this.stateHash(ga.executeIn(n.s.s));
			boolean notInMemory = true;
			Integer memoryDepth = memoryStateDepth.get(shp);
			if(memoryDepth != null){
				int md = memoryDepth;
				if(maxDepth == -1 || md <= depth+1){
					notInMemory = false;
				}
			}
			if(!statesOnPath.contains(shp) && notInMemory){
				SearchNode snp = new SearchNode(shp, ga, n);
				SearchNode result = this.dfs(snp, depth+1, statesOnPath);
				if(result != null){
					return result;
				}
			}
		}
		
		//no successors found a solution
		if(!maintainClosed){
			statesOnPath.remove(n.s);
		}
		return null;
	}

}
