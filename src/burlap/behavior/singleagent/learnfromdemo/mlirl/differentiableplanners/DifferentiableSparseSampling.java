package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.policy.BoltzmannQPolicy;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learnfromdemo.CustomRewardModel;
import burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.diffvinit.DifferentiableVInit;
import burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.diffvinit.VanillaDiffVinit;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.BoltzmannPolicyGradient;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientPlanner;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientTuple;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.datastructures.BoltzmannDistribution;
import burlap.debugtools.DPrint;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.mdp.statehashing.HashableState;
import burlap.mdp.statehashing.HashableStateFactory;

import java.util.*;

/**
 * A Differentiable finite horizon valueFunction that can also use sparse sampling over the transition dynamics when the
 * transition function is very large or infinite. This valueFunction can be used to perform Receding Horizon Inverse
 * Reinforcement Learning [1] with BURLAP's implementation of maximum likelihood inverse reinforcement learning
 * ({@link burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRL}) [2]. Additionally, the value of the leaf
 * nodes of this valueFunction may also be parametrized using a {@link burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.diffvinit.DifferentiableVInit}
 * object and learned with {@link burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRL},
 * enabling a nice separation of shaping features/rewards and the learned (or known) reward function.
 * <p>
 * 1. MacGlashan, J. Littman, M., "Between Imitation and Intention Learning," Proceedings of IJCAI 15, 2015.
 * 2. Babes, M., Marivate, V., Subramanian, K., and Littman, "Apprenticeship learning about multiple intentions." Proceedings of the 28th International Conference on Machine Learning (ICML-11). 2011.
 * @author James MacGlashan.
 */
public class DifferentiableSparseSampling extends MDPSolver implements QGradientPlanner, Planner {

	/**
	 * The height of the tree
	 */
	protected int h;

	/**
	 * The number of transition dynamics samples (for the root if depth-variable C is used)
	 */
	protected int c;

	/**
	 * Whether the number of transition dynamic samples should scale with the depth of the node. Default is false.
	 */
	protected boolean useVariableC = false;

	/**
	 * Whether previous planning results should be forgotten or reused; default is reused (false).
	 */
	protected boolean forgetPreviousPlanResults = false;

	/**
	 * The state value used for leaf nodes; default is zero.
	 */
	protected DifferentiableVInit vinit;

	protected DifferentiableRF rf;


	/**
	 * The tree nodes indexed by state and height.
	 */
	protected Map<SparseSampling.HashedHeightState, DiffStateNode> nodesByHeight;

	/**
	 * The root state node Q-values that have been estimated by previous planning calls.
	 */
	protected Map<HashableState, QAndQGradient> rootLevelQValues;


	/**
	 * The Boltzmann beta parameter that defines the differentiable Bellman equation. The larger the value, the more
	 * deterministic the soft max operator is.
	 */
	protected double boltzBeta;


	/**
	 * The dimensionality of the differentiable reward function
	 */
	protected int rfDim;


	/**
	 * The total number of pseudo-Bellman updates
	 */
	protected int numUpdates = 0;


	/**
	 * Initializes. The model of this planner will automatically be set to a {@link CustomRewardModel} using the provided reward function.
	 * @param domain the problem domain
	 * @param rf the differentiable reward function
	 * @param gamma the discount factor
	 * @param hashingFactory the hashing factory used to compare state equality
	 * @param h the planning horizon
	 * @param c how many samples from the transition dynamics to use. Set to -1 to use the full (unsampled) transition dynamics.
	 * @param boltzBeta the Boltzmann beta parameter for the differentiable Boltzmann (softmax) backup equation. The larger the value the more deterministic, the closer to 1 the softer.
	 */
	public DifferentiableSparseSampling(SADomain domain, DifferentiableRF rf, double gamma, HashableStateFactory hashingFactory, int h, int c, double boltzBeta){
		this.solverInit(domain, gamma, hashingFactory);
		this.h = h;
		this.c = c;
		this.rf = rf;
		this.boltzBeta = boltzBeta;
		this.nodesByHeight = new HashMap<SparseSampling.HashedHeightState, DiffStateNode>();
		this.rootLevelQValues = new HashMap<HashableState, DifferentiableSparseSampling.QAndQGradient>();
		this.rfDim = rf.numParameters();

		this.vinit = new VanillaDiffVinit(new ValueFunctionInitialization.ConstantValueFunctionInitialization(), rf);

		this.model = new CustomRewardModel(domain.getModel(), rf);

		this.debugCode = 6368290;
	}



