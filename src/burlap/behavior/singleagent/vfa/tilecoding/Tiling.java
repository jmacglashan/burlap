package burlap.behavior.singleagent.vfa.tilecoding;

/**
 * This class provides a tiling of a provided feature vector. Unlike the {@link Tiling} class, this tiling method does not take OO-MDP states as input; instead
 * states must first be converted into a feature vector and passed to this class. As a consequence, this tiling method is object identiferi *dependent*.
 * @author James MacGlashan
 *
 */
public class Tiling {

	/**
	 * The width of each dimension in this tiling
	 */
	protected double [] 		widths;
	
	/**
	 * The offset along each dimenision in the tiling
	 */
	protected double [] 		offset;
	
	/**
	 * The dimensions on which this tiling are dependent
	 */
	protected boolean [] 		dimensionMask;
	
	
	/**
	 * Constructs a tiling using the given widths, offset of each dimension's tiling, and assuming the tiling is over all dimensions.
	 * @param widths the widths of each dimension's tiling
	 * @param offset the offset of the tiling for each dimension (should be a faction of the width).
	 */
	public Tiling(double [] widths, double [] offset){
		this.widths = widths.clone();
		this.offset = offset.clone();
		this.dimensionMask = new boolean[this.widths.length];
		for(int i = 0; i < this.dimensionMask.length; i++){
			this.dimensionMask[i] = true;
		}
	}
	
	
	/**
	 * Constructs a tiling using the given widths, offset of each dimension's tiling, and which dimensions are actually used in the tiling.
	 * @param widths the widths of each dimension's tiling
	 * @param offset the offset of the tiling for each dimension (should be a faction of the width).
	 * @param dimensionMask specifies which dimensions are used in the tiling
	 */
	public Tiling(double [] widths, double [] offset, boolean [] dimensionMask){
		this.widths = widths.clone();
		this.offset = offset.clone();
		this.dimensionMask = dimensionMask.clone();
	}
	
	
	/**
	 * Retunrs a tile for the given input vector. The input must be the same dimensionality as the specifications for this tiling.
	 * @param input the input vector to tile
	 * @return the tile of the input vector
	 */
	public FVTile getFVTile(double [] input){
		
		if(input.length != this.widths.length){
			throw new RuntimeException("Error: the input feature vector to be tiled is a different dimensionality " +
					"than the dimensionality on which this tiling was defined; " +
					"e.g., the specified widths vector for this tiling is a different dimension than the input vector.");
		}
		
		int [] tiledVector = new int[input.length];
		for(int i = 0; i < input.length; i++){
			if(this.dimensionMask[i]){
				tiledVector[i] = (int)Math.floor((input[i] - this.offset[i]) / this.widths[i]);
			}
			else{
				tiledVector[i] = 0;
			}
		}
		
		FVTile tile = new FVTile(tiledVector);
		
		return tile;
	}
	
	
	/**
	 * Stores a tiled version of a feature vector with a hashcode and equality comparions methods implemented.
	 * @author James MacGlashan
	 *
	 */
	public class FVTile{
		public int [] tiledVector;
		protected int hashCode;
		
		public FVTile(int [] tiledVector){
			this.tiledVector = tiledVector;
			this.hashCode = 0;
			for(int i = 0; i < tiledVector.length; i++){
				if(Tiling.this.dimensionMask[i]){
					this.hashCode = 31*this.hashCode + tiledVector[i];
				}
			}
		}
		
		@Override
		public boolean equals(Object other){
			if(!(other instanceof FVTile)){
				return false;
			}
			
			FVTile o = (FVTile)other;
			if(this.tiledVector.length != o.tiledVector.length){
				return false;
			}
			
			for(int i = 0; i < this.tiledVector.length; i++){
				if(this.tiledVector[i] != o.tiledVector[i]){
					return false;
				}
			}
			
			return true;
		}
		
		
		@Override
		public int hashCode(){
			return this.hashCode;
		}
		
	}
	
}
