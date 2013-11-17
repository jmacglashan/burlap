package burlap.oomdp.visualizer;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.*;

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
public class Visualizer extends Canvas{

	private static final long serialVersionUID = 1L; //needed for Canvas extension

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
	
	
	/**
	 * the background color of the canvas
	 */
	private Color							bgColor;
	
	
	/**
	 * Offscreen image to render to first
	 */
	protected Image								offscreen = null;
	
	/**
	 * The graphics context of the offscreen image
	 */
	protected Graphics2D						bufferedGraphics = null;
	
	
	
	public Visualizer(){
		
		curState = null;
		
		staticPainters = new ArrayList <StaticPainter>();
		objectClassPainters = new HashMap <String, ObjectPainter>();
		specificObjectPainters = new HashMap <String, ObjectPainter>();
		
		bgColor = Color.white;
		
	}
	
	/**
	 * Sets the background color of the canvas
	 * @param c the background color of the canvas
	 */
	public void setBGColor(Color c){
		bgColor = c;
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
	 * Updates the state that needs to be painted and repaints.
	 * @param s the state to paint
	 */
	public void updateState(State s){
		curState = s;
		repaint();
	}
	
	
	@Override
	public void paint(Graphics g){
		
		this.initializeOffscreen();
		
		
		
		this.bufferedGraphics.setColor(bgColor);
		this.bufferedGraphics.fill(new Rectangle(this.getWidth(), this.getHeight()));
		
		if(curState == null){
			return ;
		}
		
		float cWidth = (float)this.getWidth();
		float cHeight = (float)this.getHeight();
		
		//draw the static properties
		for(StaticPainter sp : staticPainters){
			sp.paint(this.bufferedGraphics, curState, cWidth, cHeight);
		}
		
		//draw each object if there is a painter to do so
		List <ObjectInstance> objects = curState.getAllObjects();
		for(ObjectInstance o : objects){
			
			//is there a specific object painter for this object?
			if(specificObjectPainters.containsKey(o.getName())){
				specificObjectPainters.get(o.getName()).paintObject(this.bufferedGraphics, curState, o, cWidth, cHeight);
			}
			else{ //otherwise see if we have a painter for this object's class
				
				//try the parameterized class first
				if(objectClassPainters.containsKey(o.getTrueClassName())){
					objectClassPainters.get(o.getTrueClassName()).paintObject(this.bufferedGraphics, curState, o, cWidth, cHeight);
				}
				else if(objectClassPainters.containsKey(o.getTrueClassName())){ //try true class if no entry for the parameterized class
					objectClassPainters.get(o.getTrueClassName()).paintObject(this.bufferedGraphics, curState, o, cWidth, cHeight);
				}
				
			}
			
		}
		
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(offscreen,0,0,this);
		
	}
	
	
	
	/**
	 * Initializes a new offscreen image and context
	 */
	 protected void initializeOffscreen(){
		 if(this.bufferedGraphics == null){
			 this.offscreen = createImage(this.getWidth(), this.getHeight());
			 this.bufferedGraphics = (Graphics2D)offscreen.getGraphics();
		 }
	 }
	
	
}
