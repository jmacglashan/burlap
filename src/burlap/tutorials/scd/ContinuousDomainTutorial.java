package burlap.tutorials.scd;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.StateGridder;
import burlap.behavior.singleagent.learning.lspi.LSPI;
import burlap.behavior.singleagent.learning.lspi.SARSCollector;
import burlap.behavior.singleagent.learning.lspi.SARSData;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.cmac.CMACFeatureDatabase;
import burlap.behavior.singleagent.vfa.common.ConcatenatedObjectFeatureVectorGenerator;
import burlap.behavior.singleagent.vfa.fourier.FourierBasis;
import burlap.behavior.singleagent.vfa.rbf.DistanceMetric;
import burlap.behavior.singleagent.vfa.rbf.RBFFeatureDatabase;
import burlap.behavior.singleagent.vfa.rbf.functions.GaussianRBF;
import burlap.behavior.singleagent.vfa.rbf.metrics.EuclideanDistance;
import burlap.domain.singleagent.cartpole.InvertedPendulum;
import burlap.domain.singleagent.cartpole.InvertedPendulumVisualizer;
import burlap.domain.singleagent.lunarlander.LLVisualizer;
import burlap.domain.singleagent.lunarlander.LunarLanderDomain;
import burlap.domain.singleagent.lunarlander.LunarLanderRF;
import burlap.domain.singleagent.lunarlander.LunarLanderTF;
import burlap.domain.singleagent.mountaincar.MCRandomStateGenerator;
import burlap.domain.singleagent.mountaincar.MountainCar;
import burlap.domain.singleagent.mountaincar.MountainCarVisualizer;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.common.VisualActionObserver;
import burlap.oomdp.singleagent.environment.EnvironmentServer;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ContinuousDomainTutorial {

	public static void MCLSPIFB(){

		MountainCar mcGen = new MountainCar();
		Domain domain = mcGen.generateDomain();
		TerminalFunction tf = new MountainCar.ClassicMCTF();
		RewardFunction rf = new GoalBasedRF(tf, 100);

		StateGenerator rStateGen = new MCRandomStateGenerator(domain);
		SARSCollector collector = new SARSCollector.UniformRandomSARSCollector(domain);
		SARSData dataset = collector.collectNInstances(rStateGen, rf, 5000, 20, tf, null);

		ConcatenatedObjectFeatureVectorGenerator featureVectorGenerator = new ConcatenatedObjectFeatureVectorGenerator(true, MountainCar.CLASSAGENT);
		FourierBasis fb = new FourierBasis(featureVectorGenerator, 4);

		LSPI lspi = new LSPI(domain, 0.99, fb, dataset);
		Policy p = lspi.runPolicyIteration(30, 1e-6);

		Visualizer v = MountainCarVisualizer.getVisualizer(mcGen);
		VisualActionObserver vob = new VisualActionObserver(domain, v);
		vob.initGUI();

		SimulatedEnvironment env = new SimulatedEnvironment(domain, rf, tf, MountainCar.getCleanState(domain, mcGen.physParams));
		EnvironmentServer envServ = new EnvironmentServer(env, vob);

		for(int i = 0; i < 5; i++){
			p.evaluateBehavior(envServ);
			envServ.resetEnvironment();
		}

		System.out.println("Finished");


	}

	public static void MCLSPIRBF(){

		MountainCar mcGen = new MountainCar();
		Domain domain = mcGen.generateDomain();
		TerminalFunction tf = new MountainCar.ClassicMCTF();
		RewardFunction rf = new GoalBasedRF(tf, 100);
		State s = MountainCar.getCleanState(domain, mcGen.physParams);

		StateGenerator rStateGen = new MCRandomStateGenerator(domain);
		SARSCollector collector = new SARSCollector.UniformRandomSARSCollector(domain);
		SARSData dataset = collector.collectNInstances(rStateGen, rf, 5000, 20, tf, null);

		RBFFeatureDatabase rbf = new RBFFeatureDatabase(true);
		StateGridder gridder = new StateGridder();
		gridder.gridEntireDomainSpace(domain, 5);
		List<State> griddedStates = gridder.gridInputState(s);
		DistanceMetric metric = new EuclideanDistance(
				new ConcatenatedObjectFeatureVectorGenerator(true, MountainCar.CLASSAGENT));
		for(State g : griddedStates){
			rbf.addRBF(new GaussianRBF(g, metric, .2));
		}

		LSPI lspi = new LSPI(domain, 0.99, rbf, dataset);
		Policy p = lspi.runPolicyIteration(30, 1e-6);

		Visualizer v = MountainCarVisualizer.getVisualizer(mcGen);
		VisualActionObserver vob = new VisualActionObserver(domain, v);
		vob.initGUI();


		SimulatedEnvironment env = new SimulatedEnvironment(domain, rf, tf, s);
		EnvironmentServer envServ = new EnvironmentServer(env, vob);

		for(int i = 0; i < 5; i++){
			p.evaluateBehavior(envServ);
			envServ.resetEnvironment();
		}

		System.out.println("Finished");


	}


	public static void IPSS(){

		InvertedPendulum ip = new InvertedPendulum();
		ip.physParams.actionNoise = 0.;
		Domain domain = ip.generateDomain();
		RewardFunction rf = new InvertedPendulum.InvertedPendulumRewardFunction(Math.PI/8.);
		TerminalFunction tf = new InvertedPendulum.InvertedPendulumTerminalFunction(Math.PI/8.);
		State initialState = InvertedPendulum.getInitialState(domain);

		SparseSampling ss = new SparseSampling(domain, rf, tf, 1, new SimpleHashableStateFactory(), 10 ,1);
		ss.setForgetPreviousPlanResults(true);
		ss.toggleDebugPrinting(false);
		Policy p = new GreedyQPolicy(ss);

		EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf, 500);
		System.out.println("Num steps: " + ea.maxTimeStep());
		Visualizer v = InvertedPendulumVisualizer.getInvertedPendulumVisualizer();
		new EpisodeSequenceVisualizer(v, domain, Arrays.asList(ea));

	}

	public static void LLSARSA(){

		LunarLanderDomain lld = new LunarLanderDomain();
		Domain domain = lld.generateDomain();
		RewardFunction rf = new LunarLanderRF(domain);
		TerminalFunction tf = new LunarLanderTF(domain);

		State s = LunarLanderDomain.getCleanState(domain, 0);
		LunarLanderDomain.setAgent(s, 0., 5., 0.);
		LunarLanderDomain.setPad(s, 75., 95., 0., 10.);

		int nTilings = 5;
		CMACFeatureDatabase cmac = new CMACFeatureDatabase(nTilings,
				CMACFeatureDatabase.TilingArrangement.RANDOMJITTER);
		double resolution = 10.;

		double angleWidth = 2 * lld.getAngmax() / resolution;
		double xWidth = (lld.getXmax() - lld.getXmin()) / resolution;
		double yWidth = (lld.getYmax() - lld.getYmin()) / resolution;
		double velocityWidth = 2 * lld.getVmax() / resolution;

		cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS,
				domain.getAttribute(LunarLanderDomain.AATTNAME),
				angleWidth);
		cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS,
				domain.getAttribute(LunarLanderDomain.XATTNAME),
				xWidth);
		cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS,
				domain.getAttribute(LunarLanderDomain.YATTNAME),
				yWidth);
		cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS,
				domain.getAttribute(LunarLanderDomain.VXATTNAME),
				velocityWidth);
		cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS,
				domain.getAttribute(LunarLanderDomain.VYATTNAME),
				velocityWidth);


		double defaultQ = 0.5;
		ValueFunctionApproximation vfa = cmac.generateVFA(defaultQ/nTilings);
		GradientDescentSarsaLam agent = new GradientDescentSarsaLam(domain, 0.99, vfa, 0.02, 0.5);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, rf, tf, s);
		List<EpisodeAnalysis> episodes = new ArrayList<EpisodeAnalysis>();
		for(int i = 0; i < 5000; i++){
			EpisodeAnalysis ea = agent.runLearningEpisode(env);
			episodes.add(ea);
			System.out.println(i + ": " + ea.maxTimeStep());
			env.resetEnvironment();
		}

		Visualizer v = LLVisualizer.getVisualizer(lld.getPhysParams());
		new EpisodeSequenceVisualizer(v, domain, episodes);

	}


	public static void main(String[] args) {
		//MCLSPIFB();
		//MCLSPIRBF();
		//IPSS();
		LLSARSA();
	}

}
