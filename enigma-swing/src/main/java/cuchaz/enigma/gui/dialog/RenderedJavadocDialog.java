package cuchaz.enigma.gui.dialog;

import com.google.common.base.Strings;
import cuchaz.enigma.analysis.EntryReference;
import cuchaz.enigma.gui.BrowserCaret;
import cuchaz.enigma.gui.GuiController;
import cuchaz.enigma.gui.TooltipEditorPane;
import cuchaz.enigma.gui.elements.ValidatableTextArea;
import cuchaz.enigma.gui.panels.EditorPanel;
import cuchaz.enigma.gui.util.GuiUtil;
import cuchaz.enigma.gui.util.JavadocAnnotationUtil;
import cuchaz.enigma.gui.util.ScaleUtil;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.utils.I18n;
import cuchaz.enigma.utils.validation.ValidationContext;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RenderedJavadocDialog {
    private final JEditorPane renderedPane = new JEditorPane();
    private final JScrollPane renderedScrollPane;
    private final JDialog ui;
    private final GuiController controller;
    private final String renderText;

    private final ValidationContext vc = new ValidationContext();

    public RenderedJavadocDialog(JFrame parent, GuiController controller, String renderText) {
        this.ui = new JDialog(parent, I18n.translate("javadocs.render"));
        this.controller = controller;
        this.renderText = JavadocAnnotationUtil.convertRawJavadoc(renderText.replaceAll("\n", "<br>"));
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

        // buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(GuiUtil.unboldLabel(new JLabel(I18n.translate("javadocs.instruction"))));

        JButton cancelButton = new JButton(I18n.translate("prompt.cancel"));
        cancelButton.addActionListener(event -> close());
        buttonsPanel.add(cancelButton);

        contentPane.add(buttonsPanel, BorderLayout.SOUTH);

        this.renderedPane.setText(this.renderText);

        // show the frame
        this.ui.setSize(ScaleUtil.getDimension(600, 400));
        this.ui.setLocationRelativeTo(parent);
        this.ui.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public static void show(JFrame parent, GuiController controller, String renderText) {
        RenderedJavadocDialog dialog = new RenderedJavadocDialog(parent, controller, renderText);
        dialog.ui.setVisible(true);

    }

    public void close() {
        this.ui.setVisible(false);
        this.ui.dispose();
    }
}
