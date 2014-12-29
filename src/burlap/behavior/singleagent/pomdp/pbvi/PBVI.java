package burlap.behavior.singleagent.pomdp.pbvi;





import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.pomdp.POMDPPlanner;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
//import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.BeliefStatistic;
import burlap.oomdp.singleagent.pomdp.PODomain;
import burlap.oomdp.singleagent.pomdp.BeliefState.StateBelief;
import burlap.debugtools.RandomFactory;


/** PBVI implementation from - [1] -  Point-based value iteration: An anytime algorithm for POMDPs
Joelle Pineau, Geoff Gordon and Sebastian Thrun 
IJCAI 2003 - http://www.cs.cmu.edu/~ggordon/jpineau-ggordon-thrun.ijcai03.pdf
@author ngopalan  & ibaker
 */


public class PBVI extends POMDPPlanner implements QComputablePlanner{
	/**
	 * horizon is the number of steps that the agent must take to reach the goal, or the length of the problem
	 */
	private int horizon = 90;
	private int maxIterations = 100;
//	private int granularity= 4;// this needs to come back may be
	private boolean VIDone = false;
	private boolean test = false;
	private boolean beliefExpansion = true;
	private boolean givenBeliefStates = false;


	private List<State> states = new ArrayList<State>();
	private List<GroundedAction> groundedActions = new ArrayList<GroundedAction>();
	private List<State> observations = new ArrayList<State>();
	private List<ArrayList<Double>> belief_points = new ArrayList<ArrayList<Double>>();
	private List<double[]> alphaVectors = new ArrayList<double[]>();
	private List<GroundedAction> alphaVectorActions = new ArrayList<GroundedAction>();

	private int numStates;
	private int numGroundedActions;
	private int numObservations;
	private int numBeliefPoints;

	private Random rand = RandomFactory.getMapped(0);

	private double [][] vectorSetReward = null;
	private double [][][][] vectorSetActionOb = null;
	private double [][][] vectorSetActionBelief = null;

	private double [][][] transitionProbabilityMatrix = null;//sas'
	private double [][][] observationProbabilityMatrix = null;//osa


	private List<State> initialStateList = new ArrayList<State>();




	public PBVI(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, int inputHorizon, int maxIterations){
		this.domain = domain;
		this.horizon=inputHorizon;
		this.maxIterations=maxIterations;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
		initPBVI();

	}
	
	public PBVI(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, int inputHorizon, int maxIterations, int granularity){
		this.domain = domain;
		this.horizon=inputHorizon;
		this.belief_points = PBVI.makeBeliefPoints(domain.getStateEnumerator().numStatesEnumerated(), granularity);
		this.givenBeliefStates = true;
		this.maxIterations=maxIterations;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
		initPBVI();

	}




	public PBVI(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, int inputHorizon, int maxIterations, List<BeliefState> beliefStateList){
		this.domain = domain;
		this.horizon=inputHorizon;
		this.maxIterations=maxIterations;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
		this.givenBeliefStates = true;
		for(BeliefState bs: beliefStateList){
			ArrayList<Double> beliefVector = new ArrayList<Double>();
			for(double d:bs.getBeliefVector()){
				beliefVector.add(d);
			}
			this.belief_points.add(beliefVector);
		}
		initPBVI();	
	}

	public PBVI(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, int inputHorizon, List<State> initialStateList, int maxIterations){
		this.domain = domain;
		this.horizon=inputHorizon;
		this.maxIterations=maxIterations;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
		this.givenBeliefStates = true;
		this.initialStateList.addAll(initialStateList);
		initPBVI();	
	}


	public PBVI(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, double epsilon, double Rmin, double Rmax, int maxIterations){
		this.domain = domain;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
		this.horizon=calculateHorizon(epsilon, Rmin, Rmax);
		this.maxIterations=maxIterations;
		initPBVI();
	}




