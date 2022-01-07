package cuchaz.enigma.gui;

import cuchaz.enigma.source.Token;
import cuchaz.enigma.utils.Pair;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

public class TooltipEditorPane extends JEditorPane {
    private final HashMap<String, Pair<LinkedHashSet<Rectangle>, String>> tooltipMap = new HashMap<>();

    public TooltipEditorPane() {
        // Register the component on the tooltip manager
        // So that #getToolTipText(MouseEvent) gets invoked when the mouse
        // hovers the component
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Point mousePosition = new Point(event.getX(), event.getY());
        for (Pair<LinkedHashSet<Rectangle>, String> pair : tooltipMap.values()) {
            for (Rectangle rectangle : pair.getKey()){
                if (rectangle.contains(mousePosition)) {
                    return pair.getValue();
                }
            }
        }
        return super.getToolTipText(event);
    }

    public void setTooltipPosition(Token token, String message, String deObfName) {
        try {
            Rectangle start = this.modelToView(token.start);
            Rectangle end = this.modelToView(token.end);
            if (start == null || end == null) {
                return;
            }

            Rectangle tokenBox = start.union(end);
            if (tooltipMap.containsKey(deObfName)) {
                tooltipMap.get(deObfName).getKey().add(tokenBox);
            } else {
                tooltipMap.put(deObfName, new Pair<>(new LinkedHashSet<>(Arrays.asList(tokenBox)), message));
            }
        } catch (BadLocationException ex) {
            System.out.println("bad location");
        }
    }

    public Pair<LinkedHashSet<Rectangle>, String> removeTooltip(String deObfName) {
        return tooltipMap.remove(deObfName);
    }

    public enum TooltipType {
        WARNING,
        JAVADOC
    }

}
