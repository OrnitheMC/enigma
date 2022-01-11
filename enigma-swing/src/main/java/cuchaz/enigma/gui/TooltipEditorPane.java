package cuchaz.enigma.gui;

import cuchaz.enigma.gui.warning.WarningType;
import cuchaz.enigma.source.Token;
import cuchaz.enigma.utils.Pair;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;

public class TooltipEditorPane extends JEditorPane {
    private final HashMap<String, Pair<LinkedHashSet<Rectangle>, String>> warningMap = new HashMap<>();
    private final HashMap<String, Pair<LinkedHashSet<Rectangle>, String>> javadocMap = new HashMap<>();

    public TooltipEditorPane() {
        // Register the component on the tooltip manager
        // So that #getToolTipText(MouseEvent) gets invoked when the mouse
        // hovers the component
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Point mousePosition = new Point(event.getX(), event.getY());
        for (Pair<LinkedHashSet<Rectangle>, String> pair : warningMap.values()) {
            for (Rectangle rectangle : pair.getKey()){
                if (rectangle.contains(mousePosition)) {
                    return pair.getValue();
                }
            }
        }
        return super.getToolTipText(event);
    }

    public void setTooltipPosition(Token token, String message, String obfName, TooltipType type) {
        try {
            Rectangle start = this.modelToView(token.start);
            Rectangle end = this.modelToView(token.end);
            if (start == null || end == null) {
                return;
            }
            putInMapByType(type, start.union(end), message, obfName);
        } catch (BadLocationException ex) {
            System.out.println("bad location");
        }
    }

    private void putInMapByType(TooltipType type, Rectangle tokenBox, String message, String obfName) {
        HashMap<String, Pair<LinkedHashSet<Rectangle>, String>> map;
        switch (type) {
            case JAVADOC -> map = this.javadocMap;
            case WARNING -> map = this.warningMap;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
        if (map.containsKey(obfName)) {
            map.get(obfName).getKey().add(tokenBox);
        } else {
            map.put(obfName, new Pair<>(new LinkedHashSet<>(Arrays.asList(tokenBox)), message));
        }
    }

    public Pair<LinkedHashSet<Rectangle>, String> removeTooltip(String deObfName) {
        return warningMap.remove(deObfName);
    }

    public enum TooltipType {
        WARNING,
        JAVADOC
    }

}
