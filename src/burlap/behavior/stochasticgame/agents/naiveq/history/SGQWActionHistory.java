package burlap.behavior.stochasticgame.agents.naiveq.history;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.behavior.stochasticgame.agents.naiveq.SGQLAgent;
import burlap.behavior.stochasticgame.agents.naiveq.SGQValue;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.stocashticgames.Agent;
import burlap.oomdp.stocashticgames.GroundedSingleAction;
import burlap.oomdp.stocashticgames.JointAction;
import burlap.oomdp.stocashticgames.SGDomain;

public class SGQWActionHistory extends SGQLAgent {

	protected LinkedList <JointAction>			history;
	protected int								historySize;
	protected ActionIdMap						actionMap;
	
	protected ObjectClass						classHistory;
	
	
	
	public static final String					ATTHNUM = "histNum";
	public static final String					ATTHPN = "histPN";
	public static final String					ATTHAID = "histAID";
	
	public static String						CLASSHISTORY = "histAID";
	
	
	public SGQWActionHistory(SGDomain d, double discount, double learningRate, StateHashFactory hashFactory, int historySize, int maxPlayers, ActionIdMap actionMap) {
		super(d, discount, learningRate, hashFactory);
		this.historySize = historySize;
		this.actionMap = actionMap;
		
		
		//set up history augmentation object information
		Domain augmentingDomain = new SGDomain();
		
		Attribute histNum = new Attribute(augmentingDomain, ATTHNUM, Attribute.AttributeType.DISC);
		histNum.setDiscValuesForRange(0, historySize-1, 1);
		
		Attribute histPN = new Attribute(augmentingDomain, ATTHPN, Attribute.AttributeType.DISC);
		histPN.setDiscValuesForRange(0, maxPlayers-1, 1);
		
		Attribute histAID = new Attribute(augmentingDomain, ATTHAID, Attribute.AttributeType.DISC);
		histAID.setDiscValuesForRange(0, actionMap.maxValue(), 1); //maxValue is when it the action is undefined from no history occurance
		
		classHistory = new ObjectClass(augmentingDomain, CLASSHISTORY);
		classHistory.addAttribute(histNum);
		classHistory.addAttribute(histPN);
		classHistory.addAttribute(histAID);
		
		
		List <Attribute> attsForHistoryHashing = new ArrayList<Attribute>();
		attsForHistoryHashing.add(histNum);
		attsForHistoryHashing.add(histPN);
		attsForHistoryHashing.add(histAID);
		
		//ugly, but not sure how to resolve at the moment...
		if(this.hashFactory instanceof DiscreteStateHashFactory){
			((DiscreteStateHashFactory) this.hashFactory).setAttributesForClass(CLASSHISTORY, attsForHistoryHashing);
		}
	}

	@Override
	public void gameStarting() {
		this.history = new LinkedList<JointAction>();
	}
	
	
	
	@Override
	public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		GroundedSingleAction myAction = jointAction.action(worldAgentName);
		SGQValue qe = this.getSGQValue(s, myAction);
		
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
		

		qe.q = qe.q + this.learningRate * (r + (this.discount * maxQ) - qe.q);

		
	}
	
	
	
	protected State getHistoryAugmentedState(State s){
		
		State augS = s.copy();
		
		int h = 0;
		for(JointAction ja : history){
			
			for(GroundedSingleAction gsa : ja){
				augS.addObject(this.getHistoryObjectInstanceForAgent(gsa, h));
			}
			
			h++;
		}
		
		if(h < this.historySize){
			List <Agent> agents = world.getRegisteredAgents();
			while(h < this.historySize){
				for(Agent a : agents){
					augS.addObject(this.getHistoryLessObjectInstanceForAgent(a.getAgentName(), h));
				}
				h++;
			}
		}
		
		
		return augS;
		
	}
	
	
	protected ObjectInstance getHistoryObjectInstanceForAgent(GroundedSingleAction gsa, int h){
		
		String aname = gsa.actingAgent;
		
		ObjectInstance o = new ObjectInstance(classHistory, aname + "-h" + h);
		o.setValue(ATTHNUM, h);
		o.setValue(ATTHPN, world.getPlayerNumberForAgent(aname));
		o.setValue(ATTHAID, actionMap.getActionId(gsa));
		
		return o;
		
	}
	
	
	protected ObjectInstance getHistoryLessObjectInstanceForAgent(String aname, int h){
		
		ObjectInstance o = new ObjectInstance(classHistory, aname + "-h" + h);
		o.setValue(ATTHNUM, h);
		o.setValue(ATTHPN, world.getPlayerNumberForAgent(aname));
		o.setValue(ATTHAID, actionMap.maxValue());
		
		return o;
		
	}
	
	@Override
	protected StateHashTuple stateHash(State s){
		State augS = this.getHistoryAugmentedState(this.storedMapAbstraction.abstraction(s));
		return hashFactory.hashState(augS);
	}

}
