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
 * An interface for Options.
 * <p>
 * 1. Sutton, Richard S., Doina Precup, and Satinder Singh. "Between MDPs and semi-MDPs: A framework for temporal abstraction 
 * in reinforcement learning." Artificial intelligence 112.1 (1999): 181-211.
 * @author James MacGlashan
 *
 */
public interface Option extends Action{

	boolean inInitiationSet(State s);
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
