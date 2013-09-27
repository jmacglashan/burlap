package burlap.behavior.singleagent.planning.deterministic.uninformed.dfs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SearchNode;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.UniformCostRF;


public class DFS extends DeterministicPlanner {

	
	protected int				maxDepth;
	protected boolean			maintainClosed;
	protected boolean			optionsFirst;
	protected Random			rand;
	
	protected int				numVisted;
	
	public DFS(Domain domain, StateConditionTest gc, StateHashFactory hashingFactory){
		this.DFSInit(domain, new NullTermination(), gc, hashingFactory, -1, false, false);
	}
	
	public DFS(Domain domain, StateConditionTest gc, StateHashFactory hashingFactory, int maxDepth){
		this.DFSInit(domain, new NullTermination(), gc, hashingFactory, maxDepth, false, false);
	}
	
	public DFS(Domain domain, StateConditionTest gc, StateHashFactory hashingFactory, int maxDepth, boolean maintainClosed){
		this.DFSInit(domain, new NullTermination(), gc, hashingFactory, maxDepth, maintainClosed, false);
	}
	
	public DFS(Domain domain, StateConditionTest gc, StateHashFactory hashingFactory, int maxDepth, boolean maintainClosed, boolean optionsFirst){
		this.DFSInit(domain, new NullTermination(), gc, hashingFactory, maxDepth, maintainClosed, optionsFirst);
	}
	
	
	protected void DFSInit(Domain domain, TerminalFunction tf, StateConditionTest gc, StateHashFactory hashingFactory, int maxDepth, boolean maintainClosed, boolean optionsFirst){
		this.deterministicPlannerInit(domain, new UniformCostRF(), tf, gc, hashingFactory);
		this.maxDepth = maxDepth;
		this.maintainClosed = maintainClosed;
		if(optionsFirst){
			this.setOptionsFirst();
		}
		
		rand = RandomFactory.getMapped(0);
	}
	
	
	public int getNumVisited(){
		return numVisted;
	}
	
	@Override
	public void planFromState(State initialState) {
		
		
		if(optionsFirst){
			this.sortActionsWithOptionsFirst();
		}
		
		numVisted = 0;
		
		StateHashTuple sih = this.stateHash(initialState);
		
		if(mapToStateIndex.containsKey(sih)){
			return ; //no need to plan since this is already solved
		}
		
		Set <StateHashTuple> statesOnPath = new HashSet<StateHashTuple>();
		SearchNode sin = new SearchNode(sih);
		SearchNode result = this.dfs(sin, 0, statesOnPath);
		
		if(result != null){
			this.encodePlanIntoPolicy(result); 
		}
		
		DPrint.cl(debugCode, "Num visted: " + numVisted);

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
			if(!statesOnPath.contains(shp)){
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
	
	
	
	public void setOptionsFirst(){
		
		optionsFirst = true;
		
		List <Action> optionOrdered = new ArrayList<Action>();
		
		for(Action a : actions){
			if(!a.isPrimitive()){
				optionOrdered.add(a);
			}
		}
		
		for(Action a : actions){
			if(a.isPrimitive()){
				optionOrdered.add(a);
			}
		}
		
		actions = optionOrdered;
		
	}
	
	//assumes that gas are ordered with options first
	protected int numOptionsInGAs(List <GroundedAction> gas){
		for(int i = 0; i < gas.size(); i++){
			if(gas.get(i).action.isPrimitive()){
				return i;
			}
		}
		return gas.size();
	}
	
	//suffle elements on [s, e)
	protected void shuffleGroundedActions(List <GroundedAction> gas, int s, int e){
		
		int r = e-s;
		
		for(int i = s; i < e; i++){
			GroundedAction ga = gas.get(i);
			int j = rand.nextInt(r) + s;
			gas.set(i, gas.get(j));
			gas.set(j, ga);
		}
		
	}
	
	
	
	protected void sortActionsWithOptionsFirst(){
		List <Action> sactions = new ArrayList<Action>(actions.size());
		for(Action a : actions){
			if(!a.isPrimitive()){
				sactions.add(a);
			}
		}
		for(Action a : actions){
			if(a.isPrimitive()){
				sactions.add(a);
			}
		}
		
		actions = sactions;
	}

}
