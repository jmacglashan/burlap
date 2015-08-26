package burlap.behavior.singleagent.learning.modellearning;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.List;



/**
 * Use this class when an action model is being modeled. It will create a new domain object that is a reflection of the input domain,
 *  Actions are created using instances of
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
	 */
	public ModeledDomainGenerator(Domain sourceDomain, Model model){
		
		//model domain copies object classes
		modelDomain = sourceDomain.getNewDomainWithCopiedObjectClasses();

		
		for(Action srcA : sourceDomain.getActions()){
			new ModeledAction(modelDomain, srcA, model);
		}
		
		
		
		//model domain take same object pointers to propositional functions in the source domain;
		//note that the propositional functions will still belong to the original source domain
		for(PropositionalFunction pf : sourceDomain.getPropFunctions()){
			modelDomain.addPropositionalFunction(pf);
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
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			return this.model.sampleModel(s, groundedAction);
		}
		
		
		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction){
			return this.model.getTransitionProbabilities(s, groundedAction);
		}

		@Override
		public GroundedAction getAssociatedGroundedAction() {
			GroundedAction swappedPointer = sourceAction.getAssociatedGroundedAction();
			swappedPointer.action = this;
			return swappedPointer;
		}

		@Override
		public List<GroundedAction> getAllApplicableGroundedActions(State s) {
			List <GroundedAction> actionList = sourceAction.getAllApplicableGroundedActions(s);
			for(GroundedAction ga : actionList){
				ga.action = this;
			}
			return actionList;
		}
	}

	
}
