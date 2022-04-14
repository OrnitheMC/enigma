/*******************************************************************************
 * Copyright (c) 2015 Jeff Martin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * Jeff Martin - initial API and implementation
 ******************************************************************************/

package cuchaz.enigma.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cuchaz.enigma.analysis.index.EntryIndex;
import cuchaz.enigma.gui.Gui;
import cuchaz.enigma.gui.GuiController;
import cuchaz.enigma.gui.config.UiConfig;
import cuchaz.enigma.gui.elements.ValidatableTextArea;
import cuchaz.enigma.gui.config.keybind.KeyBinds;
import cuchaz.enigma.gui.util.AbstractListCellRenderer;
import cuchaz.enigma.gui.util.GridBagConstraintsBuilder;
import cuchaz.enigma.gui.util.GuiUtil;
import cuchaz.enigma.gui.util.ScaleUtil;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;
import cuchaz.enigma.translation.representation.entry.ParentedEntry;
import cuchaz.enigma.utils.I18n;
import cuchaz.enigma.gui.search.SearchEntry;
import cuchaz.enigma.gui.search.SearchUtil;

public class SearchDialog {

	private final JTextField searchField;
	private DefaultListModel<SearchEntryImpl> classListModel;
	private final JList<SearchEntryImpl> classList;
	private final JDialog dialog;

	private final Gui parent;
	private final SearchUtil<SearchEntryImpl> su;
	private SearchUtil.SearchControl currentSearch;

