package cuchaz.enigma.gui.warning;

import cuchaz.enigma.utils.I18n;

public enum WarningType {
    STATIC_FINAL_FIELD("warning.static_final_field"),
    CLASS("warning.class"),
    FIELD("warning.field"),
    ATOMIC_FIELD("warning.atomic_field"),
    METHOD("warning.method"),
    ARGUMENT("warning.argument"),
    UNKNOWN("warning.unknown");

    private final String warningMessage;

    WarningType(String translatableText) {
        this.warningMessage = I18n.translate(translatableText);
    }

    public String getMessage() {
        return warningMessage;
    }
}
