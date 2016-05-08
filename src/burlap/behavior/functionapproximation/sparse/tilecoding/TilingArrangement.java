package burlap.behavior.functionapproximation.sparse.tilecoding;

/**
 * Enum for specifying whether tilings should have their tile alignments should be chossen so that they
 * are randomly jittered from each other, or if each subsequent tiling should be offset by a uniform amount.
 * @author James MacGlashan
 *
 */
public enum TilingArrangement {

	RANDOM_JITTER(0),
	UNIFORM(1);

	private final int value;

	TilingArrangement(int i){
		this.value = i;
	}

	public int toInt(){
		return this.value;
	}

	public static TilingArrangement fromInt(int i){
		switch(i){
			case 0:
				return RANDOM_JITTER;
			case 1:
				return UNIFORM;
			default:
				return null;
		}
	}

}
