package cuchaz.enigma.gui.dialog;

import cuchaz.enigma.gui.GuiController;
import cuchaz.enigma.gui.util.JavadocAnnotationUtil;
import cuchaz.enigma.gui.util.ScaleUtil;
import cuchaz.enigma.utils.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RenderedJavadocDialog {
    private final JEditorPane renderedPane = new JEditorPane();
    private final JScrollPane renderedScrollPane;
    private final JDialog ui;
    private final GuiController controller;
    private final String renderText;

    public RenderedJavadocDialog(JFrame parent, GuiController controller, String renderText) {
        this.ui = new JDialog(parent, I18n.translate("javadocs.render"));
        this.controller = controller;
        this.renderText = JavadocAnnotationUtil.convertRawJavadoc(renderText);
        this.renderedPane.setContentType("text/html");
        this.renderedPane.setEditable(false);

        // set up dialog
        Container contentPane = ui.getContentPane();
        contentPane.setLayout(new BorderLayout());


        this.renderedScrollPane = new JScrollPane(this.renderedPane);
        this.renderedPane.setText(this.renderText);
        contentPane.add(renderedScrollPane, BorderLayout.CENTER);

        this.renderedPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    close();
                }
            }
        });

        this.renderedPane.setText(this.renderText);

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
}
