package burlap.oomdp.visualizer;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.*;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


public class Visualizer extends Canvas{

	private static final long serialVersionUID = 1L; //needed for Canvas extension

	
	private State							curState;					//the current state to be painted next
	
	private List <StaticPainter>			staticPainters;				//list of static painters that pain static non-object defined properties of the domain
	private Map <String, ObjectPainter>		objectClassPainters;		//Map of painters that define how to paint each object class
	private Map <String, ObjectPainter>		specificObjectPainters;		//Map of painters that define how to paint specific objects; if an object it appears in both specific and general lists, the specific painter is used
	
	private Color							bgColor;					//the background color of the canvas
	
	
	
	public Visualizer(){
		
		curState = null;
		
		staticPainters = new ArrayList <StaticPainter>();
		objectClassPainters = new HashMap <String, ObjectPainter>();
		specificObjectPainters = new HashMap <String, ObjectPainter>();
		
		bgColor = Color.white;
		
	}
	
	public void setBGColor(Color c){
		bgColor = c;
	}
	
	public void addStaticPainter(StaticPainter sp){
		staticPainters.add(sp);
	}
	
	public void addObjectClassPainter(String className, ObjectPainter op){
		objectClassPainters.put(className, op);
	}
	
	public void addSpecificObjectPainter(String objectName, ObjectPainter op){
		specificObjectPainters.put(objectName, op);
	}
	
	public void updateState(State st){
		curState = st;
		repaint();
	}
	
	public void paint(Graphics g){
		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(bgColor);
		g2.fill(new Rectangle(this.getWidth(), this.getHeight()));
		
		if(curState == null){
			return ;
		}
		
		float cWidth = (float)this.getWidth();
		float cHeight = (float)this.getHeight();
		
		//draw the static properties
		for(StaticPainter sp : staticPainters){
			sp.paint(g2, curState, cWidth, cHeight);
		}
		
		//draw each object if there is a painter to do so
		List <ObjectInstance> objects = curState.getAllObjects();
		for(ObjectInstance o : objects){
			
			//is there a specific object painter for this object?
			if(specificObjectPainters.containsKey(o.getName())){
				specificObjectPainters.get(o.getName()).paintObject(g2, curState, o, cWidth, cHeight);
			}
			else{ //otherwise see if we have a painter for this object's class
				
				//try the parameterized class first
				if(objectClassPainters.containsKey(o.getTrueClassName())){
					objectClassPainters.get(o.getTrueClassName()).paintObject(g2, curState, o, cWidth, cHeight);
				}
				else if(objectClassPainters.containsKey(o.getTrueClassName())){ //try true class if no entry for the parameterized class
					objectClassPainters.get(o.getTrueClassName()).paintObject(g2, curState, o, cWidth, cHeight);
				}
				
			}
			
		}
		
	}
	
	
}
