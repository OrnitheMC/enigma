package cuchaz.enigma.gui.util;

public class JavadocAnnotationUtil {
    public static final String LINK_FORMAT = "<a href=\"\">%s</a>";

    public static String generateLink(String text) {
        if (text.contains("#")) {
            return  String.format(LINK_FORMAT, text.substring(text.indexOf('#') + 1));
        } else if (text.contains(".")) {
            return String.format(LINK_FORMAT, text.substring(text.lastIndexOf('.') + 1));
        } else {
            return String.format(LINK_FORMAT, text);
        }
    }

    public static String linkToHtml(String text) {
        if (!text.startsWith("{@link") || !text.endsWith("}")) {
            return "";
        } else {
            return generateLink(text.replace("{@link", "").replace("}", ""));

        }
    }

    public static String linkPlainToHtml(String text) {
        if (!text.startsWith("{@linkplain") || !text.endsWith("}")) {
            return "";
        } else {
            return generateLink(text.replace("{@linkplain", "").replace("}", ""));
        }
    }

    public static String codeToHtml(String text) {
        return "";
    }

    public static String returnToHtml(String text) {
        return "";
    }

    public static String seeHtml(String text) {
        return "";
    }

    public static String throwsToHtml(String text) {
        return "";
    }

    public static String convertRawJavadoc(String javadoc) {
        return "";
    }
}
