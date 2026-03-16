package cn.com.toolkit.framework.core.control;

import cn.com.toolkit.framework.core.control.skin.ClearableComboBoxSkin;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

public class ClearableComboBox<T> extends ComboBox<T> {
    public ClearableComboBox() {
        super();
        setSkin(new ClearableComboBoxSkin<>(this));
    }
    public ClearableComboBox(ObservableList<T> items) {
        super(items);
        setSkin(new ClearableComboBoxSkin<>(this));
    }
}