	/**
	 * Sets whether the number of state transition samples (C) should be variable with respect to the depth of the node. If set
	 * to true, then the samples will be defined using C_i = C_0 * gamma^(2i), where i is the depth of the node from the root, gamma is the discount factor
	 * and C_0 is the normal C value set for this object.
	 * @param useVariableC if true, then depth-variable C will be used; if false, all state nodes use the same number of samples.
	 */
	public void setUseVariableCSize(boolean useVariableC){
		this.useVariableC = useVariableC;
	}


	/**
	 * Sets the number of state transition samples used.
	 * @param c the number of state transition samples used.
	 */
	public void setC(int c){
		this.c = c;
	}

	/**
	 * Sets the height of the tree.
	 * @param h the height of the tree.
	 */
	public void setH(int h){
		this.h = h;
	}

	/**
	 * Returns the number of state transition samples
	 * @return teh number of state transition samples
	 */
	public int getC(){
		return this.c;
	}

	/**
	 * Returns the height of the tree
	 * @return the height of the tree
	 */
	public int getH(){
		return this.h;
	}


	@Override
	public SampleModel getModel() {
		return this.model;
	}

	/**
	 * Sets whether previous planning results should be forgetten or resued in subsequent planning. Forgetting results is more memory efficient, but less
	 * CPU efficient.
	 * @param forgetPreviousPlanResults if true, then previous planning results will be forgotten; if true, they will be remembered and reused in susbequent planning.
	 */
	public void setForgetPreviousPlanResults(boolean forgetPreviousPlanResults){
		this.forgetPreviousPlanResults = forgetPreviousPlanResults;
		if(this.forgetPreviousPlanResults){
			this.nodesByHeight.clear();
		}
	}

	/**
	 * Sets the {@link ValueFunctionInitialization} object to use for settting the value of leaf nodes.
	 * @param vinit the {@link ValueFunctionInitialization} object to use for settting the value of leaf nodes.
	 */
	public void setValueForLeafNodes(ValueFunctionInitialization vinit){
		if(vinit instanceof DifferentiableVInit) {
			this.vinit = (DifferentiableVInit)vinit;
		}
		else{
			this.vinit = new VanillaDiffVinit(vinit, (DifferentiableRF)this.rf);
		}
	}

	/**
	 * Returns the debug code used for logging plan results with {@link burlap.debugtools.DPrint}.
	 * @return the debug code used for logging plan results with {@link burlap.debugtools.DPrint}.
	 */
	public int getDebugCode(){
		return this.debugCode;
	}


	/**
	 * Sets the debug code used for logging plan results with {@link burlap.debugtools.DPrint}.
	 * @param debugCode the debugCode to use.
	 */
	public void setDebugCode(int debugCode){
		this.debugCode = debugCode;
	}

	/**
	 * Returns the total number of state value estimates performed since the {@link #resetSolver()} call.
	 * @return the total number of state value estimates performed since the {@link #resetSolver()} call.
	 */
	public int getNumberOfValueEsitmates(){
		return this.numUpdates;
	}

	@Override
	public void setBoltzmannBetaParameter(double beta) {
		this.boltzBeta = beta;
	}


	@Override
	public List<QValue> getQs(State s) {

		HashableState sh = this.hashingFactory.hashState(s);
		QAndQGradient qvs = this.rootLevelQValues.get(sh);
		if(qvs == null){
			this.planFromState(s);
			qvs = this.rootLevelQValues.get(sh);
		}

		return qvs.qs;
	}

	@Override
	public QValue getQ(State s, Action a) {

		HashableState sh = this.hashingFactory.hashState(s);
		QAndQGradient qvs = this.rootLevelQValues.get(sh);
		if(qvs == null){
			this.planFromState(s);
			qvs = this.rootLevelQValues.get(sh);
		}

		for(QValue qv : qvs.qs){
			if(qv.a.equals(a)){
				return qv;
			}
		}

		return null;
	}

