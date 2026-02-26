package cn.com.toolkit.framework.core.editor.styler;

import cn.com.toolkit.framework.core.editor.IStyler;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class JavaStyler implements IStyler {
    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"   // for whole text processing (text blocks)
            + "|" + "/\\*[^\\v]*" + "|" + "^\\h*\\*([^\\v]*|/)";  // for visible paragraph processing (line by line)
    private static final String GROUP_KEYWORD = "KEYWORD";
    private static final String GROUP_PAREN = "PAREN";
    private static final String GROUP_BRACE = "BRACE";
    private static final String GROUP_BRACKET = "BRACKET";
    private static final String GROUP_SEMICOLON = "SEMICOLON";
    private static final String GROUP_STRING = "STRING";
    private static final String GROUP_COMMENT = "COMMENT";
    private static final Map<String, String> groupToStyleClass;

    static {
        groupToStyleClass = new HashMap<>();
        groupToStyleClass.put(GROUP_KEYWORD, "java-keyword");
        groupToStyleClass.put(GROUP_PAREN, "java-paren");
        groupToStyleClass.put(GROUP_BRACE, "java-brace");
        groupToStyleClass.put(GROUP_BRACKET, "java-bracket");
        groupToStyleClass.put(GROUP_SEMICOLON, "java-semicolon");
        groupToStyleClass.put(GROUP_STRING, "java-string");
        groupToStyleClass.put(GROUP_COMMENT, "java-comment");
    }
    private static final Pattern PATTERN = Pattern.compile(
            "(?<" + GROUP_KEYWORD + ">" + KEYWORD_PATTERN + ")" +
                    "|(?<" + GROUP_PAREN + ">" + PAREN_PATTERN + ")" +
                    "|(?<" + GROUP_BRACE + ">" + BRACE_PATTERN + ")" +
                    "|(?<" + GROUP_BRACKET + ">" + BRACKET_PATTERN + ")" +
                    "|(?<" + GROUP_SEMICOLON + ">" + SEMICOLON_PATTERN + ")" +
                    "|(?<" + GROUP_STRING + ">" + STRING_PATTERN + ")" +
                    "|(?<" + GROUP_COMMENT + ">" + COMMENT_PATTERN + ")"
    );

    @Override
    public StyleSpans<Collection<String>> style(String text) {
        return style(text,PATTERN,groupToStyleClass);
    }
}
