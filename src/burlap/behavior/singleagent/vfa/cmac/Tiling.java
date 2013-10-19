package burlap.behavior.singleagent.vfa.cmac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


public class Tiling {

	protected Map<String, List<AttributeTileSpecification>>				specification;
	protected List <String>												classOrder;
	
	
	public Tiling() {
		this.specification = new HashMap<String, List<AttributeTileSpecification>>();
		this.classOrder = new ArrayList<String>();
	}
	
	
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
	
	
	
	public StateTile getStateTile(State s){
		return new StateTile(s);
	}
	
	
	public class StateTile{
		
		public State							s;
		public List<List<ObjectTile>>			tiledObjectsByClass;
		protected int							hashCode;
		
		
		public StateTile(State s){
			
			this.s = s;
			tiledObjectsByClass = new ArrayList<List<ObjectTile>>(Tiling.this.classOrder.size());
			this.hashCode = 1;
			
			for(String className : Tiling.this.classOrder){
				List<ObjectInstance> objectsOfClass = s.getObjectsOfTrueClass(className);
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
	
	
	
	public class ObjectTile{
		
		public String objectName;
		public Map <String, Integer>	attTiles;
		public String className;
		public int hashCode;
		
		public ObjectTile(ObjectInstance o){
			this.objectName = o.getName();
			attTiles = new HashMap<String, Integer>();
			hashCode = 1;
			
			
			className = o.getTrueClassName();
			List<AttributeTileSpecification> classSpecs = specification.get(className);
			for(AttributeTileSpecification ats : classSpecs){
				String attName = ats.attribute.name;
				int tv = 0;
				if(ats.attribute.type.equals(Attribute.AttributeType.DISC)){
					tv = o.getDiscValForAttribute(attName);
				}
				else if(ats.attribute.type.equals(Attribute.AttributeType.REAL) || ats.attribute.type.equals(Attribute.AttributeType.REALUNBOUND)){
					double v = o.getRealValForAttribute(attName);
					tv = (int)((v - ats.bucketBoundary) / ats.windowSize);
				}
				attTiles.put(attName, tv);
				hashCode = 31*hashCode + tv;
			}
			
		}
		
		
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
