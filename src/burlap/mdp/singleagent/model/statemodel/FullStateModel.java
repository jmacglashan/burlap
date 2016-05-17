package burlap.mdp.singleagent.model.statemodel;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public interface FullStateModel extends SampleStateModel{
	List<StateTransitionProb> stateTransitions(State s, Action a);

	class Helper{
		public static List<StateTransitionProb> deterministicTransition(SampleStateModel model, State s, Action a){
			return Arrays.asList(new StateTransitionProb(model.sample(s, a), 1.));
		}

		public static State sampleByEnumeration(FullStateModel model, State s, Action a){

			List<StateTransitionProb> tps = model.stateTransitions(s, a);
			double roll = RandomFactory.getMapped(0).nextDouble();
			double sum = 0;
			for(StateTransitionProb tp : tps){
				sum += tp.p;
				if(roll < sum){
					return tp.s;
				}
			}

			throw new RuntimeException("Transition probabilities did not sum to one, they summed to " + sum);

		}
	}

}
