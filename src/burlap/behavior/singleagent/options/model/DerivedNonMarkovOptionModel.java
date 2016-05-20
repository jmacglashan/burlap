package burlap.behavior.singleagent.options.model;

import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.options.Option;
import burlap.datastructures.HashedAggregator;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.List;

/**
 * A slightly more memory intensive implementation of {@link DerivedMarkovOptionModel} that can also
 * compute the transitions of non-Markov options. See the java doc for {@link DerivedMarkovOptionModel}
 * for more information.
 * @author James MacGlashan.
 */
public class DerivedNonMarkovOptionModel extends DerivedMarkovOptionModel{

	public DerivedNonMarkovOptionModel(SampleModel model, double discount, HashableStateFactory hashingFactory) {
		super(model, discount, hashingFactory);
		this.requireMarkov = false;
	}

	@Override
	protected void iterateExpectationScan(Option o, ExpectationSearchNode src, double stackedDiscount, HashedAggregator<HashableState> possibleTerminations, double[] expectedReturn) {
		this.iterateExpectationScan(o, src, stackedDiscount, possibleTerminations, expectedReturn, new Episode(src.s));
	}

	protected void iterateExpectationScan(Option o, ExpectationSearchNode src, double stackedDiscount, HashedAggregator<HashableState> possibleTerminations, double[] expectedReturn, Episode history){

		double probTerm = 0.0; //can never terminate in initiation state
		if(src.nSteps > 0){
			probTerm = o.probabilityOfTermination(src.s, history);
		}

		double probContinue = 1.-probTerm;


		//handle possible termination
		if(probTerm > 0.){
			double probOfDiscountedTrajectory = src.probability*stackedDiscount*probTerm;
			possibleTerminations.add(hashingFactory.hashState(src.s), probOfDiscountedTrajectory);
			expectedReturn[0] += src.cumulativeDiscountedReward*src.probability*probTerm;
		}

		//handle continuation
		if(probContinue > 0.){

			//handle option policy selection
			List<ActionProb> actionSelection = o.policyDistribution(src.s, history);
			for(ActionProb ap : actionSelection){

				//now get possible outcomes of each action
				List <TransitionProb> transitions = ((FullModel)model).transitions(src.s, o);
				for(TransitionProb tp : transitions){
					double totalTransP = ap.pSelection * tp.p * probContinue;
					double r = stackedDiscount * tp.eo.r;
					if(tp.eo.terminated){
						srcTerminateStates.add(hashingFactory.hashState(tp.eo.op));
					}

					ExpectationSearchNode next = new ExpectationSearchNode(src, tp.eo.op, totalTransP, r);
					Episode nHistory = history.copy();
					nHistory.recordTransitionTo(ap.ga, tp.eo.op, tp.eo.r);
					if(next.probability > this.expectationSearchCutoffProb && !tp.eo.terminated){
						this.iterateExpectationScan(o, next, stackedDiscount*discount, possibleTerminations, expectedReturn, nHistory);
					}
				}

			}

		}


	}
}
