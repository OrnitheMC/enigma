package cuchaz.enigma.gui.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavadocAnnotationUtil {
    public static final String LINK_FORMAT = "<a href=\"\" style=\"text-decoration: none\">%s</a>";
    public static final Pattern SEE_PATTERN = Pattern.compile("(@see ([a-zA-Z]*)(\\.[a-zA-Z]+)*((\\.|#| )[a-zA-Z]+)(\\((([a-zA-Z]*(( [a-zA-Z]*)?, ))*[a-zA-Z]*( [a-zA-Z]+)?)\\))?)");
    public static final Pattern LINK_PATTERN = Pattern.compile("(?<=\\{@link )(([a-zA-Z]*)(\\.[a-zA-Z]+)*((\\.|#| )[a-zA-Z]+)(\\((([a-zA-Z]*(( [a-zA-Z]*)?, ))*[a-zA-Z]*( [a-zA-Z]+)?)\\))?)(?=\\s*})");
    public static final Pattern LINKPLAIN_PATTERN = Pattern.compile("(?<=\\{@linkplain )(([a-zA-Z]*)(\\.[a-zA-Z]+)*((\\.|#| )[a-zA-Z]+)(\\((([a-zA-Z]*(( [a-zA-Z]*)?, ))*[a-zA-Z]*( [a-zA-Z]+)?)\\))?)(?=\\s*})");
    public static final Pattern CODE_PATTERN = Pattern.compile("(?<=\\{@code)(.|[\\s])*?(?=})");
    public static final Pattern OTHER_PATTERN = Pattern.compile("[\\s\\S]*");
    public static final JavadocTags[] NON_INLINES = {JavadocTags.PARAM, JavadocTags.RETURN, JavadocTags.THROWS, JavadocTags.SEE};

    /**
     * Generates a valid html link from a valid linked annotation. This tries to handle as many inputs as possible.
     *
     */
    public static String generateLink(String text) {
        if (text.contains("#")) {
            if (text.startsWith("#")) {
                return  String.format(LINK_FORMAT, text.replace("#", "") );
            } else {
                return  String.format(LINK_FORMAT, text.replace("#", ".") );
            }
        } else if (text.contains(" ")) {
            return String.format(LINK_FORMAT, text.substring(text.indexOf(' ') + 1));
        } else {
            return String.format(LINK_FORMAT, text);
        }
    }

    public static String htmlSuperscript(String bruh) {
        return "<sup>" + bruh + "</sup>";
    }

    public static String htmlSubscript(String text) {
        return "<sub>" + text + "</sub>";
    }

    public static String linkToHtml(String text) {
        return generateLink(text);
    }

    public static String linkPlainToHtml(String text) {
        return generateLink(text);
    }


    public static String returnToHtml(LinkedList<String> entries) {
        return JavadocTags.RETURN.getTranslation() + entries.get(0).replace("@return ", "");
    }

    public static String paramsToHtml(LinkedList<String> entries) {
        return javadocList(entries, JavadocTags.PARAM);
    }

    /**
     * Runs over all entries and puts the after each other formatted like the see tag would format.
     * @return the line in the javadoc containing all the @see entries
     */
    public static String seeToHtml(LinkedList<String> entries) {
        StringBuilder stringBuilder = new StringBuilder(JavadocTags.SEE.getTranslation());
        int index = 0;
        for (String entry : entries) {
            if (index != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(htmlSuperscript(generateLink(entry.replace(JavadocTags.SEE.getText() + " ", ""))));
            index++;
        }
        return stringBuilder.toString();
    }

    public static String throwsToHtml(LinkedList<String> entries) {
        return javadocList(entries, JavadocTags.THROWS, true);
    }

    public static String javadocList(LinkedList<String> entries, JavadocTags tag) {
        return javadocList(entries, tag, false);
    }

    public static String javadocList(LinkedList<String> entries, JavadocTags tag, Boolean link) {
        StringBuilder stringBuilder = new StringBuilder(tag.getTranslation());
        int index = 0;
        for (String entry : entries) {
            if (index != 0) {
                stringBuilder.append("<br>").append(new String(new char[tag.getTranslation().length()*2 - 2]).replace("\0", "&nbsp;"));
            }
            if (link) {
                entry = entry.replace(tag.getText() + " ", "");
                String toLink = entry.substring(0, entry.indexOf(" "));
                stringBuilder.append(entry.replaceFirst(toLink, generateLink(toLink)));
            } else {
                stringBuilder.append(entry.replace(tag.getText() + " ", "").replaceFirst(" ", " - "));
            }
            index++;
        }
        return stringBuilder.toString();
    }

    public static String setGreen(String javadoc) {
        return setDefaultColor(javadoc, "green");
    }

    public static String setDefaultColor(String javadoc, String color) {
        return String.format("<body style=\"color:%s\">", color) + javadoc;
    }

    public static String convertRawJavadoc(String javadoc) {
        HashMap<JavadocTags, LinkedList<String>> matches = new HashMap<>();
        getMatches(javadoc, matches);
        javadoc = processMatches(javadoc, matches);
        javadoc = setGreen(javadoc);
        return javadoc;
    }

    public static void getMatches(String javadoc, HashMap<JavadocTags, LinkedList<String>> matches) {
        addLineMatches(javadoc, matches);
        addInlineMatches(javadoc, matches);
    }

    public static String processMatches(String javadoc, HashMap<JavadocTags, LinkedList<String>> matches) {
        javadoc = addTagsToEnd(javadoc, matches);
        for (JavadocTags tag : matches.keySet()) {
            for (String match : matches.get(tag)) {
                switch (tag) {
                    case LINK -> javadoc = javadoc.replace("{" + tag.getText() + " " + match.trim() + "}", linkToHtml(match));
                    case LINKPLAIN -> javadoc = javadoc.replace("{" + tag.getText() + " " + match.trim() + "}", linkPlainToHtml(match));
                    case CODE -> javadoc = javadoc.replace("{" + tag.getText() + match + "}", match.trim());
                    default -> javadoc = javadoc.replace(match, "");
                }
            }
        }
        return javadoc;
    }

    public static String addTagsToEnd(String javadoc, HashMap<JavadocTags, LinkedList<String>> lineMatches) {
        StringBuilder javadocBuilder = new StringBuilder(javadoc);
        for (JavadocTags tag : NON_INLINES) {
            if (lineMatches.containsKey(tag)) {
                javadocBuilder.append("<br>");
                switch (tag) {
                    case PARAM -> javadocBuilder.append(paramsToHtml(lineMatches.get(tag)));
                    case RETURN -> javadocBuilder.append(returnToHtml(lineMatches.get(tag)));
                    case THROWS -> javadocBuilder.append(throwsToHtml(lineMatches.get(tag)));
                    case SEE -> javadocBuilder.append(seeToHtml(lineMatches.get(tag)));
                }
            }
        }
        return javadocBuilder.toString();
    }

    public static void addLineMatches(String javadoc, HashMap<JavadocTags, LinkedList<String>> matches) {
        String[] lines = javadoc.split("\n");
        System.out.println("lines: ");
        for (String line : lines) {
            System.out.println(line);
            for (JavadocTags tag : NON_INLINES) {
                if (line.startsWith(tag.getText())) {
                    if (!matches.containsKey(tag)) {
                        matches.put(tag, new LinkedList<>(Arrays.asList(line)));
                    } else {
                        matches.get(tag).add(line);
                    }
                }
            }
        }
    }

    public static void addInlineMatches(String javadoc, HashMap<JavadocTags, LinkedList<String>> matches) {
        addMatches(matches, javadoc, JavadocTags.LINK);
        addMatches(matches, javadoc, JavadocTags.LINKPLAIN);
        addMatches(matches, javadoc, JavadocTags.CODE);
    }

    public static void addMatches(HashMap<JavadocTags, LinkedList<String>> matches, String javadoc, JavadocTags tag) {
        matches.put(tag, new LinkedList<>());
        Matcher m = tag.getPattern().matcher(javadoc);
        while (m.find()) {
            matches.get(tag).add(m.group());
        }
    }

    private enum JavadocTags {
        CODE(CODE_PATTERN),
        LINK(LINK_PATTERN),
        LINKPLAIN(LINKPLAIN_PATTERN),
        PARAM("Params: "),
        RETURN("Returns: "),
        SEE("See Also: "),
        THROWS("Throws: ");

        private final boolean inline;
        private final Pattern pattern;
        private String translation = "";

        JavadocTags(Pattern pattern) {
            this.inline = false;
            this.pattern = pattern;
        }

        JavadocTags(String translated) {
            this.inline = true;
            this.pattern = null;
            this.translation = translated;
        }

        public String getText() {
            return "@" + this.name().toLowerCase();
        }

        public boolean isInline() {
            return this.inline;
        }

        public Pattern getPattern() {
            return this.pattern;
        }

        public String getTranslation() {
            return this.translation;
        }
    }
}