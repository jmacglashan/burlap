package burlap.mdp.singleagent.model;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public interface FullModel extends SampleModel{
	List<TransitionProb> transitions(State s, Action a);

	class Helper{
		public static List<TransitionProb> deterministicTransition(SampleModel model, State s, Action a){
			EnvironmentOutcome eo = model.sampleTransition(s, a);
			return Arrays.asList(new TransitionProb(1., eo));
		}

		public static EnvironmentOutcome sampleByEnumeration(FullModel model, State s, Action a){
			List<TransitionProb> tps = model.transitions(s, a);
			double roll = RandomFactory.getMapped(0).nextDouble();
			double sum = 0;
			for(TransitionProb tp : tps){
				sum += tp.p;
				if(roll < sum){
					return tp.eo;
				}
			}

			throw new RuntimeException("Transition probabilities did not sum to one, they summed to " + sum);
		}
	}

}
