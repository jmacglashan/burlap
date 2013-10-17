package burlap.oomdp.singleagent.environment;

import java.util.ArrayList;
import java.util.HashMap;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;



/**
 * If a problem is best described by an Environment class, this domain wrapper takes a domain object
 * that specifies the state representation and action set, and creates a new domain object in which
 * actions of the same name and parameterization will make calls to the provided Environment class
 * and wait for it to return the resulting state before returning themselves.
 * @author James MacGlashan
 *
 */
public class DomainEnvironmentWrapper implements DomainGenerator {

	protected Domain srcDomain;
	protected Environment env;
	
	public DomainEnvironmentWrapper(Domain srcDomain, Environment env){
		this.srcDomain = srcDomain;
		this.env = env;
	}
	
	@Override
	public Domain generateDomain() {
		
		Domain d = new SADomain();
		
		for(Attribute a : srcDomain.getAttributes()){
			d.addAttribute(this.attCopy(d, a));
		}
		
		for(ObjectClass oc : srcDomain.getObjectClasses()){
			d.addObjectClass(this.ocCopy(d, oc));
		}
		
		for(PropositionalFunction pf : srcDomain.getPropFunctions()){
			d.addPropositionalFunction(this.pfCopy(d, pf));
		}
		
		for(Action a : srcDomain.getActions()){
			d.addAction(new EnvironmentExecutingAction(d, a));
		}
		
		return d;
	}

	
	
	protected Attribute attCopy(Domain d, Attribute src){
		Attribute a = new Attribute(d, src.name, src.type);
		a.discValues = new ArrayList<String>(src.discValues);
		a.discValuesHash = new HashMap<String, Integer>(src.discValuesHash);
		a.hidden = src.hidden;
		a.lowerLim = src.lowerLim;
		a.upperLim = src.upperLim;
		
		return a;
	}
	
	
	protected ObjectClass ocCopy(Domain d, ObjectClass src){
		ObjectClass oc = new ObjectClass(d, src.name, src.hidden);
		for(Attribute sa : src.attributeList){
			Attribute a = d.getAttribute(sa.name);
			oc.addAttribute(a);
		}
		
		return oc;
	}
	
	protected PropositionalFunction pfCopy(Domain d, PropositionalFunction src){
		
		PropositionalFunction pf = new IndirectPF(src.getName(), d, src.getParameterClasses(), src.getParameterOrderGroups(), src.getClassName(), src);
		
		return pf;
		
	}
	
	
	class IndirectPF extends PropositionalFunction{
		
		protected PropositionalFunction srcPF;
		
		
		
		public IndirectPF(String name, Domain domain,
				String[] parameterClasses, String[] parameterOrderGroup,
				String pfClassName, PropositionalFunction pf) {
			super(name, domain, parameterClasses, parameterOrderGroup, pfClassName);
			
			this.srcPF = pf;
		}
		
		
		
		@Override
		public boolean isTrue(State st, String[] params) {
			return srcPF.isTrue(st, params);
		}
		
	}
	
	
	
	class EnvironmentExecutingAction extends Action{

		
		public EnvironmentExecutingAction(Domain d, Action src){
			this.init(src.getName(), d, src.getParameterClasses(), src.getParameterOrderGroups());
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			
			State s = env.executeAction(name, params);
			
			return s;
		}
		
		
		
		
	}

}
