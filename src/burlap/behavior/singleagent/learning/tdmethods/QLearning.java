package burlap.behavior.singleagent.learning.tdmethods;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.EpsilonGreedy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class QLearning extends OOMDPPlanner implements QComputablePlanner, LearningAgent{

	protected Map<StateHashTuple, QLearningStateNode>				qIndex;
	protected double												qInit;
	protected double												learningRate;
	protected Policy												learningPolicy;
	
	protected int													maxEpisodeSize;
	protected int													eStepCounter;
	
	
	protected int													numEpisodesForPlanning;
	protected double												maxQChangeForPlanningTermination;
	protected double												maxQChangeInLastEpisode;
	
	protected LinkedList<EpisodeAnalysis>							episodeHistory;
	protected int													numEpisodesToStore;
	
	
	protected boolean												shouldDecomposeOptions = true;
	protected boolean												shouldAnnotateOptions = true;
	
	
	
	public QLearning(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, 
			double qInit, double learningRate) {
		this.QLInit(domain, rf, tf, gamma, hashingFactory, qInit, learningRate, new EpsilonGreedy(this, 0.1), Integer.MAX_VALUE);
	}
	
	public QLearning(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, 
			double qInit, double learningRate, int maxEpisodeSize) {
		this.QLInit(domain, rf, tf, gamma, hashingFactory, qInit, learningRate, new EpsilonGreedy(this, 0.1), maxEpisodeSize);
	}
	
	public QLearning(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, 
			double qInit, double learningRate, Policy learningPolicy, int maxEpisodeSize) {
		this.QLInit(domain, rf, tf, gamma, hashingFactory, qInit, learningRate, learningPolicy, maxEpisodeSize);
	}
	
	
	public void QLInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, 
			double qInit, double learningRate, Policy learningPolicy, int maxEpisodeSize){
		
		this.PlannerInit(domain, rf, tf, gamma, hashingFactory);
		this.qIndex = new HashMap<StateHashTuple, QLearningStateNode>();
		this.learningRate = learningRate;
		this.learningPolicy = learningPolicy;
		this.maxEpisodeSize = maxEpisodeSize;
		this.qInit = qInit;
		
		numEpisodesToStore = 1;
		episodeHistory = new LinkedList<EpisodeAnalysis>();
		
		numEpisodesForPlanning = 1;
		maxQChangeForPlanningTermination = 0.;

		
	}
	
	
	public void setLearningPolicy(Policy p){
		this.learningPolicy = p;
	}
	
	
	public void setMaxEpisodesForPlanning(int n){
		if(n > 0){
			this.numEpisodesForPlanning = n;
		}
		else{
			this.numEpisodesForPlanning = 1;
		}
	}
	
	public void setMaxQChangeForPlanningTerminaiton(double m){
		if(m > 0.){
			this.maxQChangeForPlanningTermination = m;
		}
		else{
			this.maxQChangeForPlanningTermination = 0.;
		}
	}
	
	public int getLastNumSteps(){
		return eStepCounter;
	}
	
	
	/**
	 * Sets whether the primitive actions taken during an options will be included as steps in produced EpisodeAnalysis objects.
	 * The default value is true. If this is set to false, then EpisodeAnalysis objects returned from a learning episode will record options
	 * as a single "action" and the steps taken by the option will be hidden. 
	 * @param toggle whether to decompose options into the primitive actions taken by them or not.
	 */
	public void toggleShouldDecomposeOption(boolean toggle){
		
		this.shouldDecomposeOptions = toggle;
		for(Action a : actions){
			if(a instanceof Option){
				((Option)a).toggleShouldRecordResults(toggle);
			}
		}
	}
	
	/**
	 * Sets whether options that are decomposed into primitives will have the option that produced them and listed.
	 * The default value is true. If option decomposition is not enabled, changing this value will do nothing. When it
	 * is enabled and this is set to true, primitive actions taken by an option in EpisodeAnalysis objects will be
	 * recorded with a special action name that indicates which option was called to produce the primitive action
	 * as well as which step of the option the primitive action is. When set to false, recorded names of primitives
	 * will be only the primitive aciton's name it will be unclear which option was taken to generate it.
	 * @param toggle whether to annotate the primitive actions of options with the calling option's name.
	 */
	public void toggleShouldAnnotateOptionDecomposition(boolean toggle){
		shouldAnnotateOptions = toggle;
		for(Action a : actions){
			if(a instanceof Option){
				((Option)a).toggleShouldAnnotateResults(toggle);
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public List<QValue> getQs(State s) {
		return this.getQs(this.stateHash(s));
	}

	@Override
	public QValue getQ(State s, GroundedAction a) {
		return this.getQ(this.stateHash(s), a);
	}
	
	

	protected List<QValue> getQs(StateHashTuple s) {
		QLearningStateNode node = this.getStateNode(s);
		return node.qEntry;
	}


	protected QValue getQ(StateHashTuple s, GroundedAction a) {
		QLearningStateNode node = this.getStateNode(s);
		
		if(a.params.length > 0){
			Map<String, String> matching = s.s.getObjectMatchingTo(node.s.s, false);
			a = this.translateAction(a, matching);
		}
		
		for(QValue qv : node.qEntry){
			if(qv.a.equals(a)){
				return qv;
			}
		}
		
		return null; //no action for this state indexed / raise problem
	}
	
	protected QLearningStateNode getStateNode(StateHashTuple s){
		
		QLearningStateNode node = qIndex.get(s);
		
		if(node == null){
			node = new QLearningStateNode(s);
			List<GroundedAction> gas = this.getAllGroundedActions(s.s);
			if(gas.size() == 0){
				gas = this.getAllGroundedActions(s.s);
				throw new RuntimeErrorException(new Error("No possible actions in this state, cannot continue Q-learning"));
			}
			for(GroundedAction ga : gas){
				node.addQValue(ga, qInit);
			}
			
			qIndex.put(s, node);
		}
		
		return node;
		
	}
	
	protected double getMaxQ(StateHashTuple s){
		List <QValue> qs = this.getQs(s);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			if(q.q > max){
				max = q.q;
			}
		}
		return max;
	}

	@Override
	public void planFromState(State initialState) {
		
		int eCount = 0;
		do{
			this.runLearningEpisodeFrom(initialState);
			eCount++;
		}while(eCount < numEpisodesForPlanning && maxQChangeInLastEpisode > maxQChangeForPlanningTermination);
		

	}


	@Override
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState) {
		
		this.toggleShouldAnnotateOptionDecomposition(shouldAnnotateOptions);
		
		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
		
		StateHashTuple curState = this.stateHash(initialState);
		eStepCounter = 0;
		
		maxQChangeInLastEpisode = 0.;
		
		while(!tf.isTerminal(curState.s) && eStepCounter < maxEpisodeSize){
			
			GroundedAction action = learningPolicy.getAction(curState.s);
			QValue curQ = this.getQ(curState, action);
			
			StateHashTuple nextState = this.stateHash(action.executeIn(curState.s));
			double maxQ = 0.;
			
			if(!tf.isTerminal(nextState.s)){
				maxQ = this.getMaxQ(nextState);
			}
			
			//manage option specifics
			double r = 0.;
			double discount = this.gamma;
			if(action.action.isPrimitive()){
				r = rf.reward(curState.s, action, nextState.s);
				eStepCounter++;
				ea.recordTransitionTo(nextState.s, action, r);
			}
			else{
				Option o = (Option)action.action;
				r = o.getLastCumulativeReward();
				int n = o.getLastNumSteps();
				discount = Math.pow(this.gamma, n);
				eStepCounter += n;
				if(this.shouldDecomposeOptions){
					ea.appendAndMergeEpisodeAnalysis(o.getLastExecutionResults());
				}
				else{
					ea.recordTransitionTo(nextState.s, action, r);
				}
			}
			
			
			
			double oldQ = curQ.q;
			
			//update Q-value
			curQ.q = curQ.q + this.learningRate * (r + (discount * maxQ) - curQ.q);
			
			double deltaQ = Math.abs(oldQ - curQ.q);
			if(deltaQ > maxQChangeInLastEpisode){
				maxQChangeInLastEpisode = deltaQ;
			}
			
			//move on
			curState = nextState;
			
			
		}
		
		if(episodeHistory.size() >= numEpisodesToStore){
			episodeHistory.poll();
		}
		episodeHistory.offer(ea);
		
		return ea;
	}


	@Override
	public EpisodeAnalysis getLastLearningEpisode() {
		return episodeHistory.getLast();
	}


	@Override
	public void setNumEpisodesToStore(int numEps) {
		if(numEps > 0){
			numEpisodesToStore = numEps;
		}
		else{
			numEpisodesToStore = 1;
		}
	}


	@Override
	public List<EpisodeAnalysis> getAllStoredLearningEpisodes() {
		return episodeHistory;
	}

}
