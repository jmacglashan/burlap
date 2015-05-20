package burlap.domain.singleagent.gridworld;

import java.util.List;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

/**
 * Simplified state parser for grid world states. Format:<br/>
 * ax ay, l1x l1y l1t, l2x l2y lt2, ..., lnx lny lnt 
 * <br/>
 * where ax and ay is the agent x and y position and lix liy is the ith location objects x and y position and lit is the type of the ith locaiton object.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class GridWorldStateParser implements StateParser {

	protected Domain				domain;
	
	
	public GridWorldStateParser(int width, int height){
		GridWorldDomain generator = new GridWorldDomain(width, height);
		this.domain = generator.generateDomain();
	}
	
	public GridWorldStateParser(Domain domain){
		this.domain = domain;
	}
	
	@Override
	public String stateToString(State s) {
		
		StringBuffer sbuf = new StringBuffer(256);
		
		ObjectInstance a = s.getObjectsOfClass(GridWorldDomain.CLASSAGENT).get(0);
		List<ObjectInstance> locs = s.getObjectsOfClass(GridWorldDomain.CLASSLOCATION);
		
		String xa = GridWorldDomain.ATTX;
		String ya = GridWorldDomain.ATTY;
		String lt = GridWorldDomain.ATTLOCTYPE;
		
		sbuf.append(a.getIntValForAttribute(xa)).append(" ").append(a.getIntValForAttribute(ya));
		for(ObjectInstance l : locs){
			sbuf.append(", ").append(l.getIntValForAttribute(xa)).append(" ").append(l.getIntValForAttribute(ya)).append(" ").append(l.getIntValForAttribute(lt));
		}
		
		
		return sbuf.toString();
	}

	@Override
	public State stringToState(String str) {
		
		String [] obcomps = str.split(", ");
		
		String [] acomps = obcomps[0].split(" ");
		int ax = Integer.parseInt(acomps[0]);
		int ay = Integer.parseInt(acomps[1]);
		
		int nl = obcomps.length - 1;
		
		State s = GridWorldDomain.getOneAgentNLocationState(domain, nl);
		GridWorldDomain.setAgent(s, ax, ay);
		
		for(int i = 1; i < obcomps.length; i++){
			String [] lcomps = obcomps[i].split(" ");
			int lx = Integer.parseInt(lcomps[0]);
			int ly = Integer.parseInt(lcomps[1]);
			
			if(lcomps.length < 3){
				GridWorldDomain.setLocation(s, i-1, lx, ly);
			}
			else{
				int lt = Integer.parseInt(lcomps[2]);
				GridWorldDomain.setLocation(s, i-1, lx, ly, lt);
			}
			
		}
		
		
		return s;
	}

}
