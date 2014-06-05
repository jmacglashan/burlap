package burlap.behavior.affordances;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.ArrowActionGlyph;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.LandmarkColorBlendInterpolation;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.StateValuePainter2D;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D.PolicyGlyphRenderStyle;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.rtdp.AffordanceRTDP;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.logicalexpressions.PFAtom;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;

public class AffordancesExample {

	// Create collections for action lists
	private ArrayList<AbstractGroundedAction> northActions = new ArrayList<AbstractGroundedAction>();;
	private ArrayList<AbstractGroundedAction> southActions = new ArrayList<AbstractGroundedAction>();;
	private ArrayList<AbstractGroundedAction> eastActions = new ArrayList<AbstractGroundedAction>();;
	private ArrayList<AbstractGroundedAction> westActions = new ArrayList<AbstractGroundedAction>();;
	
	// Pointers to action objects in the domain
	private AbstractGroundedAction northAction;
	private AbstractGroundedAction southAction;
	private AbstractGroundedAction eastAction;
	private AbstractGroundedAction westAction;
	
	// PFAtoms for used in affordance instances
	private PFAtom northPFAtom;
	private PFAtom southPFAtom;
	private PFAtom eastPFAtom;
	private PFAtom westPFAtom;
	private PFAtom goalPFAtom;
	
	private AffordancesController affController;
	
	/**
	 * Creates pointers to the AbstractGroundedAction instances that are associated with the domain and groups them
	 * according to the desired set of affordances for this example
	 * @param domain
	 * @param gwdg
	 * @param initialState
	 * @return
	 */
	public void setupActions(Domain domain, GridWorldDomain gwdg, State initialState){
		// ---- GET ACTIONS ----
		
		// NORTH
		Action northAct = domain.getAction(gwdg.ACTIONNORTH);
		this.northAction = initialState.getAllGroundedActionsFor(northAct).get(0);
		
		// SOUTH
		Action southAct = domain.getAction(gwdg.ACTIONSOUTH);
		this.southAction = initialState.getAllGroundedActionsFor(southAct).get(0);
		
		// EAST
		Action eastAct = domain.getAction(gwdg.ACTIONEAST);
		this.eastAction = initialState.getAllGroundedActionsFor(eastAct).get(0);

		// WEST
		Action westAct = domain.getAction(gwdg.ACTIONWEST);
		this.westAction = initialState.getAllGroundedActionsFor(westAct).get(0);
		
		// ---- ADD ACTIONS ----
		
		// NORTH
		this.northActions = new ArrayList<AbstractGroundedAction>();
		this.northActions.add(this.northAction);
		this.northActions.add(this.southAction);
		this.northActions.add(this.eastAction);
		this.northActions.add(this.westAction);
		
		// SOUTH
		this.southActions = (ArrayList<AbstractGroundedAction>) northActions.clone();
		
		// EAST
		this.eastActions = (ArrayList<AbstractGroundedAction>) northActions.clone();
		
		// WEST
		this.westActions = (ArrayList<AbstractGroundedAction>) northActions.clone();
	}
	
	/**
	 * Create instances of PFAtoms for use in the affordances
	 * @param domain
	 * @param gwdg
	 * @param initialState
	 */
	public void setupPFAtoms(Domain domain, GridWorldDomain gwdg, State initialState) {
		// NORTH
		PropositionalFunction northProp = domain.getPropFunction(gwdg.PFEMPTYNORTH);
		List<GroundedProp> northGroundedProps = initialState.getAllGroundedPropsFor(northProp);
		GroundedProp northGroundedProp = northGroundedProps.get(0);
		this.northPFAtom = new PFAtom(northGroundedProp);
		
		// SOUTH
		PropositionalFunction southProp = domain.getPropFunction(gwdg.PFEMPTYSOUTH);
		List<GroundedProp> southGroundedProps = initialState.getAllGroundedPropsFor(southProp);
		GroundedProp southGroundedProp = southGroundedProps.get(0);
		this.southPFAtom = new PFAtom(southGroundedProp);
		
		// EAST
		PropositionalFunction eastProp = domain.getPropFunction(gwdg.PFEMPTYEAST);
		List<GroundedProp> eastGroundedProps = initialState.getAllGroundedPropsFor(eastProp);
		GroundedProp eastGroundedProp = eastGroundedProps.get(0);
		this.eastPFAtom = new PFAtom(eastGroundedProp);
		
		// WEST
		PropositionalFunction westProp = domain.getPropFunction(gwdg.PFEMPTYWEST);
		List<GroundedProp> westGroundedProps = initialState.getAllGroundedPropsFor(westProp);
		GroundedProp westGroundedProp = westGroundedProps.get(0);
		this.westPFAtom = new PFAtom(westGroundedProp);
		
		// GOAL
		PropositionalFunction goalProp = domain.getPropFunction(GridWorldDomain.PFATLOCATION);
		List<GroundedProp> goalGroundedProps = initialState.getAllGroundedPropsFor(goalProp);
		GroundedProp goalGroundedProp = goalGroundedProps.get(0);
		this.goalPFAtom = new PFAtom(goalGroundedProp);
	}
	
