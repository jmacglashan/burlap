package burlap.oomdp.singleagent.pomdp;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;

public class BeliefMDPGenerator implements DomainGenerator {

	public static final String					CLASSBELIEF = "belief";
	public static final String					ATTBELIEF = "belief";
	
	
	protected PODomain							podomain;
	
	
	public BeliefMDPGenerator(PODomain podomain){
		this.podomain = podomain;
	}
	
	
	@Override
	public Domain generateDomain() {
		
		SADomain domain = new SADomain();
		
		//int nStates = podomain.getStateEnumerator().numStatesEnumerated();
		
		Attribute batt = new Attribute(domain, ATTBELIEF, AttributeType.DOUBLEARRAY);
		batt.setLims(0., 1.);
		
		ObjectClass beliefClass = new ObjectClass(domain, CLASSBELIEF);
		beliefClass.addAttribute(batt);
		
		for(Action mdpAction : this.podomain.getActions()){
			new BeliefAction(mdpAction, domain);
		}
		
		return domain;
	}
	
	public static State getBeliefMDPState(SADomain beliefDomain, BeliefState bs){
		return getBeliefMDPState(beliefDomain, bs.getBeliefVector());
	}
	
	public static State getBeliefMDPState(SADomain beliefDomain, double [] beliefStateVector){
		State s = new State();
		ObjectInstance bOb = new ObjectInstance(beliefDomain.getObjectClass(CLASSBELIEF), CLASSBELIEF+"0");
		bOb.setValue(ATTBELIEF, beliefStateVector);
		s.addObject(bOb);
		return s;
	}
	
	public class BeliefAction extends Action{
		
		protected Action mdpAction;
		
		public BeliefAction(Action mdpAction, SADomain domain){
			super(mdpAction.getName(), domain, mdpAction.getParameterClasses(), mdpAction.getParameterOrderGroups());
			this.mdpAction = mdpAction;
		}
		
		@Override
		public boolean applicableInState(State s, String [] params){
			//belief actions must be applicable everywhere
			return true; 
		}
		
		@Override
		public boolean parametersAreObjects(){
			return this.mdpAction.parametersAreObjects();
		}
		
		@Override
		public List<GroundedAction> getAllApplicableGroundedActions(State s){
			State anMDPState = BeliefMDPGenerator.this.podomain.getStateEnumerator().getStateForEnumertionId(0);
			List<GroundedAction> mdpGAs = this.mdpAction.getAllApplicableGroundedActions(anMDPState);
			List<GroundedAction> beliefGAs = new ArrayList<GroundedAction>(mdpGAs.size());
			for(GroundedAction mga : mdpGAs){
				beliefGAs.add(new GroundedAction(this, mga.params));
			}
			return beliefGAs;
		}

		@Override
		protected State performActionHelper(State s, String[] params) {
			
			BeliefState bs = new BeliefState(BeliefMDPGenerator.this.podomain);
			ObjectInstance bObject = s.getFirstObjectOfClass(CLASSBELIEF);
			double [] bVector = bObject.getDoubleArrayValue(ATTBELIEF);
			bs.setBeliefCollection(bVector);
			
			GroundedAction mdpGA = new GroundedAction(this.mdpAction, params);
			
			//sample a current state
			State mdpS = bs.sampleStateFromBelief();
			//sample a next state
			State mdpSP = mdpGA.executeIn(mdpS);
			//sample an observations
			State observation = BeliefMDPGenerator.this.podomain.getObservationFunction().sampleObservation(mdpSP, mdpGA);
			
			//get next belief state
			BeliefState nbs = bs.getUpdatedBeliefState(observation, mdpGA);
			
			//set the returned state object's belief vector to this
			bObject.setValue(ATTBELIEF, nbs.getBeliefVector());
			
			return s;
		}
		
