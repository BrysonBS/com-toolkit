package cn.com.toolkit.framework.core.editor.styler;

import cn.com.toolkit.framework.core.editor.IStyler;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class JsonStyler implements IStyler {
    private static final String[] KEYWORDS = {
            "true", "false", "null"
    };

    private static final String[] JSON_KEYWORDS = {
            "true", "false", "null"
    };

    private static final String STRING_PATTERN = "\"(?:[^\"\\\\]++|\\\\.)*+\"";
    private static final String NUMBER_PATTERN = "-?\\b\\d++(?:\\.\\d++)?(?:[eE][+-]?\\d++)?\\b";
    private static final String PROPERTY_PATTERN = "\"(?:[^\"\\\\]++|\\\\.)*+\"\\s*+:";
    private static final String BRACE_PATTERN = "[\\[\\]{}]";
    private static final String COLON_PATTERN = ":";
    private static final String COMMA_PATTERN = ",";
    private static final String COMMENT_PATTERN = "//[^\n\r]*+|/\\*.*?\\*/";

    private static final String GROUP_KEYWORD = "KEYWORD";
    private static final String GROUP_STRING = "STRING";
    private static final String GROUP_NUMBER = "NUMBER";
    private static final String GROUP_PROPERTY = "PROPERTY";
    private static final String GROUP_BRACE = "BRACE";
    private static final String GROUP_COLON = "COLON";
    private static final String GROUP_COMMA = "COMMA";
    private static final String GROUP_COMMENT = "COMMENT";

    private static final Map<String, String> groupToStyleClass;

    static {
        groupToStyleClass = new HashMap<>();
        groupToStyleClass.put(GROUP_KEYWORD, "json-keyword");
        groupToStyleClass.put(GROUP_STRING, "json-string");
        groupToStyleClass.put(GROUP_NUMBER, "json-number");
        groupToStyleClass.put(GROUP_PROPERTY, "json-property");
        groupToStyleClass.put(GROUP_BRACE, "json-brace");
        groupToStyleClass.put(GROUP_COLON, "json-colon");
        groupToStyleClass.put(GROUP_COMMA, "json-comma");
        groupToStyleClass.put(GROUP_COMMENT, "json-comment");
    }

    private static final Pattern PATTERN = Pattern.compile(
            "(?<" + GROUP_KEYWORD + ">" + "\\b(" + String.join("|", JSON_KEYWORDS) + ")\\b" + ")" +
                    "|(?<" + GROUP_PROPERTY + ">" + PROPERTY_PATTERN + ")" +
                    "|(?<" + GROUP_STRING + ">" + STRING_PATTERN + ")" +
                    "|(?<" + GROUP_NUMBER + ">" + NUMBER_PATTERN + ")" +
                    "|(?<" + GROUP_BRACE + ">" + BRACE_PATTERN + ")" +
                    "|(?<" + GROUP_COLON + ">" + COLON_PATTERN + ")" +
                    "|(?<" + GROUP_COMMA + ">" + COMMA_PATTERN + ")" +
                    "|(?<" + GROUP_COMMENT + ">" + COMMENT_PATTERN + ")"
    );

    @Override
    public StyleSpans<Collection<String>> style(String text) {
        return style(text, PATTERN, groupToStyleClass);
    }
}
