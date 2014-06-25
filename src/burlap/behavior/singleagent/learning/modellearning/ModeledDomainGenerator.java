package burlap.behavior.singleagent.learning.modellearning;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;



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
	 * Name of both the object class and single binary attribute used to indicate a RMax fictitious state.
	 */
	public static final String RMAXFICTIOUSSTATENAME = "RMAXState";
	
	/**
	 * Name of the only action that can be taken from a fictitious RMAx state.
	 */
	public static final String RMAXFICTIOUSSTATEACTIONNAME = "RMAXAction";
	
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
	public ModeledDomainGenerator(Domain sourceDomain, Model model, boolean useRMaxTransitionsAndFictitousState){
		
		//model domain copies object classes
		modelDomain = sourceDomain.getNewDomainWithCopiedObjectClasses();
		
		if(useRMaxTransitionsAndFictitousState){
			Attribute att = new Attribute(modelDomain, RMAXFICTIOUSSTATENAME, Attribute.AttributeType.BOOLEAN);
			ObjectClass rmaxstate = new ObjectClass(modelDomain, RMAXFICTIOUSSTATENAME);
			rmaxstate.addAttribute(att);
			
			new RMaxStateAction();
		}
		
		for(Action srcA : sourceDomain.getActions()){
			new ModeledAction(modelDomain, srcA, model, useRMaxTransitionsAndFictitousState);
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

	
	public static boolean isRmaxFictitiousState(State s){
		return s.getObjectsOfTrueClass(RMAXFICTIOUSSTATENAME).size() > 0;
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
	public class ModeledAction extends Action{
		
		/**
		 * The source action this action models
		 */
		protected Action sourceAction;
		
		/**
		 * The model of the transition dynamics that specify the outcomes of this action
		 */
		protected Model model;
		
		/**
		 * Whether this action follows the RMax paradigm of transition to a fictious RMax state when the model is not known
		 */
		protected boolean useRMax;
		
		/**
		 * The fictitious RMax state this action will transition to for unknown transitions. Only used in RMax paradigms
		 */
		protected State RMaxState = null;
		
		
		/**
		 * Initializes.
		 * @param modelDomain the model of the domain with which this action is associated
		 * @param sourceAction the source action this action models
		 * @param model the model specifying transition dynamics
		 * @param useRMax whether the RMax paradigm of transfering to fictious states for unkown transitions is followed.
		 */
		public ModeledAction(Domain modelDomain, Action sourceAction, Model model, boolean useRMax){
			super(sourceAction.getName(), modelDomain, sourceAction.getParameterClasses(), sourceAction.getParameterOrderGroups());
			this.sourceAction = sourceAction;
			this.model = model;
			this.useRMax = useRMax;
			if(useRMax){
				RMaxState = new State();
				ObjectInstance o = new ObjectInstance(ModeledDomainGenerator.this.modelDomain.getObjectClass(RMAXFICTIOUSSTATENAME), "rmax");
				o.setValue(RMAXFICTIOUSSTATENAME, 1);
				RMaxState.addObject(o);
			}
		}
		
		@Override
		public boolean applicableInState(State s, String [] params){
			if(s.getObjectsOfTrueClass(RMAXFICTIOUSSTATENAME).size() > 0){
				return false; //action cannot be performed in rmax state
			}
			return this.sourceAction.applicableInState(s, params);
		}

		@Override
		protected State performActionHelper(State s, String[] params) {
			
			if(this.useRMax){
				if(!this.model.transitionIsModeled(s, new GroundedAction(sourceAction, params))){
					return this.RMaxState;
				}
			}
			
			
			return this.model.sampleModel(s, new GroundedAction(sourceAction, params));
		}
		
		
		@Override
		public List<TransitionProbability> getTransitions(State s, String [] params){
			
			
			
			if(this.useRMax){
				if(!this.model.transitionIsModeled(s, new GroundedAction(sourceAction, params))){
					List <TransitionProbability> transition = new ArrayList<TransitionProbability>();
					TransitionProbability tp = new TransitionProbability(this.RMaxState, 1.);
					transition.add(tp);
					return transition;
				}
			}
			
			
			return this.model.getTransitionProbabilities(s, new GroundedAction(sourceAction, params));
		}
		
		
	}
	
	
	
	/**
	 * An action that is only executable in the ficitious RMax state and which always transitions back to the fictitious RMax state.
	 * @author James MacGlashan
	 *
	 */
	public class RMaxStateAction extends Action{

		/**
		 * Initializes for the owning ModeledDomainGenerator instance's ModeledDomain instance
		 */
		public RMaxStateAction(){
			super(RMAXFICTIOUSSTATEACTIONNAME, modelDomain, "");
		}
		
		@Override
		public boolean applicableInState(State s, String [] params){
			if(s.getObjectsOfTrueClass(RMAXFICTIOUSSTATENAME).size() > 0){
				return true; //action cannot be performed in rmax state
			}
			return false;
		}
		
		@Override
		protected State performActionHelper(State s, String[] params) {
			return s; //goes back to the same RMax state
		}
		
		
		
	}
	
}
