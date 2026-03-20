package cn.com.toolkit.tools.excel.domain.po;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

public class ExcelInfo {
    private File file;
    private String name;
    private Long size;
    private final StringProperty extension = new SimpleStringProperty();

    public ExcelInfo(File file) {
        this.file = file;
        this.name = file.getName();
        this.size = file.length();
    }

    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getSize() {
        return size;
    }
    public void setSize(Long size) {
        this.size = size;
    }
    public String getExtension() {
        return extension.get();
    }
    public StringProperty extensionProperty() {
        return extension;
    }
}
