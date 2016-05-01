package burlap.oomdp.core.oo.state;

/**
 * @author James MacGlashan.
 */
public class OOStateShallow extends OOStateConcrete {
	public OOStateShallow() {
	}

	public OOStateShallow(OOState srcOOState) {
		super();
		for(ObjectInstance o : srcOOState.objects()){
			this.addObject(o);
		}
	}
}
