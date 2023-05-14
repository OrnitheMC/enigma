package cuchaz.enigma.gui.dialog;

import cuchaz.enigma.gui.GuiController;
import cuchaz.enigma.gui.util.JavadocAnnotationUtil;
import cuchaz.enigma.gui.util.ScaleUtil;
import cuchaz.enigma.utils.I18n;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

public class RenderedJavadocDialog {
	private final JEditorPane renderedPane = new JEditorPane();
	private final JScrollPane renderedScrollPane;
	private final JDialog ui;
	// controller currently unused but will be used when the javadoc window will also need to check
	// whether a created link is actually valid.
	private final GuiController controller;
	private final String renderText;

	public RenderedJavadocDialog(JFrame parent, GuiController controller, String renderText) {
		this.ui = new JDialog(parent, I18n.translate("javadocs.render"));
		this.controller = controller;
		this.renderText = JavadocAnnotationUtil.convertRawJavadoc(renderText);
		this.renderedPane.setContentType("text/html");
		this.renderedPane.setEditable(false);

		// set up dialog
		Container contentPane = this.ui.getContentPane();
		contentPane.setLayout(new BorderLayout());

		// create a scrollable window and set the text to renderedText.
		this.renderedScrollPane = new JScrollPane(this.renderedPane);
		this.renderedPane.setText(this.renderText);
		contentPane.add(this.renderedScrollPane, BorderLayout.CENTER);

		// close upon pressing escape
		this.renderedPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
					RenderedJavadocDialog.this.close();
				}
			}
		});

		// show the frame
		this.ui.setSize(ScaleUtil.getDimension(600, 400));
		this.ui.setLocationRelativeTo(parent);
		this.ui.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void show() {
		this.ui.setVisible(true);
	}

	public void close() {
		this.ui.setVisible(false);
		this.ui.dispose();
	}

	public JDialog getUi() {
		return this.ui;
	}
}