	public PBVI(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, double epsilon, double Rmin, double Rmax, int maxIterations, List<BeliefState> beliefStateList){
		this.domain = domain;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
		this.givenBeliefStates  = true;
		for(BeliefState bs: beliefStateList){
			ArrayList<Double> beliefVector = convertToJavaList(bs.getBeliefVector()); 
			this.belief_points.add(beliefVector);
		}
		this.horizon=calculateHorizon(epsilon, Rmin, Rmax);
		this.maxIterations=maxIterations;
		initPBVI();	
	}
	
	
	public PBVI(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, double epsilon, double Rmin, double Rmax, int maxIterations, int granularity){
		this.domain = domain;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
		this.givenBeliefStates  = true;
		this.belief_points = PBVI.makeBeliefPoints(domain.getStateEnumerator().numStatesEnumerated(), granularity);
		this.horizon=calculateHorizon(epsilon, Rmin, Rmax);
		this.maxIterations=maxIterations;
		initPBVI();	
	}

	public PBVI(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, double epsilon, double Rmin, double Rmax, List<State> initialStateList, int maxIterations){
		this.domain = domain;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
		this.givenBeliefStates  =true;
		this.initialStateList.addAll(initialStateList);
		this.horizon=calculateHorizon(epsilon, Rmin, Rmax);
		this.maxIterations=maxIterations;
		initPBVI();	
	}


	private int calculateHorizon(double epsilon, double Rmin, double Rmax){
		return (int)Math.ceil(Math.log(epsilon/(Rmax-Rmin))/Math.log(this.gamma));
	}


	private void initPBVI(){
		StateEnumerator senum = ((PODomain)this.domain).getStateEnumerator();
		//caching states  there might be problems with this!!
		for(int i=0;i<senum.numStatesEnumerated();i++){
//			System.out.println("state index: " + i + " States: "+ senum.getStateForEnumertionId(i).toString() + " size of states " + this.states.size());
			this.states.add(senum.getStateForEnumertionId(i));
		}

		// caching grounded actions 

		Set<GroundedAction> gaSet = new HashSet<GroundedAction>();
		for(Action a: this.actions){
			for(State s:this.states){
				gaSet.addAll(a.getAllApplicableGroundedActions(s));
			}
		}
		this.groundedActions.addAll(gaSet);
		this.observations = ((PODomain)this.domain).getObservationFunction().getAllPossibleObservations();

		this.numGroundedActions = this.groundedActions.size();
		this.numStates=this.states.size();
		this.numObservations=this.observations.size();


		// filling up the transition , reward and observation matrices!


		this.vectorSetReward = new double[this.numStates][this.numGroundedActions];
		this.transitionProbabilityMatrix = new double[this.numStates][this.numGroundedActions][this.numStates]; 
		this.observationProbabilityMatrix = new double[this.numObservations][this.numStates][this.numGroundedActions];

		for(int stateIndex = 0; stateIndex < this.numStates; ++stateIndex) {
			for(int gaIndex = 0; gaIndex < this.numGroundedActions; ++gaIndex) {
				List<TransitionProbability> tprobs = this.groundedActions.get(gaIndex).action.getTransitions(this.states.get(stateIndex), this.groundedActions.get(gaIndex).params);
				for(int nextStateIndex = 0;nextStateIndex< this.numStates; nextStateIndex++){
					double prob = 0.0;
					for(TransitionProbability tp :tprobs){
						if(tp.s.equals(this.states.get(nextStateIndex))){
							prob=tp.p;
						}

					}
					this.transitionProbabilityMatrix[stateIndex][gaIndex][nextStateIndex]=prob;

					for(int observationIndex=0;observationIndex < this.numObservations; observationIndex++){
						this.observationProbabilityMatrix[observationIndex][nextStateIndex][gaIndex]=((PODomain)this.domain).getObservationFunction().getObservationProbability(this.observations.get(observationIndex), this.states.get(nextStateIndex), this.groundedActions.get(gaIndex));
					} 

				}

				this.vectorSetReward[stateIndex][gaIndex] = this.rf.reward(states.get(stateIndex), this.groundedActions.get(gaIndex), null);// eq. 8 from the paper



			}

		}

		// initializing the belief set if not initialized

		//choosing an initial sample set of twice the total number of states
		if(!this.givenBeliefStates){
		if (this.initialStateList.size()==0){
			// if no initial states were given uniform distribution on sampled belief
			for(int i=0;i<100*this.numStates;i++){
				ArrayList<Double> tempBeliefVector = new ArrayList<Double>();
				for(int j=0;j<this.numStates;j++){
					tempBeliefVector.add(rand.nextDouble()); 
				}
				normalizeVector(tempBeliefVector);
				this.belief_points.add(tempBeliefVector);
			}
		}
		else{

			// if there were initial states set them to 1.0 rest to 0.0, also have a uniform belief over all start states possible

			List<Double> zeroVector=new ArrayList<Double>();
			for(int j=0;j<this.numStates;j++){
				zeroVector.add(0.0); 
			}

			ArrayList<Double> tempUniformVector=new ArrayList<Double>(zeroVector);
			for(int i=0;i<this.initialStateList.size();i++){
				ArrayList<Double> tempZeroVector=new ArrayList<Double>(zeroVector);
				for(State s:this.states){
					if(s.equals(this.initialStateList.get(i))){
						tempZeroVector.set(this.states.indexOf(s),1.0);
						tempUniformVector.set(this.states.indexOf(s),1.0/this.initialStateList.size());
						break;
					}
				}
				this.belief_points.add(tempZeroVector);
			}
			this.belief_points.add(tempUniformVector);

			if(this.belief_points.size()<2*this.numStates){
				for(int i=this.belief_points.size();i<2*this.numStates;i++){
					ArrayList<Double> tempBeliefVector = new ArrayList<Double>();
					for(int j=0;j<this.numStates;j++){
						tempBeliefVector.add(rand.nextDouble()); 
					}
					normalizeVector(tempBeliefVector);
					this.belief_points.add(tempBeliefVector);
				}
			}
		}
		}

		// all matrices initialized looking for belief state to plan from



	}

