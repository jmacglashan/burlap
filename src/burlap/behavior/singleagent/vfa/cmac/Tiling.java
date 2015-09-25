package burlap.behavior.singleagent.vfa.cmac;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;

import java.util.*;


/**
 * This class provides a tiling specification, which tiles a state according to multi-dimensional tiling of specified attributes
 * for specified object classes.
 * @author James MacGlashan
 *
 */
public class Tiling {

	/**
	 * A map from object class names to attribute tile specifications for attributes of that class
	 */
	protected Map<String, List<AttributeTileSpecification>>				specification;
	
	/**
	 * A list specifying the order that the attributes from different classes will be combined into a single multi-dimensional tile
	 */
	protected List <String>												classOrder;
	
	
	/**
	 * Initializes an empty tiling with not attribute specifications.
	 */
	public Tiling() {
		this.specification = new HashMap<String, List<AttributeTileSpecification>>();
		this.classOrder = new ArrayList<String>();
	}


	/**
	 * Returns a copy of this tiling.
	 * @return a copy of this tiling.
	 */
	public Tiling copy(){
		Tiling nTiling = new Tiling();
		nTiling.specification = new HashMap<String, List<AttributeTileSpecification>>(this.specification);
		nTiling.classOrder = new ArrayList<String>(this.classOrder);
		return nTiling;
	}

	/**
	 * Adds an attribute tiling specification for the an attribute of the given class with the given window size and bucket/tile boundary.
	 * @param className the name of the object class whose attribute specification will be provided
	 * @param attribute the attribute for which a tiling specification will be provided
	 * @param windowSize the window size or width of the attribute tiling
	 * @param bucketBoundary the offset of this tile alignment; that is, where the first tiling boundary starts
	 */
	public void addSpecification(String className, Attribute attribute, double windowSize, double bucketBoundary){
		List<AttributeTileSpecification> specsForClass = specification.get(className);
		if(specsForClass == null){
			specsForClass = new ArrayList<AttributeTileSpecification>();
			specification.put(className, specsForClass);
			
			classOrder.add(className);
			Collections.sort(classOrder); //costly to sort with every new class added, but this should be a very short overhead with few items
		}
		else{
			//check if this attribute specification is already there; if so, remove it
			for(AttributeTileSpecification spec : specsForClass){
				if(spec.attribute == attribute){
					specsForClass.remove(spec);
					break;
				}
			}
		}
		
		AttributeTileSpecification ats = new AttributeTileSpecification(className, attribute, windowSize, bucketBoundary);
		specsForClass.add(ats);
		
	}
	
	
	/**
	 * Returns the multi-dimensional tile from this tiling that corresponds to the given input state.
	 * @param s
	 * @return the multi-dimensional tile from this tiling that corresponds to the given input state.
	 */
	public StateTile getStateTile(State s){
		return new StateTile(s);
	}
	
	
	
	/**
	 * A class for representing a tile, which can be treated as a state feature.
	 * @author James MacGlashan
	 *
	 */
	public class StateTile{
		
		/**
		 * The state the tile is for
		 */
		public State							s;
		
		/**
		 * The tiled version of object instances in the state
		 */
		public List<List<ObjectTile>>			tiledObjectsByClass;
		
		/**
		 * A hash code for this tiling for fast storage
		 */
		protected int							hashCode;
		
		
		/**
		 * Creates a state tile for the given input state
		 * @param s the state for which a state tile should be created.
		 */
		public StateTile(State s){
			
			this.s = s;
			tiledObjectsByClass = new ArrayList<List<ObjectTile>>(Tiling.this.classOrder.size());
			this.hashCode = 1;
			
			for(String className : Tiling.this.classOrder){
				List<ObjectInstance> objectsOfClass = s.getObjectsOfClass(className);
				List <ObjectTile> objectTiles = new ArrayList<Tiling.ObjectTile>(objectsOfClass.size());
				for(ObjectInstance o : objectsOfClass){
					ObjectTile ot = new ObjectTile(o);
					objectTiles.add(ot);
				}
				
				//sort it for order invariance
				Collections.sort(objectTiles, new Comparator<ObjectTile>() {

					@Override
					public int compare(ObjectTile o1, ObjectTile o2) {
						Integer hash1 = o1.hashCode;
						Integer hash2 = o2.hashCode;
						return hash1.compareTo(hash2);
					}
				});
				
				tiledObjectsByClass.add(objectTiles);
				
				//now that it's sorted, add to hash code
				for(ObjectTile ot : objectTiles){
					hashCode = 31 * hashCode + ot.hashCode;
				}
				
			}
			
		}
		
