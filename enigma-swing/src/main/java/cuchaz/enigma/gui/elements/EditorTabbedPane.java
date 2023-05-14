package cuchaz.enigma.gui.elements;

import com.google.common.collect.HashBiMap;
import cuchaz.enigma.analysis.EntryReference;
import cuchaz.enigma.classhandle.ClassHandle;
import cuchaz.enigma.gui.Gui;
import cuchaz.enigma.gui.config.keybind.KeyBinds;
import cuchaz.enigma.gui.events.EditorActionListener;
import cuchaz.enigma.gui.panels.ClosableTabTitlePane;
import cuchaz.enigma.gui.panels.EditorPanel;
import cuchaz.enigma.gui.util.GuiUtil;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.annotation.Nullable;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class EditorTabbedPane {
	private final JTabbedPane openFiles = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
	private final HashBiMap<ClassEntry, EditorPanel> editors = HashBiMap.create();

	private final EditorTabPopupMenu editorTabPopupMenu;
	private final Gui gui;

	public EditorTabbedPane(Gui gui) {
		this.gui = gui;
		this.editorTabPopupMenu = new EditorTabPopupMenu(this);

		this.openFiles.addMouseListener(GuiUtil.onMousePress(this::onTabPressed));
	}

	public EditorPanel openClass(ClassEntry entry) {
		EditorPanel activeEditor = this.getActiveEditor();
		EditorPanel editorPanel = this.editors.computeIfAbsent(entry, e -> {
			ClassHandle ch = this.gui.getController().getClassHandleProvider().openClass(entry);
			if (ch == null) return null;
			EditorPanel ed = new EditorPanel(this.gui);
			ed.setup();
			ed.setClassHandle(ch);
			this.openFiles.addTab(ed.getFileName(), ed.getUi());

			ClosableTabTitlePane titlePane = new ClosableTabTitlePane(ed.getFileName(), () -> this.closeEditor(ed));
			this.openFiles.setTabComponentAt(this.openFiles.indexOfComponent(ed.getUi()), titlePane.getUi());
			titlePane.setTabbedPane(this.openFiles);

			ed.addListener(new EditorActionListener() {
				@Override
				public void onCursorReferenceChanged(EditorPanel editor, EntryReference<Entry<?>, Entry<?>> ref) {
					if (editor == EditorTabbedPane.this.getActiveEditor()) {
						EditorTabbedPane.this.gui.showCursorReference(ref);
					}
				}

				@Override
				public void onClassHandleChanged(EditorPanel editor, ClassEntry old, ClassHandle ch) {
					EditorTabbedPane.this.editors.remove(old);
					EditorTabbedPane.this.editors.put(ch.getRef(), editor);
				}

				@Override
				public void onTitleChanged(EditorPanel editor, String title) {
					titlePane.setText(editor.getFileName());
				}
			});

			ed.getEditor().addKeyListener(GuiUtil.onKeyPress(keyEvent -> {
				if (KeyBinds.EDITOR_CLOSE_TAB.matches(keyEvent)) {
					this.closeEditor(ed);
				}
			}));

			return ed;
		});

		if (editorPanel != null && activeEditor != editorPanel) {
			this.openFiles.setSelectedComponent(this.editors.get(entry).getUi());
			this.gui.updateStructure(editorPanel);
		}

		return editorPanel;
	}

	public void closeEditor(EditorPanel ed) {
		this.openFiles.remove(ed.getUi());
		this.editors.inverse().remove(ed);
		this.gui.updateStructure(this.getActiveEditor());
		ed.destroy();
	}

	public void closeAllEditorTabs() {
		for (Iterator<EditorPanel> iter = this.editors.values().iterator(); iter.hasNext(); ) {
			EditorPanel e = iter.next();
			this.openFiles.remove(e.getUi());
			e.destroy();
			iter.remove();
		}
	}

	public void closeTabsLeftOf(EditorPanel ed) {
		int index = this.openFiles.indexOfComponent(ed.getUi());

		for (int i = index - 1; i >= 0; i--) {
			this.closeEditor(EditorPanel.byUi(this.openFiles.getComponentAt(i)));
		}
	}

	public void closeTabsRightOf(EditorPanel ed) {
		int index = this.openFiles.indexOfComponent(ed.getUi());

		for (int i = this.openFiles.getTabCount() - 1; i > index; i--) {
			this.closeEditor(EditorPanel.byUi(this.openFiles.getComponentAt(i)));
		}
	}

	public void closeTabsExcept(EditorPanel ed) {
		int index = this.openFiles.indexOfComponent(ed.getUi());

		for (int i = this.openFiles.getTabCount() - 1; i >= 0; i--) {
			if (i == index) continue;
			this.closeEditor(EditorPanel.byUi(this.openFiles.getComponentAt(i)));
		}
	}

	@Nullable
	public EditorPanel getActiveEditor() {
		return EditorPanel.byUi(this.openFiles.getSelectedComponent());
	}

	private void onTabPressed(MouseEvent e) {
		int i = this.openFiles.getUI().tabForCoordinate(this.openFiles, e.getX(), e.getY());

		if (i != -1) {
			if (SwingUtilities.isRightMouseButton(e)) {
				this.editorTabPopupMenu.show(this.openFiles, e.getX(), e.getY(), EditorPanel.byUi(this.openFiles.getComponentAt(i)));
			}

			this.gui.updateStructure(this.getActiveEditor());
		}
	}

	public void retranslateUi() {
		this.editorTabPopupMenu.retranslateUi();
		this.editors.values().forEach(EditorPanel::retranslateUi);
	}

	public Component getUi() {
		return this.openFiles;
	}

	public void reloadKeyBinds() {
		this.editors.values().forEach(EditorPanel::reloadKeyBinds);
	}
}
