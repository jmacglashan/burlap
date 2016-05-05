package burlap.oomdp.visualizer;

import burlap.oomdp.core.state.State;
import burlap.oomdp.core.oo.state.OOState;
import burlap.oomdp.core.oo.state.ObjectInstance;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link StatePainter} for painting {@link OOState} instances. Painting is defined by being given a list of
 * OO-MDP class-wise painters and specific instance-wise painters. A class-wise painter will handle painting all
 * OO-MDP objects belonging to a specific OO-MDP class, except for objects that have an associated instance-wise
 * painter, which override any class-wise painters for the painting of the object.
 * <p>
 * Painting order goes by class-wise painters, with the order that the painters were added, and then instance-wise
 * painters.
 * @author James MacGlashan.
 */
public class OOStatePainter implements StatePainter {

	/**
	 * Ordered list of painters for each object class
	 */
	protected List<ObjectPainterAndClassNamePair> objectClassPainterList = new ArrayList<ObjectPainterAndClassNamePair>();

	/**
	 * Map of painters that define how to paint specific objects; if an object it appears in both specific and general lists, the specific painter is used
	 */
	protected Map<String, ObjectPainter> specificObjectPainters = new HashMap<String, ObjectPainter>();


	/**
	 * Adds a class that will paint objects that belong to a given OO-MDPclass.
	 * @param className the name of the class that the provided painter can paint
	 * @param op the painter
	 */
	public void addObjectClassPainter(String className, ObjectPainter op){
		objectClassPainterList.add(new ObjectPainterAndClassNamePair(className, op));
	}


	/**
	 * Adds a painter that will be used to paint a specific object in states
	 * @param objectName the name of the object this painter is used to paint
	 * @param op the painter
	 */
	public void addSpecificObjectPainter(String objectName, ObjectPainter op){
		specificObjectPainters.put(objectName, op);
	}

	@Override
	public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {

		if(!(s instanceof OOState)){
			throw new RuntimeException("OOStatePainter cannot paint the state, because it is a " + s.getClass().getName() + " and does not implement OOState");
		}

		OOState os = (OOState)s;

		//draw each object by class order and if there is not a specific painter for
		for(ObjectPainterAndClassNamePair op : this.objectClassPainterList){
			List <ObjectInstance> objects = os.objectsOfClass(op.className);
			for(ObjectInstance o : objects){
				if(!this.specificObjectPainters.containsKey(o.name())){
					op.painter.paintObject(g2, os, o, cWidth, cHeight);
				}
			}
		}

		//draw each object if there is a painter to do so
		List <ObjectInstance> objects = os.objects();
		for(ObjectInstance o : objects){

			//is there a specific object painter for this object?
			if(specificObjectPainters.containsKey(o.name())){
				specificObjectPainters.get(o.name()).paintObject(g2, os, o, cWidth, cHeight);
			}

		}

	}

	/**
	 * A pair of the name of an object class to paint, and the {@link burlap.oomdp.visualizer.ObjectPainter} to
	 * use to paint it.
	 */
	public static class ObjectPainterAndClassNamePair{
		String className;
		ObjectPainter painter;

		public ObjectPainterAndClassNamePair(String className, ObjectPainter painter){
			this.className = className;
			this.painter = painter;
		}
	}

}