	@Override
	public double value(State s) {
		if(model.terminalState(s)){
			return 0.;
		}
		return QFunction.QFunctionHelper.getOptimalValue(this, s);
	}

	@Override
	public List<QGradientTuple> getAllQGradients(State s) {
		HashableState sh = this.hashingFactory.hashState(s);
		QAndQGradient qvs = this.rootLevelQValues.get(sh);
		if(qvs == null){
			this.planFromState(s);
			qvs = this.rootLevelQValues.get(sh);
		}
		return qvs.qGrads;
	}

	@Override
	public QGradientTuple getQGradient(State s, Action a) {
		HashableState sh = this.hashingFactory.hashState(s);
		QAndQGradient qvs = this.rootLevelQValues.get(sh);
		if(qvs == null){
			this.planFromState(s);
			qvs = this.rootLevelQValues.get(sh);
		}

		for(QGradientTuple qg : qvs.qGrads){
			if(qg.a.equals(a)){
				return qg;
			}
		}

		return null;
	}


	/**
	 * Plans from the input state and returns a {@link burlap.behavior.policy.BoltzmannQPolicy} following the
	 * Boltzmann parameter used for value Botlzmann value backups in this planner.
	 * @param initialState the initial state of the planning problem
	 * @return a {@link burlap.behavior.policy.BoltzmannQPolicy}
	 */
	@Override
	public BoltzmannQPolicy planFromState(State initialState) {

		if(this.forgetPreviousPlanResults){
			this.rootLevelQValues.clear();
		}

		HashableState sh = this.hashingFactory.hashState(initialState);
		if(this.rootLevelQValues.containsKey(sh)){
			return new BoltzmannQPolicy(this, 1./this.boltzBeta); //already planned for this state
		}

		DPrint.cl(this.debugCode, "Beginning Planning.");
		int oldUpdates = this.numUpdates;

		DiffStateNode sn = this.getStateNode(initialState, this.h);
		rootLevelQValues.put(sh, sn.estimateQs());

		DPrint.cl(this.debugCode, "Finished Planning with " + (this.numUpdates - oldUpdates) + " value esitmates; for a cumulative total of: " + this.numUpdates);

		if(this.forgetPreviousPlanResults){
			this.nodesByHeight.clear();
		}

		return new BoltzmannQPolicy(this, 1./this.boltzBeta);

	}

	@Override
	public void resetSolver() {
		this.nodesByHeight.clear();
		this.rootLevelQValues.clear();
		this.numUpdates = 0;
	}



	/**
	 * Returns the value of C for a node at the given height (height from a leaf node).
	 * @param height the height from a leaf node.
	 * @return the value of C to use.
	 */
	protected int getCAtHeight(int height){
		if(!this.useVariableC){
			return c;
		}

		//convert height from bottom to depth from root
		int d = this.h = height;
		int vc = (int) (c * Math.pow(this.gamma, 2*d));
		if(vc == 0){
			vc = 1;
		}
		return vc;
	}

	/**
	 * Either returns, or creates, indexes, and returns, the state node for the given state at the given height in the tree
	 * @param s the state
	 * @param height the height (distance from leaf node) of the node.
	 * @return the state node for the given state at the given height in the tree
	 */
	protected DiffStateNode getStateNode(State s, int height){
		HashableState sh = this.hashingFactory.hashState(s);
		SparseSampling.HashedHeightState hhs = new SparseSampling.HashedHeightState(sh, height);
		DiffStateNode sn = this.nodesByHeight.get(hhs);
		if(sn == null){
			sn = new DiffStateNode(sh, height);
			this.nodesByHeight.put(hhs, sn);
		}

		return sn;
	}



	/**
	 * A class for value differentiable state nodes. Includes the state, a value estimate, whether the node has been closed and methods for estimating the Q and V values.
	 * @author James MacGlashan
	 *
	 */
	public class DiffStateNode{

		/**
		 * The hashed state this node represents
		 */
		HashableState sh;

		/**
		 * The height of the node (distance from a leaf)
		 */
		int height;

