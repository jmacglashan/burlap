package burlap.domain.singleagent.tabularized;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;


/**
 * In general, it is suggested algorithms be designed to work with either factored state representations or the BURLAP State Hashing. However,
 * some algorithms may be limited to working with states that have explicit enumerated values and with an entire state space already defined. In particular,
 * if you are interfacing with code external to BURLAP this may be required. This domain generator can take any input domain and turns it
 * into a domain in which states are represented by a single int attribute and are fully enumerated.
 * The state space used must be enumerated before calling the {@link #generateDomain()} method and is performed
 * using a BURLAP state enumerator. In particular, seed states must be passed to this object and it will find all reachable states from the seed state
 * and enumerate them.
 * @author James MacGlashan
 *
 */
public class TabulatedDomainWrapper implements DomainGenerator {

	/**
	 * The single attribute name for identifying states
	 */
	public static final String						ATTSTATE = "state";
	
	/**
	 * The single class name that holds the state attribute
	 */
	public static final String						CLASSSTATE = "state";
	
	
	/**
	 * The input domain that is to be wrapped into a tabularized domain
	 */
	protected Domain								inputDomain;
	
	/**
	 * The output tabularied domain
	 */
	protected Domain								tabDomain;
	
	/**
	 * The state enumerator used for enumerating (or tabulating) all states
	 */
	protected StateEnumerator						enumerator;
	
	
	/**
	 * Constructs.
	 * @param inputDomain the input domain to be wrapped
	 * @param hashingFactory the hashing factory used to enumerate states from the input domain
	 */
	public TabulatedDomainWrapper(Domain inputDomain, HashableStateFactory hashingFactory){
		this.inputDomain = inputDomain;
		this.enumerator = new StateEnumerator(this.inputDomain, hashingFactory);
	}
	
	/**
	 * Enumerates all reachable states from the input state to include in this tabularized domain's state space.
	 * @param from the souce state from which to find and enumerate all reachable states
	 */
	public void addReachableStatesFrom(State from){
		this.enumerator.findReachableStatesAndEnumerate(from);
	}
	
	
	@Override
	public Domain generateDomain() {
		
		this.tabDomain = new SADomain();
		
		Attribute att = new Attribute(this.tabDomain, ATTSTATE, AttributeType.INT);
		att.setLims(0, this.enumerator.numStatesEnumerated()-1);
		
		ObjectClass oc = new ObjectClass(this.tabDomain, CLASSSTATE);
		oc.addAttribute(att);
		
		for(Action srcAction : this.inputDomain.getActions()){
			new ActionWrapper(tabDomain, srcAction);
		}
		
		return tabDomain;
	}
	
	/**
	 * Returns the state id for a state beloning to the input source domain
	 * @param s the source domain state
	 * @return the state id
	 */
	public int getStateId(State s){
		return s.getFirstObjectOfClass(CLASSSTATE).getIntValForAttribute(ATTSTATE);
	}
	
	/**
	 * Returns the source domain state associated with the tabularized state
	 * @param s the tabularized state
	 * @return the source domain state
	 */
	public State getSourceDomainState(State s){
		int id = this.getStateId(s);
		return this.enumerator.getStateForEnumerationId(id);
	}
	
	/**
	 * Returns a tabularized state for a source domain state
	 * @param s the source domain state
	 * @return the tabularized state
	 */
	public State getTabularizedState(State s){
		int id = this.enumerator.getEnumeratedID(s);
		State ts = new MutableState();
		ObjectInstance o = new MutableObjectInstance(this.tabDomain.getObjectClass(CLASSSTATE), "state");
		o.setValue(ATTSTATE, id);
		ts.addObject(o);
		return ts;
	}
	
	
	/**
	 * An action wrapper that coverts a tabularized state into the source domain state, perform the corresponding source domain action on it getting the
	 * resulting source domain state and returns the tabularized version of the resulting source domain state. Also wraps the preconditions and transition
	 * dynamics of the source domain action in similar ways.
	 * @author James MacGlashan
	 *
	 */
	public class ActionWrapper extends Action implements FullActionModel{

		protected Action srcAction;
		
		/**
		 * Constructs
		 * @param domain the tabularized domain
		 * @param action the source domain action to be wrapped
		 */
		public ActionWrapper(Domain domain, Action action){
			super(action.getName(), domain);
			this.srcAction = action;
		}
		
		@Override
		public boolean applicableInState(State s, GroundedAction groundedAction){
			return srcAction.applicableInState(TabulatedDomainWrapper.this.getSourceDomainState(s), groundedAction);
		}
		
		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			
			State srcState = TabulatedDomainWrapper.this.getSourceDomainState(s);
			State srcNextState = srcAction.performAction(srcState, groundedAction);
			State tabState = TabulatedDomainWrapper.this.getTabularizedState(srcNextState);
			
			return tabState;
		}
		
		
		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction){

			if(!(srcAction instanceof FullActionModel)){
				throw new RuntimeException("Cannot return the transitions for " + srcAction.getName() + " because it does not implement FullActionModel");
			}

			State srcState = TabulatedDomainWrapper.this.getSourceDomainState(s);
			List<TransitionProbability> srcTPs = ((FullActionModel)this.srcAction).getTransitions(srcState, groundedAction);
			List<TransitionProbability> tabTPs = new ArrayList<TransitionProbability>(srcTPs.size());
			for(TransitionProbability stp : srcTPs){
				TransitionProbability ttp = new TransitionProbability(TabulatedDomainWrapper.this.getTabularizedState(stp.s), stp.p);
				tabTPs.add(ttp);
			}
			
			return tabTPs;
		}

		@Override
		public boolean isPrimitive() {
			return srcAction.isPrimitive();
		}

		@Override
		public boolean isParameterized() {
			return srcAction.isParameterized();
		}

		@Override
		public GroundedAction getAssociatedGroundedAction() {
			GroundedAction ga = srcAction.getAssociatedGroundedAction();
			ga.action = this;
			return ga;
		}

		@Override
		public List<GroundedAction> getAllApplicableGroundedActions(State s) {
			List<GroundedAction> sourceSet =  super.getAllApplicableGroundedActions(s);
			//adjust pointer to this action wrapper
			for(GroundedAction ga : sourceSet){
				ga.action = this;
			}
			return sourceSet;
		}
	}

}
