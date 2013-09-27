package burlap.domain.singleagent.gridworld;

import java.util.List;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


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
		
		ObjectInstance a = s.getObjectsOfTrueClass(GridWorldDomain.CLASSAGENT).get(0);
		List<ObjectInstance> locs = s.getObjectsOfTrueClass(GridWorldDomain.CLASSLOCATION);
		
		String xa = GridWorldDomain.ATTX;
		String ya = GridWorldDomain.ATTY;
		
		sbuf.append(a.getDiscValForAttribute(xa)).append(" ").append(a.getDiscValForAttribute(ya));
		for(ObjectInstance l : locs){
			sbuf.append(", ").append(l.getDiscValForAttribute(xa)).append(" ").append(l.getDiscValForAttribute(ya));
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
			
			GridWorldDomain.setLocation(s, i-1, lx, ly);
			
		}
		
		
		return s;
	}

}
