package burlap.behavior.singleagent.interfaces.rlglue;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Action;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.SADomain;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.types.Observation;

import java.util.Arrays;
import java.util.List;

/**
 * A class for generating a BURLAP {@link burlap.mdp.core.Domain} for an RLGlue {@link org.rlcommunity.rlglue.codec.taskspec.TaskSpec}.
 * This class also provides a state generator for RLGLue {@link Observation} objects, using the {@link RLGlueState},
 * which wraps the observation and provides the relevant key values for it so normal BURLAP code and interact with it.
 * <p>
 * Since RLGlue is an RL environment, the created actions, which currently are only supported for 1 dimensional discrete actions,
 * do not support performAction, which would require knowledge of the transition dynamics.
 * @author James MacGlashan.
 */
public class RLGlueDomain implements DomainGenerator {


	/**
	 * The {@link org.rlcommunity.rlglue.codec.taskspec.TaskSpec} used to generate the BURLAP {@link burlap.mdp.core.Domain}
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
			new RLGlueActionType(domain, i);
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
	 * A BURLAP {@link ActionType} that corresponds to an RLGlue action that is defined by a single int value.
	 */
	public static class RLGlueActionType implements ActionType {

		/**
		 * The RLGlue action index
		 */
		protected int ind;

		/**
		 * Initiaizes.
		 * @param domain the BURLAP domain to which the action will belong.
		 * @param ind the RLGlue int identifier of the action
		 */
		public RLGlueActionType(Domain domain, int ind) {
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
		public String typeName() {
			return String.valueOf(this.ind);
		}

		@Override
		public Action associatedAction(String strRep) {
			return new RLGLueAction(ind);
		}

		@Override
		public List<Action> allApplicableActions(State s) {
			return Arrays.<Action>asList(new RLGLueAction(ind));
		}


		public static class RLGLueAction implements Action{

			protected int ind;

			public RLGLueAction(int ind) {
				this.ind = ind;
			}

			@Override
			public String actionName() {
				return String.valueOf(ind);
			}

			@Override
			public Action copy() {
				return new RLGLueAction(ind);
			}

			@Override
			public boolean equals(Object o) {
				if(this == o) return true;
				if(o == null || getClass() != o.getClass()) return false;

				RLGLueAction that = (RLGLueAction) o;

				return ind == that.ind;

			}

			@Override
			public int hashCode() {
				return ind;
			}

			@Override
			public String toString() {
				return actionName();
			}
		}

	}
}
