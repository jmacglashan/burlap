package burlap.behavior.singleagent.interfaces.rlglue;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.*;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Observation;

/**
 * @author James MacGlashan.
 */
public class RLGlueDomain implements DomainGenerator {

	public static final String				DISCRETECLASS = "discrete";
	public static final String				REALCLASS = "real";

	public static final String				DISCATT = "disc";
	public static final String				REALATT = "real";



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





	public static class RLGlueActionSpecification extends Action{

		/**
		 * The RLGlue action index
		 */
		protected int ind;

		public RLGlueActionSpecification(Domain domain, int ind) {
			super(""+ind, domain, "");
			this.ind = ind;
		}

		public int getInd() {
			return ind;
		}

		@Override
		protected State performActionHelper(State s, String[] params) {
			throw new RuntimeException("RLGlue Actions cannot be applied to arbitrary states; they can only be performed in an Environment.");
		}
	}
}