	/**
	 * Creates instances of affordances based on the PFAtoms and Actions created previously
	 * @param hardAffordanceFlag
	 */
	public void setupAffordances(boolean hardAffordanceFlag) {

		Affordance affNorth;
		Affordance affSouth;
		Affordance affEast;
		Affordance affWest;
		
		// --> Hard
		if (hardAffordanceFlag) {
			affNorth = new HardAffordance(northPFAtom, goalPFAtom, northActions);
			affSouth = new HardAffordance(northPFAtom, goalPFAtom, southActions);
			affEast = new HardAffordance(northPFAtom, goalPFAtom, eastActions);
			affWest = new HardAffordance(northPFAtom, goalPFAtom, westActions);
		} 
		// --> Soft
		else {
			
			// --- NORTH ---
			affNorth = new SoftAffordance(this.northPFAtom, this.goalPFAtom, this.northActions);
			
			HashMap<AbstractGroundedAction,Integer> northAlpha = new HashMap<AbstractGroundedAction,Integer>(); 
			
			northAlpha.put(this.northAction, 500);
			northAlpha.put(this.southAction, 2);
			northAlpha.put(this.eastAction, 4);
			northAlpha.put(this.westAction, 4);
			
			int[] northBeta = new int[]{0,2,6,4,1};
			
			((SoftAffordance)affNorth).setActionCounts(northAlpha);
			((SoftAffordance)affNorth).setActionNumCounts(northBeta);
			
			// == SOUTH ==
			
			affSouth = new SoftAffordance(northPFAtom, goalPFAtom, southActions);
			
			HashMap<AbstractGroundedAction,Integer> southAlpha = new HashMap<AbstractGroundedAction,Integer>(); 
			
			southAlpha.put(this.northAction, 1);
			southAlpha.put(this.southAction, 500);
			southAlpha.put(this.eastAction, 3);
			southAlpha.put(this.westAction, 3);
	
			int[] southBeta = new int[]{0,3,4,2,1};
	
			((SoftAffordance)affSouth).setActionCounts(southAlpha);
			((SoftAffordance)affSouth).setActionNumCounts(southBeta);
			
			// == EAST ==
			
			affEast = new SoftAffordance(northPFAtom, goalPFAtom, eastActions);
			
			HashMap<AbstractGroundedAction,Integer> eastAlpha = new HashMap<AbstractGroundedAction,Integer>(); 
			
			eastAlpha.put(this.northAction, 2);
			eastAlpha.put(this.southAction, 2);
			eastAlpha.put(this.eastAction, 500);
			eastAlpha.put(this.westAction, 4);
	
			int[] eastBeta = new int[]{0,2,3,2,1};
	
			((SoftAffordance)affEast).setActionCounts(eastAlpha);
			((SoftAffordance)affEast).setActionNumCounts(eastBeta);
			
			// == WEST ==
			
			affWest = new SoftAffordance(northPFAtom, goalPFAtom, westActions);
			
			HashMap<AbstractGroundedAction,Integer> westAlpha = new HashMap<AbstractGroundedAction,Integer>(); 
	
			westAlpha.put(this.northAction, 2);
			westAlpha.put(this.southAction, 2);
			westAlpha.put(this.eastAction, 4);
			westAlpha.put(this.westAction, 500);
	
			int[] westBeta = new int[]{0,2,3,4,1};
	
			((SoftAffordance)affWest).setActionCounts(westAlpha);
			((SoftAffordance)affWest).setActionNumCounts(westBeta);
			
			((SoftAffordance)affNorth).postProcess();
			((SoftAffordance)affSouth).postProcess();
			((SoftAffordance)affEast).postProcess();
			((SoftAffordance)affWest).postProcess();
		}
		
		// Now affordance instances are made, put them in delegates and into the AffordancesController
		AffordanceDelegate affDelegateNorth = new AffordanceDelegate(affNorth);
		AffordanceDelegate affDelegateSouth = new AffordanceDelegate(affSouth);
		AffordanceDelegate affDelegateEast = new AffordanceDelegate(affEast);
		AffordanceDelegate affDelegateWest = new AffordanceDelegate(affWest);

		List<AffordanceDelegate> affDelegates = new ArrayList<AffordanceDelegate>();
		affDelegates.add(affDelegateNorth);
		affDelegates.add(affDelegateSouth);
		affDelegates.add(affDelegateEast);
		affDelegates.add(affDelegateWest);
		
		this.affController = new AffordancesController(affDelegates);
	}
	
