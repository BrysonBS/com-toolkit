package cn.com.toolkit.framework.core.control;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.*;

public class TrayContextMenu extends Stage {
    private final ObservableList<MenuItem> menuItemObservableList;
    private final StringProperty stylesheet = new SimpleStringProperty();
    private MenuButton menuButton;
    private final Stage primaryStage; // 主窗口引用
    public TrayContextMenu(Stage primaryStage) {
        this.menuItemObservableList = FXCollections.observableArrayList();
        this.primaryStage = primaryStage;
        initialize();
    }
    private void initialize(){
        initOwner(primaryStage);
        initStyle(StageStyle.UTILITY);
        setAlwaysOnTop(true);
        //失去焦点自动隐藏
        setupAutoHide();
        // 1. 创建主MenuButton
        menuButton = new MenuButton();
        // 2. 将所有菜单项（包括子菜单）添加到MenuButton
        Bindings.bindContent(menuButton.getItems(), menuItemObservableList);
        Scene scene = new Scene(menuButton);

        stylesheet.addListener((observableValue, oldVal, newVal) -> {
            scene.getStylesheets().removeAll("data:text/css," + oldVal);
            scene.getStylesheets().add("data:text/css," + newVal);
        });
        setScene(scene);
    }
    public TrayContextMenu addMenuItem(MenuItem menuItem){
        menuItemObservableList.add(menuItem);
        return this;
    }
    public TrayContextMenu addMenuItems(MenuItem... menuItems){
        menuItemObservableList.addAll(Arrays.asList(menuItems));
        return this;
    }
    public TrayContextMenu addExitMenuItem(String label){
        MenuItem menuItem = new MenuItem(label);
        FontIcon fontIcon = new FontIcon(FontAwesomeSolid.POWER_OFF);
        menuItem.setGraphic(fontIcon);
        menuItem.setOnAction(event -> {
            Platform.exit();
            System.exit(0);
        });
        menuItemObservableList.add(menuItem);
        return this;
    }
    public TrayContextMenu addOpenMenuItem(String label){
        MenuItem menuItem = new MenuItem(label);
        menuItem.setOnAction(event -> showPrimaryWindow());
        menuItemObservableList.add(menuItem);
        return this;
    }
    public TrayContextMenu addSeparator(){
        menuItemObservableList.add(new SeparatorMenuItem());
        return this;
    }
    public MenuItem getMenuItem(int index){
        return menuItemObservableList.get(index);
    }
    public void removeMenuItem(int index){
        menuItemObservableList.remove(index);
    }
    public void setStyleSheet(String styleSheet){
        stylesheet.set(styleSheet);
        if (menuButton.isShowing()) {
            menuButton.hide();
            Platform.runLater(() -> menuButton.show());
        }
    }

    // 打开主界面
    public void showPrimaryWindow() {
        if (primaryStage == null) return;
        Platform.runLater(() -> {
            if(!primaryStage.isShowing()) primaryStage.show();
            if(!primaryStage.isFocused()) primaryStage.toFront();
        });
        hide();
    }
    // 设置失去焦点时自动隐藏
    private void setupAutoHide() {
        focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) hide();
        });
    }
    // 显示菜单在指定屏幕坐标 (通常在鼠标指针附近)
    public void showMenu(double screenX, double screenY) {
        setOpacity(0);
        show();
        menuButton.hide();
        Platform.runLater(() -> {
            setX(screenX);
            setY(screenY - menuButton.getHeight());
            toFront();
            requestFocus(); // 请求焦点，以便失去焦点时自动隐藏
            menuButton.show();
        });
    }
}
