package cn.com.toolkit.framework.core.control.skin;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

public class ClearableComboBoxSkin<T> extends ComboBoxListViewSkin<T> {

    private final Button clearButton;
    private final StackPane buttonContainer;

    public ClearableComboBoxSkin(ComboBox<T> comboBox) {
        super(comboBox);

        FontIcon fontIcon = new FontIcon(FontAwesomeSolid.TIMES_CIRCLE);
        // 创建清除按钮
        clearButton = new Button(null, fontIcon);
        clearButton.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT);
        clearButton.setVisible(false);
        clearButton.setFocusTraversable(false);

        comboBox.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            double buttonSize = Math.max(6, newHeight.doubleValue() - 10);
            int iconSize = (int)Math.ceil(buttonSize * 0.5);
            clearButton.setStyle(clearButton.getStyle() + "-fx-background-color: transparent;");
            clearButton.setPadding(new Insets(0,iconSize , 0, 0));
            fontIcon.setStyle(fontIcon.getStyle() + "-fx-icon-size: " + iconSize + "px;");
            fontIcon.setIconSize(10);
        });

        // 清除功能
        clearButton.setOnAction(e -> {
            comboBox.setValue(null);
            comboBox.getSelectionModel().clearSelection();
            if (comboBox.isEditable()) {
                comboBox.getEditor().clear();
            }
            e.consume();
        });

        // 监听值变化
        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            clearButton.setVisible(newVal != null);
        });

        // 创建容器
        buttonContainer = new StackPane(clearButton);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setMouseTransparent(false);
        buttonContainer.setPickOnBounds(false);

        // 添加到 Skin 的节点树
        getChildren().add(buttonContainer);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);

        if (clearButton.isVisible()) {
            double buttonWidth = 25;
            double buttonHeight = h - 8;

            buttonContainer.resizeRelocate(
                    x + w - buttonWidth - 10,  // 右侧留10px边距
                    y + (h - buttonHeight) / 2,
                    buttonWidth,
                    buttonHeight
            );
        }
    }
}

