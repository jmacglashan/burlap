package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import cc.mallet.types.Dirichlet;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class Affordance {
	
	private PropositionalFunction pf;
	private PropositionalFunction goal;
	private Map<Action,Double> actionMap;
	private Map<Action,Integer> actionCounts;
	private int[] actionNumCounts;
	private List<Action> allActions;
	private boolean dirFlag;
	private Random r = new Random();
	private Dirichlet actionDistr;
	private Dirichlet actionNumDistr;
	private double dirichletHyperParam = 1.0;
	private boolean learningMode = true;
	private boolean hardFlag = false; // For NEML results, fix after
	
	public Affordance(PropositionalFunction pf, PropositionalFunction goal, List<Action> actions) {
		this.pf = pf;
		this.goal = goal;
		this.allActions = actions;

		this.actionMap = new HashMap<Action,Double>();
		
		this.allActions = actions;

		for(Action a : this.allActions) {
			// Set all values to 1 if no softness provided. ("hard" affordances)
			this.actionMap.put(a, 1.0);
		}
		
		initCounts();
	}
	
	public Affordance(PropositionalFunction pf, PropositionalFunction goal, Map<Action,Double> actionMap) {
		this.pf = pf;
		this.goal = goal;
		this.dirFlag = false;
		this.actionMap = actionMap;

		// Populate action list if given a soft hashmap (mapping actions to probabilities)
		this.allActions = new ArrayList<Action>();
		for (Action a : actionMap.keySet()) {
			this.allActions.add(a);
		}
		
		initCounts();
	}
	
	public void setHardFlag(boolean newVal) {
		this.hardFlag = newVal;
	}
	
	private void initCounts() {
		this.actionCounts = new HashMap<Action,Integer>();
		for (Action a: this.allActions) {
			this.actionCounts.put(a, 0);
		}
		
		this.actionNumCounts = new int[this.actionCounts.size() + 1];
		for (int i = 0; i <= this.actionCounts.size(); i++) {
			this.actionNumCounts[i] = 0;
		}
		
	}

	
	/**
	 * Sets dirichlet distributions with the parameters collected during learning
	 */
	public void postProcess() {
		// Cast action counts from Collection<Integer> counts to double[] for use in Dirichlet
		double[] alpha = dirichletHyper(this.actionCounts.size(), dirichletHyperParam);
		List<Integer> actCounts = new ArrayList<Integer>(this.actionCounts.values());
		for (int i = 0; i < alpha.length; i++) {
			alpha[i] += (double)(actCounts.get(i));
		}
		
		this.actionDistr = new Dirichlet(alpha);
		
		double[] beta = dirichletHyper(this.actionCounts.size(), dirichletHyperParam);
		for (int i = 0; i < alpha.length; i++) {
			beta[i] += (double)this.actionNumCounts[i];
		}
		
		this.actionNumDistr = new Dirichlet(beta);
	}
	
	private double[] dirichletHyper(int n, double d) {
		double[] hyper = new double[n];
		for (int i = 0; i < hyper.length; i++)
			hyper[i] = d;
		return hyper;
	}
	
	/**
	 * Returns a list of actions that can be used
	 * @return
	 */
	public List<Action> getMeSumActions() {
		if(this.hardFlag){
			return this.allActions;
		}
		
		int[] sizes = this.actionNumDistr.drawObservation(1);
		int n = -1;
		
		// Loop over sizes until we find the one that was sampled
		for (int i = 0; i < sizes.length; i++) {
			if (sizes[i] > 0) {
				n = i + 1;
				break;
			}
		}
//		n += 0;
		if (n > 16)
			return null;
//			n = 16;
		n = 4;
//		int n = this.actionNumDistr.drawObservation(1)[0] + 1;
		// n will ALWAYS be an array of a single value
		
		List<Action> selectedActions = new ArrayList<Action>();
//		System.out.println("Action Distr: " + this.actionDistr);
		while(selectedActions.size() < n) {
			int[] actIndices = this.actionDistr.drawObservation(1);
			Action act = null;
			
			// Loop over action indices until we find the one that was sampled
			for (int i = 0; i < actIndices.length; i++) {
				if (actIndices[i] > 0) {
					act = this.allActions.get(i);
					break;
				}
			}

			if (!selectedActions.contains(act)) {
				selectedActions.add(act);
			}

		}

		return selectedActions;
	}
	
	public PropositionalFunction getPreCondition() {
		return this.pf;
	}
	
	public PropositionalFunction getPostCondition() {
		return this.goal;
	}
	
	public List<Action> getActions() {
		return this.allActions;
	}
	
	public void setActionCounts(Map<Action,Integer> actionCounts) {
		this.actionCounts = actionCounts;
	}

	public void setActionNumCounts(int[] actionNumCounts) {
		this.actionNumCounts = actionNumCounts;
	}

	
	public boolean isApplicable(State s, PropositionalFunction goal) {
		
		// Ignores goal right now
		if (this.pf.isTrue(s)) {
			return true;
		}
		
		return false;
	}
	
	public List<GroundedAction> getApplicableActions(State st, PropositionalFunction goal) {
		List<GroundedAction> result = new ArrayList<GroundedAction>();
		
		// Choose learning actions or hard coded actions
		List<Action> actions = getMeSumActions();
		
		for(Action a : actions) {
			
			// Check if this affordance applies (NEED TO ADD GOAL RELATIVE PART)
			if (this.pf.isTrue(st)) {
				// Do weird state binding thing

				List <List <String>> bindings = st.getPossibleBindingsGivenParamOrderGroups(a.getParameterClasses(), a.getParameterOrderGroups());
				for(List <String> params : bindings){

					String [] aprams = params.toArray(new String[params.size()]);
					GroundedAction gp = new GroundedAction(a, aprams);
					result.add(gp);
				}				
			}
		}
		
		if(result.size() == 0){
			ObjectInstance agent = st.getObject("agent0");
			int ax = agent.getDiscValForAttribute("x");
			int ay = agent.getDiscValForAttribute("y");
			int az = agent.getDiscValForAttribute("z");
			int x = 0;
			x++;
//			System.out.println(this.pf.getName());
		}
		
		return result;
	}	
	public boolean containsAction(Action a) {
		return this.allActions.contains(a);
	}
	
	public void updateActionCount(Action a) {
		Integer count = this.actionCounts.get(a);
		if (a.getName() == "placeDown") {
			int x = 0;
		}
		this.actionCounts.put(a, count + 1);
	}
	
	public void updateActionSetSizeCount(int size) {
		this.actionNumCounts[size]++;
	}
	
	public void printCounts() {
		System.out.println("Affordance pred: " + this.pf.getName());
		for (Action a: this.allActions) {
			System.out.println(a.getName() + ": " + this.actionCounts.get(a));
		}
		for (int i = 0; i < this.actionCounts.size(); i++) {
			System.out.println(i + ": " + this.actionNumCounts[i]);
		}
	}
	
	/**
	 * Assumes that the header is on the first line it reads!
	 * @param d
	 * @param scnr
	 */
	public static Affordance load(Domain d, Scanner scnr) {
		String line;
		boolean readHeader = true;
		boolean readActCounts = true;
		
		PropositionalFunction pf = null;
		PropositionalFunction goal = null;
		int[] actionNumCounts = null;
		Map<Action,Integer> actionCounts = new HashMap<Action,Integer>();
		while (scnr.hasNextLine()) {
			line = scnr.nextLine();
			
			if (line.equals("===")) {
				// Reached the end of an affordance definition
				break;
			}
			
			if (line.equals("---")) {
				// Finished reading action counts -- ready to start reading action set sizes
				readActCounts = false;
				actionNumCounts = new int[actionCounts.size()];
				continue;
			}
			
			String[] info = line.split(",");
			
			if (readHeader) {
				// We haven't read the header yet, so do that
				String pfName = info[0];
				String goalName = info[1];
				
				pf = d.getPropFunction(pfName);
				goal = d.getPropFunction(goalName);
				readHeader = false;
				continue;
			}
			
			if (readActCounts) {
				// Read the action counts
				String actName = info[0];
				Integer count = Integer.parseInt(info[1]);
				actionCounts.put(d.getAction(actName), count);
			} else {
				// Read the action set size counts
				Integer size = Integer.parseInt(info[0]);
				Integer count = Integer.parseInt(info[1]);
				
				actionNumCounts[size] = count;
			}
			
		}
		List<Action> allActions = new ArrayList<Action>(actionCounts.keySet()); 
		Affordance aff = new Affordance(pf, goal, allActions);
		aff.setActionCounts(actionCounts);
		aff.setActionNumCounts(actionNumCounts);

		return aff;
	}
	
	public String toString() {
		String out = "";

		// Header information (what the affordance's PF and LGD are)
		out += this.pf.getName() + "," + this.goal.getName() + "\n";
				
		// Add action counts
		for (Action a: this.allActions) {
			out += a.getName() + "," + this.actionCounts.get(a) + "\n";
		}
		out += "---\n";

		// Add set size counts
		for (int i = 0; i < this.actionCounts.size(); i++) {
			out += i + "," + this.actionNumCounts[i] + "\n";
		}
		out += "===\n";
		return out;
	}
	
}
