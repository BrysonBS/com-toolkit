package cn.com.toolkit.framework.core.editor.styler;

import cn.com.toolkit.framework.core.editor.IStyler;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlStyler implements IStyler {
    private static final String STYLE_COMMENT = "xml-comment";
    private static final String STYLE_TAG = "xml-tag";
    private static final String STYLE_ATTRIBUTE = "xml-attribute";
    private static final String STYLE_ATTRIBUTE_VALUE = "xml-attribute-value";

    // 分别编译各个模式
    private static final Pattern COMMENT_PATTERN = Pattern.compile("<!--.*?-->");
    private static final Pattern ATTRIBUTE_VALUE_PATTERN = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("[a-zA-Z_][\\w\\-]*\\s*=");
    private static final Pattern TAG_PATTERN = Pattern.compile("</?[a-zA-Z_][\\w\\-]*");

    @Override
    public StyleSpans<Collection<String>> style(String text) {
        if (text == null || text.isEmpty()) {
            StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
            builder.add(Collections.emptyList(), 0);
            return builder.create();
        }

        // 收集所有匹配项
        List<Match> matches = new ArrayList<>();

        // 匹配注释
        Matcher m = COMMENT_PATTERN.matcher(text);
        while (m.find()) {
            matches.add(new Match(m.start(), m.end(), STYLE_COMMENT));
        }

        // 匹配属性值
        m = ATTRIBUTE_VALUE_PATTERN.matcher(text);
        while (m.find()) {
            matches.add(new Match(m.start(), m.end(), STYLE_ATTRIBUTE_VALUE));
        }

        // 匹配属性名
        m = ATTRIBUTE_PATTERN.matcher(text);
        while (m.find()) {
            matches.add(new Match(m.start(), m.end(), STYLE_ATTRIBUTE));
        }

        // 匹配标签名
        m = TAG_PATTERN.matcher(text);
        while (m.find()) {
            matches.add(new Match(m.start(), m.end(), STYLE_TAG));
        }

        // 排序并合并
        matches.sort(Comparator.comparingInt(a -> a.start));

        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastPos = 0;

        for (Match match : matches) {
            if (match.start > lastPos) {
                spansBuilder.add(Collections.emptyList(), match.start - lastPos);
            }
            spansBuilder.add(Collections.singleton(match.style), match.end - match.start);
            lastPos = match.end;
        }

        if (lastPos < text.length()) {
            spansBuilder.add(Collections.emptyList(), text.length() - lastPos);
        }

        return spansBuilder.create();
    }

    private static class Match {
        int start, end;
        String style;
        Match(int s, int e, String st) { start = s; end = e; style = st; }
    }
}