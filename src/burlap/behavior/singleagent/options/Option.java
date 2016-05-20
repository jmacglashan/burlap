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
 * The policy is defined by the interaction of two methods, the {@link #initiateInState(State)} method, and the {@link #oneStep(State)}
 * method. When the policy of an option is to be queried, the {@link #initiateInState(State)} method
 * will always be called first, after which the {@link #oneStep(State)} method can be sequentially queried. This
 * implementation strategy is used to support non-Markov options whose decision depends on the history
 * of actions and states since initiation. Moreover, the {@link #probabilityOfTermination(State)}
 * is also conditioned on the events since {@link #initiateInState(State)} if the Option is not Markov.
 * If your option is Markov (does not depend on a history), then the
 * {@link #initiateInState(State)} can do nothing and the {@link #oneStep(State)} method can simply
 * return the policy of the option in that state.
 * <p>
 * For the {@link #control(Environment, double)} method, you can implement it using the {@link Helper#control(Option, Environment, double)}
 * which will use the {@link #initiateInState(State)} method and then the {@link #oneStep(State)} and terminate
 * randomly with probability according to the {@link #probabilityOfTermination(State)} method.
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

	/**
	 * Defines the next action selection for the given state, conditioned on the states and actions observed since
	 * the {@link #initiateInState(State)} method was called.
	 * @param s the new current {@link State} in which an action should be selected.
	 * @return
	 */
	Action oneStep(State s);
	List<Policy.ActionProb> oneStepProbabilities(State s);
	double probabilityOfTermination(State s);

	void initiateInState(State s);
	EnvironmentOptionOutcome control(Environment env, double discount);

	boolean markov();


	class Helper{
		public static EnvironmentOptionOutcome control(Option o, Environment env, double discount){
			Random rand = RandomFactory.getMapped(0);
			State initial = env.currentObservation();
			State cur = initial;

			Episode episode = new Episode(cur);
			o.initiateInState(cur);
			double roll;
			double pT;
			int nsteps = 0;
			double r = 0.;
			double cd = 1.;
			do{
				Action a = o.oneStep(cur);
				EnvironmentOutcome eo = env.executeAction(a);
				nsteps++;
				r += cd*eo.r;
				cur = eo.op;
				cd *= discount;


				Policy.AnnotatedAction annotatedAction = new Policy.AnnotatedAction(a, o.toString() + "(" + nsteps + ")");
				episode.recordTransitionTo(annotatedAction, eo.op, r);


				pT = o.probabilityOfTermination(eo.op);
				roll = rand.nextDouble();

			}while(roll > pT && !env.isInTerminalState());

			EnvironmentOptionOutcome eoo = new EnvironmentOptionOutcome(initial, o, cur, r, env.isInTerminalState(), discount, episode);

			return eoo;

		}
	}

}
