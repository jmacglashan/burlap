package burlap.examples.gridWorld;

import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.StaticPainter;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
 * Static renderer that paints the walls
 */
public class WallPainter implements StaticPainter {
    @Override
    public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
        //walls will be filled in gray
        g2.setColor(Color.DARK_GRAY);
        //set up floats for the width and height of our domain, incl boundary walls
        int[][] map = ExampleGridWorld.map;
        int fWidth = map.length + 2;
        int fHeight = map[0].length + 2;
        //determine the width of a single cell on our canvas - including boundary walls
        float width = cWidth / fWidth;
        float height = cHeight / fHeight;
        for (int x=0; x < fWidth; x++) {
            for (int y = 0; y < fHeight; y++) {
                if (x == 0 || x == fWidth-1 || y == 0 || y == fHeight-1 || map[x-1][y-1] == 1) {
                    float rx = x * width;
                    float ry = cHeight - (y+1) * height;
                    g2.fill(new Rectangle2D.Float(rx, ry, width, height));
                }
            }
        }
    }
}
