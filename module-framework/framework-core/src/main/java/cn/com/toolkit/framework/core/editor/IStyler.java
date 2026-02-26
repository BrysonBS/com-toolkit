package cn.com.toolkit.framework.core.editor;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface IStyler {
    StyleSpans<Collection<String>> style(String text);
    default StyleSpans<Collection<String>> style(String text, Pattern pattern, Map<String, String> groupToStyleClass){
        Matcher matcher = pattern.matcher(text);
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastSpanEnd = 0;
        while(matcher.find()) {
            String styleClass = null;
            for (String groupName : groupToStyleClass.keySet()) {
                if(matcher.group(groupName) != null)
                    styleClass = groupToStyleClass.get(groupName);
            }
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastSpanEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastSpanEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastSpanEnd);
        return spansBuilder.create();
    };
}
