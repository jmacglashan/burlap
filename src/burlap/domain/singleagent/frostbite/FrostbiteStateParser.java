package burlap.domain.singleagent.frostbite;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;

import java.util.List;

/**
 * @author Phillipe Morere
 */
public class FrostbiteStateParser implements StateParser{

	Domain domain;


	/**
	 * Initializes for the specific frostbite domain instance.
	 *
	 * @param domain
	 */
	public FrostbiteStateParser(Domain domain) {
		this.domain = domain;
	}

	@Override
	public String stateToString(State s) {
		StringBuffer buf = new StringBuffer(256);

		ObjectInstance agent = s.getObjectsOfClass(FrostbiteDomain.AGENTCLASS).get(0);
		ObjectInstance igloo = s.getObjectsOfClass(FrostbiteDomain.IGLOOCLASS).get(0);
		List<ObjectInstance> platforms = s.getObjectsOfClass(FrostbiteDomain.PLATFORMCLASS);

		//write agent
		buf.append(agent.getRealValForAttribute(FrostbiteDomain.XATTNAME)).append(" ");
		buf.append(agent.getRealValForAttribute(FrostbiteDomain.YATTNAME)).append("\n");

		//write pad
		buf.append(igloo.getRealValForAttribute(FrostbiteDomain.BUILDINGATTNAME));

		//write each obstacle
		for (ObjectInstance ob : platforms) {
			buf.append("\n").append(ob.getRealValForAttribute(FrostbiteDomain.XATTNAME)).append(" ");
			buf.append(ob.getRealValForAttribute(FrostbiteDomain.YATTNAME)).append(" ");
			buf.append(ob.getRealValForAttribute(FrostbiteDomain.SIZEATTNAME)).append(" ");
			buf.append(ob.getRealValForAttribute(FrostbiteDomain.ACTIVATEDATTNAME));
		}

		return buf.toString();
	}

	@Override
	public State stringToState(String str) {
		str = str.trim();

		String[] lineComps = str.split("\n");
		String[] aComps = lineComps[0].split(" ");
		String[] pComps = lineComps[1].split(" ");

		State s = FrostbiteDomain.getCleanState(domain);

		FrostbiteDomain.setAgent(s, Integer.parseInt(aComps[0]), Integer.parseInt(aComps[1]));
		FrostbiteDomain.setIgloo(s, Integer.parseInt(pComps[0]));

		for (int i = 2; i < lineComps.length; i++) {
			String[] oComps = lineComps[i].split(" ");
			FrostbiteDomain.setPlatform(s, i - 2, Integer.parseInt(oComps[0]), Integer.parseInt(oComps[1]),
					Integer.parseInt(oComps[2]), Boolean.parseBoolean(oComps[3]));
		}
		return s;
	}

}
