package burlap.visualizer;

import javax.swing.*;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


/**
 * A MultiLayerRenderer is a canvas that will sequentially render a set of render layers, one on top of the other, to the same 2D
 * graphics context. Rendering is performed offscreen to a buffered image before being displayed on the screen.
 * @author James MacGlashan
 *
 */
public class MultiLayerRenderer extends JPanel {
	
	
	private static final long serialVersionUID = 1L;
	
	

	/**
	 * The layers that will be rendered in order from index 0 to n
	 */
	protected List<RenderLayer>					renderLayers;
	
	/**
	 * the background color of the canvas
	 */
	protected Color								bgColor = Color.white;
	
	
	/**
	 * Offscreen image to render to first
	 */
	protected Image								offscreen = null;
	
	/**
	 * The graphics context of the offscreen image
	 */
	protected Graphics2D						bufferedGraphics = null;
	
	
	
	private int									lastRenderWidth = 0;
	private int									lastRenderHeight = 0;			
	
	
	public MultiLayerRenderer(){
		this.renderLayers = new ArrayList<RenderLayer>();
	}
	
	
	/**
	 * Adds the specified {@link RenderLayer} to the end of the render layer ordered list.
	 * @param l the {@link RenderLayer} to add
	 */
	public void addRenderLayer(RenderLayer l){
		this.renderLayers.add(l);
	}
	
	/**
	 * Inserts a render layer at the specified position
	 * @param i the position in which the render layer should be inserted
	 * @param l the render layer to insert
	 */
	public void insertRenderLayerTo(int i, RenderLayer l){
		this.renderLayers.add(i, l);
	}
	
	
	/**
	 * Removes the render layer at teh specified position.
	 * @param i the position of the render layer to remove
	 */
	public void removeRenderLayer(int i){
		this.renderLayers.remove(i);
	}
	
	/**
	 * Returns the number of render layers
	 * @return the number of render layers
	 */
	public int numRenderLayers(){
		return this.renderLayers.size();
	}
	
	/**
	 * Sets the color that will fill the canvas before rendering begins
	 * @param col the background color
	 */
	public void setBGColor(Color col){
		this.bgColor = col;
	}

	/**
	 * Returns the background color of the renderer
	 * @return the background color of the renderer
	 */
	public Color getBgColor() {
		return bgColor;
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		this.initializeOffscreen();

		this.bufferedGraphics.setColor(bgColor);
		this.bufferedGraphics.fill(new Rectangle(this.getWidth(), this.getHeight()));

		for(RenderLayer l : this.renderLayers){
			l.render(bufferedGraphics, this.getWidth(), this.getHeight());
		}

		Graphics2D g2 = (Graphics2D) graphics;
		g2.drawImage(offscreen,0,0,this);
	}

	
	/**
	 * Initializes a new offscreen image and context
	 */
	 protected void initializeOffscreen(){
		 if(this.bufferedGraphics == null || (this.lastRenderWidth != this.getWidth()) && this.lastRenderHeight != this.getHeight()){
			 this.offscreen = createImage(this.getWidth(), this.getHeight());
			 this.bufferedGraphics = (Graphics2D)offscreen.getGraphics();
			 this.lastRenderHeight = this.getHeight();
			 this.lastRenderWidth = this.getWidth();
		 }
	 }
	
}
