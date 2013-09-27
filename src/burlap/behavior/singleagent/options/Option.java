package burlap.behavior.singleagent.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.planning.StateMapping;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.NullAction;




/*
 * Note: will the reward tracker work for 3-level hierarchical options with discounting?
 * I think not they will only work if the one step hierarchical action selection
 * only returns primitives, in which case I will have to implement
 * that for any hierarchical subclasses 
 * 
 */


public abstract class Option extends Action {

	protected Random 												rand;
	
	protected EpisodeAnalysis										lastOptionExecutionResults;
	protected boolean												shouldRecordResults;
	protected boolean												shouldAnnotateExecution;
	
	//variables for keeping track of reward from execution
	protected RewardFunction 										rf;
	protected boolean 												keepTrackOfReward;
	protected double 												discountFactor;
	protected double 												lastCumulativeReward;
	protected double												cumulativeDiscount;
	protected int													lastNumSteps;
	protected TerminalFunction										externalTerminalFunction;
	
	//these variable required for planners that use full bellman updates
	protected StateHashFactory										expectationStateHashingFactory;
	protected Map<StateHashTuple, List <TransitionProbability>> 	cachedExpectations;
	protected Map<StateHashTuple, Double>							cachedExpectedRewards;
	protected double												expectationSearchCutoffProb = 0.001;
	
	protected StateMapping											stateMapping;
	
	protected DirectOptionTerminateMapper							terminateMapper;
	
	
	public abstract boolean isMarkov();
	public abstract boolean usesDeterministicTermination();
	public abstract boolean usesDeterministicPolicy();
	public abstract double probabilityOfTermination(State s, String [] params);
	public abstract void initiateInStateHelper(State s, String [] params); //important if the option is not Markovian; called automatically by the perform action helper
	public abstract GroundedAction oneStepActionSelection(State s, String [] params);
	public abstract List<ActionProb> getActionDistributionForState(State s, String [] params);
	
	public Option(){
		this.init();
	}
	
	public Option(String name, Domain domain, String parameterClasses) {
		super(name, domain, parameterClasses);
		this.init();
	}

	
	public Option(String name, Domain domain, String [] parameterClasses){
		super(name, domain, parameterClasses);
		this.init();
	}
	
	public Option(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames){
		super(name, domain, parameterClasses, replacedClassNames);
		this.init();
	}
	
	private void init(){
		rand = new Random();
		rf = null;
		keepTrackOfReward = false;
		discountFactor = 1.;
		lastCumulativeReward = 0.;
		cumulativeDiscount = 1.;
		lastNumSteps = 0;
		stateMapping = null;
		terminateMapper = null;
		externalTerminalFunction = new NullTermination();
		shouldRecordResults = true;
		shouldAnnotateExecution = true;
	}
	
	
	public void setExpectationHashingFactory(StateHashFactory hashingFactory){
		this.expectationStateHashingFactory = hashingFactory;
		this.cachedExpectations = new HashMap<StateHashTuple, List<TransitionProbability>>();
		this.cachedExpectedRewards = new HashMap<StateHashTuple, Double>();
	}
	
	public void setExpectationCalculationProbabilityCutoff(double cutoff){
		this.expectationSearchCutoffProb = cutoff;
	}
	
	public void toggleShouldRecordResults(boolean toggle){
		this.shouldRecordResults = toggle;
	}
	
	public void toggleShouldAnnotateResults(boolean toggle){
		this.shouldAnnotateExecution = toggle;
	}
	
	
	public boolean isRecordingExecutionResults(){
		return shouldRecordResults;
	}
	
	public boolean isAnnotatingExecutionResults(){
		return shouldAnnotateExecution;
	}
	
	public EpisodeAnalysis getLastExecutionResults(){
		return lastOptionExecutionResults;
	}
	
	public void setStateMapping(StateMapping m){
		this.stateMapping = m;
	}
	
	public void setTerminateMapper(DirectOptionTerminateMapper tm){
		this.terminateMapper = tm;
	}
	
