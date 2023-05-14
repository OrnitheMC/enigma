package cuchaz.enigma.gui.dialog;

import cuchaz.enigma.Enigma;
import cuchaz.enigma.gui.config.KeybindingsBase;
import cuchaz.enigma.gui.util.GridBagConstraintsBuilder;
import cuchaz.enigma.gui.util.ScaleUtil;
import cuchaz.enigma.utils.I18n;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class KeyBindDialog {
	public static void show(JFrame parent) {
		JDialog frame = new JDialog(parent, String.format(I18n.translate("menu.help.keybinds.title"), Enigma.NAME), true);
		Container pane = frame.getContentPane();
		pane.setLayout(new GridBagLayout());

		GridBagConstraintsBuilder cb = GridBagConstraintsBuilder.create()
				.insets(2)
				.weight(1.0, 0.0)
				.anchor(GridBagConstraints.WEST);

		JButton closeButton = new JButton(I18n.translate("menu.help.keybinds.close"));
		closeButton.setToolTipText("<html> <b> yeeet </b>");
		closeButton.addActionListener(e -> frame.dispose());

		for (KeybindingsBase.Keybindings keyBinding : KeybindingsBase.Keybindings.values()) {
			pane.add(new JLabel(keyBinding.getName()), cb.pos(0, keyBinding.ordinal()).width(2).build());
		}

		pane.add(closeButton, cb.pos(1, 21).anchor(GridBagConstraints.SOUTHEAST).build());

		frame.setSize(ScaleUtil.getDimension(400, 500));
		frame.pack();
		frame.setLocationRelativeTo(parent);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}
}