	@Override
	public List<QValue> getQs(State s) {
		// s is a beliefMDP state return a list of all actions and max alpha vectors for this belief
		
		List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.domain.getActions(), ((PODomain)this.domain).getStateEnumerator().getStateForEnumertionId(0));
		List<QValue> result = new ArrayList<QValue>(gas.size());
		BeliefState bs = new BeliefState((PODomain)this.domain);
		bs.setBeliefCollection(s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getDoubleArrayValue(BeliefMDPGenerator.ATTBELIEF));
//		List<StateBelief> beliefs = bs.getStatesAndBeliefsWithNonZeroProbability();
		
//		int numberStates = bs.getDomain().getStateEnumerator().numStatesEnumerated();
//		double[] dList = bs.getBeliefVector();
//		String str="";
//		for(double d : dList){
//			str = str +  d + ", ";
//		}
//		System.out.println("Beliefs: " +str);
		
		for(GroundedAction ga : gas){
			double q = this.qForBelief(bs, ga);
			QValue Q = new QValue(s, ga, q);
			result.add(Q);
//			System.out.println("Action:" +ga.actionName() + "qvalue: " + q);
		}
		return result;
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		BeliefState bs = new BeliefState((PODomain)this.domain);
		bs.setBeliefCollection(s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getDoubleArrayValue(BeliefMDPGenerator.ATTBELIEF));
		QValue q = new QValue(s, a, this.qForBelief(bs, (GroundedAction)a));
		
