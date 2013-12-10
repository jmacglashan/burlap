package burlap.oomdp.visualizer;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


/**
 * This class provides 2D visualization of states by being provided a set of classes that can paint
 * ObjectInstnaces to the canvas as well as classes that can paint general domain information. Painters
 * for object classes as well as specific object instances can be provided. If there is a painter
 * for an object class and a painter for a specific object instance of that same class, then the specific object instance
 * painter will be used to pain that object instead of the painter for that instnace's OO-MDP class.
 * @author James MacGlashan
 *
 */
public class StateRenderLayer implements RenderLayer{

	/**
	 * the current state to be painted next
	 */
	private State							curState;
	
	/**
	 * list of static painters that pain static non-object defined properties of the domain
	 */
	private List <StaticPainter>			staticPainters;
	
	/**
	 * Map of painters that define how to paint each object class
	 */
	private Map <String, ObjectPainter>		objectClassPainters;
	
	/**
	 * Map of painters that define how to paint specific objects; if an object it appears in both specific and general lists, the specific painter is used
	 */
	private Map <String, ObjectPainter>		specificObjectPainters;

	
	public StateRenderLayer(){
		curState = null;
		
		staticPainters = new ArrayList <StaticPainter>();
		objectClassPainters = new HashMap <String, ObjectPainter>();
		specificObjectPainters = new HashMap <String, ObjectPainter>();
	}
	
	/**
	 * Adds a static painter for the domain.
	 * @param sp the static painter to add.
	 */
	public void addStaticPainter(StaticPainter sp){
		staticPainters.add(sp);
	}
	
	
	/**
	 * Adds a class that will paint objects that belong to a given OO-MDPclass.
	 * @param className the name of the class that the provided painter can paint
	 * @param op the painter
	 */
	public void addObjectClassPainter(String className, ObjectPainter op){
		objectClassPainters.put(className, op);
	}
	
	
	/**
	 * Adds a painter that will be used to paint a specific object in states
	 * @param objectName the name of the object this painter is used to paint
	 * @param op the painter
	 */
	public void addSpecificObjectPainter(String objectName, ObjectPainter op){
		specificObjectPainters.put(objectName, op);
	}
	
	
	/**
	 * Updates the state that needs to be painted
	 * @param s the state to paint
	 */
	public void updateState(State s){
		curState = s;
	}
	
	
	
	@Override
	public void render(Graphics2D g2, float width, float height) {
		
		if(this.curState == null){
			return; //don't render anything if there is no state to render
		}
		
		//draw the static properties
		for(StaticPainter sp : staticPainters){
			sp.paint(g2, curState, width, height);
		}
		
		//draw each object if there is a painter to do so
		List <ObjectInstance> objects = curState.getAllObjects();
		for(ObjectInstance o : objects){
			
			//is there a specific object painter for this object?
			if(specificObjectPainters.containsKey(o.getName())){
				specificObjectPainters.get(o.getName()).paintObject(g2, curState, o, width, height);
			}
			else{ //otherwise see if we have a painter for this object's class
				
				//try the parameterized class first
				if(objectClassPainters.containsKey(o.getTrueClassName())){
					objectClassPainters.get(o.getTrueClassName()).paintObject(g2, curState, o, width, height);
				}
				else if(objectClassPainters.containsKey(o.getTrueClassName())){ //try true class if no entry for the parameterized class
					objectClassPainters.get(o.getTrueClassName()).paintObject(g2, curState, o, width, height);
				}
				
			}
			
		}
		
	}
	
	
	
	
	
	
}
