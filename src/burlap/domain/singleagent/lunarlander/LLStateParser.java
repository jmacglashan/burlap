package burlap.domain.singleagent.lunarlander;

import java.util.List;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.states.State;


/**
 * A state parser specifically for the lunar lander domain. This format is only defined for states that have only one
 * landing pad object and agent object in them. The format is<br/>
 * angle x y vx vy<br/>
 * padLeftBound padRightBound padBottomBound padTopBound<br/>
 * obstacle1LeftBound obstacle1RightBound obstacle1BottomBound obstacle1TopBound<br/>
 * ...<br.>
 * obstacleNLeftBound obstacleNRightBound obstacleNBottomBound obstacleNTopBound<br/>
 * Where N is the number of obstacles in the state.
 * @author James MacGlashan
 *
 */
public class LLStateParser implements StateParser {

	Domain domain;
	
	
	/**
	 * Initializes for the specific lunar lander domain instance.
	 * @param domain
	 */
	public LLStateParser(Domain domain){
		this.domain = domain;
	}
	
	
	@Override
	public String stateToString(State s) {

		StringBuffer buf = new StringBuffer(256);
		
		ObjectInstance agent = s.getObjectsOfTrueClass(LunarLanderDomain.AGENTCLASS).get(0);
		ObjectInstance pad = s.getObjectsOfTrueClass(LunarLanderDomain.PADCLASS).get(0);
		List <ObjectInstance> obsts = s.getObjectsOfTrueClass(LunarLanderDomain.OBSTACLECLASS);
		
		//write agent
		buf.append(agent.getRealValForAttribute(LunarLanderDomain.AATTNAME)).append(" ");
		buf.append(agent.getRealValForAttribute(LunarLanderDomain.XATTNAME)).append(" ");
		buf.append(agent.getRealValForAttribute(LunarLanderDomain.YATTNAME)).append(" ");
		buf.append(agent.getRealValForAttribute(LunarLanderDomain.VXATTNAME)).append(" ");
		buf.append(agent.getRealValForAttribute(LunarLanderDomain.VYATTNAME)).append("\n");
		
		//write pad
		buf.append(pad.getRealValForAttribute(LunarLanderDomain.LATTNAME)).append(" ");
		buf.append(pad.getRealValForAttribute(LunarLanderDomain.RATTNAME)).append(" ");
		buf.append(pad.getRealValForAttribute(LunarLanderDomain.BATTNAME)).append(" ");
		buf.append(pad.getRealValForAttribute(LunarLanderDomain.TATTNAME));
		
		//write each obstacle
		for(ObjectInstance ob : obsts){
			buf.append("\n").append(ob.getRealValForAttribute(LunarLanderDomain.LATTNAME)).append(" ");
			buf.append(ob.getRealValForAttribute(LunarLanderDomain.RATTNAME)).append(" ");
			buf.append(ob.getRealValForAttribute(LunarLanderDomain.BATTNAME)).append(" ");
			buf.append(ob.getRealValForAttribute(LunarLanderDomain.TATTNAME));
		}
		
		
		return buf.toString();
	}

	@Override
	public State stringToState(String str) {

		str = str.trim();
		
		String [] lineComps = str.split("\n");
		String [] aComps = lineComps[0].split(" ");
		String [] pComps = lineComps[1].split(" ");
		
		State s = LunarLanderDomain.getCleanState(domain, lineComps.length-2);
		
		LunarLanderDomain.setAgent(s, Double.parseDouble(aComps[0]), Double.parseDouble(aComps[1]), Double.parseDouble(aComps[2]), Double.parseDouble(aComps[3]), Double.parseDouble(aComps[4]));
		LunarLanderDomain.setPad(s, Double.parseDouble(pComps[0]), Double.parseDouble(pComps[1]), Double.parseDouble(pComps[2]), Double.parseDouble(pComps[3]));
		
		for(int i = 2; i < lineComps.length; i++){
			String [] oComps = lineComps[i].split(" ");
			LunarLanderDomain.setObstacle(s, i-2, Double.parseDouble(oComps[0]), Double.parseDouble(oComps[1]), Double.parseDouble(oComps[2]), Double.parseDouble(oComps[3]));
		}
		
		return s;

	}

}
