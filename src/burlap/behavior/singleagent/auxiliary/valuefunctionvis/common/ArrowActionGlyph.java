package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import burlap.mdp.core.state.range.VariableRange;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;


/**
 * An instance of the {@link ActionGlyphPainter} that will render arrows to the graphics context.
 * @author James MacGlashan
 *
 */
public class ArrowActionGlyph implements ActionGlyphPainter {

	/**
	 * The direction of the arrow.0: north; 1: south; 2: east; 3:west
	 */
	protected int			direction;
	
	/**
	 * The color of the arrow
	 */
	protected Color			fillColor = Color.BLACK;


	/**
	 * Creates and returns a {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D}
	 * where the x and y position attributes are name xAtt and yAtt, respectively, belong to the class
	 * classWithPositionAtts, and the north, south east, west actions have the corresponding names and
	 * will be rendered using {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.ArrowActionGlyph}
	 * objects.
	 * @param xVar the x variable key
	 * @param yVar the y variable key
	 * @param xRange the range of the x variable
	 * @param yRange the range of the y variable
	 * @param xWidth the width of each rendered state within the x domain
	 * @param yWidth the width of the each rendered state within the y domain
	 * @param northActionName the name of the north action
	 * @param southActionName the name of the south action
	 * @param eastActionName the name of the east action
	 * @param westActionName the name of the west action
	 * @return a {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D} instance.
	 */
	public static PolicyGlyphPainter2D getNSEWPolicyGlyphPainter(Object xVar,
																 Object yVar,
																 VariableRange xRange,
																 VariableRange yRange,
																 double xWidth,
																 double yWidth,
																 String northActionName,
																 String southActionName,
																 String eastActionName,
																 String westActionName){


		PolicyGlyphPainter2D spp = new PolicyGlyphPainter2D();
		spp.setXYKeys(xVar, yVar, xRange, yRange, xWidth, yWidth);
		spp.setActionNameGlyphPainter(northActionName, new ArrowActionGlyph(0));
		spp.setActionNameGlyphPainter(southActionName, new ArrowActionGlyph(1));
		spp.setActionNameGlyphPainter(eastActionName, new ArrowActionGlyph(2));
		spp.setActionNameGlyphPainter(westActionName, new ArrowActionGlyph(3));
		spp.setRenderStyle(PolicyGlyphPainter2D.PolicyGlyphRenderStyle.DISTSCALED);


		return spp;
	}




	
	/**
	 * creates an arrow action glyph painter in the specified direction
	 * @param direction 0: north; 1: south; 2: east; 3:west
	 */
	public ArrowActionGlyph(int direction){
		this.direction = direction;
	}
	
	
	
	@Override
	public void paintGlyph(Graphics2D g2, float x, float y, float width, float height) {
		
		int minSize = 30;
		if(width < minSize || height < minSize){
			return;
		}
		
		//force square for easy drawing
		BufferedImage glyphImage = new BufferedImage((int)width, (int)width, BufferedImage.TYPE_INT_ARGB);
		Graphics2D img = (Graphics2D)glyphImage.getGraphics();
		
		float cx = width/2f;
		//float cy = cx;
		
		float arrowHeight = .15f * width;
		
		float shaftWidth = 0.05f*width;
		float shaftHeight = (width/2f) - arrowHeight;
		float shaftRadius = shaftWidth/2f;
		
		float sx = cx - shaftRadius;
		float sy = arrowHeight;
		
		img.setColor(this.fillColor);
		
		img.fill(new Rectangle2D.Float(sx, sy, shaftWidth, shaftHeight));
		
		float arrowHeadWidth = 2.5f*shaftRadius;
		
		int [] xTriangle = new int[]{(int)(cx - arrowHeadWidth), (int)cx, (int)(cx + arrowHeadWidth)};
		int [] yTriangle = new int[]{(int)arrowHeight, 0, (int)arrowHeight};
		
		Polygon triangle = new Polygon(xTriangle, yTriangle, 3);
		
		img.fillPolygon(triangle);
		
		
		
		if(this.direction == 0){
			g2.drawImage(glyphImage, (int)x, (int)y, null);
		}
		else{
			double locationX = width / 2;
			double locationY = width / 2;
			double rotationRequired = 0.;
			if(this.direction == 1){
				rotationRequired = Math.PI;
			}
			else if(this.direction == 2){
				rotationRequired = Math.PI/2;
			}
			else if(this.direction == 3){
				rotationRequired = 3*Math.PI/2;
			}
			
			AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
			g2.drawImage(op.filter(glyphImage, null), (int)x, (int)y, null);
			
		}
		
		

	}

}
