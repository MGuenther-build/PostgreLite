package util;

import java.util.regex.*;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import java.util.Collection;



public class SQLHighlighter {

    private static final String[] KEYWORDS = {
        "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE",
        "CREATE", "TABLE", "DROP", "ALTER", "JOIN", "ON", "AS",
        "AND", "OR", "NOT", "NULL", "DEFAULT", "PRIMARY", "KEY"
    };

    private static final Pattern PATTERN = Pattern.compile(
        "\\b(" + String.join("|", KEYWORDS) + ")\\b", Pattern.CASE_INSENSITIVE
    );

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            spansBuilder.add(java.util.Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(java.util.Collections.singleton("keyword"), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(java.util.Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