	public SearchDialog(Gui parent) {
		this.parent = parent;

		su = new SearchUtil<>();

		JPanel optionsPanel = new JPanel(new GridBagLayout());

		dialog = new JDialog(parent.getFrame(), I18n.translate("menu.search"), true);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(ScaleUtil.createEmptyBorder(4, 4, 4, 4));
		contentPane.setLayout(new BorderLayout(ScaleUtil.scale(4), ScaleUtil.scale(4)));

		// build the new grid bag that fills horizontally (i.e. it takes up the complete horizontal space)
		GridBagConstraintsBuilder cb = GridBagConstraintsBuilder.create().weightX(1).fill(GridBagConstraints.HORIZONTAL);

		// add a button for each search type
		for (SearchDialog.SearchType searchType : SearchDialog.SearchType.values()) {
			JButton searchButton = new JButton(searchType.getText());
			searchButton.setIcon(searchType.getIcon());

			// when button is pressed, switch to the search mode corresponding to that button
			searchButton.addActionListener(action -> {
				show(searchType);
			});
			// add the button on the correct x position and add insets (spacing between the elements)
			switch (searchType) {
				case CLASS -> optionsPanel.add(searchButton, cb.pos(0, 0).insets(0, 2, 2, 0).build());
				case METHOD -> optionsPanel.add(searchButton, cb.pos(1, 0).insets(0, 2, 2, 2).build());
				case FIELD -> optionsPanel.add(searchButton, cb.pos(2, 0).insets(0, 0, 2, 2).build());
			}
		}

		searchField = new JTextField();
		searchField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateList();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateList();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateList();
			}

		});
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (KeyBinds.SEARCH_DIALOG_NEXT.matches(e)) {
					int next = classList.isSelectionEmpty() ? 0 : classList.getSelectedIndex() + 1;
					classList.setSelectedIndex(next);
					classList.ensureIndexIsVisible(next);
				} else if (KeyBinds.SEARCH_DIALOG_PREVIOUS.matches(e)) {
					int prev = classList.isSelectionEmpty() ? classList.getModel().getSize() : classList.getSelectedIndex() - 1;
					classList.setSelectedIndex(prev);
					classList.ensureIndexIsVisible(prev);
				} else if (KeyBinds.EXIT.matches(e)) {
					close();
				}
			}
		});
		searchField.addActionListener(e -> openSelected());

		// add the search field to the panel. Set it's with as 3, so it scales properly when resizing. Set top padding for space between the buttons above it
		optionsPanel.add(searchField, cb.pos(0, 1).width(3).weightX(3).insets(2, 0, 0, 0).build());
		contentPane.add(optionsPanel, BorderLayout.NORTH);

		classListModel = new DefaultListModel<>();
		classList = new JList<>();
		classList.setModel(classListModel);
		classList.setCellRenderer(new ListCellRendererImpl(parent));
		classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		classList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() >= 2) {
					int idx = classList.locationToIndex(mouseEvent.getPoint());
					SearchEntryImpl entry = classList.getModel().getElementAt(idx);
					openEntry(entry);
				}
			}
		});

		contentPane.add(new JScrollPane(classList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
				BorderLayout.CENTER);

		JPanel buttonBar = new JPanel();
		buttonBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton open = new JButton(I18n.translate("prompt.open"));
		open.addActionListener(event -> openSelected());
		buttonBar.add(open);
		JButton cancel = new JButton(I18n.translate("prompt.cancel"));
		cancel.addActionListener(event -> close());
		buttonBar.add(cancel);
		contentPane.add(buttonBar, BorderLayout.SOUTH);

		// apparently the class list doesn't update by itself when the list
		// state changes and the dialog is hidden
		dialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				classList.updateUI();
			}
		});

		dialog.setContentPane(contentPane);
		dialog.setSize(ScaleUtil.getDimension(400, 500));
		dialog.setLocationRelativeTo(parent.getFrame());

		contentPane.requestFocus();
		searchField.requestFocus();
	}

	public void show(SearchDialog.SearchType searchType) {
		su.clear();

		final EntryIndex entryIndex = parent.getController().project.getJarIndex().getEntryIndex();

		switch (searchType) {
			case CLASS -> entryIndex.getClasses().parallelStream()
					.filter(e -> !e.isInnerClass())
					.map(e -> SearchEntryImpl.from(e, parent.getController()))
					.map(SearchUtil.Entry::from)
					.sequential()
					.forEach(su::add);
			case METHOD -> entryIndex.getMethods().parallelStream()
					.filter(e -> !e.isConstructor() && !entryIndex.getMethodAccess(e).isSynthetic())
					.map(e -> SearchEntryImpl.from(e, parent.getController()))
					.map(SearchUtil.Entry::from)
					.sequential()
					.forEach(su::add);
			case FIELD -> entryIndex.getFields().parallelStream()
					.map(e -> SearchEntryImpl.from(e, parent.getController()))
					.map(SearchUtil.Entry::from)
					.sequential()
					.forEach(su::add);
		}

		updateList();

		searchField.requestFocus();
		searchField.selectAll();

		dialog.setVisible(true);
	}

	private void openSelected() {
		SearchEntryImpl selectedValue = classList.getSelectedValue();
		if (selectedValue != null) {
			openEntry(selectedValue);
		}
	}

	private void openEntry(SearchEntryImpl e) {
		close();
		su.hit(e);
		parent.getController().navigateTo(e.obf);
		if (e.obf instanceof ClassEntry) {
			if (e.deobf != null) {
				parent.getDeobfPanel().deobfClasses.setSelectionClass((ClassEntry) e.deobf);
			} else {
				parent.getObfPanel().obfClasses.setSelectionClass((ClassEntry) e.obf);
			}
		} else {
			if (e.deobf != null) {
				parent.getDeobfPanel().deobfClasses.setSelectionClass((ClassEntry) e.deobf.getParent());
			} else {
				parent.getObfPanel().obfClasses.setSelectionClass((ClassEntry) e.obf.getParent());
			}
		}
	}

	private void close() {
		dialog.setVisible(false);
	}

	// Updates the list of class names
	private void updateList() {
		if (currentSearch != null) currentSearch.stop();

		DefaultListModel<SearchEntryImpl> classListModel = new DefaultListModel<>();
		this.classListModel = classListModel;
		classList.setModel(classListModel);

		// handle these search result like minecraft scheduled tasks to prevent
		// flooding swing buttons inputs etc with tons of (possibly outdated) invocations
		record Order(int idx, SearchEntryImpl e) {}
		Queue<Order> queue = new ConcurrentLinkedQueue<>();
		Runnable updater = new Runnable() {
			@Override
			public void run() {
				if (SearchDialog.this.classListModel != classListModel || !SearchDialog.this.dialog.isVisible()) {
					return;
				}

				// too large count may increase delay for key and input handling, etc.
				int count = 100;
				while (count > 0 && !queue.isEmpty()) {
					var o = queue.remove();
					classListModel.insertElementAt(o.e, o.idx);
					count--;
				}

				SwingUtilities.invokeLater(this);
			}
		};

		currentSearch = su.asyncSearch(searchField.getText(), (idx, e) -> queue.add(new Order(idx, e)));
		SwingUtilities.invokeLater(updater);
	}

	public void dispose() {
		dialog.dispose();
	}

	private static final class SearchEntryImpl implements SearchEntry {

		public final ParentedEntry<?> obf;
		public final ParentedEntry<?> deobf;

		private SearchEntryImpl(ParentedEntry<?> obf, ParentedEntry<?> deobf) {
			this.obf = obf;
			this.deobf = deobf;
		}

		@Override
		public List<String> getSearchableNames() {
			if (deobf != null) {
				return Arrays.asList(obf.getSimpleName(), deobf.getSimpleName());
			} else {
				return Collections.singletonList(obf.getSimpleName());
			}
		}

		@Override
		public String getIdentifier() {
			return obf.getFullName();
		}

		@Override
		public String toString() {
			return String.format("SearchEntryImpl { obf: %s, deobf: %s }", obf, deobf);
		}

		public static SearchEntryImpl from(ParentedEntry<?> e, GuiController controller) {
			ParentedEntry<?> deobf = controller.project.getMapper().deobfuscate(e);
			if (deobf.equals(e)) deobf = null;
			return new SearchEntryImpl(e, deobf);
		}

	}

	private static final class ListCellRendererImpl extends AbstractListCellRenderer<SearchEntryImpl> {
		private final Gui gui;
		private final JLabel mainName;
		private final JLabel secondaryName;

		public ListCellRendererImpl(Gui gui) {
			this.setLayout(new BorderLayout());
			this.gui = gui;

			mainName = new JLabel();
			this.add(mainName, BorderLayout.WEST);

			secondaryName = new JLabel();
			secondaryName.setFont(secondaryName.getFont().deriveFont(Font.ITALIC));
			secondaryName.setForeground(Color.GRAY);
			this.add(secondaryName, BorderLayout.EAST);
		}

		@Override
		public void updateUiForEntry(JList<? extends SearchEntryImpl> list, SearchEntryImpl value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value.deobf == null) {
				mainName.setText(value.obf.getContextualName());
				mainName.setToolTipText(value.obf.getFullName());
				secondaryName.setText("");
				secondaryName.setToolTipText("");
			} else {
				mainName.setText(value.deobf.getContextualName());
				mainName.setToolTipText(value.deobf.getFullName());
				secondaryName.setText(value.obf.getSimpleName());
				secondaryName.setToolTipText(value.obf.getFullName());
			}

			if (value.obf instanceof ClassEntry classEntry) {
				mainName.setIcon(GuiUtil.getClassIcon(gui, classEntry));
			} else if (value.obf instanceof MethodEntry methodEntry) {
				mainName.setIcon(GuiUtil.getMethodIcon(methodEntry));
			} else if (value.obf instanceof FieldEntry) {
				mainName.setIcon(GuiUtil.FIELD_ICON);
			}
		}

	}

	public enum SearchType {
		CLASS(true, GuiUtil.CLASS_ICON),
		METHOD(true, GuiUtil.METHOD_ICON),
		FIELD(true, GuiUtil.FIELD_ICON);

		private final boolean inline;
		private final Icon icon;

		private SearchType(boolean inline, Icon icon) {
			this.inline = inline;
			this.icon = icon;
		}

		public String getText() {
			return this.name().charAt(0) + this.name().substring(1).toLowerCase();
		}

		public Icon getIcon() {
			return this.icon;
		}

		public boolean isInline() {
			return this.inline;
		}
	}
}
