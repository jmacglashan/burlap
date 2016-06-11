package burlap.behavior.singleagent.options.model;

import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.options.Option;
import burlap.datastructures.HashedAggregator;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * A slightly more memory intensive implementation of {@link BFSNonMarkovOptionModel} that can also
 * compute the transitions of non-Markov options. See the java doc for {@link BFSNonMarkovOptionModel}
 * for more information on how it computes the model and how to increase performance.
 * @author James MacGlashan.
 */
public class BFSNonMarkovOptionModel extends BFSMarkovOptionModel {

	public BFSNonMarkovOptionModel(SampleModel model, double discount, HashableStateFactory hashingFactory) {
		super(model, discount, hashingFactory);
		this.requireMarkov = false;
	}

	@Override
	protected double computeTransitions(State s, Option o, HashedAggregator<HashableState> possibleTerminations, double[] expectedReturn) {

		double sumTermProb = 0.;

		LinkedList<NonMarkovOptionScanNode> openList = new LinkedList<NonMarkovOptionScanNode>();
		NonMarkovOptionScanNode inode = new NonMarkovOptionScanNode(s);
		openList.addLast(inode);

		while(openList.size() > 0 && sumTermProb < this.minProb){

			NonMarkovOptionScanNode src = openList.poll();
			double probTerm = 0.0; //can never terminate in initiation state
			if(src.nSteps > 0){
				probTerm = o.probabilityOfTermination(src.s, src.episode);
			}
			if(this.model.terminal(src.s)){
				probTerm = 1.;
			}

			double probContinue = 1.-probTerm;
			double stackedDiscount = Math.pow(this.discount, src.nSteps);

			//handle possible termination
			if(probTerm > 0.){
				double probOfDiscountedTrajectory = src.probability*stackedDiscount*probTerm;
				possibleTerminations.add(hashingFactory.hashState(src.s), probOfDiscountedTrajectory);
				expectedReturn[0] += src.cumulativeDiscountedReward*src.probability*probTerm;
				sumTermProb += src.probability;
			}

			//handle continuation
			if(probContinue > 0.){

				//handle option policy selection
				List<ActionProb> actionSelction = o.policyDistribution(src.s, src.episode);
				for(ActionProb ap : actionSelction){

					//now get possible outcomes of each action
					List <TransitionProb> transitions = ((FullModel)model).transitions(src.s, ap.ga);
					for(TransitionProb tp : transitions){
						double totalTransP = ap.pSelection * tp.p * probContinue;
						double r = stackedDiscount * tp.eo.r;
						if(tp.eo.terminated){
							srcTerminateStates.add(hashingFactory.hashState(tp.eo.op));
						}

						NonMarkovOptionScanNode next = new NonMarkovOptionScanNode(src, tp.eo.op, totalTransP, r, ap.ga);
						openList.addLast(next);

					}

				}

			}

		}


		return sumTermProb;

	}


	public static class NonMarkovOptionScanNode extends OptionScanNode{

		protected Episode episode;

		public NonMarkovOptionScanNode(State s) {
			super(s);
			episode = new Episode(s);
		}

		public NonMarkovOptionScanNode(NonMarkovOptionScanNode src, State s, double transProb, double discountedR, Action a) {
			super(src, s, transProb, discountedR);
			this.episode = src.episode.copy();
			this.episode.transition(a, s, 0.);
		}
	}
}
