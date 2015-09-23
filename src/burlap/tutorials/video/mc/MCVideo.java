package burlap.tutorials.video.mc;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.lspi.LSPI;
import burlap.behavior.singleagent.learning.lspi.SARSCollector;
import burlap.behavior.singleagent.learning.lspi.SARSData;
import burlap.behavior.singleagent.vfa.common.ConcatenatedObjectFeatureVectorGenerator;
import burlap.behavior.singleagent.vfa.fourier.FourierBasis;
import burlap.domain.singleagent.mountaincar.MCRandomStateGenerator;
import burlap.domain.singleagent.mountaincar.MountainCar;
import burlap.domain.singleagent.mountaincar.MountainCarVisualizer;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.GoalBasedRF;
import burlap.oomdp.singleagent.common.VisualActionObserver;
import burlap.oomdp.singleagent.environment.EnvironmentServer;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.visualizer.Visualizer;

public class MCVideo {

	public static void main(String[] args) {

		MountainCar mcGen = new MountainCar();
		Domain domain = mcGen.generateDomain();
		TerminalFunction tf = new MountainCar.ClassicMCTF();
		RewardFunction rf = new GoalBasedRF(tf, 100);

		StateGenerator rStateGen = new MCRandomStateGenerator(domain);
		SARSCollector collector = new SARSCollector.UniformRandomSARSCollector(domain);
		SARSData dataset = collector.collectNInstances(rStateGen, rf, 5000, 20, tf, null);

		ConcatenatedObjectFeatureVectorGenerator fvGen = new ConcatenatedObjectFeatureVectorGenerator(true,
				MountainCar.CLASSAGENT);
		FourierBasis fb = new FourierBasis(fvGen, 4);

		LSPI lspi = new LSPI(domain, 0.99, fb, dataset);
		Policy p = lspi.runPolicyIteration(30, 1e-6);

		Visualizer v = MountainCarVisualizer.getVisualizer(mcGen);
		VisualActionObserver vob = new VisualActionObserver(domain, v);
		vob.initGUI();

		SimulatedEnvironment env = new SimulatedEnvironment(domain, rf, tf,
				MountainCar.getCleanState(domain, mcGen.physParams));
		EnvironmentServer envServ = new EnvironmentServer(env, vob);

		for(int i = 0; i < 100; i++){
			p.evaluateBehavior(envServ);
			envServ.resetEnvironment();
		}

		System.out.println("Finished");

	}

}
