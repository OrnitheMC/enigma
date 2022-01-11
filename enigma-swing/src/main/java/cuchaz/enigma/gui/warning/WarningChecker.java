package cuchaz.enigma.gui.warning;

import cuchaz.enigma.analysis.EntryReference;
import cuchaz.enigma.analysis.index.EntryIndex;
import cuchaz.enigma.gui.Gui;
import cuchaz.enigma.gui.GuiController;
import cuchaz.enigma.gui.TooltipEditorPane;
import cuchaz.enigma.source.Token;
import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.mapping.EntryRemapper;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WarningChecker {
    private final GuiController controller;
    private final Gui gui;
    private TooltipEditorPane editor;
    private WarningType warningType;

    public WarningChecker(Gui gui, GuiController controller, TooltipEditorPane editor) {
        this.gui = gui;
        this.controller = controller;
        this.editor = editor;
    }

    public static ClassEntry getClassParent(EntryReference<Entry<?>, Entry<?>> reference) {
        // runs through the reference's parents until it finds a class
        Entry<?> entry = reference.entry;
        while (entry != null && !(entry instanceof ClassEntry)) {
            entry = entry.getParent();
        }
        return (ClassEntry) entry;
    }

    public boolean checkOverwrittenMethods(EntryReference<Entry<?>, Entry<?>> reference, EntryRemapper mapper) {
        Collection<MethodEntry> methods = this.controller.project.getJarIndex().getEntryIndex().getMethods();
        for (MethodEntry methodEntry : methods) {
            if (Objects.equals(methodEntry.getName(), reference.entry.getName())) {
                String methodName = mapper.getDeobfMapping(methodEntry).targetName();
                if (methodName != null) {
                    return followsMethodStyle(reference, methodName);
                }
            }
        }
        return true;
    }

    public boolean followsStyle(EntryReference<Entry<?>, Entry<?>> reference) {
        // get the mapper and get the mapped name from the mapper
        EntryRemapper mapper = this.controller.project.getMapper();
        String name = mapper.getDeobfMapping(reference.entry).targetName();
        if (name != null) {
            if (reference.entry instanceof ClassEntry) {
                // remove the path (net/minecraft/ ... /) from the class name
                return followsClassStyle(reference, name.substring(name.lastIndexOf('/') + 1));
            } else if (reference.entry instanceof MethodEntry) {
                return followsMethodStyle(reference, name);
            } else if (reference.entry instanceof FieldEntry) {
                return followsFieldStyle(reference, name);
            } else if (reference.entry instanceof LocalVariableEntry) {
                return followsArgumentStyle(reference, name);
            } else {
                return true;
            }
        } else if (reference.entry instanceof MethodEntry && ((MethodEntry)reference.entry).isConstructor()){
            // methods have no name (name = null) and are called <init> or <clinit>. We have to handle them differently.
            ClassEntry parent = getClassParent(reference);
            String parentName = mapper.getDeobfMapping(parent).targetName();
            if (parentName != null) {
                return followsClassStyle(reference, parentName.substring(parentName.lastIndexOf('/') + 1));
            }
        } else if (reference.entry instanceof MethodEntry) {
            return checkOverwrittenMethods(reference, mapper);
        }
        // return true if the reference isn't mapped yet
        return true;
    }

    public boolean followsClassStyle(EntryReference<Entry<?>, Entry<?>> reference, String name) {
        if (!isPascalCase(name)) {
            this.warningType = WarningType.CLASS;
            return false;
        }
        return true;
    }

    public boolean followsMethodStyle(EntryReference<Entry<?>, Entry<?>> reference, String name) {
         if (!isCamelCase(name)){
            this.warningType = WarningType.METHOD;
            return false;
        }
        return true;
    }

    public boolean followsArgumentStyle(EntryReference<Entry<?>, Entry<?>> reference, String name) {
        if (!isCamelCase(name)) {
            this.warningType = WarningType.ARGUMENT;
            return false;
        }
        return true;
    }

    public boolean followsFieldStyle(EntryReference<Entry<?>, Entry<?>> reference, String name) {
        EntryIndex entryIndex = this.controller.project.getJarIndex().getEntryIndex();
        FieldEntry fieldEntry = (FieldEntry)reference.entry;
        AccessFlags accessFlags = entryIndex.getFieldAccess(fieldEntry);
        String fieldType = fieldEntry.getDesc().toString();

        // Static Final fields follow a different naming style than other fields excluding atomics
        if (name != null) {
            if (accessFlags != null && accessFlags.isFinal() && accessFlags.isStatic()) {
                if (fieldType.contains("Atomic")) {
                    if (!isCamelCase(name)) {
                        this.warningType = WarningType.ATOMIC_FIELD;
                        return false;
                    }
                    return true;
                } else if (!isUpperSnakeCase(name)) {
                    this.warningType = WarningType.STATIC_FINAL_FIELD;
                    return false;
                }
            } else if (!isCamelCase(name)){
                this.warningType = WarningType.FIELD;
                return false;
            }
        }
        return true;

    }

    public boolean isCamelCase(String name) {
        return Character.isLowerCase(name.charAt(0)) && name.indexOf('_') < 0;
    }

    public boolean isPascalCase(String name) {
        return Character.isUpperCase(name.charAt(0)) && name.indexOf('_') < 0;
    }

    public boolean isUpperSnakeCase(String name) {
        return name.equals(name.toUpperCase());
    }

    public boolean useWarningPainter(EntryReference<Entry<?>, Entry<?>> reference, Token token, TooltipEditorPane editor) {
        this.editor = editor;
        this.warningType = WarningType.UNKNOWN;
        String obfName = reference.entry.getName();
        if (!followsStyle(reference)) {
            this.gui.addWarningClass(WarningChecker.getClassParent(reference));
            this.editor.setTooltipPosition(token, this.warningType.getMessage(), obfName, TooltipEditorPane.TooltipType.WARNING);
            return true;
        } else if (this.editor.removeTooltip(obfName) != null){
            this.gui.removeWarningClass(WarningChecker.getClassParent(reference));
        }
        return false;
    }
}
