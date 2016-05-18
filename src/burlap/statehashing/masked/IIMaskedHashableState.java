package burlap.statehashing.masked;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.statehashing.simple.IISimpleHashableState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * @author James MacGlashan.
 */
public class IIMaskedHashableState extends IISimpleHashableState {

	public MaskedConfig config;

	public IIMaskedHashableState() {
	}

	public IIMaskedHashableState(MaskedConfig config) {
		this.config = config;
	}

	public IIMaskedHashableState(State s, MaskedConfig config) {
		super(s);
		this.config = config;
	}


	@Override
	protected int computeOOHashCode(OOState s) {
		List<Integer> hashCodes = new ArrayList<Integer>(s.numObjects());
		List<ObjectInstance> objects = s.objects();
		for(int i = 0; i < s.numObjects(); i++){
			ObjectInstance o = objects.get(i);
			if(!config.maskedObjectClasses.contains(o.className())) {
				int oHash = this.computeFlatHashCode(o);
				int classNameHash = o.className().hashCode();
				int totalHash = oHash + 31 * classNameHash;
				hashCodes.add(totalHash);
			}
		}

		//sort for invariance to order
		Collections.sort(hashCodes);

		//combine
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
		for(int hashCode : hashCodes){
			hashCodeBuilder.append(hashCode);
		}

		return hashCodeBuilder.toHashCode();
	}

	@Override
	protected void appendHashCodeForValue(HashCodeBuilder hashCodeBuilder, Object key, Object value) {
		if(!config.maskedVariables.contains(key)){ //only consider variables that have not been masked
			super.appendHashCodeForValue(hashCodeBuilder, key, value);
		}
	}

	@Override
	protected boolean ooStatesEqual(OOState s1, OOState s2) {
		if(s1 == s2){
			return true;
		}

		Set<String> matchedObjects = new HashSet<String>();
		for(Map.Entry<String, List<ObjectInstance>> e1 : OOStateUtilities.objectsByClass(s1).entrySet()){

			String oclass = e1.getKey();

			if(config.maskedObjectClasses.contains(oclass)){
				continue;
			}

			List<ObjectInstance> objects = e1.getValue();

			List<ObjectInstance> oobjects = s2.objectsOfClass(oclass);
			if(objects.size() != oobjects.size()){
				return false;
			}

			for(ObjectInstance o : objects){
				boolean foundMatch = false;
				for(ObjectInstance oo : oobjects){
					String ooname = oo.name();
					if(matchedObjects.contains(ooname)){
						continue;
					}
					if(flatStatesEqual(o, oo)){
						foundMatch = true;
						matchedObjects.add(ooname);
						break;
					}
				}
				if(!foundMatch){
					return false;
				}
			}

		}

		return true;
	}

	@Override
	protected boolean valuesEqual(Object key, Object v1, Object v2) {
		if(config.maskedVariables.contains(key)){
			return true;
		}
		return super.valuesEqual(key, v1, v2);
	}

	@Override
	public IISimpleHashableState copy() {
		return new IIMaskedHashableState(s.copy(), config);
	}
}
