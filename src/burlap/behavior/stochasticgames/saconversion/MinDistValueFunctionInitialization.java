package burlap.behavior.stochasticgames.saconversion;

import java.util.List;

import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;

public class MinDistValueFunctionInitialization implements
ValueFunctionInitialization {

	private Domain ggDomain; 
	private String agentName;
	private double goalReward;
	
	public MinDistValueFunctionInitialization(Domain ggDomain, String agentName, double goalReward){
		this.ggDomain = ggDomain;
		this.agentName = agentName;
		this.goalReward = goalReward;
	}

	@Override
	public double value(State s) {

		String an = GridWorldDomain.CLASSAGENT;

		//find what agent we are
		int agentNum = 0;
		if(agentName.contains("1")){
			agentNum = 1;
		}

		ObjectInstance agent = null;
		List<ObjectInstance> agentList  = s.getObjectsOfClass(an);
		for (ObjectInstance o : agentList){
			if(o.getStringValForAttribute(GridGame.ATTPN).contains(Integer.toString(agentNum))){
				agent = o;
			}
		}

		double closestMDist=Double.MAX_VALUE;
		if(agent != null){
			//get agent x,y position
			int ax = agent.getIntValForAttribute(GridWorldDomain.ATTX);
			int ay = agent.getIntValForAttribute(GridWorldDomain.ATTY);

			int lx;
			int ly;
			
			double mdist;
			List<ObjectInstance> objects = s.getObjectsOfClass(GridGame.CLASSGOAL);
			//System.out.println("Num goals" +objects.size());
			
			//loop over all goal objects and find the closest to the agent
			for(ObjectInstance oi : objects){

				//System.out.println("AgentNum: "+agentNum+" GT: "+oi.getIntValForAttribute("gt"));
				if(oi.getIntValForAttribute("gt")==agentNum+1){

					lx = oi.getIntValForAttribute(GridWorldDomain.ATTX);
					ly = oi.getIntValForAttribute(GridWorldDomain.ATTY);

					mdist = Math.abs(ax-lx) + Math.abs(ay-ly);
					//System.out.println("Agent "+agentNum+"'s goal: "+lx+", "+ly+" agent at: "+ax+", "+ay+" mdist: "+mdist);
					if(mdist<closestMDist){
						closestMDist = mdist;
					}
				}
			}
			
		}
		return goalReward-1.0*closestMDist;
	}

	@Override
	public double qValue(State s, AbstractGroundedAction a) {
		value(s);
		return value(s);
	}

}
