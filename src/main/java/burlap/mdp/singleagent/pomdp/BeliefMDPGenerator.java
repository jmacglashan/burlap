package burlap.mdp.singleagent.pomdp;

import burlap.datastructures.HashedAggregator;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.mdp.singleagent.pomdp.beliefstate.*;
import burlap.mdp.singleagent.pomdp.observations.DiscreteObservationFunction;
import burlap.statehashing.HashableState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A class for taking an input POMDP (defined by a {@link burlap.mdp.singleagent.pomdp.PODomain} and turning it into
 * a BeliefMDP, which can then be input to any MDP solver to solve the POMDP. This conversion only works if
 * your belief states are {@link TabularBeliefState} instances.
 * <p>
 * For more information on Belief MDPs, see the POMDP wikipedia page: https://en.wikipedia.org/wiki/Partially_observable_Markov_decision_process#Belief_MDP
 *
 */
public class BeliefMDPGenerator implements DomainGenerator {

	/**
	 * The input POMDP domain
	 */
	protected PODomain							podomain;



	/**
	 * Initializes
	 * @param podomain the input POMDP domain that will be turned into a Belief MDP.
	 */
	public BeliefMDPGenerator(PODomain podomain){
		this.podomain = podomain;
	}
	
	
	@Override
	public SADomain generateDomain() {
		
		SADomain domain = new SADomain();


		for(ActionType mdpActionType : this.podomain.getActionTypes()){
			domain.addActionType(mdpActionType);
		}

		domain.setModel(new BeliefModel(podomain));
		
		return domain;
	}



	public static class BeliefModel implements FullModel {


		protected PODomain poDomain;
		protected BeliefUpdate						updater;

		public BeliefModel(PODomain poDomain) {
			this.poDomain = poDomain;
			this.updater = new TabularBeliefUpdate(poDomain);
		}

		@Override
		public List<TransitionProb> transitions(State s, Action a) {

			if(!(s instanceof TabularBeliefState)) {
				throw new RuntimeException("transitions for Belief MDP  must operate on TabularBeliefState instances, but was requested to be operated on a " + s.getClass().getName() + " instance.");
			}

			if(!(this.poDomain.getObservationFunction() instanceof DiscreteObservationFunction)) {
				throw new RuntimeException("BeliefAction cannot return the full BeliefMDP transition dynamics distribution, because" +
						"the POMDP observation function is not a DiscreteObservationFunction instance. Consider sampling" +
						"with the performAction method instead.");
			}

			DiscreteObservationFunction of = (DiscreteObservationFunction) this.poDomain.getObservationFunction();
			FullModel model = (FullModel) poDomain.getModel();

			TabularBeliefState bs = (TabularBeliefState) s;



			//this block will compute the expected reward in sumR
			//and it will compute \sum_s b(s) * T(s' |s, a), held in nbsTemp (each element is for the s' value)
			TabularBeliefState nbsTemp = (TabularBeliefState) bs.copy();
			nbsTemp.zeroOutBeliefVector();
			double sumR = 0.;
			for(EnumerableBeliefState.StateBelief sb : bs.nonZeroBeliefs()) {
				double sumTransR = 0.;
				List<TransitionProb> tps = model.transitions(sb.s, a);
				for(TransitionProb tp : tps) {
					sumTransR += tp.p * tp.eo.r;
					double bstProd = sb.belief * tp.p;
					double oldSum = nbsTemp.belief(tp.eo.op);
					nbsTemp.setBelief(tp.eo.op, bstProd + oldSum);
				}
				sumR += sb.belief * sumTransR;
			}


			HashedAggregator<HashableState> aggregator = new HashedAggregator<HashableState>();
			List<EnumerableBeliefState.StateBelief> nsBeliefs = nbsTemp.nonZeroBeliefs();
			for(State obs : of.allObservations()) {

				TabularBeliefState nbs = (TabularBeliefState) nbsTemp.copy();
				double norm = 0.;

				for(EnumerableBeliefState.StateBelief sb : nsBeliefs) {
					double op = of.probability(obs, sb.s, a);
					double p = op * sb.belief;
					nbs.setBelief(sb.s, p);
					norm += p;
				}

				if(norm != 1 && norm != 0.) {
					for(EnumerableBeliefState.StateBelief sb : nsBeliefs) {
						double oldP = nbs.belief(sb.s);
						double p = oldP / norm;
						nbs.setBelief(sb.s, p);
					}
				}

				if(norm != 0.) {
					aggregator.add(nbs, norm);
				}
			}

			List<TransitionProb> tps = new ArrayList<TransitionProb>(aggregator.size());
			double sumP = 0.;
			for(Map.Entry<HashableState, Double> e : aggregator.entrySet()) {
				State nsb = e.getKey().s();
				double p = e.getValue();
				sumP += p;
				EnvironmentOutcome eo = new EnvironmentOutcome(s, a, nsb, sumR, false);
				TransitionProb tp = new TransitionProb(p, eo);
				tps.add(tp);
			}
			if(Math.abs(1 - sumP) > 1e-15) {
				throw new RuntimeException("Final transition probabilities did not sum to 1, they summed to " + sumP);
			}

			return tps;


		}

		@Override
		public EnvironmentOutcome sample(State s, Action a) {

			FullModel model = (FullModel) poDomain.getModel();

			double sumR = 0.;
			for(EnumerableBeliefState.StateBelief sb : ((EnumerableBeliefState) s).nonZeroBeliefs()) {
				double sumTransR = 0.;
				List<TransitionProb> tps = model.transitions(sb.s, a);
				for(TransitionProb tp : tps) {
					sumTransR += tp.p * tp.eo.r;
				}
				sumR += sb.belief * sumTransR;
			}


			State curS = ((BeliefState) s).sample();
			EnvironmentOutcome hiddenEO = model.sample(curS, a);
			State obs = this.poDomain.obsevationFunction.sample(hiddenEO.op, a);

			BeliefState nbs = this.updater.update((BeliefState)s, obs, a);
			EnvironmentOutcome eo = new EnvironmentOutcome(s, a, nbs, sumR, false);

			return eo;
		}

		@Override
		public boolean terminal(State s) {
			return false;
		}
	}


}
