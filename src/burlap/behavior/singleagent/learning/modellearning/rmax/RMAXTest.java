package burlap.behavior.singleagent.learning.modellearning.rmax;

import java.awt.BorderLayout;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import minecraft.NameSpace;
import minecraft.MinecraftBehavior.MinecraftBehavior;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.behavior.singleagent.learning.modellearning.ModelPlanner.ModelPlannerGenerator;
import burlap.behavior.singleagent.learning.modellearning.modelplanners.VIModelPlanner;
import burlap.behavior.singleagent.learning.modellearning.models.TabularModel;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.AttActionEffectTuple;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionHypothesis;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.PaperAttributeActionTuple;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Prediction;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.PredictionsLearner;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.OOMDPModel;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionLearners.OOMDPConditionLearner;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionLearners.PerceptionConditionLearner;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionLearners.PerceptionConditionLearnerPF;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.EffectHelpers;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.FullAttributeStatePerception;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.GroupOfPerceptions;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.PFStatePerception;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.StatePerception;
import burlap.behavior.singleagent.learning.modellearning.rmax.TaxiDomain.VictoryText;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.shaping.potential.PotentialFunction;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;





public class RMAXTest {

	private static void printNumIncorrectPredictions(Model model, State initialState, Domain d) {
		List<State> allStates = getAllStates(initialState, d);
		List<State> incorrectStates = new ArrayList<State>();
		for (State s : allStates) {
			for (Action a : d.getActions()) {
				for (GroundedAction ga : a.getAllApplicableGroundedActions(s)) {
					State sPrime = ga.executeIn(s);
					State predictedState = ((OOMDPModel) model).sampleModelHelper(s, ga);
					//					List<Effect> predictedEffects = ((OOMDPModel) model).getPredictionsLearner().predictEffects(s, ga);
					if (!predictedState.equals(sPrime) && model.transitionIsModeled(s, ga)) {
						incorrectStates.add(s);
					}
				}
			}
		}
		System.out.println("Num incorrect predictions: " + incorrectStates.size());
	}

	private static List<State> getAllStates(State initialState, Domain d) {
		PrintStream originalStream = System.out;

		OutputStream dummyStreamO = new OutputStream(){
			public void write(int b) {
				//NO-OP
			}
		};


		System.setOut(new PrintStream(dummyStreamO));
		List<State> allStates = StateReachability.getReachableStates(initialState, (SADomain)d, new DiscreteStateHashFactory());
		System.setOut(originalStream);

		return allStates;
	}

	private static List<State> getUnmodelledStates(State initialState, Domain d, Model model, TerminalFunction tf) {

		List<State> allStates = getAllStates(initialState, d);

		List<State> unmodeledStates = new ArrayList<State>();
		for (State state : allStates) {
			if (!(model.stateTransitionsAreModeled(state)) && !tf.isTerminal(state)) {
				unmodeledStates.add(state);
			}
		}
		return unmodeledStates;
	}



	public static class WallRF implements RewardFunction{

		int gx;
		int gy;

		public WallRF(int gx, int gy){
			this.gx = gx;
			this.gy = gy;
		}


		@Override
		public double reward(State s, GroundedAction a, State sprime) {

			ObjectInstance nagent = sprime.getFirstObjectOfClass(GridWorldDomain.CLASSAGENT);
			int nx = nagent.getDiscValForAttribute(GridWorldDomain.ATTX);
			int ny = nagent.getDiscValForAttribute(GridWorldDomain.ATTY);

			//did agent reach goal location?
			if(nx == this.gx && ny == this.gy){
				return 1000.;
			}

			ObjectInstance pagent = s.getFirstObjectOfClass(GridWorldDomain.CLASSAGENT);
			int px = pagent.getDiscValForAttribute(GridWorldDomain.ATTX);
			int py = pagent.getDiscValForAttribute(GridWorldDomain.ATTY);

			//if agent didn't change position, they must have hit a wall
			if(px == nx && py == ny){
				return -100.;
			}

			return -1.;
		}
	}



