package burlap.examples.gridWorld;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.ObjectPainter;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class LocationPainter implements ObjectPainter {

    @Override
    public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
        g2.setColor(Color.GREEN);
        //set up floats for the width and height of our domain
        float fWidth = ExampleGridWorld.map.length + 2;
        float fHeight = ExampleGridWorld.map[0].length + 2;
        //determine the width of a single cell on our canvas
        //such that the whole map can be painted
        float width = cWidth / fWidth;
        float height = cHeight / fHeight;
        int ax = ob.getIntValForAttribute(ExampleGridWorld.ATT_X) + 1;
        int ay = ob.getIntValForAttribute(ExampleGridWorld.ATT_Y) + 1;
        //left coordinate of cell on our canvas
        float rx = ax*width;
        //top coordinate of cell on our canvas
        //coordinate system adjustment because the java canvas
        //origin is in the top left instead of the bottom right
        float ry = cHeight - height - ay*height;
        //paint the rectangle
        g2.fill(new Rectangle2D.Float(rx, ry, width, height));
    }
}
