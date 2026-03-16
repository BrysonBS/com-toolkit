package cn.com.toolkit.tools.image.domain.bo;

import javafx.beans.property.*;

import java.io.File;

public class ImageInfo {
    private BooleanProperty select = new SimpleBooleanProperty(true);
    private File file;
    private String name;
    private String dimension;
    private Long size;
    private final StringProperty originExtension = new SimpleStringProperty();
    private Integer originWidth;
    private Integer originHeight;
    private final IntegerProperty width = new SimpleIntegerProperty();
    private final IntegerProperty height = new SimpleIntegerProperty();
    private final StringProperty extension = new SimpleStringProperty();
    private final BooleanProperty keepAspectRatio = new SimpleBooleanProperty(true);

    public ImageInfo() {}
    public ImageInfo(File file,String name, Long size, int width, int height, String originExtension) {
        this.file = file;
        this.name = name;
        this.dimension = width + "x" + height;
        this.size = size;
        originWidth = width;
        originHeight = height;
        this.width.set(width);
        this.height.set(height);
        this.originExtension.set(originExtension);
    }
    public boolean needAspectRatio() {
        return (originWidth != null && !originWidth.equals(width.get()))
                || (originHeight != null && !originHeight.equals(height.get()));
    }

    public boolean getKeepAspectRatio() {
        return keepAspectRatio.get();
    }

    public BooleanProperty keepAspectRatioProperty() {
        return keepAspectRatio;
    }
    public void setKeepAspectRatio(boolean keepAspectRatio) {
        this.keepAspectRatio.set(keepAspectRatio);
    }

    public boolean getSelect() {
        return select.get();
    }

    public BooleanProperty selectProperty() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select.set(select);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public int getWidth() {
        return width.get();
    }

    public IntegerProperty widthProperty() {
        return width;
    }

    public int getHeight() {
        return height.get();
    }

    public IntegerProperty heightProperty() {
        return height;
    }

    public String getExtension() {
        return extension.get();
    }

    public StringProperty extensionProperty() {
        return extension;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    public String getOriginExtension() {
        return originExtension.get();
    }

    public StringProperty originExtensionProperty() {
        return originExtension;
    }
}
