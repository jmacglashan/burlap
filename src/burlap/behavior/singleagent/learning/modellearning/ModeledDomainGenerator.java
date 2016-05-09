package burlap.behavior.singleagent.learning.modellearning;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.singleagent.Action;
import burlap.mdp.singleagent.FullActionModel;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.SADomain;

import java.util.List;



/**
 * Use this class when an action model is being modeled. It will create a new domain object with correpsonding actions
 * to actions for an input domain, but which use a learned
 * model instead of the actual definition.
 * Actions are created using instances of
 * the {@link ModeledAction} class and their execution and transition dynamics should be defined by
 * the given model that was learned by some {@link Model} class. To retrieve the Domain object that
 * was created, make a call to the {@link #generateDomain()} method.
 * @author James MacGlashan
 *
 */
public class ModeledDomainGenerator implements DomainGenerator{

	
	/**
	 * The domain object to be returned
	 */
	protected Domain modelDomain;
	
	
	/**
	 * Creates a new domain object that is a reflection of the input domain,
	 * Actions are created using
	 * the {@link ModeledAction} class and their execution and transition dynamics should be defined by
	 * the given model that was learned by some {@link Model} class. To retrieve the Domain object that
	 * was created, make a call to the {@link #generateDomain()} method.
	 * @param sourceDomain the source domain that the create domain will reflect.
	 * @param model the model on which transition dynamics will be made
	 */
	public ModeledDomainGenerator(Domain sourceDomain, Model model){
		
		//model domain copies object classes
		modelDomain = new SADomain();

		
		for(Action srcA : sourceDomain.getActions()){
			new ModeledAction(modelDomain, srcA, model);
		}

	}
	
	
	
	
	@Override
	public Domain generateDomain() {
		return modelDomain;
	}


	
	
	/**
	 * A class for creating an action model for some source action. This class copies
	 * source action's name, parameters, and parameter order groups. This action's preconditions
	 * are satisfied whenever the source action's preconditions are satisfied. The class's
	 * execution and transition dynamics methods are determined using the specified source model.
	 * Optionally, the action may be set to use RMax in which transitions that are "unknown"
	 * by the model take the the agent to a special RMax state that can only transition to itself. 
	 * @author James MacGlashan
	 *
	 */
	public class ModeledAction extends Action implements FullActionModel{
		
		/**
		 * The source action this action models
		 */
		protected Action sourceAction;
		
		/**
		 * The model of the transition dynamics that specify the outcomes of this action
		 */
		protected Model model;

		/**
		 * Initializes.
		 * @param modelDomain the model of the domain with which this action is associated
		 * @param sourceAction the source action this action models
		 * @param model the model specifying transition dynamics
		 */
		public ModeledAction(Domain modelDomain, Action sourceAction, Model model){
			super(sourceAction.getName(), modelDomain);
			this.sourceAction = sourceAction;
			this.model = model;

		}
		
		@Override
		public boolean applicableInState(State s, GroundedAction groundedAction){
			return this.sourceAction.applicableInState(s, groundedAction);
		}

		@Override
		public boolean isPrimitive() {
			return sourceAction.isPrimitive();
		}

		@Override
		public boolean isParameterized() {
			return sourceAction.isParameterized();
		}

		@Override
		protected State sampleHelper(State s, GroundedAction groundedAction) {
			return this.model.sampleModel(s, groundedAction);
		}
		
		
		@Override
		public List<TransitionProbability> transitions(State s, GroundedAction groundedAction){
			return this.model.getTransitionProbabilities(s, groundedAction);
		}

		@Override
		public GroundedAction associatedGroundedAction() {
			GroundedAction swappedPointer = sourceAction.associatedGroundedAction();
			swappedPointer.action = this;
			return swappedPointer;
		}

		@Override
		public List<GroundedAction> allApplicableGroundedActions(State s) {
			List <GroundedAction> actionList = sourceAction.allApplicableGroundedActions(s);
			for(GroundedAction ga : actionList){
				ga.action = this;
			}
			return actionList;
		}
	}

	
}
