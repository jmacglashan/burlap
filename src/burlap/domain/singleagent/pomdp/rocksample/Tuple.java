package burlap.domain.singleagent.pomdp.rocksample;

public class Tuple<X, Y> {
	
	private final X x;
	private final Y y;

	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	public X getX() {
		return this.x;
	}

	public Y getY() {
		return this.y;
	}
}
