package cn.com.toolkit.app.controller;

import atlantafx.base.theme.*;
import cn.com.toolkit.app.domain.bo.PluginInfo;
import cn.com.toolkit.app.plugin.PluginContainer;
import cn.com.toolkit.app.plugin.PluginUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Objects;

public class ToolkitAppController {
    @FXML private TabPane tabPane;
    @FXML private FlowPane contentFlowPane;

    @FXML
    private void initialize(){
        List<PluginInfo> toolList = PluginUtils.loadPlugins();
        for (PluginInfo pluginInfo : toolList) {
            Button button = new Button(pluginInfo.getName());
            button.setPrefSize(120,70);
            contentFlowPane.getChildren().add(button);
            contentFlowPane.setHgap(3);
            contentFlowPane.setVgap(3);
            button.setUserData(pluginInfo);
            button.setOnAction(this::handleAction);
            button.setContentDisplay(ContentDisplay.TOP);
            button.setGraphicTextGap(5);
            button.setText(pluginInfo.getName());
        }

    }


    private void handleAction(ActionEvent event) {
        PluginInfo pluginInfo = (PluginInfo) ((Button)(event.getSource())).getUserData();
        try {
            Tab tab = tabPane.getTabs().stream()
                    .filter(e -> e.getContent() == pluginInfo.getRoot())
                    .findFirst().orElse(null);
            if(tab == null) {
                tab = new Tab(pluginInfo.getName());
                if(pluginInfo.getRoot() == null){
                    PluginContainer container = new PluginContainer(pluginInfo);
                    pluginInfo.setRoot(container.getParentNode());
                }
                Parent root = pluginInfo.getRoot();
                tab.setContent(root);
                tabPane.getTabs().add(tab);
            }
            tabPane.getSelectionModel().select(tab);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void handleThemeChange(ActionEvent event) {
        RadioMenuItem radioMenuItem = (RadioMenuItem) event.getSource();
        radioMenuItem.setSelected(true);
        String themeName = radioMenuItem.getText();
        switch (themeName) {
            case "PrimerLight":
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                break;
            case "PrimerDark":
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                break;
            case "NordLight":
                Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
                break;
            case "NordDark":
                Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
                break;
            case "CupertinoLight":
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
                break;
            case "CupertinoDark":
                Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
                break;
            case "Dracula":
                Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
                break;
        }
    }
}