		@Override
		public List<TransitionProbability> getTransitions(State s, String [] params){
			
			//mdp action
			GroundedAction mdpGA = new GroundedAction(this.mdpAction, params);
			
			//belief state
			BeliefState bs = new BeliefState(BeliefMDPGenerator.this.podomain);
			ObjectInstance bObject = s.getFirstObjectOfClass(CLASSBELIEF);
			double [] bVector = bObject.getDoubleArrayValue(ATTBELIEF);
			bs.setBeliefCollection(bVector);
			
			List<State> observations = BeliefMDPGenerator.this.podomain.getObservationFunction().getAllPossibleObservations();
			List<TransitionProbability> tps = new ArrayList<TransitionProbability>(observations.size());
			for(State obseration : observations){
				double p = bs.probObservation(obseration, mdpGA);
				if(p > 0){
					BeliefState nbs = bs.getUpdatedBeliefState(obseration, mdpGA);
					State ns = new State();
					ObjectInstance nbObject = new ObjectInstance(this.domain.getObjectClass(CLASSBELIEF), CLASSBELIEF+"0");
					nbObject.setValue(ATTBELIEF, nbs.getBeliefVector());
					ns.addObject(nbObject);
					
					TransitionProbability tp = new TransitionProbability(ns, p);
					tps.add(tp);
				}
			}
			
			List<TransitionProbability> collapsed = this.collapseTransitionProbabilityDuplicates(tps);
			
			return collapsed;
		}
		
		
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
		
		protected TransitionProbability matchingStateTP(List<TransitionProbability> tps, State s){
			
			for(TransitionProbability tp : tps){
				if(tp.s.equals(s)){
					return tp;
				}
			}
			
			return null;
			
		}
		
	}
	
	
	
	public static class BeliefRF implements RewardFunction{

		protected PODomain			podomain;
		protected RewardFunction	mdpRF;
		protected StateEnumerator	stateEnumerator;
		
		protected boolean 			srcRFIsSAOnly;
		
		public BeliefRF(PODomain podomain, RewardFunction mdpRF){
			this(podomain, mdpRF, false);
		}
		
		public BeliefRF(PODomain podomain, RewardFunction mdpRF, boolean srcRFIsSAOnly){
			this.podomain = podomain;
			this.mdpRF = mdpRF;
			this.stateEnumerator = this.podomain.getStateEnumerator();
			this.srcRFIsSAOnly = srcRFIsSAOnly;
		}
		
		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			
			GroundedAction mdpGA = new GroundedAction(this.podomain.getAction(a.actionName()), a.params);
			
			if(this.srcRFIsSAOnly){
				return this.saOnlyReward(s, mdpGA);
			}
			
			return this.sasReward(s, mdpGA);
			
		}
		
		protected double saOnlyReward(State s, GroundedAction a){
			double [] belief = s.getFirstObjectOfClass(CLASSBELIEF).getDoubleArrayValue(ATTBELIEF);
			
			double sum = 0.;
			for(int i = 0; i < belief.length; i++){
				if(belief[i] > 0.){
					State mdpS = this.stateEnumerator.getStateForEnumertionId(i);
					double r = this.mdpRF.reward(mdpS, a, null);
					sum += belief[i]*r;
				}
			}
			
			return sum;
		}
		
		protected double sasReward(State s, GroundedAction a){
			
			double [] belief = s.getFirstObjectOfClass(CLASSBELIEF).getDoubleArrayValue(ATTBELIEF);
			
			double sum = 0.;
			for(int i = 0; i < belief.length; i++){
				if(belief[i] > 0.){
					State mdpS = this.stateEnumerator.getStateForEnumertionId(i);
					List<TransitionProbability> tps = a.action.getTransitions(mdpS, a.params);
					double sumTransR = 0.;
					for(TransitionProbability tp : tps){
						double r = this.mdpRF.reward(mdpS, a, tp.s);
						double wr = r*tp.p;
						sumTransR += wr;
					}
					sum += belief[i]*sumTransR;
				}
			}
			
			return sum;
		}
		
		
		
	}

}
