package burlap.behavior.singleagent.options;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.List;
import java.util.Random;



/**
 * An interface for Options [1] that extends the {@link Action} interface. Requires additional methods for defining the
 * option, initiation set, termination conditions, its policy, whether the option is Markov, and giving it control
 * int an environment.
 * <p>
 *     .
 * <p>
 * 1. Sutton, Richard S., Doina Precup, and Satinder Singh. "Between MDPs and semi-MDPs: A framework for temporal abstraction 
 * in reinforcement learning." Artificial intelligence 112.1 (1999): 181-211.
 * @author James MacGlashan
 *
 */
public interface Option extends Action{

	/**
	 * Returns true if the input state is in the initiation set of the {@link Option}
	 * @param s the {@link State} to test.
	 * @return true if the state is in the initiation set; false if it is not
	 */
	boolean inInitiationSet(State s);


	Action policy(State s, Episode history);
	List<Policy.ActionProb> policyDistribution(State s, Episode history);

	double probabilityOfTermination(State s, Episode history);

	EnvironmentOptionOutcome control(Environment env, double discount);

	boolean markov();


	class Helper{
		public static EnvironmentOptionOutcome control(Option o, Environment env, double discount){
			Random rand = RandomFactory.getMapped(0);
			State initial = env.currentObservation();
			State cur = initial;

			Episode episode = new Episode(cur);
			Episode history = new Episode(cur);
			double roll;
			double pT;
			int nsteps = 0;
			double r = 0.;
			double cd = 1.;
			do{
				Action a = o.policy(cur, history);
				EnvironmentOutcome eo = env.executeAction(a);
				nsteps++;
				r += cd*eo.r;
				cur = eo.op;
				cd *= discount;


				history.recordTransitionTo(a, eo.op, eo.r);

				Policy.AnnotatedAction  annotatedAction = new Policy.AnnotatedAction(a, o.toString() + "(" + nsteps + ")");
				episode.recordTransitionTo(annotatedAction, eo.op, r);


				pT = o.probabilityOfTermination(eo.op, history);
				roll = rand.nextDouble();

			}while(roll > pT && !env.isInTerminalState());

			EnvironmentOptionOutcome eoo = new EnvironmentOptionOutcome(initial, o, cur, r, env.isInTerminalState(), discount, episode);

			return eoo;

		}
	}

}
