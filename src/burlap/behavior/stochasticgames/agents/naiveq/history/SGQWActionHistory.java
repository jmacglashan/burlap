package burlap.behavior.stochasticgames.agents.naiveq.history;

import burlap.behavior.stochasticgames.agents.naiveq.SGNaiveQLAgent;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.SGDomain;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * A Tabular Q-learning [1] algorithm for stochastic games formalisms that augments states with the actions each agent took in n
 * previous time steps. If the constructor is not passed the maximum number of players and an {@link ActionIdMap} to use,
 * then when the first game starts, it will be initialized to an {@link ParameterNaiveActionIdMap} and the number of
 * players will be set to the number of players in the world which this agent has joined. If the world contains
 * parameterized actions, this may be a problem and you should use the {@link #SGQWActionHistory(SGDomain, double, double, burlap.oomdp.statehashing.HashableStateFactory, int, int, ActionIdMap)}
 * constructor to resolve action parameterization instead.
 * 
 * <p>
 * 1. Watkins, Christopher JCH, and Peter Dayan. "Q-learning." Machine learning 8.3-4 (1992): 279-292. <p>
 * @author James MacGlashan
 *
 */
public class SGQWActionHistory extends SGNaiveQLAgent {

	
	/**
	 * the joint action history
	 */
	protected LinkedList <JointAction>			history;
	
	/**
	 * The size of action history to store.
	 */
	protected int								historySize;
	
	/**
	 * a map from actions to int values which can be used to fill in an action history attribute value
	 */
	protected ActionIdMap						actionMap = null;
	
	
	/**
	 * The object class that will be used to represent a history component. A history component
	 * consists a player identifier, the action that player took, and how long ago that action was taken. A object
	 * instance of this class will be created for each player in the world and for each of the n time steps
	 * that this learning algorithm is told to keep.
	 */
	protected ObjectClass						classHistory;
	
	
	/**
	 * A constant for the name of the history time index attribute. For instance, a history object representing the
	 * action of an agent in the previous time step will have a value of 1 for this attribute
	 */
	public static final String					ATTHNUM = "histNum";
	
	/**
	 * A constant for the name of the attribute used to define which agent in the world this history object represents
	 */
	public static final String					ATTHPN = "histPN";
	
	/**
	 * A constant for the name of the attribute used to define which action an agent took
	 */
	public static final String					ATTHAID = "histAID";
	
	
	/**
	 * A constant for the name of the history object class. 
	 */
	public static String						CLASSHISTORY = "histAID";
	
	
	
	/**
	 * Initializes the learning algorithm using 0.1 epsilon greedy learning strategy/policy
	 * @param d the domain in which the agent will act
	 * @param discount the discount factor
	 * @param learningRate the learning rate
	 * @param hashFactory the state hashing factory to use
	 * @param historySize the number of previous steps to remember and with which to augment the state space
	 * @param maxPlayers the maximum number of players that will be in the game
	 * @param actionMap a mapping from actions to integer identifiers for them
	 */
	public SGQWActionHistory(SGDomain d, double discount, double learningRate, HashableStateFactory hashFactory, int historySize, int maxPlayers, ActionIdMap actionMap) {
		super(d, discount, learningRate, hashFactory);
		this.historySize = historySize;
		this.actionMap = actionMap;
		
		
		this.initializeHistoryAugmentedDomain(maxPlayers);
		
	}
	
	/**
	 * Initializes the learning algorithm using 0.1 epsilon greedy learning strategy/policy
	 * @param d the domain in which the agent will act
	 * @param discount the discount factor
	 * @param learningRate the learning rate
	 * @param hashFactory the state hashing factory to use
	 * @param historySize the number of previous steps to remember and with which to augment the state space
	 */
	public SGQWActionHistory(SGDomain d, double discount, double learningRate, HashableStateFactory hashFactory, int historySize) {
		super(d, discount, learningRate, hashFactory);
		this.historySize = historySize;
	}
	
	/**
	 * Initializes the history augmented domain/state representation the agent will use
	 * @param maxPlayers the maximum number of players in the game
	 */
	protected void initializeHistoryAugmentedDomain(int maxPlayers){
		Domain augmentingDomain = new SGDomain();
		
		Attribute histNum = new Attribute(augmentingDomain, ATTHNUM, Attribute.AttributeType.DISC);
		histNum.setDiscValuesForRange(0, historySize-1, 1);
		
		Attribute histPN = new Attribute(augmentingDomain, ATTHPN, Attribute.AttributeType.DISC);
		histPN.setDiscValuesForRange(0, maxPlayers-1, 1);
		
		Attribute histAID = new Attribute(augmentingDomain, ATTHAID, Attribute.AttributeType.DISC);
		histAID.setDiscValuesForRange(0, this.actionMap.maxValue(), 1); //maxValue is when it the action is undefined from no history occurance
		
		classHistory = new ObjectClass(augmentingDomain, CLASSHISTORY);
		classHistory.addAttribute(histNum);
		classHistory.addAttribute(histPN);
		classHistory.addAttribute(histAID);


	}

	@Override
	public void gameStarting() {
		this.history = new LinkedList<JointAction>();
		if(this.actionMap == null){
			this.initializeActionMapAndAugmentedDomain();
		}
	}
	
	/**
	 * Initializes the action map to be an instance of {@link ParameterNaiveActionIdMap} and then initializes
	 * the history augmented domain using the max players as the number of players in the world which this agent
	 * has joined.
	 */
	protected void initializeActionMapAndAugmentedDomain(){
		this.actionMap = new ParameterNaiveActionIdMap(this.domain);
		int maxPlayers = this.world.getRegisteredAgents().size();
		this.initializeHistoryAugmentedDomain(maxPlayers);
	}
	
	
	
	@Override
	public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		GroundedSGAgentAction myAction = jointAction.action(worldAgentName);
		QValue qe = this.getQ(s, myAction);
		
		State augS = this.getHistoryAugmentedState(s);
		
		//update history
		if(history.size() == historySize){
			history.removeLast();
		}
		history.addFirst(jointAction);
		
		State augSP = this.getHistoryAugmentedState(sprime);
		
		
		if(internalRewardFunction != null){
			jointReward = internalRewardFunction.reward(augS, jointAction, augSP);
		}
		
		
		double r = jointReward.get(worldAgentName);
		double maxQ = 0.;
		if(!isTerminal){
			maxQ = this.getMaxQValue(sprime); //no need to use augmented states because the method will implicitly get them from the state hash call
		}
		

		qe.q = qe.q + this.learningRate.pollLearningRate(this.totalNumberOfSteps, s, myAction) * (r + (this.discount * maxQ) - qe.q);
		
		this.totalNumberOfSteps++;

		
	}
	
	
	/**
	 * Takes an input state and returns an augmented state with the history of actions each agent previously took.
	 * @param s the input state to augment
	 * @return an augmented state with the history of actions each agent previously took.
	 */
	protected State getHistoryAugmentedState(State s){
		
		State augS = s.copy();
		
		int h = 0;
		for(JointAction ja : history){
			
			for(GroundedSGAgentAction gsa : ja){
				augS.addObject(this.getHistoryObjectInstanceForAgent(gsa, h));
			}
			
			h++;
		}
		
		if(h < this.historySize){
			List <SGAgent> agents = world.getRegisteredAgents();
			while(h < this.historySize){
				for(SGAgent a : agents){
					augS.addObject(this.getHistoryLessObjectInstanceForAgent(a.getAgentName(), h));
				}
				h++;
			}
		}
		
		
		return augS;
		
	}
	
	
	/**
	 * Returns a history object instance for the corresponding action and how far back in history it occurred
	 * @param gsa the action that was taken (which includes which agent took it)
	 * @param h how far back in history the action was taken.
	 * @return a history object instance for the corresponding action and how far back in history it occurred
	 */
	protected ObjectInstance getHistoryObjectInstanceForAgent(GroundedSGAgentAction gsa, int h){
		
		String aname = gsa.actingAgent;
		
		ObjectInstance o = new MutableObjectInstance(classHistory, aname + "-h" + h);
		o.setValue(ATTHNUM, h);
		o.setValue(ATTHPN, world.getPlayerNumberForAgent(aname));
		o.setValue(ATTHAID, actionMap.getActionId(gsa));
		
		return o;
		
	}
	
	
	/**
	 * Returns a history object instance for a given agent in which the action that was taken is unset because
	 * the episode has not last h steps.
	 * @param aname the name of agent for which the history object should be returned
	 * @param h how many step backs this object instance represents
	 * @return a history object instance
	 */
	protected ObjectInstance getHistoryLessObjectInstanceForAgent(String aname, int h){
		
		ObjectInstance o = new MutableObjectInstance(classHistory, aname + "-h" + h);
		o.setValue(ATTHNUM, h);
		o.setValue(ATTHPN, world.getPlayerNumberForAgent(aname));
		o.setValue(ATTHAID, actionMap.maxValue());
		
		return o;
		
	}
	
	@Override
	protected HashableState stateHash(State s){
		State augS = this.getHistoryAugmentedState(this.storedMapAbstraction.abstraction(s));
		return hashFactory.hashState(augS);
	}

}