	public static List<PropositionalFunction> OOMDPModelToListOfPropositionalFunctions(OOMDPModel model, Domain d) {
		List<PropositionalFunction> toReturn = new ArrayList<PropositionalFunction>();
		List<Prediction> allPredictions = model.getPredictionsLearner().getAllPredictions();
		for (Prediction pred : allPredictions) {
			PerceptionConditionLearner percCondLearner = (PerceptionConditionLearner) pred.getConditionLearner();
			String name = percCondLearner.toString()+"PF";
			PerceptionConditionLearnerPF percCondLearnerPF = new PerceptionConditionLearnerPF(name, d, "", percCondLearner);
			toReturn.add(percCondLearnerPF);
		}




		return toReturn;
	}

	private static void printGeneralizationErrors(State initialState, Domain d, Model model, boolean acrossDomain, int numTrials, int numRandStatesToTest) {
		ObjectClass oClass = d.getObjectClass(TaxiDomain.TAXICLASS);

		Attribute xAtt = d.getAttribute(TaxiDomain.XATT);
		Attribute yAtt = d.getAttribute(TaxiDomain.YATT);

		Action westA = d.getAction(TaxiDomain.WESTACTION);
		Action eastA = d.getAction(TaxiDomain.EASTACTION);
		Action northA = d.getAction(TaxiDomain.NORTHACTION);
		Action southA = d.getAction(TaxiDomain.SOUTHACTION);


		GroundedAction westGA = westA.getAllApplicableGroundedActions(initialState).get(0);
		GroundedAction eastGA = eastA.getAllApplicableGroundedActions(initialState).get(0);
		GroundedAction southGA = southA.getAllApplicableGroundedActions(initialState).get(0);
		GroundedAction northGA = northA.getAllApplicableGroundedActions(initialState).get(0);

		for(GroundedAction ga : new GroundedAction[]{westGA, eastGA, southGA, northGA}) {
			Attribute att = null;



			if (ga == westGA || ga == eastGA) {
				att = xAtt;
			}
			else {
				att = yAtt;
			}

			PropositionalFunction pf = null;
			if (ga == westGA) {
				pf =  new TaxiDomain.wallWest("wallWestPF", d, "");

			}
			else if(ga == eastGA) {
				pf =  new TaxiDomain.wallEast("wallEastPF", d, "");
			}
			else if(ga == southGA) {
				pf =  new TaxiDomain.wallSouth("wallSouthPF", d, "");
			}
			else {
				pf =  new TaxiDomain.wallNorth("wallNorthPF", d, "");
			}

			System.out.println("Classifier for " + ga.actionName() + "'s effect on " + oClass.name +  "'s " + att.name + " compared against "  + pf.getName());

			HashMap<AttActionEffectTuple, List<Prediction>> hm = ((OOMDPModel) model).getPredictionsLearner().getPredictionsByAttActionAndEffect();
			AttActionEffectTuple toHashBy = new AttActionEffectTuple(oClass, att, ga, EffectHelpers.arithEffect);
			if (!hm.get(toHashBy).isEmpty()) {
				Prediction pred = hm.get(toHashBy).get(0);
				PerceptionConditionLearner condLearner =  ((PerceptionConditionLearner)pred.getConditionLearner());
				condLearner.trainClassifier();
				List<Double> trialResults = new ArrayList<Double>();

				
				for (int j = 0; j < numTrials; j++) {
					int incorrectStates = 0;
						for (int i = 0; i < numRandStatesToTest; i++) {
							State randomState = TaxiDomain.getRandomState(d);
							//						System.out.println("\tis true: " + pf.isTrue(randomState, ""));
							//						System.out.println("\tcond learner:" + condLearner.predict(randomState));

							if ((boolean)pf.isTrue(randomState, "") == (boolean)condLearner.predict(randomState)) {
								incorrectStates += 1;
							}

						}	

	
					trialResults.add(((double) incorrectStates)/ ((double)numRandStatesToTest));
				}
				//Print results for trial
				System.out.println("\tFull results: " + trialResults);
				double average = 0;
				//Average
				for (Double res : trialResults) average += res;
				average = average/trialResults.size();
				System.out.println("\tAverage : " + average);
				//STD
				double STD = 0;
				for (Double res: trialResults) STD += Math.pow(average-res, 2);
				
				STD = Math.sqrt(STD/(trialResults.size()-1));
				System.out.println("\tSTD : " + STD);

			}
		}
	}