		/**
		 * Returns the tiling specification that produced this tiling
		 * @return the tiling specification that produced this tiling
		 */
		public Tiling getOuterTiling(){
			return Tiling.this;
		}
		
		@Override
		public int hashCode(){
			return hashCode;
		}
		
		@Override
		public boolean equals(Object other){
			
			if(this == other){
				return true;
			}
			if(!(other instanceof StateTile)){
				return false;
			}
			
			StateTile that = (StateTile)other;
			
			if(this.hashCode != that.hashCode){
				return false;
			}
			
			if(this.tiledObjectsByClass.size() != that.tiledObjectsByClass.size()){
				return false;
			}
	
			for(int i = 0; i < this.tiledObjectsByClass.size(); i++){
				List <ObjectTile> thisObjectsOfClass = this.tiledObjectsByClass.get(i);
				List <ObjectTile> thatObjectsOfClass = that.tiledObjectsByClass.get(i);
				if(thisObjectsOfClass.size() != thatObjectsOfClass.size()){
					return false;
				}
				for(ObjectTile ot : thisObjectsOfClass){
					if(!thatObjectsOfClass.contains(ot)){
						return false;
					}
				}
			}
			
			
			return true;
			
		}
		
		
	}
	
	
	
	/**
	 * A class for creating a tiling of a single OO-MDP object instance which will be combined with other object instance tiles
	 * to create a single state tiling.
	 * @author James MacGlashan
	 *
	 */
	public class ObjectTile{
		
		/**
		 * The name of the object instance this tile is for.
		 */
		public String objectName;
		
		/**
		 * The tiled attribute values for this object's OO-MDP object instance
		 */
		public Map <String, Integer>	attTiles;
		
		/**
		 * The OO-MDP class name for this object's OO-MDP object instance
		 */
		public String className;
		
		/**
		 * The hash code for fast storage and retrieval
		 */
		public int hashCode;
		
		
		/**
		 * Creates a tile for the given object instance
		 * @param o the object instance for which a tile will be created
		 */
		public ObjectTile(ObjectInstance o){
			this.objectName = o.getName();
			attTiles = new HashMap<String, Integer>();
			hashCode = 1;
			
			
			className = o.getClassName();
			List<AttributeTileSpecification> classSpecs = specification.get(className);
			for(AttributeTileSpecification ats : classSpecs){
				String attName = ats.attribute.name;
				int tv = 0;
				if(ats.attribute.type.equals(Attribute.AttributeType.DISC)){
					tv = o.getIntValForAttribute(attName);
				}
				else if(ats.attribute.type.equals(Attribute.AttributeType.REAL) || ats.attribute.type.equals(Attribute.AttributeType.REALUNBOUND)){
					double v = o.getRealValForAttribute(attName);
					tv = (int)((v - ats.bucketBoundary) / ats.windowSize);
				}
				attTiles.put(attName, tv);
				hashCode = 31*hashCode + tv;
			}
			
		}
		
		
		/**
		 * Returns the tiling that was used to produce this object instance tile.
		 * @return the tiling that was used to produce this object instance tile.
		 */
		public Tiling getOuterTiling(){
			return Tiling.this;
		}
		
		@Override
		public int hashCode(){
			return hashCode;
		}
		
		
		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			if(!(other instanceof ObjectTile)){
				return false;
			}
			
			ObjectTile that = (ObjectTile)other;
			if(this.hashCode != that.hashCode){
				return false;
			}
			
			if(!this.className.equals(that.className)){
				return false;
			}
			
			if(Tiling.this != that.getOuterTiling()){
				return false;
			}
			
			for(String attName : this.attTiles.keySet()){
				int tv = this.attTiles.get(attName);
				int ttv = that.attTiles.get(attName);
				if(tv != ttv){
					return false;
				}
			}
			
			
			return true;
			
		}
		
	}

}