	public static void main(String[] args) {
		// ---- SETUP DOMAIN ----
		GridWorldDomain gwdg = new GridWorldDomain(11, 11);
		gwdg.setMapToFourRooms(); 
		Domain domain = gwdg.generateDomain();

		// Define the task
		RewardFunction rf = new UniformCostRF(); 
		TerminalFunction tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION)); 

		// Set up the initial state of the task
		State initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 10, 10);
		
		DiscreteStateHashFactory hashingFactory = new DiscreteStateHashFactory();

		AffordancesExample gridWorldExample = new AffordancesExample();
		
		// Setup Actions
		gridWorldExample.setupActions(domain, gwdg, initialState);
		
		// ---- SETUP PROP FUNCS ----
		gridWorldExample.setupPFAtoms(domain, gwdg, initialState);
		
		// ---- AFFORDANCES ----
		boolean hardAffordanceFlag = false;
		gridWorldExample.setupAffordances(hardAffordanceFlag);
		
		// ---- SETUP PLANNER ----
		
		// Params for Planners
		int numRollouts = 1000; // RTDP
		int maxDepth = 30; // RTDP
		double vInit = 0;
		double maxDelta = 0.01;
		double gamma = 0.99;
		
		boolean affordanceMode = true;
		RTDP planner;
		Policy p;
		if(affordanceMode) {
			planner = new AffordanceRTDP(domain, rf, tf, gamma, hashingFactory, vInit, numRollouts, maxDelta, maxDepth, gridWorldExample.affController);
			planner.planFromState(initialState);
			
			// Create a Q-greedy policy from the planner
			p = new AffordanceGreedyQPolicy(gridWorldExample.affController, (QComputablePlanner)planner);
		} else {
			planner = new RTDP(domain, rf, tf, gamma, hashingFactory, vInit, numRollouts, maxDelta, maxDepth);
			planner.planFromState(initialState);
			
			// Create a Q-greedy policy from the planner
			p = new GreedyQPolicy((QComputablePlanner)planner);
		}
		valueFunctionVisualize(planner, p, initialState, domain, hashingFactory);
		
		// Print out the planning results
		EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf,100);
		System.out.println(ea.getActionSequenceString());
		
	}
	
	
	/**
	 * Visualizer for GridDomain
	 * @param planner
	 * @param p
	 * @param initialState
	 * @param domain
	 * @param hashingFactory
	 */
	public static void valueFunctionVisualize(QComputablePlanner planner, Policy p, State initialState, Domain domain, DiscreteStateHashFactory hashingFactory){
		List <State> allStates = StateReachability.getReachableStates(initialState, 
			(SADomain)domain, hashingFactory);
		LandmarkColorBlendInterpolation rb = new LandmarkColorBlendInterpolation();
		rb.addNextLandMark(0., Color.RED);
		rb.addNextLandMark(1., Color.BLUE);
		
		StateValuePainter2D svp = new StateValuePainter2D(rb);
		svp.setXYAttByObjectClass(GridWorldDomain.CLASSAGENT, GridWorldDomain.ATTX, 
			GridWorldDomain.CLASSAGENT, GridWorldDomain.ATTY);
		
		PolicyGlyphPainter2D spp = new PolicyGlyphPainter2D();
		spp.setXYAttByObjectClass(GridWorldDomain.CLASSAGENT, GridWorldDomain.ATTX, 
			GridWorldDomain.CLASSAGENT, GridWorldDomain.ATTY);
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONNORTH, new ArrowActionGlyph(0));
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONSOUTH, new ArrowActionGlyph(1));
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONEAST, new ArrowActionGlyph(2));
		spp.setActionNameGlyphPainter(GridWorldDomain.ACTIONWEST, new ArrowActionGlyph(3));
		spp.setRenderStyle(PolicyGlyphRenderStyle.DISTSCALED);
		
		ValueFunctionVisualizerGUI gui = new ValueFunctionVisualizerGUI(allStates, svp, planner);
		gui.setSpp(spp);
		gui.setPolicy(p);
		gui.setBgColor(Color.GRAY);
		gui.initGUI();
}
	
	
}
