package cn.com.toolkit.app.application;


import cn.com.toolkit.framework.core.control.TrayContextMenu;
import cn.com.toolkit.framework.core.support.BaseFxApplication;
import cn.com.toolkit.framework.core.support.FXScreenshot;
import cn.com.toolkit.framework.core.support.FXTraySupport;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;


public class ToolkitApplication extends BaseFxApplication {
    @Override
    public void startWithLog(Stage stage) throws Exception {
        Platform.setImplicitExit(false);

        FXMLLoader fxmlLoader = new FXMLLoader(ToolkitApplication.class.getResource("/views/toolkit-app.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 900, 600);
        FXScreenshot fxScreenshot = new FXScreenshot(stage);
        registerGlobalHotkey(fxScreenshot);
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
    private void registerGlobalHotkey(FXScreenshot fxScreenshot) {
        try {
            // 关闭jnativehook的日志
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);

            // 注册全局屏幕
            GlobalScreen.registerNativeHook();

            // 添加全局键盘监听
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    // 检查是否是 Alt + Shift + A
                    if (e.getKeyCode() == NativeKeyEvent.VC_A &&
                            (e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0 &&
                            (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) {
                        Platform.runLater(() -> fxScreenshot.startScreenshot(true, null));
                    }
                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent e) {}

                @Override
                public void nativeKeyTyped(NativeKeyEvent e) {}
            });

        } catch (Exception ignored) {}
    }
}
