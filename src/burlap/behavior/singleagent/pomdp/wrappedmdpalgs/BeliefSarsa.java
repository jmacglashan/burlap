package burlap.behavior.singleagent.pomdp.wrappedmdpalgs;

import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.pomdp.POMDPPlanner;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.cmac.CMACFeatureDatabase.TilingArrangement;
import burlap.behavior.singleagent.vfa.cmac.FVCMACFeatureDatabase;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.BeliefStatistic;
import burlap.oomdp.singleagent.pomdp.PODomain;

public class BeliefSarsa extends POMDPPlanner implements QComputablePlanner {

	protected SADomain							beliefMDP;
	protected RewardFunction					beliefRF;
	protected GradientDescentSarsaLam			agent;
	protected int								numPlanningSteps;
	
	public BeliefSarsa(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, 
			int resolution, int nTilings, boolean indepdentTiles, 
			double defaultQ, double learningRate, double lambda,
			int numPlanningSteps){
		
		
		this.plannerInit(domain, rf, tf, discount, null);
		
		BeliefMDPGenerator bdgen = new BeliefMDPGenerator(domain);
		this.beliefMDP = (SADomain)bdgen.generateDomain();
		this.beliefRF = new BeliefMDPGenerator.BeliefRF(domain, rf);
		
		int nStates = domain.getStateEnumerator().numStatesEnumerated();
		double width = 1./(double)resolution;
		double [] widths = this.getConstantArray(nStates, width);
		int totalTilings = nTilings;
		
		FVCMACFeatureDatabase cmac = new FVCMACFeatureDatabase(new BeliefStateToFeatureVector());
		if(indepdentTiles){
			totalTilings = nTilings * nStates;
			for(int i = 0; i < nStates; i++){
				boolean [] mask = this.getMaskForDim(i, nStates);
				cmac.addTilingsForDimensionsAndWidths(mask, widths, nTilings, TilingArrangement.RANDOMJITTER);
			}
		}
		else{
			cmac.addTilingsForAllDimensionsWithWidths(widths, nTilings, TilingArrangement.RANDOMJITTER);
		}
		
		ValueFunctionApproximation vfa = cmac.generateVFA(defaultQ/(double)totalTilings);
		
		this.agent = new GradientDescentSarsaLam(beliefMDP, beliefRF, tf, discount, vfa, learningRate, lambda);
		this.numPlanningSteps = numPlanningSteps;

		
	}
	
	@Override
	public void planFromState(State initialState){
		
		int stepsRemaining = this.numPlanningSteps;
		while(stepsRemaining > 0){
			EpisodeAnalysis ea = this.agent.runLearningEpisodeFrom(initialState, stepsRemaining);
			stepsRemaining -= ea.numTimeSteps()-1;
		}
		
	}
	
	@Override
	public List<QValue> getQs(State s) {
		return this.agent.getQs(s);
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		return this.agent.getQ(s, a);
	}

	@Override
	public void planFromBeliefStatistic(BeliefStatistic bsInput) {
		BeliefState bs = new BeliefState(bsInput.getDomain(),bsInput);
		State s = BeliefMDPGenerator.getBeliefMDPState(this.beliefMDP, bs);
		this.planFromState(s);
	}

	@Override
	public void resetPlannerResults() {
		this.agent.resetPlannerResults();
	}
	

	protected boolean [] getMaskForDim(int d, int size){
		boolean [] mask = new boolean[size];
		for(int i = 0; i < size; i++){
			if(i != d){
				mask[i] = false;
			}
			else{
				mask[i] = true;
			}
		}
		return mask;
	}
	
	protected double [] getConstantArray(int size, double v){
		double [] a = new double[size];
		for(int i = 0; i < size; i++){
			a[i] = v;
		}
		return a;
	}
	
	public static class BeliefStateToFeatureVector implements StateToFeatureVectorGenerator{

		@Override
		public double[] generateFeatureVectorFrom(State s) {
			return s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getDoubleArrayValue(BeliefMDPGenerator.ATTBELIEF);
		}
		
		
		
	}
	


}
