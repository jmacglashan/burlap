package burlap.oomdp.singleagent.oo;

import burlap.oomdp.core.oo.OODomain;
import burlap.oomdp.core.oo.propositional.PropositionalFunction;
import burlap.oomdp.singleagent.SADomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author James MacGlashan.
 */
public class OOSADomain extends SADomain implements OODomain {


	protected Map<String, Class<?>> stateClassesMap = new HashMap<String, Class<?>>();

	protected Map<String, PropositionalFunction> propFunctionMap = new HashMap<String, PropositionalFunction>();

	@Override
	public List<Class<?>> stateClasses() {
		return new ArrayList<Class<?>>(stateClassesMap.values());
	}

	@Override
	public Class<?> stateClass(String className) {
		return stateClassesMap.get(className);
	}

	@Override
	public void addStateClass(String className, Class<?> stateClass) {
		this.stateClassesMap.put(className, stateClass);
	}

	@Override
	public List<PropositionalFunction> getPropFunctions() {
		return new ArrayList<PropositionalFunction>(this.propFunctionMap.values());
	}

	@Override
	public PropositionalFunction getPropFunction(String name) {
		return this.propFunctionMap.get(name);
	}

	@Override
	public void addPropFunction(PropositionalFunction prop) {
		this.propFunctionMap.put(prop.getName(), prop);
	}
}
