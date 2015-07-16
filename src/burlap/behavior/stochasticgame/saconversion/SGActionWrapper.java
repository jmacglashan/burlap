package burlap.behavior.stochasticgame.saconversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cern.colt.Arrays;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.datastructures.HashedAggregator;
import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.SingleAction;

/**
 * This method takes in an acting agent's SingleAction, the game's joint action model, the agent's name and 
 * a mapping from all other agents' to their policies.
 * 
 * It then creates a new TransitionProbability list that models other agents as being 
 * part of the environment as opposed to a part of the joint action being taken.
 * 
 * @author Betsy Hilliard (betsy@cs.brown.edu)
 *
 */

public class SGActionWrapper extends Action {

	protected SingleAction singleAction;
	protected JointActionModel jaModel;
	protected String agentName;
	protected Map<String,Policy> agentPolicyMap;
	protected StateHashFactory hashFactory;
	protected SADomain saDomain;
	protected List<SingleAction> actions;

	public SGActionWrapper(SingleAction singleAction, JointActionModel jaModel,
			String agentName, Map<String,Policy> agentPolicyMap, SADomain saDomain, StateHashFactory hashFactory, List<SingleAction> actions) {
		super(singleAction.actionName, saDomain, singleAction.parameterTypes);
		this.saDomain = saDomain;
		this.singleAction = singleAction ;
		this.jaModel = jaModel;
		this.agentName = agentName;
		this.agentPolicyMap = agentPolicyMap;
		this.hashFactory = hashFactory;
		this.actions = actions;

	}

	public SGActionWrapper(String name, Domain domain, String parameterClasses) {
		super(name, domain, parameterClasses);
		// TODO Auto-generated constructor stub
	}

	public SGActionWrapper(String name, Domain domain, String[] parameterClasses) {
		super(name, domain, parameterClasses);
		// TODO Auto-generated constructor stub
	}