	public void setExernalTermination(TerminalFunction tf){
		if(tf == null){
			this.externalTerminalFunction = new NullTermination();
		}
		else{
			this.externalTerminalFunction = tf;
		}
	}
	
	protected State map(State s){
		if(stateMapping == null){
			return s;
		}
		return stateMapping.mapState(s);
	}
	
	public void keepTrackOfRewardWith(RewardFunction rf, double discount){
		this.keepTrackOfReward = true;
		this.rf = rf;
		this.discountFactor = discount;
	}
	
	//options should not be connected from the domain, because the domain describes base level behavior
	//this method overrides and removes the statement connecting the domain to it
	public void init(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames){
		
		this.name = name;
		this.domain = domain;
		this.parameterClasses = parameterClasses;
		this.parameterOrderGroup = replacedClassNames;
		
	}
	
	
	public double getLastCumulativeReward(){
		return this.lastCumulativeReward;
	}
	
	public int getLastNumSteps(){
		return this.lastNumSteps;
	}
	
	//should be overridden by special primitive option subclass which simply encapsulates an action and provides option interfaces
	@Override
	public boolean isPrimitive(){
		return false;
	}
	
	public void initiateInState(State s, String [] params){
		lastCumulativeReward = 0.;
		cumulativeDiscount = 1.;
		lastNumSteps = 0;
		lastOptionExecutionResults = new EpisodeAnalysis(s);
		this.initiateInStateHelper(s, params);
	}
	
	protected State performActionHelper(State st, String[] params){
		
		if(terminateMapper != null){
			State ns = terminateMapper.generateOptionTerminalState(st);
			lastNumSteps = terminateMapper.getNumSteps(st, ns);
			lastCumulativeReward = terminateMapper.getCumulativeReward(st, ns, rf, discountFactor);
			return ns;
		}
		
		State curState = st;
		
		this.initiateInState(curState, params);
		
		do{
			curState = this.oneStep(curState, params);
		}while(this.continueFromState(curState, params) && !externalTerminalFunction.isTerminal(curState));
		
		
		
		return curState;
	}
	
	
	public State oneStep(State s, String [] params){
		GroundedAction ga = this.oneStepActionSelection(s, params);
		State sprime = ga.executeIn(s);
		lastNumSteps++;
		double r = 0.;
		if(keepTrackOfReward){
			r = rf.reward(s, ga, sprime);
			lastCumulativeReward += cumulativeDiscount*r;
			cumulativeDiscount *= discountFactor;
		}
		
		if(shouldRecordResults){
			GroundedAction recordAction = ga;
			if(shouldAnnotateExecution){
				NullAction annotatedPrimitive = new NullAction(this.name + "(" + (lastNumSteps-1) + ")-" + ga.action.getName());
				recordAction = new GroundedAction(annotatedPrimitive, ga.params);
			}
			lastOptionExecutionResults.recordTransitionTo(sprime, recordAction, r);
		}
		
		
		
		return sprime;
	}
	
	
	
	public boolean continueFromState(State s, String [] params){
		double pt = this.probabilityOfTermination(s, params);
		
		//deterministic case needs no random roll
		if(pt == 1.){
			return false;
		}
		else if(pt == 0.){
			return true;
		}
		
		//otherwise need to do a random roll to determine if we terminated here or not
		double roll = rand.nextDouble();
		if(roll < pt){
			return false; //terminate
		}
		
		return true;
		
	}
	
	
	public double getExpectedRewards(State s, String [] params){
		StateHashTuple sh = this.expectationStateHashingFactory.hashState(s);
		Double result = this.cachedExpectedRewards.get(sh);
		if(result != null){
			return result;
		}
		this.getTransitions(s, params);
		return this.cachedExpectedRewards.get(sh);
	}
	
