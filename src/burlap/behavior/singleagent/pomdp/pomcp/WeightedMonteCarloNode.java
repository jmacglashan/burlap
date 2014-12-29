package burlap.behavior.singleagent.pomdp.pomcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.BeliefStatistic;
import burlap.oomdp.singleagent.pomdp.PODomain;


public class WeightedMonteCarloNode extends BeliefStatistic{
	protected Map<HistoryElement, WeightedMonteCarloNode> children = new HashMap<HistoryElement, WeightedMonteCarloNode>();
	protected List<State> beliefParticles = new ArrayList<State>();
	private List<Double> particleWeights = new ArrayList<Double>();
	private List<Double> weightCDF = new ArrayList<Double>();
	private boolean listNormalizedFlag = false;
	private Random randomNumber = RandomFactory.getMapped(0);
	

	protected int visits;
	protected double value;
	 
	public WeightedMonteCarloNode(PODomain domain) {
		super(domain);
		this.value=0;
		this.visits=1;
	}
	
	public WeightedMonteCarloNode(PODomain domain, int vis, double val){
		super(domain);
		this.visits = vis;
		this.value = val;
	}
	
	public void pruneExcept(GroundedAction a) {
		pruneExcept(new HistoryElement(a));
	}

	public void pruneExcept(State o) {
		pruneExcept(new HistoryElement(o));
	}

	public void pruneExcept(HistoryElement h) {
		if(this.isLeaf()) return;

		List<HistoryElement> tbd = new ArrayList<HistoryElement>(); 
		for(HistoryElement elem : children.keySet()) {
			if(!elem.equals(h)) {
				children.get(elem).prune();
				tbd.add(elem);
			}
		}	
		for(HistoryElement elem : tbd) {
			children.remove(elem);
		}
		
	}

	public void prune() {
		if(this.isLeaf()) return;
		for(HistoryElement elem : children.keySet()) {
			children.get(elem).prune();
		}
		children.clear();
	}

	public synchronized void visit() {
		visits++;
	}
	
	public synchronized void augmentValue(double inc) {
		value += inc;
	}
	
	
	
	//overriding the addParticle method
	public synchronized void addParticle(State s, State o, GroundedAction a){
		beliefParticles.add(s);
		particleWeights.add(((PODomain)this.domain).getObservationFunction().getObservationProbability(o, s, a));
		listNormalizedFlag=false;
	}
	
	public synchronized void addParticle(State s, double probability){
		beliefParticles.add(s);
		particleWeights.add(probability);
		listNormalizedFlag=false;
	}
	
	
	public int particleCount() {
		return beliefParticles.size();
	}

	public GroundedAction bestRealAction() {
		if(this.isLeaf()) System.out.println("Requested action from leaf... :(");

		double maxValue = Double.NEGATIVE_INFINITY;
		GroundedAction bestAction = null;
		
		for(HistoryElement h : children.keySet()) {
			if(children.get(h).getValue() > maxValue) {
				maxValue = children.get(h).getValue();
				bestAction = h.getAction();
			}
		}	
		return bestAction;
	}
	
	
	public GroundedAction bestExploringAction(double C) {
		double maxValue = Double.NEGATIVE_INFINITY;
		GroundedAction bestAction = null;

		for(HistoryElement h : children.keySet()) {
			WeightedMonteCarloNode child = children.get(h);
			int childVisitCount = child.getVisits();
			double test =Double.MAX_VALUE;
			if(childVisitCount > 0){

			test = child.getValue() + C * Math.sqrt(Math.log(this.getVisits()+1)/childVisitCount);
			}
			

			if(test > maxValue) {
				maxValue = test;
				bestAction = h.getAction();
			}
		}
		return bestAction;
	}
	
	
	public int getVisits() {
		return this.visits;
	}

	public double getValue() {
		return this.value;
	}
	
	public List<State> getParticles() {
		return this.beliefParticles;
	}
	
	public  Map<HistoryElement, WeightedMonteCarloNode> getMap() {
		return this.children;
	}
	
	
	
	public WeightedMonteCarloNode advance(GroundedAction a) {
		return advance(new HistoryElement(a));
	}

	public WeightedMonteCarloNode advance(State o) {
		return advance(new HistoryElement(o));
	}

	public WeightedMonteCarloNode advance(HistoryElement h) {
		return children.get(h);
	}

	public boolean hasChild(State o) {
		return children.containsKey(new HistoryElement(o));
	}
	
	public boolean hasChild(GroundedAction a) {
		return children.containsKey(new HistoryElement(a));
	}

	public synchronized void addChild(State o) {
		addChild(new HistoryElement(o), 0, 0);
	}

	public synchronized void addChild(GroundedAction a) {
		addChild(new HistoryElement(a), 0, 0);
	}

