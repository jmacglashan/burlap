package burlap.behavior.singleagent.vfa.cmac;

import burlap.oomdp.core.Attribute;


/**
 * Specifies how a single attribute of a specific object class is to be tiled.
 * @author James MacGlashan
 *
 */
public class AttributeTileSpecification {

	
	/**
	 * The object class name this tiling specification concerns
	 */
	public String			className;
	
	/**
	 * The attribute this tiling specification concerns
	 */
	public Attribute		attribute;
	
	/**
	 * How large of a window to use; i.e., the width a tile along this attribute dimension
	 */
	public double			windowSize;
	
	/**
	 * The offset of this tile alignment; that is, where the first tiling boundary starts
	 */
	public double			bucketBoundary;
	
	
	
	/**
	 * Initializes
	 * @param className The object class name this tiling specification concerns
	 * @param attribute The attribute this tiling specification concerns
	 * @param windowSize How large of a window to use; i.e., the width a tile along this attribute dimension
	 * @param bucketBoundary The offset of this tile alignment; that is, where the first tiling boundary starts
	 */
	public AttributeTileSpecification(String className, Attribute attribute, double windowSize, double bucketBoundary) {
		this.className = className;
		this.attribute = attribute;
		this.windowSize = windowSize;
		this.bucketBoundary = bucketBoundary;
	}

}
