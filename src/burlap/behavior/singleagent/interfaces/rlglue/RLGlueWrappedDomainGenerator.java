package burlap.behavior.singleagent.interfaces.rlglue;

import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Observation;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;


/**
 * This class is used to generate a domain object from an RLGlue task specification.
 * @author James MacGlashan
 *
 */
public class RLGlueWrappedDomainGenerator implements DomainGenerator {

	public static final String				DISCRETECLASS = "discrete";
	public static final String				REALCLASS = "real";
	public static final String				TERMCLASS = "terminal";
	
	public static final String				DISCATT = "disc";
	public static final String				REALATT = "real";
	public static final String				TERMATT = "terminal";
	
	
	protected RLGlueAgentShell				aShell;
	protected Domain						domain;
	
	protected boolean						hasDiscAtts;
	protected boolean						hasRealAtts;
	
	
	/**
	 * Constructs the domain.
	 * @param aShell the BURLAP-RLGlue agent interface class which BURLAP actions for this domain should call.
	 * @param theTaskSpec the RLGlue task specification.
	 */
	public RLGlueWrappedDomainGenerator(RLGlueAgentShell aShell, TaskSpec theTaskSpec){
		
		this.aShell = aShell;
		
		this.domain = new SADomain();
		
		ObjectClass termClass = new ObjectClass(this.domain, TERMCLASS);
		Attribute termAtt = new Attribute(this.domain, TERMATT, AttributeType.BOOLEAN);
		termClass.addAttribute(termAtt);
		
		ObjectClass discObClass = new ObjectClass(this.domain, DISCRETECLASS);
		for(int i = 0; i < theTaskSpec.getNumDiscreteObsDims(); i++){
			this.hasDiscAtts = true;
			Attribute a = new Attribute(this.domain, DISCATT+i, AttributeType.INT);
			IntRange rng = theTaskSpec.getDiscreteActionRange(i);
			a.setLims(rng.getMin(), rng.getMax());
			discObClass.addAttribute(a);
		}
		
		ObjectClass realObClass = new ObjectClass(this.domain, REALCLASS);
		for(int i = 0; i < theTaskSpec.getNumContinuousObsDims(); i++){
			this.hasRealAtts = true;
			Attribute a = new Attribute(this.domain, REALATT+i, AttributeType.REAL);
			DoubleRange rng = theTaskSpec.getContinuousActionRange(i);
			a.setLims(rng.getMin(), rng.getMax());
			realObClass.addAttribute(a);
		}
		
		if(theTaskSpec.getNumDiscreteActionDims() != 1 || theTaskSpec.getNumContinuousActionDims() > 0){
			throw new RuntimeException("Can only create domains with one discrete action dimension");
		}
		
		for(int i = 0; i < theTaskSpec.getDiscreteActionRange(0).getRangeSize(); i++){
			new RLGlueActionWrapper(this.domain, i);
		}
		
		
	}
	
	@Override
	public Domain generateDomain() {
		return this.domain;
	}
	
	
	/**
	 * Returns a state object for the domain of this generator that is a result of the RLGlue observation.
	 * @param obsv the RLGlue observation
	 * @return a OO-MDP state represnting the RLGlue observation
	 */
	public State stateFromObservation(Observation obsv){
		State s = new State();
		if(this.hasDiscAtts){
			ObjectInstance o = new ObjectInstance(this.domain.getObjectClass(DISCRETECLASS), "discreteVals");
			s.addObject(o);
			for(int i = 0; i < obsv.intArray.length; i++){
				o.setValue(DISCATT+i, obsv.intArray[i]);
			}
		}
		if(this.hasRealAtts){
			ObjectInstance o = new ObjectInstance(this.domain.getObjectClass(REALCLASS), "realVals");
			s.addObject(o);
			for(int i = 0; i < obsv.doubleArray.length; i++){
				o.setValue(REALATT+i, obsv.doubleArray[i]);
			}
		}
		
		return s;
	}
	
	/**
	 * Returns a terminal state.
	 * This method is necessary because when an episode ends in RLGlue, it does not provide the last state received. However, BURLAP still expects
	 * to receive the ending state, so this method returns a special state that indicates a termainal state.
	 * @return a special terminal state object.
	 */
	public State getTerminalState(){
		State s = new State();
		ObjectInstance o = new ObjectInstance(this.domain.getObjectClass(TERMCLASS), "terminal");
		o.setValue(TERMATT, 1);
		s.addObject(o);
		return s;
	}
	
	
	/**
	 * Returns the corresponding RLGlue action for the given action id.
	 * @param id the action id
	 * @return An RLGlue action for the corresponding aciton id.
	 */
	public org.rlcommunity.rlglue.codec.types.Action getRLGlueAction(int id){
		
		org.rlcommunity.rlglue.codec.types.Action act = new org.rlcommunity.rlglue.codec.types.Action();
		act.intArray = new int[]{id};
		
		return act;
		
	}
	
	
	/**
	 * A BURLAP Action class that has an associated RLGlue action index and will make calls to the BURLAP-RLGlue interface
	 * ({@link RLGlueAgentShell}).
	 * @author James MacGlashan
	 *
	 */
	protected class RLGlueActionWrapper extends Action{

		/**
		 * The RLGlue action index
		 */
		protected int ind;

		/**
		 * Constructs for the given BURLAP domain and RLGlue action index.
		 * @param domain the BURLAP domain
		 * @param ind the RLGLue action index
		 */
		public RLGlueActionWrapper(Domain domain, int ind){
			super(""+ind, domain, "");
			this.ind = ind;
			
		}
		
		
		@Override
		protected State performActionHelper(State s, String[] params) {
			return RLGlueWrappedDomainGenerator.this.aShell.actionCall(this.ind);
		}
		
		
		
	}

}
