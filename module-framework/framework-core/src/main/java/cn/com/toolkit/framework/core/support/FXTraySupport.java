package cn.com.toolkit.framework.core.support;

import cn.com.toolkit.framework.core.control.TrayContextMenu;
import cn.com.toolkit.framework.core.util.ToolKitFXUtil;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Locale;

public class FXTraySupport {
    private final TrayContextMenu trayContextMenu;
    private TrayIcon trayIcon;
    private final Image icon;
    private final String tooltip;
    private static final int iconSize = isWin() ? 16 : 22;

    public FXTraySupport(TrayContextMenu trayContextMenu) {
        this(trayContextMenu,null,null);
    }
    public FXTraySupport(TrayContextMenu trayContextMenu,Image icon) {
        this(trayContextMenu,icon,null);
    }
    public FXTraySupport(TrayContextMenu trayContextMenu,Image icon, String tooltip) {
        this.trayContextMenu = trayContextMenu;
        this.icon = icon == null ? defaultIconImage() : icon;
        this.tooltip = tooltip;
        initialize();
    }
    //初始化托盘图标
    private void initialize(){
        if (!isSupported()) return;
        trayIcon = new TrayIcon(icon, tooltip);
        //托盘图标
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Platform.runLater(() -> trayContextMenu.showMenu(e.getXOnScreen(), e.getYOnScreen()));
                } else if (e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e)) {
                    Platform.runLater(trayContextMenu::showPrimaryWindow);
                }
            }
        });
    }
    public void apply() {
        // 1. 检查系统是否支持托盘
        if (!isSupported()) return;
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::apply);
            return;
        }
        //将图标添加到系统托盘
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }
    public void showMessage(String title, String message) {
        if(StringUtils.isEmpty(title) && StringUtils.isEmpty(message)) return;
        if (isMac()) showMacAlert(title, message, "Information");
        else EventQueue.invokeLater(() -> {
            if(trayIcon == null) return;
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        });
    }
    private static boolean isWin() {
        return System.getProperty("os.name")
                .toLowerCase(Locale.ENGLISH)
                .contains("windows");
    }
    private boolean isMac() {
        return System.getProperty("os.name")
                .toLowerCase(Locale.ENGLISH)
                .contains("mac");
    }
    private static boolean isSupported() {
        return Desktop.isDesktopSupported() && SystemTray.isSupported();
    }
    private void showMacAlert(String subTitle, String message, String title) {
        String execute = String.format(
                "display notification \"%s\""
                        + " with title \"%s\""
                        + " subtitle \"%s\"",
                message != null ? message : "",
                title != null ? title : "",
                subTitle != null ? subTitle : ""
        );
        try {
            Runtime.getRuntime().exec(new String[]{"osascript", "-e", execute});
        }
        catch (IOException e) {
            throw new UnsupportedOperationException("Cannot run osascript with given parameters.");
        }
    }
    private static Image defaultIconImage() {
        return ToolKitFXUtil.convertIkonliIconToImage(FontAwesomeSolid.TOOLS,iconSize,"-color-accent-fg");
    }
}
