package cuchaz.enigma.gui;

import cuchaz.enigma.source.Token;
import cuchaz.enigma.utils.Pair;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import javax.swing.JEditorPane;
import javax.swing.ToolTipManager;
import javax.swing.text.BadLocationException;

public class TooltipEditorPane extends JEditorPane {
	private final HashMap<String, Pair<LinkedHashSet<Rectangle2D>, String>> warningMap = new HashMap<>();
	private final HashMap<String, Pair<LinkedHashSet<Rectangle2D>, String>> javadocMap = new HashMap<>();

	public TooltipEditorPane() {
		// Register the component on the tooltip manager
		// So that #getToolTipText(MouseEvent) gets invoked when the mouse
		// hovers the component
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		Point mousePosition = new Point(event.getX(), event.getY());
		for (Pair<LinkedHashSet<Rectangle2D>, String> pair : this.warningMap.values()) {
			for (Rectangle2D rectangle : pair.a()) {
				if (rectangle.contains(mousePosition)) {
					return pair.b();
				}
			}
		}

		return super.getToolTipText(event);
	}

	public void setTokenTooltip(Token token, String message, String obfName) {
		try {
			Rectangle2D start = this.modelToView2D(token.start);
			Rectangle2D end = this.modelToView2D(token.end);
			if (start == null || end == null) {
				return;
			}

			this.putInMapByType(TooltipType.WARNING, start.createUnion(end), message, obfName);
		} catch (BadLocationException ex) {
			System.out.println("bad location");
		}
	}

	public void setJavadocTooltip(Token token, String message, String obfName) {
		try {
			Rectangle2D start = this.modelToView2D(token.start);
			Rectangle2D end = this.modelToView2D(token.end);
			if (start == null || end == null) {
				return;
			}

			this.putInMapByType(TooltipType.JAVADOC, start.createUnion(end), message, obfName);
		} catch (BadLocationException ex) {
			System.out.println("bad location");
		}
	}

	private void putInMapByType(TooltipType type, Rectangle2D tokenBox, String message, String obfName) {
		HashMap<String, Pair<LinkedHashSet<Rectangle2D>, String>> map;
		switch (type) {
			case JAVADOC -> map = this.javadocMap;
			case WARNING -> map = this.warningMap;
			default -> throw new IllegalStateException("Unexpected value: " + type);
		}

		if (map.containsKey(obfName)) {
			map.get(obfName).a().add(tokenBox);
		} else {
			map.put(obfName, new Pair<>(new LinkedHashSet<>(Arrays.asList(tokenBox)), message));
		}
	}

	public Pair<LinkedHashSet<Rectangle2D>, String> removeTooltip(String deObfName) {
		return this.warningMap.remove(deObfName);
	}

	public enum TooltipType {
		WARNING,
		JAVADOC
	}
}
