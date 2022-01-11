package cuchaz.enigma.gui.panels;

import cuchaz.enigma.gui.ClassSelector;
import cuchaz.enigma.gui.Gui;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.utils.I18n;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class WarningPanel extends JPanel {
    public final ClassSelector warningClasses;
    private final JLabel title = new JLabel();
    private Set<ClassEntry> warningEntries = new HashSet<>() {
    };

    private final Gui gui;

    public WarningPanel(Gui gui) {
        this.gui = gui;

        Comparator<ClassEntry> obfClassComparator = (a, b) -> {
            String aname = a.getFullName();
            String bname = b.getFullName();
            if (aname.length() != bname.length()) {
                return aname.length() - bname.length();
            }
            return aname.compareTo(bname);
        };

        this.warningClasses = new ClassSelector(gui, obfClassComparator, false);
        this.warningClasses.setSelectionListener(gui.getController()::navigateTo);
        this.warningClasses.setRenameSelectionListener(gui::onRenameFromClassTree);

        this.setLayout(new BorderLayout());
        this.add(this.title, BorderLayout.NORTH);
        this.add(new JScrollPane(this.warningClasses), BorderLayout.CENTER);

        this.retranslateUi();
    }

    public void retranslateUi() {
        this.title.setText("Warnings");
    }

    public void addEntry(ClassEntry entry) {
        this.warningEntries.add(entry);
        this.warningClasses.setClasses(this.warningEntries);
    }

    public void removeEntry(ClassEntry entry) {
        this.warningEntries.remove(entry);
        this.warningClasses.setClasses(this.warningEntries);
    }
}
