package burlap.behavior.singleagent.learning.tdmethods.vfa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.EpsilonGreedy;
import burlap.behavior.singleagent.vfa.ActionApproximationResult;
import burlap.behavior.singleagent.vfa.ActionFeaturesQuery;
import burlap.behavior.singleagent.vfa.ApproximationResult;
import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.behavior.singleagent.vfa.FunctionWeight;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.WeightGradient;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class GradientDescentSarsaLam extends OOMDPPlanner implements QComputablePlanner, LearningAgent {
	
	
	protected ValueFunctionApproximation							vfa;
	protected double												learningRate;
	protected Policy												learningPolicy;
	protected double												lambda;
	
	protected int													maxEpisodeSize;
	protected int													eStepCounter;
	
	
	protected int													numEpisodesForPlanning;
	protected double												maxWeightChangeForPlanningTermination;
	protected double												maxWeightChangeInLastEpisode;
	
	protected double												minEligibityForUpdate = 0.01;
	
	protected LinkedList<EpisodeAnalysis>							episodeHistory;
	protected int													numEpisodesToStore;
	
	protected boolean												useReplacingTraces = false;
	
	protected boolean												shouldDecomposeOptions = true;
	protected boolean												shouldAnnotateOptions = true;
	
	
	
	
	

	public GradientDescentSarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, ValueFunctionApproximation vfa, 
			double learningRate, double lamda) {
		
		this.GDSLInit(domain, rf, tf, gamma, vfa, learningRate, new EpsilonGreedy(this, 0.1), Integer.MAX_VALUE, lamda);
		
	}
	
	public GradientDescentSarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, ValueFunctionApproximation vfa, 
			double learningRate, int maxEpisodeSize, double lamda) {
		
		this.GDSLInit(domain, rf, tf, gamma, vfa, learningRate, new EpsilonGreedy(this, 0.1), maxEpisodeSize, lamda);
		
	}
	
	public GradientDescentSarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, ValueFunctionApproximation vfa, 
			double learningRate, Policy learningPolicy, int maxEpisodeSize, double lamda) {
	
		this.GDSLInit(domain, rf, tf, gamma, vfa, learningRate, learningPolicy, maxEpisodeSize, lamda);
	}

	
	protected void GDSLInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, ValueFunctionApproximation vfa, 
			double learningRate, Policy learningPolicy, int maxEpisodeSize, double lamda){
		
		this.plannerInit(domain, rf, tf, gamma, null);
		this.vfa = vfa;
		this.learningRate = learningRate;
		this.learningPolicy = learningPolicy;
		this.maxEpisodeSize = maxEpisodeSize;
		this.lambda = lamda;
		
		numEpisodesToStore = 1;
		episodeHistory = new LinkedList<EpisodeAnalysis>();
		
		numEpisodesForPlanning = 1;
		maxWeightChangeForPlanningTermination = 0.;

		
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
			this.maxWeightChangeForPlanningTermination = m;
		}
		else{
			this.maxWeightChangeForPlanningTermination = 0.;
		}
	}
	
	public int getLastNumSteps(){
		return eStepCounter;
	}
	
	public void setUseReplaceTraces(boolean toggle){
		this.useReplacingTraces = toggle;
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
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState) {
		
		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
		maxWeightChangeInLastEpisode = 0.;
		
		State curState = initialState;
		eStepCounter = 0;
		Map <Integer, EligibilityTraceVector> traces = new HashMap<Integer, GradientDescentSarsaLam.EligibilityTraceVector>();
		
		GroundedAction action = this.learningPolicy.getAction(curState);
		List<ActionApproximationResult> allCurApproxResults = this.getAllActionApproximations(curState);
		ActionApproximationResult curApprox = ActionApproximationResult.extractApproximationForAction(allCurApproxResults, action);
		
		
		while(!tf.isTerminal(curState) && eStepCounter < maxEpisodeSize){
			
			
			WeightGradient gradient = this.vfa.getWeightGradient(curApprox.approximationResult);
			
			State nextState = action.executeIn(curState);
			GroundedAction nextAction = this.learningPolicy.getAction(nextState);
			List<ActionApproximationResult> allNextApproxResults = this.getAllActionApproximations(nextState);
			ActionApproximationResult nextApprox = ActionApproximationResult.extractApproximationForAction(allNextApproxResults, nextAction);
			double nextQV = nextApprox.approximationResult.predictedValue;
			if(tf.isTerminal(nextState)){
				nextQV = 0.;
			}
			
			
			//manage option specifics
			double r = 0.;
			double discount = this.gamma;
			if(action.action.isPrimitive()){
				r = rf.reward(curState, action, nextState);
				eStepCounter++;
				ea.recordTransitionTo(nextState, action, r);
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
					ea.recordTransitionTo(nextState, action, r);
				}
			}
			
			//delta
			double delta = r + (discount * nextQV) - curApprox.approximationResult.predictedValue;
			
			
			if(useReplacingTraces){
				//then first clear traces of unselected action and reset the trace for the selected one
				for(ActionApproximationResult aar : allCurApproxResults){
					if(!aar.ga.equals(action)){ //clear unselected action trace
						for(FunctionWeight fw : aar.approximationResult.functionWeights){
							traces.remove(fw.weightId());
						}
					}
					else{ //reset trace of selected action
						for(FunctionWeight fw : aar.approximationResult.functionWeights){
							EligibilityTraceVector storedTrace = traces.get(fw.weightId());
							if(storedTrace != null){
								storedTrace.eligibilityValue = 0.;
							}
						}
					}
				}
			}
			
			
			//update all traces
			Set <Integer> deletedSet = new HashSet<Integer>();
			for(EligibilityTraceVector et : traces.values()){
				
				int weightId = et.weight.weightId();
				
				et.eligibilityValue += gradient.getPartialDerivative(weightId);
				double newWeight = et.weight.weightValue() + this.learningRate*delta*et.eligibilityValue;
				et.weight.setWeight(newWeight);
				
				double deltaW = Math.abs(et.initialWeightValue - newWeight);
				if(deltaW > maxWeightChangeInLastEpisode){
					maxWeightChangeInLastEpisode = deltaW;
				}
				
				et.eligibilityValue *= this.lambda*discount;
				if(et.eligibilityValue < this.minEligibityForUpdate){
					deletedSet.add(weightId);
				}
				
			}
			
			//add new traces if need be
			for(FunctionWeight fw : curApprox.approximationResult.functionWeights){
				
				int weightId = fw.weightId();
				if(!traces.containsKey(fw)){
					
					//then it's new and we need to add it
					EligibilityTraceVector et = new EligibilityTraceVector(fw, gradient.getPartialDerivative(weightId));
					double newWeight = fw.weightValue() + this.learningRate*delta*et.eligibilityValue;
					fw.setWeight(newWeight);
					
					double deltaW = Math.abs(et.initialWeightValue - newWeight);
					if(deltaW > maxWeightChangeInLastEpisode){
						maxWeightChangeInLastEpisode = deltaW;
					}
					
					et.eligibilityValue *= this.lambda*discount;
					if(et.eligibilityValue >= this.minEligibityForUpdate){
						traces.put(weightId, et);
					}
					
				}
				
			}
			
			//delete any traces
			for(Integer t : deletedSet){
				traces.remove(t);
			}
			
			
			//move on
			curState = nextState;
			action = nextAction;
			curApprox = nextApprox;
			allCurApproxResults = allNextApproxResults;
			
		}
		
		if(episodeHistory.size() >= numEpisodesToStore){
			episodeHistory.poll();
			episodeHistory.offer(ea);
		}
		
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

	@Override
	public List<QValue> getQs(State s) {
		List<GroundedAction> gas = this.getAllGroundedActions(s);
		List <QValue> qs = new ArrayList<QValue>(gas.size());
		
		
		List<ActionApproximationResult> results = vfa.getStateActionValues(s, gas);
		for(GroundedAction ga : gas){
			qs.add(this.getQFromFeaturesFor(results, s, ga));
		}
		
		return qs;
	}

	@Override
	public QValue getQ(State s, GroundedAction a) {
		
		List <GroundedAction> gaList = new ArrayList<GroundedAction>(1);
		gaList.add(a);
		
		List<ActionApproximationResult> results = vfa.getStateActionValues(s, gaList);
		
		return this.getQFromFeaturesFor(results, s, a);
	}
	
	
	protected QValue getQFromFeaturesFor(List<ActionApproximationResult> results, State s, GroundedAction ga){
		
		ActionApproximationResult result = ActionApproximationResult.extractApproximationForAction(results, ga);
		QValue q = new QValue(s, ga, result.approximationResult.predictedValue);
		
		return q;
	}
	
	protected List <ActionApproximationResult> getAllActionApproximations(State s){
		List<GroundedAction> gas = this.getAllGroundedActions(s);
		return this.vfa.getStateActionValues(s, gas);
	}
	
	protected ActionApproximationResult getActionApproximation(State s, GroundedAction ga){
		List <GroundedAction> gaList = new ArrayList<GroundedAction>(1);
		gaList.add(ga);
		
		List<ActionApproximationResult> results = vfa.getStateActionValues(s, gaList);
		
		return ActionApproximationResult.extractApproximationForAction(results, ga);
	}

	@Override
	public void planFromState(State initialState) {
		
		int eCount = 0;
		do{
			this.runLearningEpisodeFrom(initialState);
			eCount++;
		}while(eCount < numEpisodesForPlanning && maxWeightChangeInLastEpisode > maxWeightChangeForPlanningTermination);

	}
	
	
	
	
	
	public static class EligibilityTraceVector{
		
		public FunctionWeight		weight;
		public double				eligibilityValue;
		public double				initialWeightValue;
		
		
		public EligibilityTraceVector(FunctionWeight weight, double eligibilityValue){
			this.weight = weight;
			this.eligibilityValue = eligibilityValue;
			this.initialWeightValue = weight.weightValue();
		}
		
	}

}
