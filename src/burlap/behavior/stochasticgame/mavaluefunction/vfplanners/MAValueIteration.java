package burlap.behavior.stochasticgame.mavaluefunction.vfplanners;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.behavior.stochasticgame.mavaluefunction.MAValueFunctionPlanner;
import burlap.behavior.stochasticgame.mavaluefunction.SGBackupOperator;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SGDomain;

public class MAValueIteration extends MAValueFunctionPlanner {

	protected Set<StateHashTuple> states = new HashSet<StateHashTuple>();
	protected double maxDelta;
	protected int maxIterations;
	protected boolean hasPerformedSR = false;
	
	
	protected int debugCode = 88934789;
	
	
	public MAValueIteration(SGDomain domain, JointActionModel jointActionModel, JointReward jointReward, TerminalFunction terminalFunction, 
			double discount, StateHashFactory hashingFactory, double qInit, SGBackupOperator backupOperator, double maxDelta, int maxIterations){
		
		this.initMAVF(domain, null, jointActionModel, jointReward, terminalFunction, discount, hashingFactory, new ValueFunctionInitialization.ConstantValueFunctionInitialization(qInit), backupOperator);
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		
	}
	
	
	public MAValueIteration(SGDomain domain, JointActionModel jointActionModel, JointReward jointReward, TerminalFunction terminalFunction, 
			double discount, StateHashFactory hashingFactory, ValueFunctionInitialization qInit, SGBackupOperator backupOperator, double maxDelta, int maxIterations){
		
		this.initMAVF(domain, null, jointActionModel, jointReward, terminalFunction, discount, hashingFactory, qInit, backupOperator);
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		
	}
	
	
	
	public MAValueIteration(SGDomain domain, Map<String, AgentType> agentDefinitions, JointActionModel jointActionModel, JointReward jointReward, TerminalFunction terminalFunction, 
			double discount, StateHashFactory hashingFactory, double qInit, SGBackupOperator backupOperator, double maxDelta, int maxIterations){
		
		this.initMAVF(domain, agentDefinitions, jointActionModel, jointReward, terminalFunction, discount, hashingFactory, new ValueFunctionInitialization.ConstantValueFunctionInitialization(qInit), backupOperator);
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		
	}
	
	
	public MAValueIteration(SGDomain domain, Map<String, AgentType> agentDefinitions, JointActionModel jointActionModel, JointReward jointReward, TerminalFunction terminalFunction, 
			double discount, StateHashFactory hashingFactory, ValueFunctionInitialization qInit, SGBackupOperator backupOperator, double maxDelta, int maxIterations){
		
		this.initMAVF(domain, agentDefinitions, jointActionModel, jointReward, terminalFunction, discount, hashingFactory, qInit, backupOperator);
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		
	}
	
	

	@Override
	public void planFromState(State s) {
		
		if(this.performStateReachabilityFrom(s)){
			this.runVI();
		}
		
	}
	
	
	
	protected void runVI(){
		
		if(!this.hasPerformedSR){
			throw new RuntimeException("State reacability needs to be performed before runVI can be called. Consider using planFromState(State s) method instead.");
		}
		
		int i;
		for(i = 0; i < this.maxIterations; i++){
			
			double maxChange = Double.NEGATIVE_INFINITY;
			for(StateHashTuple sh : this.states){
				double change = this.backupAllQs(sh.s);
				maxChange = Math.max(change, maxChange);
			}
			
			if(maxChange < this.maxDelta){
				break ;
			}
			
			System.out.println("Finished pass: " + i + " with max change: " + maxChange);
			
		}
		
		DPrint.cl(this.debugCode, "Performed " + i + " passes.");
		
	}
	
	protected boolean performStateReachabilityFrom(State s){
		
		StateHashTuple shi = this.hashingFactory.hashState(s);
		if(this.states.contains(shi)){
			return false;
		}
		
		this.states.add(shi);
		
		LinkedList<StateHashTuple> openQueue = new LinkedList<StateHashTuple>();
		openQueue.add(shi);
		
		while(openQueue.size() > 0){
			
			StateHashTuple sh = openQueue.poll();
			
			//expand
			List<JointAction> jas = JointAction.getAllJointActions(sh.s, this.agentDefinitions);
			for(JointAction ja : jas){
				List<TransitionProbability> tps = this.jointActionModel.transitionProbsFor(sh.s, ja);
				for(TransitionProbability tp : tps){
					StateHashTuple shp = this.hashingFactory.hashState(tp.s);
					if(!this.states.contains(shp)){
						this.states.add(shp);
						openQueue.add(shp);
					}
				}
			}
			
		}
		
		hasPerformedSR = true;
		
		DPrint.cl(this.debugCode, "Finished State reachability; " + this.states.size() + " unique states found.");
		
		
		return true;
	}
	
}