	public SGActionWrapper(String name, Domain domain,
			String[] parameterClasses, String[] parameterOrderGroups) {
		super(name, domain, parameterClasses, parameterOrderGroups);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns true if this action can be applied in this specified state with the specified parameters.
	 * Default behavior is that an action can be applied in any state, but the {@link #applicableInState(State, String [])}
	 * method will need to be override if this is not the case.
	 * @param s the state in which to check if this action can be applied
	 * @param params a comma delineated String specifying the action object parameters
	 * @return true if this action can be applied in this specified state with the specified parameters; false otherwise.
	 */

	public boolean applicableInState(State s, String [] params){
		// this method should simply return the result of the input singleAction isApplicableInState method
		return singleAction.isApplicableInState(s, agentName, params);
	}



	@Override
	public List<TransitionProbability> getTransitions(State s, String [] params){

		/*
		 * In the getTransitions method you want to marginalize over all possible joint actions. 
		 * The acting agent's action is simply defined by this Action's input SingleAction and the
		 *  parameters passed to getTransitions. 
		 *  The rest of the agent's actions get derived from
		 *  their corresponding policy object. However, policies may be stochastic so you need to 
		 *  consider the action selection probabilities and you need to consider all combinatorial 
		 *  selections by other agents. 
		 *  
		 *  You can get action selection probabilities for each non-zero 
		 *  probable action for an agent from the policy object with the method: 
		 *  
		 * getActionDistributionForState(State s). 
		 * 
		 * You should assume (and type cast) the output to be 
		 * GroundedSingleAction. Given one combination of GroundedSingleAction selections for each 
		 * agent, you can then construct a joint action for it. You can create an empty joint action as follows
		 * 
		 * JointAction ja = new JointAction();
		 * 
		 * Add to the joint action the grounded version of the acting agent's corresponding SingleAction and parameters:
		 * 
		 * ja.addAction(new GroundedSingleAction(agentName, singleAction, params));
		 * 
		 * For each GroundedSingleAction for a single combination of the other agents' actions, add those in similar ways.
		 * Once you have a JointAction object, you can use the JointActionModel to determine the outcome states with its method:
		 * 
		 * transitionProbsFor(State s, JointAction ja)
		 * 
		 * Combine its outcome state probabilities with the probability of that joint action being selected. After 
		 * marginalizing over all joint actions, you now have your final set of outcome states and their 
		 * probabilities which can be returned.
		 */

		// this is the data structure we will use to create a new TransitionProbability list
		// the state is the next state, given that action (and all possible other agent actions)
		HashedAggregator<StateHashTuple> transProbsToNextStates = new HashedAggregator<StateHashTuple>();

		//this map is from agent names to their policy at the given state
		Map<String, List<ActionProb>> mapOfActionProbs = new HashMap<String, List<ActionProb>>();

		//this is used to map a counter to the agent name 
		Map<Integer,String> mapping = new HashMap<Integer,String>();
		List<ActionProb> actionProbs = new ArrayList<ActionProb>();
		int i = 0;
		if(agentPolicyMap==null){
			//create the map from all agents to policy at this state

			//for all actions create uniform 
			List<GroundedSingleAction> gsas = singleAction.getAllGroundedActionsFor(s, agentName);

			for(GroundedSingleAction gsa : gsas){
				actionProbs.add(new ActionProb(gsa, 1.0/gsas.size()));
			}
			mapOfActionProbs.put(agentName, actionProbs);
			mapping.put(i,agentName);
			i=i+1;

		}else{

			//create the map from all agents to policy at this state

			for(String agent : agentPolicyMap.keySet()){
				actionProbs = agentPolicyMap.get(agent).getActionDistributionForState(s);
				mapOfActionProbs.put(agent, actionProbs);
				mapping.put(i,agent);
				i=i+1;
			}
		}

		//add the non-normalized probability of all next states for all combinations of other agent actions
		transProbsToNextStates = addAllCombinations(s, mapOfActionProbs, mapping,params);
		//System.out.println("TransProbsSize: "+transProbsToNextStates.size());

		double total = 0.0;
		//sum over all next states so me can normalize
		for(Map.Entry<StateHashTuple, Double> e: transProbsToNextStates.entrySet()){

			double val = e.getValue();
			//printTheState(e.getKey().s);
			//System.out.println("VAL: "+val);
			total+=val;
		}

		//System.out.println("Total: "+total);
		//normalize and add to new List<TransitionProbability>

		List<TransitionProbability> newTPs = new ArrayList<TransitionProbability>();
		
		for(Map.Entry<StateHashTuple, Double> e: transProbsToNextStates.entrySet()){
			TransitionProbability newTP = new TransitionProbability(e.getKey().s,e.getValue()/total);
			newTPs.add(newTP);
		}
		

		return newTPs;
	}

	/**
	 * addAllCombinations loops over all sets of other agent actions and calculates the probability 
	 * of all other agents taking that action and ending up in each next state
	 */
	private HashedAggregator<StateHashTuple> addAllCombinations(State s, Map<String, List<ActionProb>> lists,
			Map<Integer,String> mapping,String[] params){
		//this array just tracks where we are in each list of actions for each other agent

		int[] counters = new int[lists.size()];

		//
		//Map<State,Double> probOfNextStates = new HashMap<State,Double>();
		HashedAggregator<StateHashTuple> probOfNextStates = new HashedAggregator<StateHashTuple>(0.0);

		do{
			//the transition probabilities for this set of other agent actions to all next states
			List<TransitionProbability> transProbs = getNextStatesTransitionProbabilities(s, counters, mapping, lists,params);
			//System.out.println("TransProbs Size: "+transProbs.size());
			//multiply 
			for(TransitionProbability tp : transProbs){
				//System.out.println("Num states found: "+probOfNextStates.keySet().size());

				probOfNextStates.add(hashFactory.hashState(tp.s),tp.p);


			}
			//System.out.println("Incrementing: "+Arrays.toString(counters));
			//System.out.println("probOfNextStates size: "+probOfNextStates.size());
		}while(increment(counters, mapping,lists));
		
		
		

		return probOfNextStates;
	}


	/**
	 * this increments the counters so that we go through all other agent actions
	 * @param counters
	 * @param mapping
	 * @param sets
	 * @return
	 */
	private static boolean increment(int[] counters, Map<Integer, String> mapping, Map<String, List<ActionProb>> sets){
		for(int i=counters.length-1;i>=0;i--){
			if(counters[i] < sets.get(mapping.get(i)).size()-1){
				counters[i]++;
				return true;
			} else {
				counters[i] = 0;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param state
	 * @param counters
	 * @param mapping
	 * @param sets
	 * @param params
	 * @return
	 */
	private List<TransitionProbability> getNextStatesTransitionProbabilities(State state, int[] counters, Map<Integer, String> mapping, 
			Map<String, List<ActionProb>> sets, String [] params){

		//we are going to create a fake joint action for a set of other agent actions
		JointAction ja = new JointAction();

		//add the action agent's action
		ja.addAction(new GroundedSingleAction(agentName,singleAction,params));

		//create and calculate the probability of this joint action
		double probOfJA = 1.0;
		for(int i = 0; i<counters.length;i++){
			String otherAgentName = mapping.get(i);
			int actionNum = counters[i];
			//System.out.println("ActionNum: "+actionNum);
			// Add to the joint action the grounded version of the acting agent's corresponding SingleAction and parameters:
			//TODO: fix this. it's right somewhere
			
			AbstractGroundedAction aga = sets.get(otherAgentName).get(actionNum).ga; //this is a GroundedAction
			//temp.actionName();
			//saDomain.getAction(temp.actionName());
			
			
			
			Random rand = new Random();
			List<GroundedSingleAction>  otherAgentsActions = new ArrayList<GroundedSingleAction>();
			for(SingleAction sa : actions){
				if(sa.actionName==aga.actionName()){
					otherAgentsActions = sa.getAllGroundedActionsFor(state, otherAgentName);
				}
			}
			
			ja.addAction(otherAgentsActions.get(rand.nextInt(otherAgentsActions.size())));
			//System.out.println("JA: "+ja);
			//multiply in the prob of this action from the other agents' policies
			probOfJA=probOfJA*agentPolicyMap.get(otherAgentName).getProbOfAction(state, aga);
		}
		//these are the probabilities of going to each possible next state
		List<TransitionProbability> transProbs = jaModel.transitionProbsFor(state, ja);
		//System.out.println("Size before mult: "+transProbs.size());
		//then multiply by the prob of taking that joint action
		List<TransitionProbability> newTransProbs = new ArrayList<TransitionProbability>();
		for(TransitionProbability tp : transProbs){
			TransitionProbability newTP = new TransitionProbability(tp.s, tp.p*probOfJA);
			newTransProbs.add(newTP);
		}

		//return the next states
		return newTransProbs;
	}

	/**
	 * performActionHelper gets the TransitionProbability list and samples 
	 * from the next states based on the dynamics
	 */
	@Override
	protected State performActionHelper(State s, String[] params) {
		List<TransitionProbability> transitions = getTransitions(s, params);
		//TODO: Should this sample from the distribution or uniformly??????
		Random rand = new Random();
		double val = rand.nextDouble();

		double total = transitions.get(0).p;
		int i = 0;
		while(total<val){
			i++;
			total+=transitions.get(i).p;
		}
		return transitions.get(i).s;


	}

	public void printTheState(State s){
		List<ObjectInstance> agentLocals = s.getObjectsOfClass(GridGame.CLASSAGENT);
		for(ObjectInstance oi : agentLocals){
			System.out.println("Agent: "+oi.getName()+" X: "+oi.getIntValForAttribute(GridGame.ATTX)+
					" Y: "+oi.getIntValForAttribute(GridGame.ATTY));
		}
	}

}
