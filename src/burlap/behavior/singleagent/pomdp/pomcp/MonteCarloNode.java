package burlap.behavior.singleagent.pomdp.pomcp;


import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.BeliefStatistic;
import burlap.oomdp.singleagent.pomdp.PODomain;

public class MonteCarloNode extends BeliefStatistic{
	protected Map<HistoryElement, MonteCarloNode> children = new HashMap<HistoryElement, MonteCarloNode>();
	protected List<State> beliefParticles = new ArrayList<State>();
	protected List<Double> valueHistory = new ArrayList<Double>();
	protected Random rand = RandomFactory.getMapped(0);
//	private Random rand = new java.util.Random();

	protected int visits;
	protected double value;

	public MonteCarloNode(PODomain domain) {
		super(domain);
		this.visits = 0;
		this.value = 0;
	}
 	
	public MonteCarloNode(PODomain domain,int vis, double val) {
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

	public synchronized void addParticle(State s) {
		beliefParticles.add(s);
	}

	public synchronized void  saveValues() {
		valueHistory.add(value);
		for(HistoryElement he : children.keySet()) {
			children.get(he).saveValues();
		}
	}

	public synchronized  List<Double> getValueHistory() {
		return valueHistory;
	}

	public synchronized void removeParticle(int index) {
		beliefParticles.remove(index);
	}

	public synchronized void removeRandomParticle() {
//		beliefParticles.remove(rand.nextInt(beliefParticles.size()));
		beliefParticles.remove(rand.nextInt(Integer.MAX_VALUE) % beliefParticles.size());
//		beliefParticles.remove(new java.util.Random(89).nextInt(beliefParticles.size()));
	}

	public  State sampleParticles() {
//		return beliefParticles.get(rand.nextInt(beliefParticles.size()));
		return beliefParticles.get(rand.nextInt(Integer.MAX_VALUE) % beliefParticles.size());
//		return beliefParticles.get(new java.util.Random(89).nextInt(beliefParticles.size()));
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

	public synchronized GroundedAction bestExploringAction(double C) {
		double maxValue = Double.NEGATIVE_INFINITY;
		GroundedAction bestAction = null;

		for(HistoryElement h : children.keySet()) {
			MonteCarloNode child = children.get(h);
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

	public synchronized MonteCarloNode advance(GroundedAction a) {
		return advance(new HistoryElement(a));
	}

	public synchronized MonteCarloNode advance(State o) {
		return advance(new HistoryElement(o));
	}

	public synchronized MonteCarloNode advance(HistoryElement h) {
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
		this.children.put(h, new MonteCarloNode(this.domain,vis, val));
	}

	public boolean isLeaf() {
		return this.children.isEmpty();
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
	
	public void setParticles(List<State> particlesList) {
		this.beliefParticles = particlesList;
	}

	public Map<HistoryElement, MonteCarloNode> getMap() {
		return children;
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
	
	

	@Override
	public List<State> getStatesWithNonZeroProbability() {
		// this is not a unique set of states
		Set<State> stateSet = new HashSet<State>(this.beliefParticles);
		return new ArrayList<State>(stateSet);
	}

	@Override
	public double belief(State s) {
		double count = 0.0;
		for(State stateParticle:this.beliefParticles){
			if(stateParticle.equals(s)){
				count+=1.0;
			}
		}
		return count/this.beliefParticles.size();
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
		
	}
	
	
	
	public BeliefState getCompleteBelief() {
		BeliefState bs = new BeliefState(this.domain); 

		StateEnumerator senum = (this.domain).getStateEnumerator();
		if(senum==null){
			System.err.println("MonteCarloNode: getting belief state needs a state enumerator, which is not declared within the domain");
			return null;
		}
		double sumToAdd = 1.0/this.particleCount();

		// default belief points zero
		double[] beliefPoints = new double[senum.numStatesEnumerated()]; 
		for(State s : this.getParticles()){
			int temp = senum.getEnumeratedID(s);
			beliefPoints[temp] += sumToAdd; 
		}

		bs.setBeliefCollection(beliefPoints);

		return bs;
	}
	 
}
