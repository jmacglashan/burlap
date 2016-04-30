package burlap.behavior.singleagent.interfaces.rlglue;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.*;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SimpleAction;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Observation;

/**
 * A class for generating a BURLAP {@link burlap.oomdp.core.Domain} for an RLGlue {@link org.rlcommunity.rlglue.codec.taskspec.TaskSpec}.
 * The representation consists of up to two objects. One object contains all RLGlue discrete attributes and the other all
 * Real (double) attributes. The domain can only support RLGlue problems that have discrete actions. The created BURLAP
 * {@link burlap.oomdp.singleagent.Action} objects that correspond to the RLGlue actions cannot be applied to states since
 * RLGlue does not provide action transition dynamics; as a consequence a runtime exception will be thrown is the action
 * {@link burlap.oomdp.singleagent.Action#performAction(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)} method is called. Instead,
 * only the {@link burlap.oomdp.singleagent.Action#performInEnvironment(burlap.oomdp.singleagent.environment.Environment, burlap.oomdp.singleagent.GroundedAction)}
 * method may be used to use an action.
 * @author James MacGlashan.
 */
public class RLGlueDomain implements DomainGenerator {


	/**
	 * The object class name for the object that holds the RLGlue discrete attributes
	 */
	public static final String				DISCRETECLASS = "discrete";

	/**
	 * The object class name for the object that holds the RLGlue real-valued (double) attributes
	 */
	public static final String				REALCLASS = "real";

	/**
	 * The base name of a discrete attribute. The ith discrete attribute will be named DISCATTi
	 */
	public static final String				DISCATT = "disc";

	/**
	 * The base name of a real (double) attribute. The ith real attribute will be named REALATTi
	 */
	public static final String				REALATT = "real";


	/**
	 * The {@link org.rlcommunity.rlglue.codec.taskspec.TaskSpec} used to generate the BURLAP {@link burlap.oomdp.core.Domain}
	 */
	protected TaskSpec theTaskSpec;

	public RLGlueDomain(TaskSpec theTaskSpec){
		this.theTaskSpec = theTaskSpec;
	}

	public TaskSpec getTheTaskSpec() {
		return theTaskSpec;
	}

	public void setTheTaskSpec(TaskSpec theTaskSpec) {
		this.theTaskSpec = theTaskSpec;
	}

	@Override
	public Domain generateDomain() {

		Domain domain = new SADomain();


		ObjectClass discObClass = new ObjectClass(domain, DISCRETECLASS);
		for(int i = 0; i < theTaskSpec.getNumDiscreteObsDims(); i++){
			Attribute a = new Attribute(domain, DISCATT+i, Attribute.AttributeType.INT);
			IntRange rng = theTaskSpec.getDiscreteObservationRange(i);
			a.setLims(rng.getMin(), rng.getMax());
			discObClass.addAttribute(a);
		}

		ObjectClass realObClass = new ObjectClass(domain, REALCLASS);
		for(int i = 0; i < theTaskSpec.getNumContinuousObsDims(); i++){
			Attribute a = new Attribute(domain, REALATT+i, Attribute.AttributeType.REAL);
			DoubleRange rng = theTaskSpec.getContinuousObservationRange(i);
			a.setLims(rng.getMin(), rng.getMax());
			realObClass.addAttribute(a);
		}


		if(theTaskSpec.getNumDiscreteActionDims() != 1 || theTaskSpec.getNumContinuousActionDims() > 0){
			throw new RuntimeException("Can only create domains with one discrete action dimension");
		}

		for(int i = 0; i < theTaskSpec.getDiscreteActionRange(0).getRangeSize(); i++){
			new RLGlueActionSpecification(domain, i);
		}

		return domain;
	}


	/**
	 * Creates a BURLAP {@link burlap.oomdp.core.states.State} from a RLGlue {@link org.rlcommunity.rlglue.codec.types.Observation}.
	 * @param domain the domain to which the state {@link burlap.oomdp.core.ObjectClass} instances belong.
	 * @param obsv the RLGlue {@link org.rlcommunity.rlglue.codec.types.Observation}
	 * @return the corresponding BURLAP {@link burlap.oomdp.core.states.State}.
	 */
	public static State stateFromObservation(Domain domain, Observation obsv){

		State s = new MutableState();

		if(obsv.intArray != null && obsv.intArray.length > 0){
			ObjectInstance o = new MutableObjectInstance(domain.getObjectClass(DISCRETECLASS), "discreteVals");
			s.addObject(o);
			for(int i = 0; i < obsv.intArray.length; i++){
				o.setValue(DISCATT+i, obsv.intArray[i]);
			}
		}

		if(obsv.doubleArray != null && obsv.doubleArray.length > 0){
			ObjectInstance o = new MutableObjectInstance(domain.getObjectClass(REALCLASS), "realVals");
			s.addObject(o);
			for(int i = 0; i < obsv.doubleArray.length; i++){
				o.setValue(REALATT+i, obsv.doubleArray[i]);
			}
		}

		return s;
	}


	/**
	 * A BURLAP {@link burlap.oomdp.singleagent.Action} that corresponds to an RLGlue action that is defined by a single int value.
	 */
	public static class RLGlueActionSpecification extends SimpleAction{

		/**
		 * The RLGlue action index
		 */
		protected int ind;

		/**
		 * Initiaizes.
		 * @param domain the BURLAP domain to which the action will belong.
		 * @param ind the RLGlue int identifier of the action
		 */
		public RLGlueActionSpecification(Domain domain, int ind) {
			super(String.valueOf(ind), domain);
			this.ind = ind;
		}

		/**
		 * Returns the RLGlue int identifier of this action
		 * @return the RLGlue int identifier of this action
		 */
		public int getInd() {
			return ind;
		}



		@Override
		protected State performActionHelper(State s, GroundedAction ga) {
			throw new RuntimeException("RLGlue Actions cannot be applied to arbitrary states; they can only be performed in an Environment.");
		}
	}
}
