package burlap.behavior.affordances;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.ArrowActionGlyph;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.LandmarkColorBlendInterpolation;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D.PolicyGlyphRenderStyle;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.StateValuePainter2D;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.rtdp.AffordanceRTDP;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.logicalexpressions.LogicalExpression;
import burlap.oomdp.logicalexpressions.PFAtom;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;

public class AffordancesController {

	protected List<AffordanceDelegate> affordances;
	protected LogicalExpression currentGoal;
	
	public AffordancesController(List<AffordanceDelegate> affs) {
		this.affordances = affs;
	}
	
	public void setCurrentGoal(LogicalExpression currentGoal){
		this.currentGoal = currentGoal;
		for(AffordanceDelegate aff : this.affordances){
			aff.setCurrentGoal(currentGoal);
		}
	}
	
	public void resampleActionSets(){
		for(AffordanceDelegate aff : this.affordances){
			aff.resampleActionSet();
		}
	}
	
	public List<AbstractGroundedAction> filterIrrelevantActionsInState(List<AbstractGroundedAction> actions, State s){
		
		List<AffordanceDelegate> activeAffordances = new ArrayList<AffordanceDelegate>(this.affordances.size());
		for(AffordanceDelegate aff : this.affordances){
			if(aff.primeAndCheckIfActiveInState(s)){
				activeAffordances.add(aff);
			}
		}
		
		List<AbstractGroundedAction> filteredList = new ArrayList<AbstractGroundedAction>(actions.size());
		for(AbstractGroundedAction a : actions){
			for(AffordanceDelegate aff : activeAffordances){
				if(aff.actionIsRelevant(a)){
					filteredList.add(a);
					break;
				}
			}
		}
		
		return filteredList;
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

		// ---- SETUP ACTIONS ----
		
		// NORTH
		Action northAct = domain.getAction(gwdg.ACTIONNORTH);
		List<GroundedAction> groundNorthAct = initialState.getAllGroundedActionsFor(northAct);
		
		// SOUTH
		Action southAct = domain.getAction(gwdg.ACTIONSOUTH);
		List<GroundedAction> groundSouthAct = initialState.getAllGroundedActionsFor(southAct);
		
		// EAST
		Action eastAct = domain.getAction(gwdg.ACTIONEAST);
		List<GroundedAction> groundEastAct = initialState.getAllGroundedActionsFor(eastAct);

		
		// WEST
		Action westAct = domain.getAction(gwdg.ACTIONWEST);
		List<GroundedAction> groundWestAct = initialState.getAllGroundedActionsFor(westAct);
		
		// ---- ADD ACTIONS ----
		
		// NORTH
		List<AbstractGroundedAction> northActions = new ArrayList<AbstractGroundedAction>();
		northActions.add(groundNorthAct.get(0));
		northActions.add(groundSouthAct.get(0));
		northActions.add(groundEastAct.get(0));
		northActions.add(groundWestAct.get(0));
		
		// SOUTH
		List<AbstractGroundedAction> southActions = new ArrayList<AbstractGroundedAction>();
		southActions.add(groundNorthAct.get(0));
		southActions.add(groundSouthAct.get(0));
		southActions.add(groundEastAct.get(0));
		southActions.add(groundWestAct.get(0));
		
		// EAST
		List<AbstractGroundedAction> eastActions = new ArrayList<AbstractGroundedAction>();
		eastActions.add(groundNorthAct.get(0));
		eastActions.add(groundSouthAct.get(0));
		eastActions.add(groundEastAct.get(0));
		eastActions.add(groundWestAct.get(0));
		
		// WEST
		List<AbstractGroundedAction> westActions = new ArrayList<AbstractGroundedAction>();
		westActions.add(groundNorthAct.get(0));
		westActions.add(groundSouthAct.get(0));
		westActions.add(groundEastAct.get(0));
		westActions.add(groundWestAct.get(0));
		
		// ---- SETUP PROP FUNCS ----
		
		// NORTH
		PropositionalFunction northProp = domain.getPropFunction(gwdg.PFEMPTYNORTH);
		List<GroundedProp> northGroundedProps = initialState.getAllGroundedPropsFor(northProp);
		GroundedProp northGroundedProp = northGroundedProps.get(0);
		PFAtom northPFAtom = new PFAtom(northGroundedProp);
		
		// SOUTH
		PropositionalFunction southProp = domain.getPropFunction(gwdg.PFEMPTYSOUTH);
		List<GroundedProp> southGroundedProps = initialState.getAllGroundedPropsFor(southProp);
		GroundedProp southGroundedProp = southGroundedProps.get(0);
		PFAtom southPFAtom = new PFAtom(southGroundedProp);
		
		// EAST
		PropositionalFunction eastProp = domain.getPropFunction(gwdg.PFEMPTYEAST);
		List<GroundedProp> eastGroundedProps = initialState.getAllGroundedPropsFor(eastProp);
		GroundedProp eastGroundedProp = eastGroundedProps.get(0);
		PFAtom eastPFAtom = new PFAtom(eastGroundedProp);
		
		// WEST
		PropositionalFunction westProp = domain.getPropFunction(gwdg.PFEMPTYWEST);
		List<GroundedProp> westGroundedProps = initialState.getAllGroundedPropsFor(westProp);
		GroundedProp westGroundedProp = westGroundedProps.get(0);
		PFAtom westPFAtom = new PFAtom(westGroundedProp);
		
		// GOAL
		PropositionalFunction goalProp = domain.getPropFunction(GridWorldDomain.PFATLOCATION);
		List<GroundedProp> goalGroundedProps = initialState.getAllGroundedPropsFor(goalProp);
		GroundedProp goalGroundedProp = goalGroundedProps.get(0);
		PFAtom goalPFAtom = new PFAtom(goalGroundedProp);
		
		// ---- AFFORDANCES ----
//		Affordance affNorth = new HardAffordance(northPFAtom, goalPFAtom, northActions);
//		Affordance affSouth = new HardAffordance(northPFAtom, goalPFAtom, southActions);
//		Affordance affEast = new HardAffordance(northPFAtom, goalPFAtom, eastActions);
//		Affordance affWest = new HardAffordance(northPFAtom, goalPFAtom, westActions);
		
		
		// --- NORTH ---
		Affordance affNorth = new SoftAffordance(northPFAtom, goalPFAtom, northActions);
		
		HashMap<AbstractGroundedAction,Integer> northAlpha = new HashMap<AbstractGroundedAction,Integer>(); 
		
		northAlpha.put(groundNorthAct.get(0), 500);
		northAlpha.put(groundSouthAct.get(0), 2);
		northAlpha.put(groundEastAct.get(0), 4);
		northAlpha.put(groundWestAct.get(0), 4);
		
		int[] northBeta = new int[]{0,2,6,4,1};
		
		((SoftAffordance)affNorth).setActionCounts(northAlpha);
		((SoftAffordance)affNorth).setActionNumCounts(northBeta);
		
		// == SOUTH ==
		
		Affordance affSouth = new SoftAffordance(northPFAtom, goalPFAtom, southActions);
		
		HashMap<AbstractGroundedAction,Integer> southAlpha = new HashMap<AbstractGroundedAction,Integer>(); 
		
		southAlpha.put(groundNorthAct.get(0), 1);
		southAlpha.put(groundSouthAct.get(0), 500);
		southAlpha.put(groundEastAct.get(0), 3);
		southAlpha.put(groundWestAct.get(0), 3);

		int[] southBeta = new int[]{0,3,4,2,1};

		((SoftAffordance)affSouth).setActionCounts(southAlpha);
		((SoftAffordance)affSouth).setActionNumCounts(southBeta);
		
		// == EAST ==
		
		Affordance affEast = new SoftAffordance(northPFAtom, goalPFAtom, eastActions);
		
		HashMap<AbstractGroundedAction,Integer> eastAlpha = new HashMap<AbstractGroundedAction,Integer>(); 
		
		eastAlpha.put(groundNorthAct.get(0), 2);
		eastAlpha.put(groundSouthAct.get(0), 2);
		eastAlpha.put(groundEastAct.get(0), 500);
		eastAlpha.put(groundWestAct.get(0), 4);

		int[] eastBeta = new int[]{0,2,3,2,1};

		((SoftAffordance)affEast).setActionCounts(eastAlpha);
		((SoftAffordance)affEast).setActionNumCounts(eastBeta);
		
		// == WEST ==
		
		Affordance affWest = new SoftAffordance(northPFAtom, goalPFAtom, westActions);
		
		HashMap<AbstractGroundedAction,Integer> westAlpha = new HashMap<AbstractGroundedAction,Integer>(); 

		westAlpha.put(groundNorthAct.get(0), 2);
		westAlpha.put(groundSouthAct.get(0), 2);
		westAlpha.put(groundEastAct.get(0), 4);
		westAlpha.put(groundWestAct.get(0), 500);

		int[] westBeta = new int[]{0,2,3,4,1};

		((SoftAffordance)affWest).setActionCounts(westAlpha);
		((SoftAffordance)affWest).setActionNumCounts(westBeta);
		
		((SoftAffordance)affNorth).postProcess();
		((SoftAffordance)affSouth).postProcess();
		((SoftAffordance)affEast).postProcess();
		((SoftAffordance)affWest).postProcess();
		
		AffordanceDelegate affDelegateNorth = new AffordanceDelegate(affNorth);
		AffordanceDelegate affDelegateSouth = new AffordanceDelegate(affSouth);
		AffordanceDelegate affDelegateEast = new AffordanceDelegate(affEast);
		AffordanceDelegate affDelegateWest = new AffordanceDelegate(affWest);

		List<AffordanceDelegate> affDelegates = new ArrayList<AffordanceDelegate>();
		affDelegates.add(affDelegateNorth);
		affDelegates.add(affDelegateSouth);
		affDelegates.add(affDelegateEast);
		affDelegates.add(affDelegateWest);
		
		AffordancesController affController = new AffordancesController(affDelegates);
		
		
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
			planner = new AffordanceRTDP(domain, rf, tf, gamma, hashingFactory, vInit, numRollouts, maxDelta, maxDepth, affController);
			planner.planFromState(initialState);
			
			// Create a Q-greedy policy from the planner
			p = new AffordanceGreedyQPolicy(affController, (QComputablePlanner)planner);
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
