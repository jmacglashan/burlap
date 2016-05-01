package burlap.behavior.singleagent.interfaces.rlglue;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.state.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SimpleAction;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.types.Observation;

/**
 * A class for generating a BURLAP {@link burlap.oomdp.core.Domain} for an RLGlue {@link org.rlcommunity.rlglue.codec.taskspec.TaskSpec}.
 * This class also provides a state generator for RLGLue {@link Observation} objects, using the {@link RLGlueState},
 * which wraps the observation and provides the relevant key values for it so normal BURLAP code and interact with it.
 * <p>
 * Since RLGlue is an RL environment, the created actions, which currently are only supported for 1 dimensional discrete actions,
 * do not support performAction, which would require knowledge of the transition dynamics.
 * @author James MacGlashan.
 */
public class RLGlueDomain implements DomainGenerator {


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

		if(theTaskSpec.getNumDiscreteActionDims() != 1 || theTaskSpec.getNumContinuousActionDims() > 0){
			throw new RuntimeException("Can only create domains with one discrete action dimension");
		}

		for(int i = 0; i < theTaskSpec.getDiscreteActionRange(0).getRangeSize(); i++){
			new RLGlueActionSpecification(domain, i);
		}

		return domain;
	}


	/**
	 * Creates a BURLAP {@link State} from a RLGlue {@link org.rlcommunity.rlglue.codec.types.Observation}.
	 * @param obsv the RLGlue {@link org.rlcommunity.rlglue.codec.types.Observation}
	 * @return the corresponding BURLAP {@link State}.
	 */
	public static State stateFromObservation(Observation obsv){

		return new RLGlueState(obsv);
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