		return q;
	}
	
	

	/**
	 * returns the maximum alpha vector product of a belief state
	 * @param bs
	 * @param a
	 * @return
	 */
	private double qForBelief(BeliefState bs, GroundedAction a) {
		double qv = Double.NEGATIVE_INFINITY;
		for(int i =0;i<this.alphaVectorActions.size();i++){
			if(a.equals(this.alphaVectorActions.get(i))){
				double tempQValue=vectorProduct(bs.getBeliefVector(),this.alphaVectors.get(i));
				if(qv < tempQValue){
					qv = tempQValue;
				}
			}
		}
		return qv;
	}




	/**
	 * vector product of two vector of doubles
	 * @param vec1
	 * @param vec2
	 * @return
	 */
	private double vectorProduct(double[] vec1, double[] vec2) {
		double sum = 0.0;
		if(vec1.length != vec2.length){
			System.err.println("PBVI: vectorProduct: vectors not of same size");
			System.exit(-1);
		}
		for(int i = 0; i<vec1.length;i++){
			sum+=vec1[i]*vec2[i];
		}
		return sum;
	}




	@Override
	public void planFromBeliefStatistic(BeliefStatistic bsInput) {
		// this is where the planning will actually happen we will make an any time planner here!
		BeliefState bs = new BeliefState(bsInput.getDomain(),bsInput);
		
		
		

		ArrayList<Double> inputBeliefState = convertToJavaList(bs.getBeliefVector());
		boolean insertVector = true;
		for(List<Double> beliefVector:this.belief_points){
			if(vectorsEqual(beliefVector, inputBeliefState)){
				insertVector = false;
				break;
			}
		}
		if(insertVector){
			this.belief_points.add(inputBeliefState);
		}

		this.numBeliefPoints  = this.belief_points.size();

		// have all the belief vectors, observation probabilities, transitions and reward vectors saved initialization compelete
		if(this.test){
			System.out.println("PointBasedValueIteration: test");
			for(int stateIndex = 0; stateIndex < this.numStates; ++stateIndex) {
				System.out.println("Start state " + states.get(stateIndex).toString());
				for(int gaIndex = 0; gaIndex < this.numGroundedActions; ++gaIndex) {
					System.out.println("Action " + this.groundedActions.get(gaIndex).toString());
					if(this.groundedActions.get(gaIndex).params.length>0){
						System.out.println(this.groundedActions.get(gaIndex).params[0]);
					}
					List<TransitionProbability> tprobs = this.groundedActions.get(gaIndex).action.getTransitions(states.get(stateIndex), new String[]{""});
					for(TransitionProbability prob : tprobs)
						System.out.println("State " + prob.s.toString() + " probability: " + prob.p);


					for(int observationIndex=0;observationIndex < this.numObservations; observationIndex++){
						System.out.println("Observation: " + this.observations.get(observationIndex).toString() + " " + this.observationProbabilityMatrix[observationIndex][stateIndex][gaIndex]);
						//					this.observations.get(observationIndex).getProbability(states.get(stateIndex), actions.get(gaIndex)));
					}

					System.out.println("reward: " +this.rf.reward(states.get(stateIndex), this.groundedActions.get(gaIndex), null));
				}
			}
			System.out.println("PointBasedValueIteration: test end");
		}


		// intializing alpha vectors

//		List<GroundedAction> alphaVectorActions = new ArrayList<GroundedAction>();
//		List<double[]> alphaVectors = new ArrayList<double[]>();
		for(int k = 0; k < this.numBeliefPoints; ++k) {
			this.alphaVectors.add(null);
			this.alphaVectorActions.add(null);
		}


		for (int mainCount = 0; mainCount< this.maxIterations; mainCount++){
			System.out.println("iteration number" + mainCount);
			this.numBeliefPoints = this.belief_points.size();

			if(mainCount != 0) vectorSetActionOb = new double[this.numGroundedActions][this.numObservations][this.alphaVectors.size()][this.numStates];
			vectorSetActionBelief = new double[this.numGroundedActions][this.numBeliefPoints][this.numStates];

			for(int stateIndex = 0; stateIndex < this.numStates; ++stateIndex) {
				for(int actionIndex = 0; actionIndex < this.numGroundedActions; ++actionIndex) {
					//					System.out.println(num_actions);
					//					System.out.println(num_states);
					//					System.out.println(stateIndex);
					//					System.out.println(actionIndex);
					//					System.out.println(states.get(stateIndex));
					//					System.out.println(actions.get(actionIndex));
					//					System.out.println(reward_function.getClass().toString());
					//					vectorSetReward[stateIndex][actionIndex] = reward_function.reward(states.get(stateIndex), actions.get(actionIndex), null);// eq. 8 from the paper
					// change this to be in an outer loop this is being calculated repeatedly
					//System.out.println("PointBasedValueIteration: Iteration " + i + ", VSR[" + actionIndex + "][" +stateIndex + "]: " + vectorSetReward[stateIndex][actionIndex]);

					if(mainCount == 0) continue;

					for(int observationIndex = 0; observationIndex < this.numObservations; ++observationIndex) {
						for(int rvIndex = 0; rvIndex < this.alphaVectors.size(); ++rvIndex) {
							double nextStateSum = 0.0;


							for(int sPrimeIndex = 0; sPrimeIndex < this.numStates; ++sPrimeIndex) {
								/*
								double prob = 0.0;

								List<TransitionProbability> tprobs = actions.get(actionIndex).action.getTransitions(states.get(stateIndex), actions.get(actionIndex).params);
								for(TransitionProbability tp : tprobs) {

//									if(this.statesAreEqual(tp.s, states.get(sPrimeIndex))) {
									if(tp.s.equals(states.get(sPrimeIndex))) {
										prob = tp.p;
										// if(actions.get(actionIndex).action.getName().equals(P.ACTION_SX_ADVANCE) && tp.p == 1) {
										// System.out.println("----------");
										// System.out.println("S  = " + states.get(stateIndex).getObject(P.OBJ_HOLDER).getAllRelationalTargets(P.ATTR_MENTAL_STATE).toArray()[0]);
										// System.out.println("A  = " + actions.get(actionIndex).action.getName());
										// System.out.println("S' = " + states.get(sPrimeIndex).getObject(P.OBJ_HOLDER).getAllRelationalTargets(P.ATTR_MENTAL_STATE).toArray()[0]);
										// System.out.println("Probability = " + tp.p); }
//										if (i==1){
//										System.out.println("probability["+stateIndex+"]["+actionIndex+"]["+sPrimeIndex+"]= " + prob);
//										System.out.println(observations.get(observationIndex).toString(actions.get(actionIndex)));
//										//System.out.println("Observation at "+ observations.get(observationIndex).getName() + " is " +observations.get(observationIndex).getProbability(states.get(sPrimeIndex), actions.get(actionIndex)));
//										System.out.println("    " + "Action at index " + actionIndex + " is " + actions.get(actionIndex).action.getName());
//										System.out.println("    " + "State at index " + stateIndex + " is\n" + states.get(stateIndex));
//										}
									}
								}

								nextStateSum += prob * observations.get(observationIndex).getProbability(states.get(sPrimeIndex), actions.get(actionIndex)) * returnVectors.get(rvIndex).getY()[sPrimeIndex];// eq 9
								 */
								
//								System.out.println("size transition prob: " + transitionProbabilityMatrix.length + " observation prob mat size: " + observationProbabilityMatrix.length + " alpha vector size: "+ alphaVectors.size() );
//								System.out.println("indices: sI " + stateIndex+ " aI: " + actionIndex + " sPI: " + sPrimeIndex + "alphaVector index: "+ rvIndex);

								nextStateSum += transitionProbabilityMatrix[stateIndex][actionIndex][sPrimeIndex] * observationProbabilityMatrix[observationIndex][sPrimeIndex][actionIndex] * this.alphaVectors.get(rvIndex)[sPrimeIndex];// eq 9
							}

							vectorSetActionOb[actionIndex][observationIndex][rvIndex][stateIndex] = this.gamma * nextStateSum;// equation 9 outside of the summation
						}
					}
				}
			}

			for(int actionIndex = 0; actionIndex < this.numGroundedActions; ++actionIndex) {
				for(int beliefIndex = 0; beliefIndex < this.numBeliefPoints; ++beliefIndex) {

					double[] sum = new double[this.numStates];

					for(int j = 0; j < this.numStates; ++j) {
						sum[j] = 0;
					}

					if(mainCount == 0) continue;

					double[] productArray = null;
					if(mainCount != 0) productArray = new double[alphaVectors.size()];

					for(int observationIndex = 0; observationIndex < this.numObservations; ++observationIndex) {
						for(int rvIndex = 0; rvIndex < alphaVectors.size(); ++rvIndex) {
							double acc = 0.0;
							// the vector multiplication per return vector and belief point vector
							for(int stateIndex = 0; stateIndex < this.numStates; ++stateIndex) {
								acc += vectorSetActionOb[actionIndex][observationIndex][rvIndex][stateIndex] * belief_points.get(beliefIndex).get(stateIndex);
								//System.out.println("Belief Point output"+beliefIndex  +" " + stateIndex+" |"+ belief_points.get(beliefIndex).get(stateIndex) + "|");
							}

							//System.out.println("RVINDEX " + rvIndex + ", ACC " + acc);
							productArray[rvIndex] = acc;
						}

						double max_value = Double.NEGATIVE_INFINITY;
						int max_index = -1;
						// maximization over return vectors in eq 9

						for(int j = 0; j < alphaVectors.size(); ++j) {
							double test = productArray[j];
							if(test > max_value) {
								max_value = test;
								max_index = j;
							}
						}

						//System.out.println("ObsIndex: " + observationIndex + ", Max Val: " + max_value + ", Max Ind: " + max_index);



						// summation over observations in eq 9 this sum is for a particular action vector, notice no ob index!!
						for(int j = 0; j < this.numStates; ++j) {
							sum[j] += vectorSetActionOb[actionIndex][observationIndex][max_index][j]; // this is sum per obs. for a particular action and belief point!
						}
					}



					// notice observations have been summed out from the previous step as per eq 9
					for(int j = 0; j < this.numStates; ++j) {
						vectorSetActionBelief[actionIndex][beliefIndex][j] = vectorSetReward[j][actionIndex] + sum[j];
					}
				}
			}

			//	System.out.println("Iteration Number " + i);
			for(int beliefIndex = 0; beliefIndex < this.numBeliefPoints; ++beliefIndex) {
				//	System.out.println("   Belief Index: " + beliefIndex);
				double[] productArray = new double[this.numGroundedActions];
				for(int actionIndex = 0; actionIndex < this.numGroundedActions; ++actionIndex) {
					//System.out.println("      Action Index: " + actionIndex);
					double acc = 0.0;
					for(int j = 0; j < this.numStates; ++j) {
						//System.out.println("j: " + j + ", BP[beliefIndex][j]: " + belief_points.get(beliefIndex).get(j));
						acc += vectorSetActionBelief[actionIndex][beliefIndex][j] * belief_points.get(beliefIndex).get(j);
					}
					//System.out.println("      P.A. at that index: " + acc);
					productArray[actionIndex] = acc;
				}

				double max_value = Double.NEGATIVE_INFINITY;
				int max_index = -1;

				for(int j = 0; j < this.numGroundedActions; ++j) {
					double test = productArray[j];
					if(test > max_value) {
						max_value = test;
						max_index = j;
					}
				}
				this.alphaVectors.set(beliefIndex, vectorSetActionBelief[max_index][beliefIndex]);
				this.alphaVectorActions.set(beliefIndex,this.groundedActions.get(max_index));
			}

			if(mainCount>this.horizon && this.beliefExpansion){
				// expansion of belief points and addition to alpha vectors and actions!

				//the belief space will double after this step

				// list of new belief points to add
				List<ArrayList<Double>> addBeliefPoints = new ArrayList<ArrayList<Double>>(); 


				for(List<Double> beliefStateOld:this.belief_points){
					List<ArrayList<Double>> tempBeliefPointList = new ArrayList<ArrayList<Double>>(); 

					int tempStateIndex=-1;
					int tempObsIndex=-1;
					int tempNextStateIndex=-1;


					//stochastically pick an action, sample a state from belief state, get a next state and next pick an observation based on p(o|s,a)

					

					// for loop over actions!
					for(int tempGAIndex  = 0;tempGAIndex <this.groundedActions.size();tempGAIndex ++){
						
						// sampling a state
						double sum = 0;
						double temp = rand.nextDouble();


						for(int i=0;i<beliefStateOld.size();i++){
							sum+=beliefStateOld.get(i);
							if(sum>temp){
								tempStateIndex = i;
								break;
							}
						}

						if(tempStateIndex==-1){
							System.err.println("PBVI: belief state probabilities not summing to 1!");
							System.exit(-1);
						}


						// based on action picking next state
						temp = rand.nextDouble();
						sum = 0.0;

						for(int i=0;i<this.states.size();i++){
							sum+=this.transitionProbabilityMatrix[tempStateIndex][tempGAIndex][i];
							if(sum>temp){
								tempNextStateIndex = i;
								break;
							}
						}

						if(tempNextStateIndex==-1){
							System.err.println("PBVI: transition probabilities not summing to 1!");
							System.exit(-1);
						}

						// based on next state picking an observation
						temp = rand.nextDouble();
						sum = 0.0;
						for(int i=0;i<this.observations.size();i++){
							sum+=this.observationProbabilityMatrix[i][tempNextStateIndex][tempGAIndex];
							if(sum>temp){
								tempObsIndex = i;
								break;
							}
						}

						if(tempNextStateIndex==-1){
							System.err.println("PBVI: observation probabilities not summing to 1!");
							System.exit(-1);
						}


						//updating belief using the o and a, using the same indices as the indices are common across all matrices
						tempBeliefPointList.add(forward(beliefStateOld,tempObsIndex,tempGAIndex));
					}
					
					// adding the farthest belief point from the set of all forwarded belief points
					addBeliefPoints.add(findFarthestBP(tempBeliefPointList));
				}
				// doubling belief points adding them one by one to maintain order
				for(ArrayList<Double> tempBP : addBeliefPoints){
					insertVector = true;
					for(List<Double> beliefVector:this.belief_points){
						if(vectorsEqual(beliefVector, tempBP)){
							insertVector = false;
							break;
						}
					}
					if(insertVector){
						// adding belief point as it is not the same as any preexisting point
						this.belief_points.add(tempBP);
						// finding the best alpha vector in the set present since all other will not do
						int actionIndexToAdd = getBestActionIndex(tempBP);
						this.alphaVectors.add(this.alphaVectors.get(actionIndexToAdd));
						this.alphaVectorActions.add(this.alphaVectorActions.get(actionIndexToAdd));
					}
				}
				
			}

		}
		
//		this.alphaVectors=alphaVectors;
//		this.alphaVectorActions=alphaVectorActions;

	}

	@Override
	public void resetPlannerResults() {
		this.belief_points.clear();
		this.alphaVectors.clear();
	}

	//methods needed
	private void normalizeVector(List<Double> inputVector){
		double sum=0.0;
		for(Double d:inputVector){
			sum+=d;
		}
		if(sum==0.0){
			System.err.println("PBVI: normalizeVector : normalize sum of beliefs was zero");
			for(int i=0;i< inputVector.size();i++){
				inputVector.set(i,1.0/inputVector.size());
			}
		}
		else{
			for(int i=0;i< inputVector.size();i++){
				inputVector.set(i,inputVector.get(i)/sum);
			}
		}

	}

	private boolean vectorsEqual(List<Double> v1, List<Double> v2){
		if(v1.size()==v2.size()){
			for(int i =0; i<v1.size();i++){
				if(v1.get(i)!=v2.get(i)){
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private ArrayList<Double> convertToJavaList(double[] inputList){
		ArrayList<Double> outputList = new ArrayList<Double>();
		for(double d : inputList){
			outputList.add(d);
		}
		return outputList;
	}


	/**
	 * gets new belief points using the POMDP forward belief equation or eq. 2 in [1]
	 * @param prevBeliefState
	 * @param ObservationIndex
	 * @param GAIndex
	 * @param params
	 * @return
	 */
	private ArrayList<Double> forward(List<Double> prevBeliefState, Integer ObservationIndex , Integer GAIndex) {
		ArrayList<Double> newBeliefState = new ArrayList<Double>();

		for(int sPrimeIndex = 0; sPrimeIndex < this.states.size(); sPrimeIndex++) {
			double sum = 0;
			double obsProb = this.observationProbabilityMatrix[ObservationIndex][sPrimeIndex][GAIndex];

			for(int currStateIndex = 0; currStateIndex < this.states.size(); currStateIndex++) {
				double tprob = this.transitionProbabilityMatrix[currStateIndex][GAIndex][sPrimeIndex];
				tprob *= prevBeliefState.get(currStateIndex);
				sum += tprob;
			}

			sum *= obsProb;
			newBeliefState.add(sum);
		}


		//System.out.println("belief state prior to normalization " + newBeliefState);
		normalizeVector(newBeliefState);
		//System.out.println("belief state after normalization " + newBeliefState);
		return newBeliefState;

	}



	/**
	 * find the belief point in a set of belief points that is the farthest to the current set of belief points
	 */

	private ArrayList<Double> findFarthestBP(List<ArrayList<Double>> searchSet){
		ArrayList<Double> returnBP = new ArrayList<Double>();
		double tempMax=Double.NEGATIVE_INFINITY;
		for(List<Double> currentBP : searchSet){
			if(findMaxDist(currentBP) > tempMax){
				returnBP.clear();
				returnBP = new ArrayList<Double>(currentBP);
			}
		}

		if(returnBP.size() == 0){
			System.err.println("PBVI: findFarthestBP : all elemets less far than Negative Infinity!");
			System.exit(-1);
		}

		return returnBP;
	} 



	/**
	 * max distance possible of this belief point to all the belief points in the belief point set, used in expansion
	 * @param bp
	 * @return
	 */
	private double findMaxDist(List<Double> bp){
		double prevBest=Double.NEGATIVE_INFINITY;
		for(List<Double> compareBP : this.belief_points){
			double tempDouble = l1Norm(compareBP,bp);
			if(tempDouble >prevBest){
				prevBest = tempDouble;
			}
		}

		return prevBest;
	}

	/**
	 * argmax finding function for a general vector
	 * @param listDouble
	 * @return
	 */
	private Integer findMaxElement(List<Double> listDouble){
		Double prevBest = Double.NEGATIVE_INFINITY;
		int maxIndex=-1;
		for(int i=0;i< listDouble.size();i++){
			double tempDouble = listDouble.get(i);
			if(tempDouble > prevBest){
				prevBest = tempDouble;
				maxIndex = i;
			}
		}	

		if(maxIndex == -1){
			System.err.println("PBVI: findMaxIndex : all elemets less than negative infinity!");
			System.exit(-1);
		}
		return maxIndex;

	}


	/**
	 * find L1 distance between two vectors
	 * @param vec1
	 * @param vec2
	 * @return
	 */
	private double l1Norm(List<Double> vec1, List<Double> vec2){
		double sum = 0.0;
		if(vec1.size()!=vec2.size()){
			System.err.println("PBVI: l1Norm: length of vectors not same!");
			System.exit(-1);
		}

		for(int i=0;i<vec1.size();i++){
			sum+=Math.abs(vec1.get(i) - vec2.get(i));
		}
		return sum;
	}


	/**
	 * find L2 distance between two vectors
	 * @param vec1
	 * @param vec2
	 * @return
	 */
	private double l2Norm(List<Double> vec1, List<Double> vec2){
		double sum = 0.0;
		if(vec1.size()!=vec2.size()){
			System.err.println("PBVI: l1Norm: length of vectors not same!");
			System.exit(-1);
		}

		for(int i=0;i<vec1.size();i++){
			sum+=(vec1.get(i) - vec2.get(i))*(vec1.get(i) - vec2.get(i));
		}
		return Math.sqrt(sum);
	} 

	
	/**
	 * returns the index of the alpha vector and action that fits best
	 * @param input_belief_point
	 * @return
	 */
	public Integer getBestActionIndex(List<Double> input_belief_point) {
		int maxIndex = -1;
		double maxSum = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < this.alphaVectors.size(); ++i) {
			Double tempSum=0.0;
			for (int j=0; j < this.alphaVectors.get(i).length;++j){
				tempSum+=this.alphaVectors.get(i)[j]*input_belief_point.get(j);
				}
			if(tempSum > maxSum){
				maxSum = tempSum;
				maxIndex = i;
			}
			
		}
					
		
		return maxIndex;
	}
	
	
	
	public static List<ArrayList<Double>> makeBeliefPoints(int num_states, int granularity) {
		System.out.println("PBVI: size of states "+ num_states);
		System.out.println("PBVI: granularity "+ granularity);
		List<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();






		int num = multichoose(num_states, granularity);
		System.out.println("PBVI: num "+ num);
		for(int bIndex = 0; bIndex < num; ++bIndex) {


			ArrayList<Double> temp;
			while(true) {
				temp = new ArrayList<Double>();
				for(int i = 0; i < num_states; ++i) {
					temp.add(0.0);
				}
				for(int sCount = 0; sCount < granularity; ++sCount) {
					int index = (int) (new java.util.Random().nextDouble() * num_states);
					temp.set(index, temp.get(index) + 1/(double)granularity);
				}
				if(!result.contains(temp)) {
					break;
				} else {
					continue;
				}
			}
			listNorm(temp);
			result.add(temp);

		}
		System.out.println("PBVI: size of belief points "+ result.size());
		
		return result;
	}

	public static void listNorm(List<Double> list) {
		double sum = 0.0;
		for(int i = 0; i < list.size(); ++i) {
			sum += list.get(i);
		}
		for(int i = 0; i < list.size(); ++i) {
			list.set(i, list.get(i)/sum);
		}
	}

	public static long factorial(int n) {
		if(n == 0) {
			return 1;
		}
		return n * factorial(n - 1);
	}
	
	public static int multichoose(int n, int k) {
		long temp=1;
		for(int i=n; i<(n+k);i++){
			temp*=i;
		}
		return (int)(temp/(factorial(k)));
	}


	public void testPBVI(){
		this.test  = true;
	}

	public static void main(){

	}

}
