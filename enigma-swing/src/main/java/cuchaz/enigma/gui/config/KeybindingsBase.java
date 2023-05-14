package cuchaz.enigma.gui.config;

import cuchaz.enigma.utils.I18n;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class KeybindingsBase {
	public enum Category {
		SEARCH("key.category.search"), EDITOR("key.category.editor"), GENERAL("key.category.keybindings");

		private final String name;

		Category(String translatableText) {
			this.name = I18n.translate(translatableText);
		}

		public String getName() {
			return this.name;
		}
	}

	public enum Keybindings {
		GENERAL(Category.SEARCH, "key.keybinding.search.general", KeyEvent.VK_SPACE, InputEvent.SHIFT_DOWN_MASK),
		CLASS(Category.SEARCH, "key.keybinding.search.class", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK),
		METHOD(Category.SEARCH, "key.keybinding.search.method", KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK),
		FIELDS(Category.SEARCH, "key.keybinding.search.fields", KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK),

		RENAME(Category.EDITOR, "key.keybinding.editor.rename", KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK),
		PASTE(Category.EDITOR, "key.keybinding.editor.paste", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK),
		EDIT_JAVADOC(Category.EDITOR, "key.keybinding.editor.edit_javadoc", KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
		SHOW_INHERITANCE(Category.EDITOR, "key.keybinding.editor.show_inheritance", KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK),
		SHOW_IMPLEMENTATIONS(Category.EDITOR, "key.keybinding.editor.show_implementations", KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK),
		SHOW_CALLS(Category.EDITOR, "key.keybinding.editor.rename.show_calls", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
		SHOW_CALLS_SPECIFIC(Category.EDITOR, "key.keybinding.editor.rename.show_calls_specific", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK),
		OPEN_ENTRY(Category.EDITOR, "key.keybinding.editor.open_entry", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
		OPEN_PREVIOUS(Category.EDITOR, "key.keybinding.editor.open_previous", KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK),
		OPEN_NEXT(Category.EDITOR, "key.keybinding.editor.open_next", KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK),
		TOGGLE_MAPPING(Category.EDITOR, "key.keybinding.editor.toggle_mapping", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK),
		ZOOM_IN(Category.EDITOR, "key.keybinding.editor.zoom_in", KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK),
		ZOOM_OUT(Category.EDITOR, "key.keybinding.editor.zoom_out", KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK),

		KEYBINDINGS(Category.EDITOR, "key.keybinding.general.keybindings", KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK),
		SAVE_MAPPINGS(Category.EDITOR, "key.keybinding.general.save_mappings", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);

		private final int keyCode;
		private final int modifiers;
		private final String name;
		private final Category category;

		Keybindings(Category category, String translatableText, int keyCode, int modifiers) {
			this.keyCode = keyCode;
			this.modifiers = modifiers;
			this.name = I18n.translate(translatableText);
			this.category = category;
		}

		public int getKeyCode() {
			return this.keyCode;
		}

		public int getModifiers() {
			return this.modifiers;
		}

		public String getName() {
			return this.name;
		}

		public Category getCategory() {
			return this.category;
		}
	}
}
