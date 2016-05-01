package burlap.domain.singleagent.lunarlander;


import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.state.State;
import burlap.oomdp.legacy.StateParser;
import burlap.oomdp.stateserialization.SerializableState;
import burlap.oomdp.stateserialization.SerializableStateFactory;

import java.util.List;

/**
 * A {@link burlap.oomdp.stateserialization.SerializableStateFactory} for simple string representations of {@link burlap.domain.singleagent.lunarlander.LunarLanderDomain} states.
 * @author James MacGlashan.
 */
public class SerializableLunarLanderStateFactory implements SerializableStateFactory{

	@Override
	public SerializableState serialize(State s) {
		return new SerializableLunarLanderState(s);
	}

	@Override
	public Class<?> getGeneratedClass() {
		return SerializableLunarLanderState.class;
	}


	public static class SerializableLunarLanderState extends SerializableState {


		public String stringRep;

		public SerializableLunarLanderState() {
		}

		public SerializableLunarLanderState(State s) {
			super(s);
		}

		@Override
		public void serialize(State s) {
			this.stringRep = stateToString(s);
		}

		@Override
		public State deserialize(Domain domain) {
			return stringToState(domain, this.stringRep);
		}

	}

	public static class LunarLanderStateParser implements StateParser {

		Domain domain;

		public LunarLanderStateParser(Domain domain) {
			this.domain = domain;
		}

		@Override
		public String stateToString(State s) {
			return SerializableLunarLanderStateFactory.stateToString(s);
		}

		@Override
		public State stringToState(String str) {
			return SerializableLunarLanderStateFactory.stringToState(domain, str);
		}
	}



	public static String stateToString(State s){
	    StringBuilder buf = new StringBuilder(256);

		OldObjectInstance agent = s.getObjectsOfClass(LunarLanderDomain.AGENTCLASS).get(0);
		OldObjectInstance pad = s.getObjectsOfClass(LunarLanderDomain.PADCLASS).get(0);
		List<OldObjectInstance> obsts = s.getObjectsOfClass(LunarLanderDomain.OBSTACLECLASS);

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
		for(OldObjectInstance ob : obsts){
			buf.append("\n").append(ob.getRealValForAttribute(LunarLanderDomain.LATTNAME)).append(" ");
			buf.append(ob.getRealValForAttribute(LunarLanderDomain.RATTNAME)).append(" ");
			buf.append(ob.getRealValForAttribute(LunarLanderDomain.BATTNAME)).append(" ");
			buf.append(ob.getRealValForAttribute(LunarLanderDomain.TATTNAME));
		}


		return buf.toString();
	}

	public static State stringToState(Domain domain, String str){

		str = str.trim();

		String [] lineComps = str.split("\n");
		String [] aComps = lineComps[0].split(" ");
		String [] pComps = lineComps[1].split(" ");

		State s = LunarLanderDomain.getCleanState(domain, lineComps.length - 2);

		LunarLanderDomain.setAgent(s, Double.parseDouble(aComps[0]), Double.parseDouble(aComps[1]), Double.parseDouble(aComps[2]), Double.parseDouble(aComps[3]), Double.parseDouble(aComps[4]));
		LunarLanderDomain.setPad(s, Double.parseDouble(pComps[0]), Double.parseDouble(pComps[1]), Double.parseDouble(pComps[2]), Double.parseDouble(pComps[3]));

		for(int i = 2; i < lineComps.length; i++){
			String [] oComps = lineComps[i].split(" ");
			LunarLanderDomain.setObstacle(s, i-2, Double.parseDouble(oComps[0]), Double.parseDouble(oComps[1]), Double.parseDouble(oComps[2]), Double.parseDouble(oComps[3]));
		}

		return s;

	}

}
