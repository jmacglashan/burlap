package burlap.domain.singleagent.minecraft;

import java.util.List;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


public class MinecraftStateParser implements StateParser {

	protected Domain				domain;
	
	
	public MinecraftStateParser(int width, int height){
		MinecraftDomain generator = new MinecraftDomain();
		this.domain = generator.generateDomain();
	}
	
	public MinecraftStateParser(Domain domain){
		this.domain = domain;
	}
	
	@Override
	public String stateToString(State s) {
		
		StringBuffer sbuf = new StringBuffer(256);
		
		ObjectInstance a = s.getObjectsOfTrueClass(MinecraftDomain.CLASSAGENT).get(0);
		ObjectInstance goal = s.getObjectsOfTrueClass(MinecraftDomain.CLASSGOAL).get(0);
		
		String xa = MinecraftDomain.ATTX;
		String ya = MinecraftDomain.ATTY;
		String za = MinecraftDomain.ATTZ;
		
		sbuf.append(a.getDiscValForAttribute(xa)).append(",").append(a.getDiscValForAttribute(ya)).append(",").append(a.getDiscValForAttribute(za)).append(" ");
		sbuf.append(goal.getDiscValForAttribute(xa)).append(",").append(goal.getDiscValForAttribute(ya)).append(",").append(goal.getDiscValForAttribute(za)).append(" ");
		
		
		return sbuf.toString();
	}

	@Override
	public State stringToState(String str) {
		// TODO Auto-generated method stub
		return null;
	}

/*	@Override
	public State stringToState(String str) {
		System.out.println(str);
		String [] obcomps = str.split(" ");
		
		String [] acomps = obcomps[0].split(",");
		int ax = Integer.parseInt(acomps[0]);
		int ay = Integer.parseInt(acomps[1]);
		//int az = Integer.parseInt(acomps[2]);
		
		int ng = obcomps.length - 1;
		
		State s = MinecraftDomain.getCleanState(domain, null, null);
		MinecraftDomain.setAgent(s, ax, ay, 2, 4);
		
		String [] gcomps = obcomps[1].split(",");
		int gx = Integer.parseInt(gcomps[0]);
		int gy = Integer.parseInt(gcomps[1]);
		//int gz = Integer.parseInt(lcomps[2]);
		
		MinecraftDomain.setGoal(s, gx, gy, 2);
		
		return s;
	}*/

}