	public List<TransitionProbability> getTransitions(State st, String [] params){
		
		StateHashTuple sh = this.expectationStateHashingFactory.hashState(st);
		
		List <TransitionProbability> result = this.cachedExpectations.get(sh);
		if(result != null){
			return result;
		}
		
		this.initiateInState(st, params);
		
		ExpectationSearchNode esn = new ExpectationSearchNode(st, params);
		Map <StateHashTuple, Double> possibleTerminations = new HashMap<StateHashTuple, Double>();
		double [] expectedReturn = new double[]{0.};
		this.iterateExpectationScan(esn, 1., possibleTerminations, expectedReturn);
		
		this.cachedExpectedRewards.put(sh, expectedReturn[0]);
		
		List <TransitionProbability> transition = new ArrayList<TransitionProbability>();
		for(Map.Entry<StateHashTuple, Double> e : possibleTerminations.entrySet()){
			TransitionProbability tp = new TransitionProbability(e.getKey().s, e.getValue());
			transition.add(tp);
		}
		
		this.cachedExpectations.put(sh, transition);
		
		//State res = this.performAction(st, params);
		//transition.add(new TransitionProbability(res, 1.0));
		
		return transition;
	}
	
	

	protected void iterateExpectationScan(ExpectationSearchNode src, double stackedDiscount, 
			Map <StateHashTuple, Double> possibleTerminations, double [] expectedReturn){
		
		
		double probTerm = 0.0; //can never terminate in initiation state
		if(src.nSteps > 0){
			probTerm = this.probabilityOfTermination(src.s, src.optionParams);
		}
		
		double probContinue = 1.-probTerm;
		
		
		//handle possible termination
		if(probTerm > 0.){
			double probOfDiscountedTrajectory = src.probability*stackedDiscount;
			this.accumulateDiscountedProb(possibleTerminations, src.s, probOfDiscountedTrajectory);
			expectedReturn[0] += src.cumulativeDiscountedReward;
		}
		
		//handle continuation
		if(probContinue > 0.){
			
			//handle option policy selection
			List <ActionProb> actionSelction = this.getActionDistributionForState(src.s, src.optionParams);
			for(ActionProb ap : actionSelction){
				
				//now get possible outcomes of each action
				List <TransitionProbability> transitions = ap.ga.action.getTransitions(src.s, src.optionParams);
				for(TransitionProbability tp : transitions){
					double totalTransP = ap.pSelection * tp.p;
					double r = stackedDiscount * this.rf.reward(src.s, ap.ga, tp.s);
					ExpectationSearchNode next = new ExpectationSearchNode(src, tp.s, totalTransP, r);
					if(next.probability > this.expectationSearchCutoffProb){
						this.iterateExpectationScan(next, stackedDiscount*discountFactor, possibleTerminations, expectedReturn);
					}
				}
				
			}
			
		}
		
	}
	
	
	protected void accumulateDiscountedProb(Map <StateHashTuple, Double> possibleTerminations, State s, double p){
		StateHashTuple sh = expectationStateHashingFactory.hashState(s);
		Double stored = possibleTerminations.get(sh);
		double newP = p;
		if(stored != null){
			newP = stored + p;
		}
		possibleTerminations.put(sh, newP);
	}
	
	protected List <ActionProb> getDeterministicPolicy(State s, String [] params){
		GroundedAction ga = this.oneStepActionSelection(s, params);
		ActionProb ap = new ActionProb(ga, 1.);
		List <ActionProb> aps = new ArrayList<Policy.ActionProb>();
		aps.add(ap);
		return aps;
	}
	
	
	
	
	
	
	
	
	class ExpectationSearchNode{
		
		public State s;
		public String [] optionParams;
		
		public double	probability;
		public double	cumulativeDiscountedReward;
		public int		nSteps;
		
		
		public ExpectationSearchNode(State s, String [] optionParams){
			this.s = s;
			this.optionParams = optionParams;
			this.probability = 1.;
			this.cumulativeDiscountedReward = 0.;
			this.nSteps = 0;
		}
		
		
		public ExpectationSearchNode(ExpectationSearchNode src, State s, double transProb, double discountedR){
		
			this.s = s;
			this.optionParams = src.optionParams;
			this.probability = src.probability*transProb;
			this.cumulativeDiscountedReward = src.cumulativeDiscountedReward + discountedR;
			this.nSteps = src.nSteps+1;
			
			
		}
		
	}
	

}
