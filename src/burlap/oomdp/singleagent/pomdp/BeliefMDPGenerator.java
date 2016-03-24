package burlap.oomdp.singleagent.pomdp;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.*;
import burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState;
import burlap.oomdp.singleagent.pomdp.beliefstate.EnumerableBeliefState;

import java.util.ArrayList;
import java.util.List;


/**
 * A class for taking an input POMDP (defined by a {@link burlap.oomdp.singleagent.pomdp.PODomain} and turning it into
 * a BeliefMDP, which can then be input to any MDP solver to solve the POMDP. To get the belief MDP reward function,
 * create and instance of the static inner class {@link burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator.BeliefRF}.
 * <p>
 * The generated Belief MDP action getTransitions method, and the BeliefRF require that the input state object classes
 * implement the {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState} and {@link burlap.oomdp.singleagent.pomdp.beliefstate.EnumerableBeliefState}
 * interfaces. The getTransitions method (used by planning algorithms that require the full transition dynamics)
 * also operates by iterating over all possible observations. If your domain has many observations, consider using
 * a sample-based MDP planning algorithm.
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
	public Domain generateDomain() {
		
		SADomain domain = new SADomain();


		
		for(Action mdpAction : this.podomain.getActions()){
			new BeliefAction(podomain, mdpAction, domain);
		}
		
		return domain;
	}


	/**
	 * A Belief MDP action. (transitions between belief states). This requires that the input states on which the action
	 * operates be an instance of {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState} and
	 * {@link burlap.oomdp.singleagent.pomdp.beliefstate.EnumerableBeliefState}.  The {@link #getTransitions(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)} method
	 * (used by planning algorithms that require the full transition dynamics)
	 * operates by iterating over all possible observations.
	 */
	public class BeliefAction extends Action implements FullActionModel{

		/**
		 * The source POMDP action this action will turn into a Belief MDP action.
		 */
		protected Action pomdpAction;

		/**
		 * The source POMDP domain
		 */
		protected PODomain poDomain;


		/**
		 * Initializes
		 * @param poDomain the POMDP {@link burlap.oomdp.singleagent.pomdp.PODomain}
		 * @param pomdpAction the POMDP {@link burlap.oomdp.singleagent.Action} that this {@link burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator.BeliefAction} will wrap.
		 * @param domain the Belief MDP {@link burlap.oomdp.core.Domain} to which this {@link burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator.BeliefAction} will be associated..
		 */
		public BeliefAction(PODomain poDomain, Action pomdpAction, SADomain domain){
			super(pomdpAction.getName(), domain);
			this.pomdpAction = pomdpAction;
			this.poDomain = poDomain;
		}
		
		@Override
		public boolean applicableInState(State s, GroundedAction ga){
			//belief actions must be applicable everywhere
			return true; 
		}

		@Override
		public boolean isParameterized() {
			return pomdpAction.isParameterized();
		}

		@Override
		public GroundedAction getAssociatedGroundedAction() {
			GroundedAction mga = this.pomdpAction.getAssociatedGroundedAction();
			if(mga instanceof AbstractObjectParameterizedGroundedAction){
				return new ObjectParameterizedGroundedBeliefAction(this, mga);
			}
			else{
				return new GroundedBeliefAction(this, mga);
			}

		}

		@Override
		public List<GroundedAction> getAllApplicableGroundedActions(State s){
			State anMDPState = ((BeliefState)s).sampleStateFromBelief();
			List<GroundedAction> mdpGAs = this.pomdpAction.getAllApplicableGroundedActions(anMDPState);
			List<GroundedAction> beliefGAs = new ArrayList<GroundedAction>(mdpGAs.size());
			for(GroundedAction mga : mdpGAs){

				if(mga instanceof AbstractObjectParameterizedGroundedAction){
					beliefGAs.add(new ObjectParameterizedGroundedBeliefAction(this, mga));
				}
				else{
					beliefGAs.add(new GroundedBeliefAction(this, mga));
				}
			}
			return beliefGAs;
		}

		@Override
		protected State performActionHelper(State s, GroundedAction ga) {

			if(!(s instanceof BeliefState)){
				throw new RuntimeException("Belief MDP actions must operate on BeliefState instances, but was requested to be operated on a " + s.getClass().getName() + " instance.");
			}

			BeliefState bs = (BeliefState)s;
			
			GroundedAction mdpGA = ((GroundedBeliefAction)ga).pomdpAction;
			
			//sample a current state
			State mdpS = bs.sampleStateFromBelief();
			//sample a next state
			State mdpSP = mdpGA.executeIn(mdpS);
			//sample an observations
			State observation = BeliefMDPGenerator.this.podomain.getObservationFunction().sampleObservation(mdpSP, mdpGA);
			
			//get next belief state
			BeliefState nbs = bs.getUpdatedBeliefState(observation, mdpGA);

			
			return nbs;
		}

		@Override
		public boolean isPrimitive() {
			return this.pomdpAction.isPrimitive();
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction ga){

			if(!(s instanceof BeliefState) || !(s instanceof EnumerableBeliefState)){
				throw new RuntimeException("getTransitions for Belief MDP actions must operate on EnumerableBeliefState instances, but was requested to be operated on a " + s.getClass().getName() + " instance.");
			}

			if(!this.poDomain.getObservationFunction().canEnumerateObservations()){
				throw new RuntimeException("BeliefAction cannot return the full BeliefMDP transition dynamics distribution, because" +
						"the POMDP observation function does not support observation enumeration. Consider sampling" +
						"with the performAction method instead.");
			}

			BeliefState bs = (BeliefState)s;

			//mdp action
			GroundedAction mdpGA = ((GroundedBeliefAction)ga).pomdpAction;

			
			List<State> observations = BeliefMDPGenerator.this.podomain.getObservationFunction().getAllPossibleObservations();
			List<TransitionProbability> tps = new ArrayList<TransitionProbability>(observations.size());
			for(State observation : observations){
				double p = this.probObservation(bs, observation, mdpGA);
				if(p > 0){
					BeliefState nbs = bs.getUpdatedBeliefState(observation, mdpGA);
					TransitionProbability tp = new TransitionProbability(nbs, p);
					tps.add(tp);
				}
			}
			
			List<TransitionProbability> collapsed = this.collapseTransitionProbabilityDuplicates(tps);
			
			return collapsed;
		}


		/**
		 * Computes and returns the probability of observing an observation in a given BeleifState when a specific action is taken.
		 * @param bs the previous belief state
		 * @param observation the observation that will be observed
		 * @param ga the pomdp action that would be selected in the previous belief state
		 * @return the probability of observing the given observation
		 */
		protected double probObservation(BeliefState bs, State observation, GroundedAction ga){

			ObservationFunction of = this.poDomain.getObservationFunction();
			double sum = 0.;
			List <EnumerableBeliefState.StateBelief> beliefs =((EnumerableBeliefState)bs).getStatesAndBeliefsWithNonZeroProbability();
			for(EnumerableBeliefState.StateBelief sb : beliefs){
				List<TransitionProbability> mdpTps = ga.getTransitions(sb.s);
				for(TransitionProbability tp : mdpTps){
					double op = of.getObservationProbability(observation, tp.s, ga);
					double term = sb.belief * tp.p * op;
					sum += term;
				}
			}

			return sum;

		}

		/**
		 * Finds transitions that go to the same state and collapses them into a single {@link burlap.oomdp.core.TransitionProbability} object
		 * with the sum of their probabilities.
		 * @param tps the {@link java.util.List} of transitions specified by {@link burlap.oomdp.core.TransitionProbability} objects.
		 * @return the collapsed list of {@link burlap.oomdp.core.TransitionProbability} with any duplicate transitions aggregated into a single {@link burlap.oomdp.core.TransitionProbability} object.
		 */
		protected List<TransitionProbability> collapseTransitionProbabilityDuplicates(List<TransitionProbability> tps){
			List<TransitionProbability> collapsed = new ArrayList<TransitionProbability>(tps.size());
			for(TransitionProbability tp : tps){
				TransitionProbability stored = this.matchingStateTP(collapsed, tp.s);
				if(stored == null){
					collapsed.add(tp);
				}
				else{
					stored.p += tp.p;
				}
			}
			return collapsed;
		}


		/**
		 * Finds a transition in the input list of transitions that matches the input state and returns it. If no match is found
		 * then null is returned.
		 * @param tps The input {@link java.util.List} of transitions to search.
		 * @param s the query state for which a matching transition is to be found.
		 * @return the {@link burlap.oomdp.core.TransitionProbability} in tps that matches state s or null if one does not exist.
		 */
		protected TransitionProbability matchingStateTP(List<TransitionProbability> tps, State s){
			
			for(TransitionProbability tp : tps){
				if(tp.s.equals(s)){
					return tp;
				}
			}
			
			return null;
			
		}




		
	}


	/**
	 * A {@link burlap.oomdp.singleagent.GroundedAction} implementation for a Belief MDP that curries the
	 * {@link burlap.oomdp.singleagent.GroundedAction} for the underlying POMDP. The underlying
	 * POMDP. {@link burlap.oomdp.singleagent.GroundedAction} and its parameters is stored in the
	 * {@link #pomdpAction} datamember.
	 */
	public static class GroundedBeliefAction extends GroundedAction{

		public GroundedAction pomdpAction;

		public GroundedBeliefAction(Action action, GroundedAction pomdpAction) {
			super(action);
			this.pomdpAction = pomdpAction;
		}

		@Override
		public void initParamsWithStringRep(String[] params) {
			pomdpAction.initParamsWithStringRep(params);
		}

		@Override
		public String[] getParametersAsString() {
			return pomdpAction.getParametersAsString();
		}

		@Override
		public String toString() {
			return pomdpAction.toString();
		}

		@Override
		public GroundedAction copy() {
			return new GroundedBeliefAction(this.action, (GroundedAction)pomdpAction.copy());
		}
	}


	/**
	 * A {@link burlap.oomdp.singleagent.GroundedAction} implementation for a Belief MDP that curries
	 * an {@link burlap.oomdp.core.AbstractObjectParameterizedGroundedAction} {@link burlap.oomdp.singleagent.GroundedAction} for the underlying POMDP.
	 */
	public static class ObjectParameterizedGroundedBeliefAction extends GroundedBeliefAction implements AbstractObjectParameterizedGroundedAction{


		public ObjectParameterizedGroundedBeliefAction(Action action, GroundedAction pomdpAction) {
			super(action, pomdpAction);
		}


		@Override
		public GroundedAction copy() {
			return new ObjectParameterizedGroundedBeliefAction(this.action, (GroundedAction)pomdpAction.copy());
		}

		@Override
		public String[] getObjectParameters() {
			return ((AbstractObjectParameterizedGroundedAction)pomdpAction).getObjectParameters();
		}

		@Override
		public void setObjectParameters(String[] params) {
			((AbstractObjectParameterizedGroundedAction)this.pomdpAction).setObjectParameters(params);
		}

		@Override
		public boolean actionDomainIsObjectIdentifierIndependent() {
			return ((AbstractObjectParameterizedGroundedAction)this.pomdpAction).actionDomainIsObjectIdentifierIndependent();
		}
	}


	/**
	 * A class for turning a POMDP reward function into a Belief MDP reward function. This class requires that
	 * the input {@link State} objects are classes that implement {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState}
	 * and {@link burlap.oomdp.singleagent.pomdp.beliefstate.EnumerableBeliefState}.
	 * If the POMDP reward function does not depend on the next state, then this can be declared with the srcRFIsNextStateIndependent
	 * flag in the
	 * constructor, which will decrease the computational demands since the next states do not have to be marginalized over.
	 */
	public static class BeliefRF implements RewardFunction{

		/**
		 * The source POMDP domain
		 */
		protected PODomain			podomain;

		/**
		 * The source POMDP reward function to turn into a belief MDP reward function
		 */
		protected RewardFunction 	pomdpRF;

		/**
		 * A boolean flag indicating whether the POMDP reward function is independent of the next state transition.
		 */
		protected boolean 			srcRFIsNextStateIndependent;


		/**
		 * Initializes. Class will take the safe (but more computationally demanding) assumption that the POMDP reward
		 * function depends on the next state transition.
		 * @param podomain the source POMDP domain
		 * @param pomdpRF the source POMDP reward function
		 */
		public BeliefRF(PODomain podomain, RewardFunction pomdpRF){
			this(podomain, pomdpRF, false);
		}


		/**
		 * Initializes.
		 * @param podomain the source POMDP domain
		 * @param pomdpRF the source POMDP reward function
		 * @param srcRFIsNextStateIndependent a boolean flag indicating whether the POMDP reward function is independent of the next state transition.
		 */
		public BeliefRF(PODomain podomain, RewardFunction pomdpRF, boolean srcRFIsNextStateIndependent){
			this.podomain = podomain;
			this.pomdpRF = pomdpRF;
			this.srcRFIsNextStateIndependent = srcRFIsNextStateIndependent;
		}
		
		@Override
		public double reward(State s, GroundedAction a, State sprime) {

			GroundedAction mdpGA = ((GroundedBeliefAction)a).pomdpAction;

			if(this.srcRFIsNextStateIndependent){
				return this.saOnlyReward(s, mdpGA);
			}
			
			return this.sasReward(s, mdpGA);
			
		}


		/**
		 * Returns the belief MDP reward when the POMDP reward function is independent from the next state transition.
		 * @param s the input belief state
		 * @param a the action selected.
		 * @return the belief MDP reward
		 */
		protected double saOnlyReward(State s, GroundedAction a){

			if(!(s instanceof BeliefState) || !(s instanceof EnumerableBeliefState)){
				throw new RuntimeException("Belief MDP reward function must operate on EnumerableBeliefState instances, but was requested to be operated on a " + s.getClass().getName() + " instance.");
			}

			EnumerableBeliefState bs = (EnumerableBeliefState)s;

			List<EnumerableBeliefState.StateBelief> beliefs = bs.getStatesAndBeliefsWithNonZeroProbability();
			double sum = 0.;
			for(EnumerableBeliefState.StateBelief sb : beliefs){
				double r = this.pomdpRF.reward(sb.s, a, null);
				sum += sb.belief*r;
			}
			
			return sum;
		}

		/**
		 * Returns the belief MDP reward when the POMDP reward function is dependent on the next state transition. Requires marginalizing over the possible
		 * next states.
		 * @param s the input belief state
		 * @param a the action selected.
		 * @return the belief MDP reward
		 */
		protected double sasReward(State s, GroundedAction a){

			if(!(s instanceof BeliefState) || !(s instanceof EnumerableBeliefState)){
				throw new RuntimeException("Belief MDP reward function must operate on EnumerableBeliefState instances, but was requested to be operated on a " + s.getClass().getName() + " instance.");
			}

			BeliefState bs = (BeliefState)s;

			List<EnumerableBeliefState.StateBelief> beliefs = ((EnumerableBeliefState)bs).getStatesAndBeliefsWithNonZeroProbability();
			double sum = 0.;
			for(EnumerableBeliefState.StateBelief sb : beliefs){
				List<TransitionProbability> tps = a.getTransitions(sb.s);
				double sumTransR = 0.;
				for(TransitionProbability tp : tps){
					double r = this.pomdpRF.reward(sb.s, a, tp.s);
					double wr = r*tp.p;
					sumTransR += wr;
				}
				sum += sb.belief*sumTransR;
			}
			
			return sum;
		}
		
		
		
	}


}