		/**
		 * The estimated value of the state at this height
		 */
		double v;

		/**
		 * The gradient of the value function
		 */
		FunctionGradient vgrad;


		/**
		 * Whether this node has been closed.
		 */
		boolean closed = false;


		public DiffStateNode(HashableState sh, int height){
			this.sh = sh;
			this.height = height;
		}


		public QAndQGradient estimateQs(){

			int dim = DifferentiableSparseSampling.this.rfDim;

			List<Action> gas = DifferentiableSparseSampling.this.getAllGroundedActions(this.sh.s);
			QAndQGradient qs = new QAndQGradient(gas.size());

			int c = DifferentiableSparseSampling.this.getCAtHeight(this.height);
			for(Action ga : gas){
				if(this.height == 0 || c == 0){
					qs.add(new QValue(this.sh.s, ga, DifferentiableSparseSampling.this.vinit.value(this.sh.s)),
							new QGradientTuple(
									this.sh.s,
									ga,
									DifferentiableSparseSampling.this.vinit.getQGradient(this.sh.s, ga)));
				}
				else{

					if(c > 0){
						this.sampledBellmanQEstimate(ga, qs);
					}
					else{
						this.fulldBellmanQEstimate(ga, qs);
					}

				}


			}


			return qs;

		}

		public void sampledBellmanQEstimate(Action ga, QAndQGradient qs){

			FunctionGradient qGradient = new FunctionGradient.SparseGradient();

			//generate C samples
			double sum = 0.;
			for(int i = 0; i < c; i++){

				//execute
				EnvironmentOutcome eo = model.sampleTransition(this.sh.s, ga);
				State ns = eo.op;
				double r = eo.r;
				FunctionGradient rGradient = DifferentiableSparseSampling.this.rf.gradient(this.sh.s, ga, ns);

				DiffStateNode nsn = DifferentiableSparseSampling.this.getStateNode(ns, this.height-1);

				VAndVGradient vVals = nsn.estimateV();
				Set<Integer> params = combinedNonZeroPDParameters(vVals.vGrad, rGradient);
				sum += r + DifferentiableSparseSampling.this.gamma*vVals.v;
				for(Integer p : params){
					double curVal = qGradient.getPartialDerivative(p);
					double nextVal = curVal + rGradient.getPartialDerivative(p) + DifferentiableSparseSampling.this.gamma * vVals.vGrad.getPartialDerivative(p);
					qGradient.put(p, nextVal);
				}


			}
			sum /= (double)c;
			for(FunctionGradient.PartialDerivative pd : qGradient.getNonZeroPartialDerivatives()){
				double nextVal = pd.value / (double)c;
				qGradient.put(pd.parameterId, nextVal);
			}


			qs.add(new QValue(this.sh.s, ga, sum), new QGradientTuple(this.sh.s, ga, qGradient));


		}


		public void fulldBellmanQEstimate(Action ga, QAndQGradient qs){

			int dim = DifferentiableSparseSampling.this.rfDim;
			FunctionGradient qGradient = new FunctionGradient.SparseGradient();

			double sum = 0.;
			List<TransitionProb> tps = ((FullModel)model).transitions(sh.s, ga);
			for(TransitionProb tp : tps){

				State ns = tp.eo.op;
				double r = tp.eo.r;
				FunctionGradient rGradient = DifferentiableSparseSampling.this.rf.gradient(this.sh.s, ga, ns);

				DiffStateNode nsn = DifferentiableSparseSampling.this.getStateNode(ns, this.height-1);

				VAndVGradient vVals = nsn.estimateV();
				Set<Integer> params = combinedNonZeroPDParameters(vVals.vGrad, rGradient);
				sum += tp.p * (r + DifferentiableSparseSampling.this.gamma*vVals.v);
				for(Integer p : params){
					double curVal = qGradient.getPartialDerivative(p);
					double nextVal = curVal + tp.p * (rGradient.getPartialDerivative(p) + DifferentiableSparseSampling.this.gamma * vVals.vGrad.getPartialDerivative(p));
					qGradient.put(p, nextVal);
				}

			}

			qs.add(new QValue(this.sh.s, ga, sum), new QGradientTuple(this.sh.s, ga, qGradient));

		}


