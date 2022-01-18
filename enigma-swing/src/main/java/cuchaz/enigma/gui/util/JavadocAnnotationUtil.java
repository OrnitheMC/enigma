package cuchaz.enigma.gui.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavadocAnnotationUtil {
    public static final String LINK_FORMAT = "<a href=\"\">%s</a>";
    public static final String INLINE_FORMAT = "(\\{@%s ([a-zA-Z]*|\\.|#|(\\(([a-zA-Z]*|([a-zA-Z]* ))*\\)))*\\})";
    public static final String OTHER_FORMAT = "%s";

    public static String generateLink(String text) {
        System.out.println(text);
        if (text.contains("#")) {
            return  String.format(LINK_FORMAT, text.substring(text.lastIndexOf('#') + 1));
        } else if (text.contains(".")) {
            return String.format(LINK_FORMAT, text.substring(text.lastIndexOf('.') + 1));
        } else {
            return String.format(LINK_FORMAT, text);
        }
    }

    public static String htmlSuperscript(String text) {
        return "<sup>" + text + "</sup>";
    }

    public static String htmlSubscript(String text) {
        return "<sub>" + text + "</sub>";
    }

    public static String linkToHtml(String text) {
        if (!text.startsWith("{@link") || !text.endsWith("}")) {
            return "";
        } else {
            return generateLink(htmlSuperscript(text.replaceAll("(\\{@link|}| )", "")));

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
        HashSet<String> matches = getAllMatches(new HashSet<>(), javadoc);
        for (String match : matches) {
            javadoc = javadoc.replace(match, linkToHtml(match));
        }
        return javadoc;
    }

    public static HashSet<String> getAllMatches(HashSet<String> matches, String javadoc) {
        addMatches(matches, javadoc, JavadocTags.LINK.getPattern());
        addMatches(matches, javadoc, JavadocTags.LINKPLAIN.getPattern());
        return matches;
    }

    public static void addMatches(HashSet<String> matches, String javadoc, String expression) {
        Matcher m = Pattern.compile(expression).matcher(javadoc);
        while (m.find()) {
            matches.add(m.group());
        }
    }

    private enum JavadocTags {
        CODE(true, String.format(INLINE_FORMAT, "code")),
        LINK(true, String.format(INLINE_FORMAT, "link")),
        LINKPLAIN(true, String.format(INLINE_FORMAT, "linkplain")),
        RETURN(false, String.format(OTHER_FORMAT, "return")),
        SEE(false, String.format(OTHER_FORMAT, "see")),
        THROWS(false, String.format(OTHER_FORMAT, "throws"));

        private final boolean inline;
        private final String pattern;

        JavadocTags(boolean inline, String pattern) {
            this.inline = inline;
            this.pattern = pattern;
        }

        public String getText() {
            return "@" + this.name().toLowerCase();
        }

        public boolean isInline() {
            return this.inline;
        }

        public String getPattern() {
            return this.pattern;
        }
    }
}