	public static void main(String[] args) {

		//PARAMS TO SET
		boolean useRmax = false; //if false uses DOORMAX
		String statePerceptionToUse = StatePerception.ClassRelationalStatePerception;//if null then classic DOORMAX

		boolean gridWorld = false;
		boolean taxi = true;
		boolean minecraft = false;


		boolean exhaustiveLearning = false;

		boolean learnWithRandPolicyFirst = true;
		boolean taxiRandomStateAtStartOfRandIteration = false;
		boolean acrossDomainGenError = false;
		int randIterations = 1;
		int randActionsPerIteration = 10000;
		int numTrialsForGenError = 10;
		int numRandomStatesTested = 1000;

		boolean runLearningWithModel = false;
		int learningIterations = 4;
		int actionsPerIteration = 50;


		final int nConfident = 1;
		final double maxVIDelta = .1;
		final int maxVIPasses = 20;

		int k = 2;

		//NULL PARAMS
		final DiscreteStateHashFactory hf = new DiscreteStateHashFactory();

		final double maxReward = 0;
		Domain d = null;
		State initialState = null;
		TerminalFunction tf = null;
		RewardFunction rf = null;
		PotentialShapedRMax rmax = null;
		Model model = null;
		DomainGenerator dg = null;

		StringBuilder toWriteToFile = new StringBuilder();
		//GRID WORLD DOMAIN
		//Set up domain and initial state
		if (gridWorld) {
			GridWorldDomain gwdg = new GridWorldDomain(11, 11);
			gwdg.setMapToFourRooms();

			int endX = 10;
			int endY = 10;

			d = gwdg.generateDomain();
			initialState = GridWorldDomain.getOneAgentOneLocationState(d);
			GridWorldDomain.setAgent(initialState, 0, 0);
			GridWorldDomain.setLocation(initialState, 0, endX, endY, 0);

			rf = new UniformCostRF();

			tf = new GridWorldTerminalFunction(10, 10);

		}

		//MINECRAFT DOMAIN
		if (minecraft) {
			String mapName = "src/plane1.map";

			MinecraftBehavior mcBeh = new MinecraftBehavior(mapName);
			d = mcBeh.getDomain();
			rf = mcBeh.getRewardFunction();
			tf = mcBeh.getTerminalFunction();
			initialState = mcBeh.getInitialState();
			rf = new UniformCostRF();

			List<State> allReachableStates= StateReachability.getReachableStates(initialState, (SADomain) d, hf);
			for (State state : allReachableStates) {
				if (state.getAllUnsetAttributes().keySet().size() != 0) {
					System.out.println(state.getAllUnsetAttributes());
				}
			}
		}
		//TAXI DOMAIN
		if (taxi) {
			dg = new TaxiDomain();
			d = dg.generateDomain();
			initialState = TaxiDomain.getClassicState(d);
			tf = new TaxiDomain.TaxiTF();
			rf = new UniformCostRF();
		}
		//BFS
		//		TFGoalCondition goalCondition = new TFGoalCondition(tf);

		//		DeterministicPlanner planner = new BFS(d, goalCondition, new DiscreteStateHashFactory());
		//		
		//		planner.planFromState(initialState);
		//		
		//		Policy p = new SDPlannerPolicy(planner);
		//		
		//		
		//		EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf);
		//		System.out.println(ea.getActionSequenceString());



		//RMAX
		if (useRmax) {
			model = new TabularModel(d,new DiscreteStateHashFactory(),1);
		}
		//DOORMAX
		else {
			List<PropositionalFunction> propFunsToUse = d.getPropFunctions();
			List<String> effectsToUse = new ArrayList<String>();
			effectsToUse.add(EffectHelpers.arithEffect);
			effectsToUse.add(EffectHelpers.assigEffect);
			model = new OOMDPModel(d,rf, tf,propFunsToUse, effectsToUse, initialState, k, statePerceptionToUse);
		}
		//RUN ALGORITHM

		//exhaustive learning
		int numStatesObserved = 0;
		if (exhaustiveLearning) {
			List<State> allStatesT = StateReachability.getReachableStates(initialState, (SADomain)d, new DiscreteStateHashFactory());
			for (State s : allStatesT) {
				for (Action a : d.getActions()) {
					for (GroundedAction ga : a.getAllApplicableGroundedActions(s)) {
						State sprime = ga.executeIn(s);
						double r = rf.reward(s, ga, sprime);
						boolean sprimeIsTerminal = tf.isTerminal(sprime);
						model.updateModel(s, ga, sprime, r, sprimeIsTerminal);
					}

				}
				numStatesObserved+= 1;
				List<State> unmodeledStates = getUnmodelledStates(initialState, d, model, tf);
				if (numStatesObserved % 10 == 0) {
					System.out.println("Num unmodelled states after " + numStatesObserved + " states checked:" + unmodeledStates.size());
					printNumIncorrectPredictions(model, initialState, d);
				}

			}
			List<State> unmodeledStates = getUnmodelledStates(initialState, d, model, tf);
			System.out.println("Num unmodelled states after exhaustive learning: " + unmodeledStates.size());

		}

		//random policy
		if (learnWithRandPolicyFirst) {

			for(int i = 0; i < randIterations; i++){
				System.out.println("Rand iteration: " + i);
				State currState = initialState.copy();
				if (taxiRandomStateAtStartOfRandIteration) {
					currState = TaxiDomain.getRandomState(d);
					System.out.println("Randomizing taxi initial state");

				}
				else {
					//					List<State> unmodeledStates = getUnmodelledStates(currState, d, model, tf);

					//					System.out.println("Num unmodelled states: " + unmodeledStates.size());
					//					printNumIncorrectPredictions(model, currState, d);
				}
				Policy randPol = new Policy.RandomPolicy(d);


				for (int j = 0; j < randActionsPerIteration; j++) {


					GroundedAction ga = (GroundedAction) randPol.getAction(currState);
					State sprime = ga.executeIn(currState);
					double r = rf.reward(currState, ga, sprime);
					boolean sprimeIsTerminal = tf.isTerminal(sprime);
					model.updateModel(currState, ga, sprime, r, sprimeIsTerminal);
					currState = sprime;
				}
			}
		}


		//VI policy
		if (runLearningWithModel) {
			rmax = new PotentialShapedRMax(d, rf, tf,.95, hf,maxReward, nConfident, maxVIDelta, maxVIPasses,  model);
			for (int i = 0; i < learningIterations; i ++) {
				List<State> unmodeledStates = getUnmodelledStates(initialState, d, rmax.getModel(), tf);

				toWriteToFile.append(randActionsPerIteration*i + "\t" + unmodeledStates.size() + "\n");

				EpisodeAnalysis ea = rmax.runLearningEpisodeFrom(initialState, actionsPerIteration);
				//				System.out.println(ea.actionSequence);

				System.out.println("Num unmodelled states: " + unmodeledStates.size());
			}
		}


		//PRINT INCORRECT PREDICTIONS
		//		printNumIncorrectPredictions(model, initialState, d);




		//PRINT NUM UNLEARNED STATES and PREDICTIONS LEARNER
		//		System.out.println("Num unmodeled states: " + getUnmodelledStates(initialState, d, model, tf).size());
		//		if (!useRmax) {
		//			System.out.println(((OOMDPModel) model).getPredictionsLearner());
		//		}

		//GRID WORLD VISUALIZER
		//get reachable states
		if (gridWorld && rmax != null) {

			ValueFunctionPlanner planner = ((VIModelPlanner)rmax.getModelPlanner()).getValueIterationPlanner().getCopyOfValueFunction();

			List<State> allStates = StateReachability.getReachableStates(initialState, (SADomain)d, new DiscreteStateHashFactory());

			List<State> modeledStates = new ArrayList<State>();
			for (State state : allStates) {
				if (model.stateTransitionsAreModeled(state)) {
					modeledStates.add(state);
				}
			}
			ValueFunctionVisualizerGUI gui = GridWorldDomain.getGridWorldValueFunctionVisualization(modeledStates, planner, new GreedyQPolicy(planner));
			gui.initGUI();
		}

		//TAXI VISUALIZER
		if (taxi) {

		}



		//PRINT PREDICTED EFFECTS ON INTIALSTATE
		//		GroundedAction gaToTest = d.getActions().get(0).getAllApplicableGroundedActions(initialState).get(0);
		//
		//		System.out.println("EFFECTS PREDICTED ON " + gaToTest.actionName() + " ON INITIAL STATE:");
		//		PredictionsLearner pLearner =((OOMDPModel) model).getPredictionsLearner(); 
		//		System.out.println(pLearner.predictEffects(initialState, gaToTest));
		//		System.out.println(model.getUnmodeledActionsForState(initialState));

		//		System.out.println(toWriteToFile);


		//USE classifiers as PFs
		//		List<PropositionalFunction> classifierPFs = OOMDPModelToListOfPropositionalFunctions((OOMDPModel)model, d);
		//		System.out.println("Turned classifiers into PFs");
		//		List<String> effectsToUse = new ArrayList<String>();
		//		effectsToUse.add(EffectHelpers.arithEffect);
		//		effectsToUse.add(EffectHelpers.assigEffect);
		//		model = new OOMDPModel(d,rf, tf,classifierPFs, effectsToUse, initialState, k, null);
		//		if (learnWithRandPolicyFirst) {
		//
		//			for(int i = 0; i < 5; i++){
		//				System.out.println("Rand iteration: " + i);
		//				State currState = initialState.copy();
		//
		//				Policy randPol = new Policy.RandomPolicy(d);
		//
		//
		//				for (int j = 0; j < 200; j++) {
		//
		//					GroundedAction ga = (GroundedAction) randPol.getAction(currState);
		//					State sprime = ga.executeIn(currState);
		//					double r = rf.reward(currState, ga, sprime);
		//					boolean sprimeIsTerminal = tf.isTerminal(sprime);
		//					model.updateModel(currState, ga, sprime, r, sprimeIsTerminal);
		//					currState = sprime;
		//				}
		//			}
		//		}
		//		//		printNumIncorrectPredictions(model, initialState, d);
		//		//		System.out.println("Num unmodeled states: " + getUnmodelledStates(initialState, d, model, tf).size());
		//		if (!useRmax) {
		//			System.out.println(((OOMDPModel) model).getPredictionsLearner());
		//		}


		//Print generalization error for condition learner classifier across random
		printGeneralizationErrors(initialState, d, model, acrossDomainGenError, numTrialsForGenError, numRandomStatesTested);



		// display classifier
		//				GroupOfPerceptions perceptions = condLearner.getObservedPerceptions();
		//
		//				J48 cls = ((J48) condLearner.getClassifier());
		//
		//				final javax.swing.JFrame jf = 
		//						new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
		//				jf.setSize(1400,1000);
		//				jf.getContentPane().setLayout(new BorderLayout());
		//				TreeVisualizer tv = null;
		//				try {
		//					tv = new TreeVisualizer(null,
		//							cls.graph(),
		//							new PlaceNode2());
		//				} catch (Exception e1) {
		//					e1.printStackTrace();
		//				}
		//				jf.getContentPane().add(tv, BorderLayout.CENTER);
		//				jf.addWindowListener(new java.awt.event.WindowAdapter() {
		//					public void windowClosing(java.awt.event.WindowEvent e) {
		//						jf.dispose();
		//					}
		//				});
		//
		//				jf.setVisible(true);
		//				tv.fitToScreen();
		//			}
		//			else {
		//				System.out.println("No predictions to display tree for");
		//			}
	}

}


//TODO:
/**
 * Make efficient
 * Don't count on state equality -- use hashing factories
 */



