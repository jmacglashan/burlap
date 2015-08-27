package burlap.behavior.singleagent.pomdp.wrappedmdpalgs;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.cmac.CMACFeatureDatabase.TilingArrangement;
import burlap.behavior.singleagent.vfa.cmac.FVCMACFeatureDatabase;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.oomdp.singleagent.pomdp.PODomain;
import burlap.oomdp.singleagent.pomdp.beliefstate.DenseBeliefVector;


/**
 * A POMPD planning algorithm that converts a POMDP into a Belief MDP and then uses {@link burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam} with
 * tile coding to to perform planning. This class requires that the input {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState} implements
 * {@link burlap.oomdp.singleagent.pomdp.beliefstate.DenseBeliefVector} or a runtime exception will be thrown.
 */
public class BeliefSarsa extends MDPSolver implements Planner, QFunction {

	/**
	 * The generated belief MDP to solve
	 */
	protected SADomain							beliefMDP;

	/**
	 * The generated beleif MDP reward function
	 */
	protected RewardFunction					beliefRF;

	/**
	 * A {@link burlap.oomdp.singleagent.environment.SimulatedEnvironment} for the Belief MDP that will be used by the {@link burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam} instance.
	 */
	protected SimulatedEnvironment				simulatedBeliefMDP;

	/**
	 * The SARSA(lambda) solver to run on the belief MDP
	 */
	protected GradientDescentSarsaLam			agent;

	/**
	 * The number of planning steps (i.e., SARSA learning steps) to use.
	 */
	protected int								numPlanningSteps;


	/**
	 * Initializes
	 * @param domain the POMDP domain
	 * @param rf the POMDP reward function
	 * @param discount the discount factor
	 * @param resolution the tile coding resolution
	 * @param nTilings the number of tilings
	 * @param indepdentTiles if true then the Q-value function will be a linear combinatino of the individual belief state values; if false then then each tile spans all dimensions of the belief vector
	 * @param defaultQ the initial Q value for belief states
	 * @param learningRate the SARSA learning rate parameter
	 * @param lambda the SARSA lambda parameter (between 0 and 1)
	 * @param numPlanningSteps the number of planning steps (i.e., SARSA learning steps) to use.
	 */
	public BeliefSarsa(PODomain domain, RewardFunction rf, double discount,
			int resolution, int nTilings, boolean indepdentTiles, 
			double defaultQ, double learningRate, double lambda,
			int numPlanningSteps){
		
		if(!domain.providesStateEnumerator()){
			throw new RuntimeException(("BeliefSarsa cannot be instantiated because the provided domain does not provide a StateEnumerator."));
		}


		this.solverInit(domain, rf, new NullTermination(), discount, null);
		
		BeliefMDPGenerator bdgen = new BeliefMDPGenerator(domain);
		this.beliefMDP = (SADomain)bdgen.generateDomain();
		this.beliefRF = new BeliefMDPGenerator.BeliefRF(domain, rf);

		this.simulatedBeliefMDP = new SimulatedEnvironment(this.beliefMDP, this.beliefRF, new NullTermination());
		
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
		
		this.agent = new GradientDescentSarsaLam(beliefMDP, discount, vfa, learningRate, lambda);
		this.numPlanningSteps = numPlanningSteps;

		
	}
	
	@Override
	public Policy planFromState(State initialState){
		
		int stepsRemaining = this.numPlanningSteps;
		while(stepsRemaining > 0){
			this.simulatedBeliefMDP.setCurStateTo(initialState);
			EpisodeAnalysis ea = this.agent.runLearningEpisode(this.simulatedBeliefMDP, stepsRemaining);
			stepsRemaining -= ea.numTimeSteps()-1;
		}

		return new GreedyQPolicy(this);

	}
	
	@Override
	public List<QValue> getQs(State s) {
		List<QValue> beliefQs =  this.agent.getQs(s);
		List <QValue> pomdpQs = new ArrayList<QValue>(beliefQs.size());
		for(QValue bq : beliefQs){
			pomdpQs.add(new QValue(s, ((BeliefMDPGenerator.GroundedBeliefAction)bq.a).pomdpAction, bq.q));
		}
		return pomdpQs;
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		QValue bq =  this.agent.getQ(s, a);
		return new QValue(s, ((BeliefMDPGenerator.GroundedBeliefAction)bq.a).pomdpAction, bq.q);
	}


	@Override
	public double value(State s) {
		return QFunctionHelper.getOptimalValue(this, s);
	}

	@Override
	public void resetSolver() {
		this.agent.resetSolver();
	}

	/**
	 * returns a belief dimension mask where all values of the mask are set to false except the input dimension which is set to true.
	 * @param d the dimension with a true value
	 * @param size the size of the mask array
	 * @return a belief dimension mask where all values of the mask are set to false except the input dimension which is set to true.
	 */
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

	/**
	 * Returns a double array of the specified size with all values set to v.
	 * @param size the size of the array
	 * @param v the value for all elements
	 * @return a double array of the specified size with all values set to v.
	 */
	protected double [] getConstantArray(int size, double v){
		double [] a = new double[size];
		for(int i = 0; i < size; i++){
			a[i] = v;
		}
		return a;
	}

	/**
	 * Converts a {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState} that implements {@link burlap.oomdp.singleagent.pomdp.beliefstate.DenseBeliefVector}
	 * into a double array representation.
	 */
	public static class BeliefStateToFeatureVector implements StateToFeatureVectorGenerator{

		@Override
		public double[] generateFeatureVectorFrom(State s) {
			if(!(s instanceof DenseBeliefVector)){
				throw new RuntimeException("BeliefStateToFeatureVector cannot turn the input state into a belief vector because the state does not implement DenseBeliefVector. Input state is an instance of " + s.getClass().getName());
			}
			return ((DenseBeliefVector)s).getBeliefVector();
		}
		
		
		
	}
	


}