		public VAndVGradient estimateV(){

			if(this.closed){
				return new VAndVGradient(this.v, this.vgrad);
			}

			if(model.terminalState(this.sh.s)){
				this.v = 0.;
				this.vgrad = new FunctionGradient.SparseGradient();
				this.closed = true;
				return new VAndVGradient(this.v, this.vgrad);
			}

			QAndQGradient qs = this.estimateQs();
			this.setV(qs);
			this.setVGrad(qs);
			this.closed = true;
			DifferentiableSparseSampling.this.numUpdates++;

			return new VAndVGradient(this.v, this.vgrad);
		}


		protected void setV(QAndQGradient qvs){
			double [] qArray = new double[qvs.qs.size()];
			for(int i = 0; i < qvs.qs.size(); i++){
				qArray[i] = qvs.qs.get(i).q;
			}
			BoltzmannDistribution bd = new BoltzmannDistribution(qArray, 1./DifferentiableSparseSampling.this.boltzBeta);
			double [] probs = bd.getProbabilities();
			double sum = 0.;
			for(int i = 0; i < qArray.length; i++){
				sum += qArray[i] * probs[i];
			}
			this.v = sum;
		}

		protected void setVGrad(QAndQGradient qvs){

			this.vgrad = new FunctionGradient.SparseGradient();

			//pack qs into double array
			double [] qs = new double[qvs.qs.size()];
			for(int i = 0; i < qs.length; i++){
				qs[i] = qvs.qs.get(i).q;
			}

			//get all q gradients
			FunctionGradient [] gqs = new FunctionGradient[qs.length];
			for(int i = 0; i < qs.length; i++){
				gqs[i] = qvs.qGrads.get(i).gradient;
			}

			double maxBetaScaled = BoltzmannPolicyGradient.maxBetaScaled(qs, DifferentiableSparseSampling.this.boltzBeta);
			double logSum = BoltzmannPolicyGradient.logSum(qs, maxBetaScaled, DifferentiableSparseSampling.this.boltzBeta);

			for(int i = 0; i < qs.length; i++){

				double probA = Math.exp(DifferentiableSparseSampling.this.boltzBeta * qs[i] - logSum);
				FunctionGradient policyGradient = BoltzmannPolicyGradient.computePolicyGradient(
						DifferentiableSparseSampling.this.boltzBeta, qs, maxBetaScaled, logSum, gqs, i);


				for(FunctionGradient.PartialDerivative pd : policyGradient.getNonZeroPartialDerivatives()){
					double curVal = this.vgrad.getPartialDerivative(pd.parameterId);
					double nextVal = curVal + (probA * gqs[i].getPartialDerivative(pd.parameterId)) + (qs[i] * pd.value);
					vgrad.put(pd.parameterId, nextVal);
				}


			}

		}


	}

	protected Set<Integer> combinedNonZeroPDParameters(FunctionGradient...gradients){

		Set<Integer> c = new HashSet<Integer>();
		for(FunctionGradient g : gradients){
			Set<FunctionGradient.PartialDerivative> p = g.getNonZeroPartialDerivatives();
			for(FunctionGradient.PartialDerivative e : p){
				c.add(e.parameterId);
			}
		}

		return c;
	}


	/**
	 * A tuple for storing Q-values and their gradients.
	 */
	protected static class QAndQGradient{
		List<QValue> qs;
		List<QGradientTuple> qGrads;

		public QAndQGradient(List<QValue> qs, List<QGradientTuple> qGrads){
			this.qs = qs;
			this.qGrads = qGrads;
		}

		public QAndQGradient(int capacity){
			this.qs = new ArrayList<QValue>(capacity);
			this.qGrads = new ArrayList<QGradientTuple>(capacity);
		}

		public void add(QValue q, QGradientTuple qGrad){
			this.qs.add(q);
			this.qGrads.add(qGrad);
		}
	}


	/**
	 * A tuple for storing a state value and its gradient.
	 */
	protected static class VAndVGradient{

		double v;
		FunctionGradient vGrad;

		public VAndVGradient(double v, FunctionGradient vGrad){
			this.v = v;
			this.vGrad = vGrad;
		}

	}


}
