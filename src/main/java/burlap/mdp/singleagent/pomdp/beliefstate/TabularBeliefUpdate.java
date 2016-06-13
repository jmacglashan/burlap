package burlap.mdp.singleagent.pomdp.beliefstate;

import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.datastructures.HashedAggregator;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.mdp.singleagent.pomdp.PODomain;
import burlap.mdp.singleagent.pomdp.observations.ObservationFunction;

import java.util.List;
import java.util.Map;

/**
 * @author James MacGlashan.
 */
public class TabularBeliefUpdate implements BeliefUpdate{

	protected PODomain domain;
	protected StateEnumerator stateEnumerator;

	public TabularBeliefUpdate(PODomain domain) {
		this.domain = domain;
		this.stateEnumerator = domain.getStateEnumerator();
	}

	public TabularBeliefUpdate(PODomain domain, StateEnumerator stateEnumerator) {
		this.domain = domain;
		this.stateEnumerator = stateEnumerator;
	}

	public PODomain getDomain() {
		return domain;
	}

	public void setDomain(PODomain domain) {
		this.domain = domain;
	}

	public StateEnumerator getStateEnumerator() {
		return stateEnumerator;
	}

	public void setStateEnumerator(StateEnumerator stateEnumerator) {
		this.stateEnumerator = stateEnumerator;
	}

	@Override
	public BeliefState update(BeliefState belief, State observation, Action a) {

		TabularBeliefState b = (TabularBeliefState)belief;

		FullModel model = (FullModel)this.domain.getModel();
		ObservationFunction of = this.domain.getObservationFunction();
		HashedAggregator<Integer> probs = new HashedAggregator<Integer>(0., 2);
		for(Map.Entry<Integer, Double> bs : b.getBeliefValues().entrySet()){
			List<TransitionProb> tps = model.transitions(this.stateEnumerator.getStateForEnumerationId(bs.getKey()), a);
			for(TransitionProb tp : tps){
				double prodProb = tp.p * bs.getValue();
				int nsid = this.stateEnumerator.getEnumeratedID(tp.eo.op);
				probs.add(nsid, prodProb);
			}
		}

		TabularBeliefState nbs = new TabularBeliefState(domain, stateEnumerator);
		double norm = 0.;
		for(Map.Entry<Integer, Double> e : probs.entrySet()){
			State ns = this.stateEnumerator.getStateForEnumerationId(e.getKey());
			double ofp = of.probability(observation, ns, a);
			double nval = ofp*e.getValue();
			nbs.setBelief(e.getKey(), nval);
			norm += nval;
		}

		if(norm == 0){
			throw new RuntimeException("Cannot get updated belief state, because probabilities summed to 0");
		}



		for(Map.Entry<Integer, Double> e : probs.entrySet()){
			double p = nbs.belief(e.getKey()) / norm;
			if(p > 0) {
				nbs.setBelief(e.getKey(), p);
			}
		}

		return nbs;
	}
}
