package burlap.behavior.singleagent.pomdp.utils;

public class Triple<X, Y, Z> {
	private X x;
	private Y y;
	private Z z;

	public Triple(X x, Y y, Z z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public X getX() {
		return this.x;
	}
	
	public Y getY() {
		return this.y;
	}
	
	public Z getZ() {
		return this.z;
	}

	@Override
	public int hashCode() {
		return Integer.parseInt("" + x.hashCode() + y.hashCode() + z.hashCode());
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Triple)) return false;
		
		Triple<X, Y, Z> tup = null;
		
		try {
			tup = (Triple<X, Y, Z>) o;
		} catch (ClassCastException e) {
			return false;
		}
		
		return tup.getX().equals(x) && tup.getY().equals(y) && tup.getZ().equals(z);
	}
}