	public synchronized void addChild(HistoryElement h) {
		addChild(h, 0, 0);
	}

	public synchronized void addChild(State o, int vis, double val) {
		addChild(new HistoryElement(o), vis, val);
	}

	public synchronized void addChild(GroundedAction a, int vis, double val) {
		addChild(new HistoryElement(a), vis, val);
	}

	public synchronized void addChild(HistoryElement h, int vis, double val) {
		this.children.put(h, new WeightedMonteCarloNode(this.domain,vis, val));
	}
	
	
	
	
	public State sampleParticles(){
		if(!listNormalizedFlag){
			System.err.println("WeightedMonteCarloNode: list not normalized before sampling");
			System.exit(-1);
		}
		double temp = randomNumber.nextDouble();
		for (int count=0;count<weightCDF.size();count++){
//			System.out.println("CDF value: index- "+count + "value"+weightCDF.get(count));
			if (temp < weightCDF.get(count)) {
//				System.out.println("random number: " + temp + " index returned " + count);
				return beliefParticles.get(count);
			}
			
		}
		System.err.println("WeightedMonteCarloNode: sampleparticles weights not summing to 1, tempRandom: " + temp + "lastCountOfCDF: "+ weightCDF.get(weightCDF.size()-1));
		return new State();
	}
	
	public void normalizeWeights(){
		if(this.particleWeights.size()==0){
			System.err.println("No weights added");
			System.exit(-1);
		}
		if(this.particleWeights.size()!=this.beliefParticles.size()){
			System.err.println("Weights list does not have the same size as the belief particles list");
			System.exit(-1);
		}
		listNorm(this.particleWeights);
		this.weightCDF.addAll(listCDF(particleWeights));
		listNormalizedFlag = true;
	}
	
	
	public boolean isLeaf() {
		return this.children.isEmpty();
	}
	
	public List<Double> getWeights(){
		return this.particleWeights;
	}
	
	public void setParticles(List<State> particlesList, List<Double> wtLst) {
		this.beliefParticles = particlesList;
		this.particleWeights = wtLst;
	}

	@Override
	public List<State> getStatesWithNonZeroProbability() {
		Set<State> stateSet = new HashSet<State>(this.beliefParticles);
		return new ArrayList<State>(stateSet);
	}

	@Override
	public double belief(State s) {
		this.normalizeWeights();
		double count = 0.0;
		for(int i = 0;i<this.beliefParticles.size();i++){
			if(this.beliefParticles.get(i).equals(s)){
				count+=this.particleWeights.get(i);
			}
		}
		return count;
	}

	@Override
	public State sampleStateFromBelief() {
		return sampleParticles();
	}

	@Override
	public void clearBeliefCollection() {
		this.prune();
		this.children.clear();
		this.beliefParticles.clear();
		this.particleWeights.clear();
		
	}
	
	
	public BeliefState getCompleteBelief() {
		
		this.normalizeWeights();
		BeliefState bs = new BeliefState(this.domain); 

		StateEnumerator senum = (this.domain).getStateEnumerator();
		
		if(senum==null){
			System.err.println("WeightedMonteCarloNode: getting belief state needs a state enumerator, which is not declared within the domain");
			return null;
		}
		
		double[] beliefPoints = new double[senum.numStatesEnumerated()]; 
		for(int i = 0;i<this.beliefParticles.size();i++){
			State s = this.beliefParticles.get(i);
			int temp = senum.getEnumeratedID(s);
			beliefPoints[temp] += this.particleWeights.get(i); 
		}

		bs.setBeliefCollection(beliefPoints);

		return bs;
	}
	
	
	public static void listNorm(List<Double> list) {
		double sum = 0.0;
		for(int i = 0; i < list.size(); ++i) {
			sum += list.get(i);
		}
		for(int i = 0; i < list.size(); ++i) {
			list.set(i, list.get(i)/sum);
		}
	}
	
	public static List<Double> listCDF(List<Double> list) {
		double sum = 0.0;
		List<Double> returnList = new ArrayList<Double>(); 
		for(int i = 0; i < list.size(); ++i) {
			sum += list.get(i);
			returnList.add(sum);
		}
		return returnList;
	}
	
	
	public List<QValue> returnQVlaueForNode(State s){
		List<QValue> returnQValueList = new ArrayList<QValue>();
		for(HistoryElement h : this.children.keySet()){
			GroundedAction a = h.getAction();
			if(a==null){
				System.out.println("MonteCarloNode: Queried actions from a node without actions as children");
				return null;
			}
			else{
				returnQValueList.add(new QValue(s, a, this.advance(a).value));
			}
		}
		return returnQValueList;
	}

}
