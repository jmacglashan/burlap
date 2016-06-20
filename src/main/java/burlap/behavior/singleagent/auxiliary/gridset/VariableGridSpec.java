package burlap.behavior.singleagent.auxiliary.gridset;

/**
 * @author James MacGlashan.
 */
public class VariableGridSpec {


	/**
	 * The lower value of the variable on the gird
	 */
	public double lowerVal;

	/**
	 * The upper value of the variable on the grid
	 */
	public double upperVal;

	/**
	 * The number of grid points along this variable
	 */
	public int numGridPoints;


	public VariableGridSpec(double lowerVal, double upperVal, int numGridPoints) {
		this.lowerVal = lowerVal;
		this.upperVal = upperVal;
		this.numGridPoints = numGridPoints;
	}

	/**
	 * Returns the width of a grid cell along this attribute. Returns 0 if the number of grid points
	 * is 1. This value is defined as (upperVal - lowerVal) / (numGridPoints - 1)
	 * @return the width of a grid cell along this attribute
	 */
	public double cellWidth(){
		if(numGridPoints == 1){
			return 0.;
		}
		return (this.upperVal - this.lowerVal) / (double)(this.numGridPoints-1);
	}

}
