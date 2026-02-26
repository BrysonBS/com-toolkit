package cn.com.toolkit.framework.core.editor;

import cn.com.toolkit.framework.core.editor.styler.JavaStyler;
import cn.com.toolkit.framework.core.editor.styler.JsonStyler;
import cn.com.toolkit.framework.core.editor.styler.SqlStyler;

import java.util.List;

public enum LanguageType {
    JAVA(List.of("java"), "Java", "/assets/styles/editor/java-keywords.css",new JavaStyler()),
    SQL(List.of("sql"), "SQL", "/assets/styles/editor/sql-keywords.css",new SqlStyler()),
    JSON(List.of("json"), "JSON", "/assets/styles/editor/json-keywords.css",new JsonStyler()),
    XML(List.of("xml"), "XML", "/assets/styles/editor/xml-keywords.css",null),
    C(List.of("json"), "C", "/assets/styles/editor/c-keywords.css",null),
    PLAIN_TEXT(List.of("txt", "text", "md", "log"), "Plain Text", null,null)

    ;

    private final List<String> extensions;
    private final String displayName;
    private final String keywordsFile;
    private final IStyler styler;

    LanguageType(List<String> extensions, String displayName, String keywordsFile,IStyler styler) {
        this.extensions = extensions;
        this.displayName = displayName;
        this.keywordsFile = keywordsFile;
        this.styler = styler;
    }
    public static LanguageType fromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return PLAIN_TEXT;
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        for (LanguageType type : values()) {
            if (type.extensions.contains(extension)) {
                return type;
            }
        }
        return PLAIN_TEXT;
    }
    public List<String> getExtensions() { return extensions; }
    public String getDisplayName() { return displayName; }
    public String getKeywordsFile() { return keywordsFile; }
    public IStyler getStyler() { return styler; }
}