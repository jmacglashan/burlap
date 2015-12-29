package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionLearners;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.Instances;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.ClassRelationalStatePerception;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.FullAttributeStatePerception;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.GroupOfPerceptions;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.PFStatePerception;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.RelationalStatePerception;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.StatePerception;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.StatePerceptionNotFoundException;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.AttributeRelations.AttributeRelation;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class PerceptionConditionLearner extends OOMDPConditionLearner {

	private Classifier classifier;
	private GroupOfPerceptions observedPerceptions;
	boolean classifierTrained = false;
	List<PropositionalFunction> propFuns;
	String statePerceptionToUse;


	public PerceptionConditionLearner(List<PropositionalFunction> propFuns, String statePerceptionToUse) {
		this.observedPerceptions = new GroupOfPerceptions();
		this.classifierTrained = false;
		this.propFuns = propFuns;

		this.statePerceptionToUse =statePerceptionToUse;
		
		
		
		
		this.classifier = new Logistic();//new J48();
		//Classifier options
		String[] options = new String []{};
		try {
			this.classifier.setOptions(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public GroupOfPerceptions getObservedPerceptions() {
		return this.observedPerceptions;
	}
	
	private StatePerception getStatePerception(State s, Boolean trueInState) {
		if (this.statePerceptionToUse.equals(StatePerception.PFStatePerception)) {
			return new PFStatePerception(s, this.propFuns, trueInState);
		}
		else if (this.statePerceptionToUse.equals(StatePerception.FullAttributeStatePerception)) {
			return new FullAttributeStatePerception(s, trueInState);
		}
		else if (this.statePerceptionToUse.equals(StatePerception.RelationalStatePerception)) {
			return new RelationalStatePerception(s, trueInState,AttributeRelation.getAllRelations());
		}
		
		else if (this.statePerceptionToUse.equals(StatePerception.ClassRelationalStatePerception)) {
			return new ClassRelationalStatePerception(s, trueInState,AttributeRelation.getAllRelations());
		}

		else {
			try {
				throw new StatePerceptionNotFoundException();
			} catch (StatePerceptionNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return null;
	}
	
	public Classifier getClassifier() {
		return this.classifier;
	}

	public void learn(State s, boolean trueInState) {
		StatePerception sPerc = getStatePerception(s, trueInState);

		observedPerceptions.addPerception(sPerc);
		this.classifierTrained = false;
	}

	public Boolean predict(State s) {
		//Train classifier if not trained 
		if (!classifierTrained) {
			trainClassifier();
			this.classifierTrained = true;
		}

		StatePerception toClassify = getStatePerception(s, null);

		return this.classify(toClassify);
	}

	public void trainClassifier() {
		Instances train = null;
		try {
			train = new Instances(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(observedPerceptions.getArffString(true).getBytes()))));
			train.setClassIndex(train.numAttributes()-1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			this.classifier.buildClassifier(train);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean dontKnow(double probTrue) {
		return Math.abs(probTrue-.5) < 0.0001;
	}

	/**
	 * Assumes trained classifier
	 * @param toClassify
	 * @return
	 */
	private Boolean classify(StatePerception toClassify) {
		//		return this.observedPerceptions.containsPerception(toClassify); //tabular model -- works!
		Instance instanceToClassify = null;
		double probTrue = 0;
		try {
			Instances test = new Instances(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(toClassify.getFullArffString(false).getBytes()))));
			test.setClassIndex(test.numAttributes()-1);
			instanceToClassify = test.firstInstance();
			probTrue = this.classifier.distributionForInstance(instanceToClassify)[0];


//			double[] prediction=this.classifier.distributionForInstance(instanceToClassify);
//
//			//output predictions
//			for(int i=0; i<prediction.length; i=i+1)
//			{
//				System.out.println("Probability of class "+
//						test.classAttribute().value(i)+
//						" : "+Double.toString(prediction[i]));
//			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		//Don't know condition

//		if (this.dontKnow(probTrue)) return null;

		boolean toReturn = probTrue > .5;

		
		return toReturn;
	}


	@Override
	public boolean conditionsOverlap(OOMDPConditionLearner otherLearner) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString() {
		if (this.classifier instanceof Logistic) {
			StringBuilder sb = new StringBuilder();
			List<String> attStrings = this.observedPerceptions.getAttributeNames();
			int numFeaturesToPrint = 4;
			this.trainClassifier();
			double [] [] coefficients = ((Logistic)this.classifier).coefficients();
			double [] firstClass = new double [coefficients.length];
			List<Double> listOfFirstClass = new ArrayList<Double>();
			List<Double> sortedListOfFirstClass = new ArrayList<Double>();
			List<Double> revSortedListOfFirstClass = new ArrayList<Double>();
			for (int i = 1; i < coefficients.length; i++) {
				firstClass[i] = coefficients[i][0];
				listOfFirstClass.add((coefficients[i][0]));
				sortedListOfFirstClass.add((coefficients[i][0]));
				revSortedListOfFirstClass.add((coefficients[i][0]));

			}
			if (revSortedListOfFirstClass.isEmpty()) return sb.toString();
			Collections.sort(revSortedListOfFirstClass);
			Collections.sort(sortedListOfFirstClass);
			Collections.reverse(sortedListOfFirstClass);

			double biggestNeg = revSortedListOfFirstClass.get(0);
			double biggestPos = sortedListOfFirstClass.get(0);
			for (int i = 0; i < listOfFirstClass.size() && i < numFeaturesToPrint; i++) {
				Double valToAddToPrint= null;
				if (Math.abs(biggestNeg) > Math.abs(biggestPos)) {
					valToAddToPrint = biggestNeg;
					revSortedListOfFirstClass.remove(0);
					biggestNeg = revSortedListOfFirstClass.get(0);
				}
				else {
					valToAddToPrint = biggestPos;
					sortedListOfFirstClass.remove(0);
					biggestPos = revSortedListOfFirstClass.get(0);
				}

				String valToPrint = new DecimalFormat("#.#").format(valToAddToPrint);

				int indexOfFeature = listOfFirstClass.indexOf(valToAddToPrint);
				String attName = attStrings.get(indexOfFeature).split(" ")[0];
				sb.append(attName + "(" + valToPrint +"), ");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);

			return sb.toString();
		}
		else {
			{
				return "PerceptionConditionLearner";
			}
		}
	}

}



