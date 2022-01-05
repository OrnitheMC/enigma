package cuchaz.enigma.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

public class TooltipEditorPane extends JEditorPane {
    private static final Ellipse2D circle1 = new Ellipse2D.Double(0, 0, 20, 20);
    private static final Ellipse2D circle2 = new Ellipse2D.Double(300, 200, 20, 20);
    private static final Ellipse2D circle3 = new Ellipse2D.Double(200, 100, 20, 20);

    @Override
    public String getToolTipText(MouseEvent event) {
        Point p = new Point(event.getX(), event.getY());
        String t = tooltipForCircle(p, circle1);
        if (t != null) {
            return t;
        }
        t = tooltipForCircle(p, circle2);
        if (t != null) {
            return t;
        }
        t = tooltipForCircle(p, circle3);
        if (t != null) {
            return t;
        }
        return super.getToolTipText(event);
    }

    protected String tooltipForCircle(Point p, Ellipse2D circle) {
        // Test to check if the point  is inside circle
        if (circle.contains(p)) {
            // p is inside the circle, we return some information
            // relative to that circle.
            return "Circle: (" + circle.getX() + " " + circle.getY() + ")";
        }
        return null;
    }
}
