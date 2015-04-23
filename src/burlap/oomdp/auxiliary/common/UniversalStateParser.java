package burlap.oomdp.auxiliary.common;

import java.util.List;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;



/**
 * A StateParser class that can convert states for any possible input domain.
 * @author James MacGlashan
 *
 */
public class UniversalStateParser implements StateParser {

	protected Domain domain;
	
	
	/**
	 * This parser only requires that the source domain for the states is provided.
	 * @param domain the domain for which the states will be converted.
	 */
	public UniversalStateParser(Domain domain){
		this.domain = domain;
	}
	
	@Override
	public String stateToString(State s) {
		
		StringBuffer sbuf = new StringBuffer();
		
		List <ObjectInstance> objects = s.getAllObjects();
		for(int i = 0; i < objects.size(); i++){
			ObjectInstance o = objects.get(i);
			if(i > 0){
				sbuf.append("\n");
			}
			sbuf.append("##bo\n");
			ObjectClass oc = o.getObjectClass();
			sbuf.append(oc.name).append("\n");
			sbuf.append(o.getName());
			
			for(int j = 0; j < oc.attributeList.size(); j++){
				Attribute att = oc.attributeList.get(j);
				sbuf.append("\n");
				sbuf.append(att.name).append("\n");
				sbuf.append(o.getStringValForAttribute(att.name));
			}
			
		}
		
		return sbuf.toString();
	}

	@Override
	public State stringToState(String str) {
		
		State s = new MutableState();
		
		String [] obcomps = str.split("##bo\n");
		for(int i = 1; i < obcomps.length; i++){ //first comp will be empty so skip it
			
			String [] obinst = obcomps[i].split("\n");
			String ocname = obinst[0];
			String oname = obinst[1];
			
			ObjectInstance o = new ObjectInstance(domain.getObjectClass(ocname), oname);
			
			for(int j = 2; j < obinst.length; j+=2){
				String attName = obinst[j];
				String attV = obinst[j+1];
				o.setValue(attName, attV);
			}
			
			s.addObject(o);
			
		}
		
		
		return s;
	}

}
