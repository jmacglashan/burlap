package burlap.statehashing.masked;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.statehashing.simple.IDSimpleHashableState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * @author James MacGlashan.
 */
public class IDMaskedHashableState extends IDSimpleHashableState{

	public MaskedConfig config;

	public IDMaskedHashableState() {
	}

	public IDMaskedHashableState(MaskedConfig config) {
		this.config = config;
	}

	public IDMaskedHashableState(State s, MaskedConfig config) {
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
				int nameHash = o.name().hashCode();
				int totalHash = oHash + 31 * classNameHash + 31 * 31 * nameHash;
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
		if (s1 == s2) {
			return true;
		}

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
				ObjectInstance oo = s2.object(o.name());
				if(oo == null || !flatStatesEqual(o, oo)){
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



}
