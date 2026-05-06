package cn.com.toolkit.app.application;


import cn.com.toolkit.app.controller.ToolkitAppController;
import cn.com.toolkit.framework.core.control.TrayContextMenu;
import cn.com.toolkit.framework.core.support.BaseFxApplication;
import cn.com.toolkit.framework.core.support.FXScreenshot;
import cn.com.toolkit.framework.core.support.FXTraySupport;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ToolkitApplication extends BaseFxApplication {
    private static final String appId = ToolkitApplication.class.getName();
    @Override
    public void startWithLog(Stage stage) throws Exception {
        try {
            JUnique.acquireLock(appId, message -> {
                if ("SHOW".equals(message)) {
                    Platform.runLater(() -> {
                        if (primaryStage != null) {
                            primaryStage.show();
                            primaryStage.toFront();
                        }
                    });
                }
                return "OK";
            });
        } catch (AlreadyLockedException e) {
            // 已有实例运行，发送消息并退出
            JUnique.sendMessage(appId, "SHOW");
            System.exit(0);
        }
        Platform.setImplicitExit(false);

        FXMLLoader fxmlLoader = new FXMLLoader(ToolkitApplication.class.getResource("/views/toolkit-app.fxml"));
        Parent root = fxmlLoader.load();
        ToolkitAppController toolkitAppController = fxmlLoader.getController();

        Scene scene = new Scene(root, 900, 600);
        FXScreenshot fxScreenshot = new FXScreenshot(stage);
        fxScreenshot.registerScreenshotHotkey();
        toolkitAppController.setFxScreenshot(fxScreenshot);


        //最小化托盘区
        TrayContextMenu trayContextMenu = new TrayContextMenu(primaryStage)
            .addOpenMenuItem("打开主界面")
            .addExitMenuItem("退出");
        FXTraySupport fxTraySupport = new FXTraySupport(trayContextMenu);
        fxTraySupport.apply();

        stage.setTitle("toolkit");
        stage.setScene(scene);
        stage.show();
    }
}
