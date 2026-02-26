package cn.com.toolkit.framework.core.editor.styler;

import cn.com.toolkit.framework.core.editor.IStyler;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SqlStyler implements IStyler {
    private static final String[] KEYWORDS = {
            "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES",
            "UPDATE", "SET", "DELETE", "CREATE", "DROP", "TABLE",
            "DATABASE", "INDEX", "VIEW", "ALTER", "ADD", "COLUMN",
            "DISTINCT", "ORDER", "BY", "GROUP", "HAVING", "JOIN",
            "INNER", "LEFT", "RIGHT", "OUTER", "ON", "AS", "AND",
            "OR", "NOT", "IN", "LIKE", "BETWEEN", "IS", "NULL",
            "TRUE", "FALSE", "UNION", "ALL", "EXISTS", "CASE",
            "WHEN", "THEN", "END", "LIMIT", "OFFSET", "COMMENT"
            ,"UNIQUE","ENGINE","KEY"
    };
    private static final String[] TYPES = {
            "INT", "VARCHAR", "CHAR", "TEXT", "DATE", "DATETIME",
            "TIMESTAMP", "DECIMAL", "FLOAT", "DOUBLE", "BOOLEAN",
            "BLOB", "CLOB"
    };
    private static final String[] FUNCTIONS = {
            "COUNT", "SUM", "AVG", "MIN", "MAX", "UPPER", "LOWER",
            "SUBSTRING", "CONCAT", "COALESCE", "NOW", "CURRENT_DATE"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String TYPE_PATTERN = "\\b(" + String.join("|", TYPES) + ")\\b";
    private static final String FUNCTION_PATTERN = "\\b(" + String.join("|", FUNCTIONS) + ")\\b";
    private static final String STRING_PATTERN = "'[^']*'|\"[^\"]*\"";
    private static final String COMMENT_PATTERN = "--[^\n]*" + "|" + "/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/";
    private static final String NUMBER_PATTERN = "\\b\\d+(\\.\\d+)?\\b";
    private static final String PARAM_PATTERN = ":[\\w]+|\\$\\d+";

    private static final String GROUP_KEYWORD = "KEYWORD";
    private static final String GROUP_TYPE = "TYPE";
    private static final String GROUP_FUNCTION = "FUNCTION";
    private static final String GROUP_STRING = "STRING";
    private static final String GROUP_COMMENT = "COMMENT";
    private static final String GROUP_NUMBER = "NUMBER";
    private static final String GROUP_PARAM = "PARAM";
    private static final Map<String, String> groupToStyleClass;

    static {
        groupToStyleClass = new HashMap<>();
        groupToStyleClass.put(GROUP_KEYWORD, "sql-keyword");
        groupToStyleClass.put(GROUP_TYPE, "sql-type");
        groupToStyleClass.put(GROUP_FUNCTION, "sql-function");
        groupToStyleClass.put(GROUP_STRING, "sql-string");
        groupToStyleClass.put(GROUP_COMMENT, "sql-comment");
        groupToStyleClass.put(GROUP_NUMBER, "sql-number");
        groupToStyleClass.put(GROUP_PARAM, "sql-param");
    }
    private static final Pattern PATTERN = Pattern.compile(
            "(?<" + GROUP_KEYWORD + ">" + KEYWORD_PATTERN + ")" +
                    "|(?<" + GROUP_TYPE + ">" + TYPE_PATTERN + ")" +
                    "|(?<" + GROUP_FUNCTION + ">" + FUNCTION_PATTERN + ")" +
                    "|(?<" + GROUP_STRING + ">" + STRING_PATTERN + ")" +
                    "|(?<" + GROUP_COMMENT + ">" + COMMENT_PATTERN + ")" +
                    "|(?<" + GROUP_NUMBER + ">" + NUMBER_PATTERN + ")" +
                    "|(?<" + GROUP_PARAM + ">" + PARAM_PATTERN + ")"
    ,Pattern.CASE_INSENSITIVE);
    @Override
    public StyleSpans<Collection<String>> style(String text) {
        return style(text,PATTERN,groupToStyleClass);
    }
}
