package cn.com.toolkit.framework.core.support;

import atlantafx.base.theme.Styles;
import cn.com.toolkit.framework.core.enums.DatePatternEnum;
import cn.com.toolkit.framework.core.enums.HomeDirectoryEnum;
import cn.com.toolkit.framework.core.util.ToolKitFXUtil;
import cn.com.toolkit.framework.core.util.ToolKitUtil;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 完整的JavaFX截图工具
 * 功能类似QQ截图，支持区域选择、移动和调整大小
 */
public class FXScreenshot {
    //复制快捷键
    private final KeyCombination ctrlC = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
    //保存快捷键
    private final KeyCombination ctrlS = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private final Stage primaryStage;
    private Consumer<Image> screenshotConsumer;
    private Boolean isShowing;
    private Stage screenshotStage;
    private ImageView fullscreenImageView;
    private Pane overlay;
    private Rectangle backgroundRectangle;
    private File initialDirectory;

    // 截图相关变量
    private Rectangle selectionRect;
    private final Rectangle[] resizeHandles = new Rectangle[8];
    private double startX, startY;
    private double rectStartX, rectStartY;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private int resizeHandleIndex = -1;

    // 工具栏
    private ToolBar toolBar;
    private Label sizeLabel;

    // 最小选择区域尺寸
    private static final double MIN_SIZE = 0;
    // 调整手柄大小
    private static final double HANDLE_SIZE = 8;

    public FXScreenshot(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public FXScreenshot() {
        this(ToolKitFXUtil.getPrimaryStage());
    }

    /**
     * 开始截图
     */
    public void startScreenshot(boolean hidePrimaryStage,Consumer<Image> screenshotConsumer) {
        this.screenshotConsumer = screenshotConsumer;
        if(primaryStage != null) isShowing = primaryStage.isShowing();
        try {
            if(hidePrimaryStage){
                if(primaryStage != null && isShowing) primaryStage.setIconified(true);
                PauseTransition pause = new PauseTransition(Duration.millis(200));
                pause.setOnFinished(e -> Platform.runLater(this::showScreenshotWindow));
                pause.play();
            }
            else showScreenshotWindow();
        }catch (Exception e){
            if(hidePrimaryStage && primaryStage != null && isShowing != primaryStage.isShowing())
                primaryStage.setIconified(!isShowing);
            throw new RuntimeException(e);
        }
    }

    private void showScreenshotWindow(){
        // 获取屏幕尺寸
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        int screenWidth = (int)Math.round(screenBounds.getWidth());
        int screenHeight = (int)Math.round(screenBounds.getHeight());
        // 截取全屏
        WritableImage fullScreenShot = captureScreen(screenWidth,screenHeight);

        // 创建截图窗口
        screenshotStage = new Stage();
        screenshotStage.initStyle(StageStyle.TRANSPARENT);
        screenshotStage.setAlwaysOnTop(true);
        screenshotStage.setFullScreen(true);
        screenshotStage.setFullScreenExitHint("");

        // 创建背景层
        Pane root = new Pane();
        // 显示截图的ImageView
        fullscreenImageView = new ImageView(fullScreenShot);
        fullscreenImageView.setFitWidth(screenWidth);
        fullscreenImageView.setFitHeight(screenHeight);

        // 创建遮罩层
        overlay = createOverlayPane(screenWidth, screenHeight);
        root.getChildren().addAll(fullscreenImageView, overlay);

        Scene scene = new Scene(root, screenWidth, screenHeight);
        scene.setFill(Color.TRANSPARENT);


        screenshotStage.setScene(scene);
        screenshotStage.show();

        // 添加键盘监听
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) closeScreenshot();
            else if (ctrlS.match(event)) saveCurrentScreenshot();
            else if(ctrlC.match(event)) copyToClipboard();
        });
        scene.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton() != MouseButton.SECONDARY) return;
            closeScreenshot();
        });
    }

    /**
     * 创建遮罩层和选择区域
     */
    private Pane createOverlayPane(double width, double height) {
        backgroundRectangle = new Rectangle(width, height);
        overlay = new Pane();
        overlay.setPrefSize(width, height);

        String accentColor = "-color-accent-emphasis";
        String fgColor = "-color-fg-default";

        // 创建选择矩形
        selectionRect = new Rectangle(0, 0, 0, 0);
        selectionRect.setFill(Color.TRANSPARENT);
        selectionRect.setStyle(selectionRect.getStyle() + "-fx-stroke: " + accentColor + ";");
        selectionRect.setStrokeWidth(2);
        selectionRect.setStrokeDashOffset(5);
        selectionRect.getStrokeDashArray().addAll(10.0, 5.0);

        // 创建8个调整手柄
        for (int i = 0; i < 8; i++) {
            resizeHandles[i] = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
            resizeHandles[i].setStyle(resizeHandles[i].getStyle()
                    + "-fx-stroke: " + accentColor + ";"
                    + "-fx-fill: " + fgColor + ";");

            resizeHandles[i].setStrokeWidth(1);
            resizeHandles[i].setVisible(false);
            overlay.getChildren().add(resizeHandles[i]);
        }

        // 创建工具栏
        toolBar = createToolBar();
        toolBar.setVisible(false);

        // 大小标签
        sizeLabel = new Label();
        sizeLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-text-fill: -color-light; -fx-padding: 3;");
        sizeLabel.setVisible(false);
        updateOverlay();
        overlay.getChildren().addAll(selectionRect, toolBar, sizeLabel);

        // 鼠标事件处理
        setupMouseHandlers(overlay);

        return overlay;
    }

    /**
     * 设置鼠标事件处理器
     */
    private void setupMouseHandlers(Pane overlay) {
        // 鼠标按下
        overlay.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double x = event.getX();
                double y = event.getY();

                // 检查是否点击了调整手柄
                resizeHandleIndex = getResizeHandleIndex(x, y);
                if (resizeHandleIndex != -1) {
                    isResizing = true;
                    startX = x;
                    startY = y;
                    rectStartX = selectionRect.getX();
                    rectStartY = selectionRect.getY();
                    event.consume();
                }
                // 检查是否点击在选择区域内（移动区域）
                else if (isPointInSelectionRect(x, y)) {
                    isDragging = true;
                    startX = x - selectionRect.getX();
                    startY = y - selectionRect.getY();
                    overlay.setCursor(Cursor.MOVE);
                    event.consume();
                }
                // 开始新选择
                else {
                    selectionRect.setX(x);
                    selectionRect.setY(y);
                    selectionRect.setWidth(0);
                    selectionRect.setHeight(0);
                    startX = x;
                    startY = y;
                    isDragging = false;
                    isResizing = false;

                    // 隐藏工具栏和手柄
                    toolBar.setVisible(false);
                    sizeLabel.setVisible(false);
                    for (Rectangle handle : resizeHandles) {
                        handle.setVisible(false);
                    }
                }
            }
        });

        // 鼠标拖动
        overlay.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double x = event.getX();
                double y = event.getY();

                if (isResizing) {
                    // 调整大小
                    resizeSelection(x, y);
                    updateHandlePositions();
                } else if (isDragging) {
                    // 移动区域
                    moveSelection(x, y);
                    updateHandlePositions();
                } else {
                    // 绘制新区域
                    drawSelection(x, y);
                }

                updateOverlay();
                // 更新大小标签
                updateSizeLabelAndToolBar();
                event.consume();
            }
        });

        // 鼠标释放
        overlay.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (selectionRect.getWidth() > MIN_SIZE && selectionRect.getHeight() > MIN_SIZE) {
                    showToolbarAndHandles();
                } else {
                    selectionRect.setWidth(0);
                    selectionRect.setHeight(0);
                    updateOverlay();
                }

                isDragging = false;
                isResizing = false;
                resizeHandleIndex = -1;
                overlay.setCursor(Cursor.DEFAULT);
            }
        });

        // 鼠标移动 - 改变光标形状
        overlay.setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();

            if (selectionRect.getWidth() > 0 && selectionRect.getHeight() > 0) {
                int handleIndex = getResizeHandleIndex(x, y);
                if (handleIndex != -1) {
                    setCursorForHandle(handleIndex, overlay);
                } else if (isPointInSelectionRect(x, y)) {
                    overlay.setCursor(Cursor.MOVE);
                } else {
                    overlay.setCursor(Cursor.CROSSHAIR);
                }
            } else {
                overlay.setCursor(Cursor.CROSSHAIR);
            }
        });
    }

    /**
     * 绘制选择区域
     */
    private void drawSelection(double x, double y) {
        double newX = Math.min(startX, x);
        double newY = Math.min(startY, y);
        double newWidth = Math.abs(x - startX);
        double newHeight = Math.abs(y - startY);

        selectionRect.setX(newX);
        selectionRect.setY(newY);
        selectionRect.setWidth(newWidth);
        selectionRect.setHeight(newHeight);
    }

    /**
     * 移动选择区域
     */
    private void moveSelection(double x, double y) {
        double newX = x - startX;
        double newY = y - startY;

        // 边界检查
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        newX = Math.max(0, Math.min(newX, screenBounds.getWidth() - selectionRect.getWidth()));
        newY = Math.max(0, Math.min(newY, screenBounds.getHeight() - selectionRect.getHeight()));

        selectionRect.setX(newX);
        selectionRect.setY(newY);
    }

    /**
     * 调整选择区域大小
     */
    private void resizeSelection(double x, double y) {
        double rectX = selectionRect.getX();
        double rectY = selectionRect.getY();
        double rectWidth = selectionRect.getWidth();
        double rectHeight = selectionRect.getHeight();

        switch (resizeHandleIndex) {
            case 0: // 左上
                rectWidth = rectX + rectWidth - x;
                rectHeight = rectY + rectHeight - y;
                rectX = x;
                rectY = y;
                break;
            case 1: // 上中
                rectHeight = rectY + rectHeight - y;
                rectY = y;
                break;
            case 2: // 右上
                rectWidth = x - rectX;
                rectHeight = rectY + rectHeight - y;
                rectY = y;
                break;
            case 3: // 右中
                rectWidth = x - rectX;
                break;
            case 4: // 右下
                rectWidth = x - rectX;
                rectHeight = y - rectY;
                break;
            case 5: // 下中
                rectHeight = y - rectY;
                break;
            case 6: // 左下
                rectWidth = rectX + rectWidth - x;
                rectHeight = y - rectY;
                rectX = x;
                break;
            case 7: // 左中
                rectWidth = rectX + rectWidth - x;
                rectX = x;
                break;
        }

        // 最小尺寸限制
        if (rectWidth >= MIN_SIZE && rectHeight >= MIN_SIZE) {
            selectionRect.setX(rectX);
            selectionRect.setY(rectY);
            selectionRect.setWidth(rectWidth);
            selectionRect.setHeight(rectHeight);
        }
    }

    /**
     * 判断点是否在选择区域内
     */
    private boolean isPointInSelectionRect(double x, double y) {
        return x >= selectionRect.getX() &&
                x <= selectionRect.getX() + selectionRect.getWidth() &&
                y >= selectionRect.getY() &&
                y <= selectionRect.getY() + selectionRect.getHeight();
    }

    /**
     * 根据手柄位置设置光标
     */
    private void setCursorForHandle(int index, Pane overlay) {
        switch (index) {
            case 0: overlay.setCursor(Cursor.NW_RESIZE); break;
            case 1: overlay.setCursor(Cursor.N_RESIZE); break;
            case 2: overlay.setCursor(Cursor.NE_RESIZE); break;
            case 3: overlay.setCursor(Cursor.E_RESIZE); break;
            case 4: overlay.setCursor(Cursor.SE_RESIZE); break;
            case 5: overlay.setCursor(Cursor.S_RESIZE); break;
            case 6: overlay.setCursor(Cursor.SW_RESIZE); break;
            case 7: overlay.setCursor(Cursor.W_RESIZE); break;
        }
    }

    /**
     * 更新大小标签和工具栏
     */
    private void updateSizeLabelAndToolBar() {
        if (selectionRect.getWidth() > 0 && selectionRect.getHeight() > 0) {
            String sizeText = String.format("%.0f x %.0f", selectionRect.getWidth(), selectionRect.getHeight());
            sizeLabel.setText(sizeText);
            sizeLabel.setVisible(true);

            double x = selectionRect.getX();
            double y = selectionRect.getY();
            double w = selectionRect.getWidth();
            double h = selectionRect.getHeight();

            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            int screenWidth = (int)Math.round(screenBounds.getWidth());
            int screenHeight = (int)Math.round(screenBounds.getHeight());
            int sizeLabelWidth = (int)Math.ceil(sizeLabel.getWidth());
            int sizeLabelHeight = (int)Math.ceil(sizeLabel.getHeight());
            int toolBarWidth = (int)Math.ceil(toolBar.getWidth());
            int toolBarHeight = (int)Math.ceil(toolBar.getHeight());
            int maxNodeWidth = Math.max(toolBarWidth,sizeLabelWidth);
            int maxNodeHeight = Math.max(toolBarHeight,sizeLabelHeight);

            //定位标签与工具栏

            double sizeLabelX = x + 3;
            double sizeLabelY = y + h + 3;
            double toolBarX = x + w - toolBarWidth - 3;
            double toolBarY = sizeLabelY;
            //下方内侧
            if(w > sizeLabelWidth + toolBarWidth + 6 && screenHeight - y - h > maxNodeHeight + 3);
            //下方外侧
            else if(x > sizeLabelWidth + 3 && screenWidth - x - w > toolBarWidth + 3 && screenHeight - y - h > maxNodeHeight + 3){
                sizeLabelX = x - sizeLabelWidth - 3;
                toolBarX = x + w + 3;
            }
            //上方内侧
            else if(w > sizeLabelWidth + toolBarWidth + 6 && y > maxNodeHeight + 3){
                sizeLabelY = y - sizeLabelHeight - 3;
                toolBarY = y - toolBarHeight - 3;
            }
            //上方外侧
            else if(x > sizeLabelWidth + 3 && screenWidth - x - w > toolBarWidth + 3 && y > maxNodeHeight + 3){
                sizeLabelX = x - sizeLabelWidth - 3;
                toolBarX = x + w + 3;
                sizeLabelY = y - sizeLabelHeight - 3;
                toolBarY = y - toolBarHeight - 3;
            }
            //右侧内侧
            else if(h > sizeLabelHeight + toolBarHeight + 6 && screenWidth - x - w > maxNodeWidth + 3){
                sizeLabelX = x + w + 3;
                toolBarX = sizeLabelX;
                sizeLabelY = y + 3;
                toolBarY = y + h - toolBarHeight - 3;
            }
            //右侧外侧
            else if(y > sizeLabelHeight + 3 && screenHeight - y - h > toolBarHeight + 3 && screenWidth - x - w > maxNodeWidth + 3){
                sizeLabelX = x + w + 3;
                toolBarX = sizeLabelX;
                sizeLabelY = y - sizeLabelHeight - 3;
                toolBarY = y + h + 3;
            }
            //左侧内侧
            else if(h > sizeLabelHeight + toolBarHeight + 6 && x > maxNodeWidth + 3){
                sizeLabelX = x - sizeLabelWidth - 3;
                toolBarX = x - toolBarWidth - 3;
                sizeLabelY = y + 3;
                toolBarY = y + h - toolBarHeight - 3;
            }
            //左侧外侧
            else if(y > sizeLabelHeight + 3 && screenHeight - y - h > toolBarHeight + 3 && x > maxNodeWidth + 3){
                sizeLabelX = x - sizeLabelWidth - 3;
                toolBarX = x - toolBarWidth - 3;
                sizeLabelY = y - sizeLabelHeight - 3;
                toolBarY = y + h + 3;
            }
            //右上向下并列
            else if(screenHeight - y > sizeLabelHeight + toolBarHeight + 3 && screenWidth - x - w > maxNodeWidth + 3){
                sizeLabelX = x + w + 3;
                toolBarX = sizeLabelX;
                sizeLabelY = y;
                toolBarY = y + sizeLabelHeight + 3;
            }
            //右下向上并列
            else if(y + h > sizeLabelHeight + toolBarHeight + 3 && screenWidth - x - w > maxNodeWidth + 3){
                sizeLabelX = x + w + 3;
                toolBarX = sizeLabelX;
                sizeLabelY = y + h - toolBarHeight - 3 - sizeLabelHeight;
                toolBarY = y + h - toolBarHeight;
            }
            //左上向下并列
            else if(screenHeight - y > sizeLabelHeight + toolBarHeight + 3 && x > maxNodeWidth + 3){
                sizeLabelX = x - sizeLabelWidth - 3;
                toolBarX = x - toolBarWidth - 3;
                sizeLabelY = y;
                toolBarY = y + sizeLabelHeight + 3;
            }
            //左下向上并列
            else if(y + h > sizeLabelHeight + toolBarHeight + 3 && x > maxNodeWidth + 3){
                sizeLabelX = x - sizeLabelWidth - 3;
                toolBarX = x - toolBarWidth - 3;
                sizeLabelY = y + h - toolBarHeight - 3 - sizeLabelHeight;
                toolBarY = y + h - toolBarHeight;
            }
            //下方内部
            else if(w > sizeLabelWidth + toolBarWidth + 6){
                sizeLabelX = x + 3;
                toolBarX = x + w - toolBarWidth - 3;
                sizeLabelY = y + h - sizeLabelHeight - 3;
                toolBarY = y + h - toolBarHeight - 3;
            }


            sizeLabel.setLayoutX(sizeLabelX);
            sizeLabel.setLayoutY(sizeLabelY);
            toolBar.setLayoutX(toolBarX);
            toolBar.setLayoutY(toolBarY);
        }
    }
    private void updateOverlay(){
        String key = "overlay:subtract";
        javafx.scene.shape.Shape subtract = javafx.scene.shape.Shape.subtract(backgroundRectangle, selectionRect);
        subtract.setFill(Color.rgb(0, 0, 0, 0.4));
        subtract.setUserData(key);
        overlay.getChildren().removeIf(node -> key.equals(node.getUserData()));
        overlay.getChildren().addFirst(subtract);
    }

    /**
     * 获取调整手柄索引
     */
    private int getResizeHandleIndex(double x, double y) {
        if (selectionRect.getWidth() == 0) return -1;

        double rectX = selectionRect.getX();
        double rectY = selectionRect.getY();
        double rectW = selectionRect.getWidth();
        double rectH = selectionRect.getHeight();

        // 检查每个手柄的位置
        if (Math.abs(x - rectX) <= HANDLE_SIZE/2 && Math.abs(y - rectY) <= HANDLE_SIZE/2) return 0; // 左上
        if (Math.abs(x - (rectX + rectW/2)) <= HANDLE_SIZE/2 && Math.abs(y - rectY) <= HANDLE_SIZE/2) return 1; // 上中
        if (Math.abs(x - (rectX + rectW)) <= HANDLE_SIZE/2 && Math.abs(y - rectY) <= HANDLE_SIZE/2) return 2; // 右上
        if (Math.abs(x - (rectX + rectW)) <= HANDLE_SIZE/2 && Math.abs(y - (rectY + rectH/2)) <= HANDLE_SIZE/2) return 3; // 右中
        if (Math.abs(x - (rectX + rectW)) <= HANDLE_SIZE/2 && Math.abs(y - (rectY + rectH)) <= HANDLE_SIZE/2) return 4; // 右下
        if (Math.abs(x - (rectX + rectW/2)) <= HANDLE_SIZE/2 && Math.abs(y - (rectY + rectH)) <= HANDLE_SIZE/2) return 5; // 下中
        if (Math.abs(x - rectX) <= HANDLE_SIZE/2 && Math.abs(y - (rectY + rectH)) <= HANDLE_SIZE/2) return 6; // 左下
        if (Math.abs(x - rectX) <= HANDLE_SIZE/2 && Math.abs(y - (rectY + rectH/2)) <= HANDLE_SIZE/2) return 7; // 左中

        return -1;
    }

    /**
     * 显示工具栏和调整手柄
     */
    private void showToolbarAndHandles() {
        toolBar.setVisible(true);
        updateHandlePositions();
        for (Rectangle handle : resizeHandles)
            handle.setVisible(true);
    }

    /**
     * 更新调整手柄位置
     */
    private void updateHandlePositions() {
        double x = selectionRect.getX();
        double y = selectionRect.getY();
        double w = selectionRect.getWidth();
        double h = selectionRect.getHeight();

        // 8个手柄的位置
        resizeHandles[0].setX(x - HANDLE_SIZE/2);
        resizeHandles[0].setY(y - HANDLE_SIZE/2);

        resizeHandles[1].setX(x + w/2 - HANDLE_SIZE/2);
        resizeHandles[1].setY(y - HANDLE_SIZE/2);

        resizeHandles[2].setX(x + w - HANDLE_SIZE/2);
        resizeHandles[2].setY(y - HANDLE_SIZE/2);

        resizeHandles[3].setX(x + w - HANDLE_SIZE/2);
        resizeHandles[3].setY(y + h/2 - HANDLE_SIZE/2);

        resizeHandles[4].setX(x + w - HANDLE_SIZE/2);
        resizeHandles[4].setY(y + h - HANDLE_SIZE/2);

        resizeHandles[5].setX(x + w/2 - HANDLE_SIZE/2);
        resizeHandles[5].setY(y + h - HANDLE_SIZE/2);

        resizeHandles[6].setX(x - HANDLE_SIZE/2);
        resizeHandles[6].setY(y + h - HANDLE_SIZE/2);

        resizeHandles[7].setX(x - HANDLE_SIZE/2);
        resizeHandles[7].setY(y + h/2 - HANDLE_SIZE/2);
    }

    /**
     * 创建工具栏
     */
    private ToolBar createToolBar() {
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: rgba(0, 0, 0,0.7); -fx-padding: 3;");

        Button saveBtn = new Button(null, new FontIcon(FontAwesomeSolid.DOWNLOAD));
        saveBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.ACCENT);
        saveBtn.setStyle("-fx-background-color: -color-base-7;");
        saveBtn.setMouseTransparent(false);
        saveBtn.setTooltip(new Tooltip("保存 (Ctrl+S)"));
        saveBtn.setOnAction(e -> saveCurrentScreenshot());

        Button copyBtn = new Button(null, new FontIcon(FontAwesomeSolid.CHECK));
        copyBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.SUCCESS);
        copyBtn.setStyle("-fx-background-color: -color-base-7;");
        copyBtn.setMouseTransparent(false);
        copyBtn.setTooltip(new Tooltip("复制 (Ctrl+C)"));
        copyBtn.setOnAction(e -> copyToClipboard());

        Button cancelBtn = new Button(null, new FontIcon(FontAwesomeSolid.TIMES));
        cancelBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
        cancelBtn.setStyle("-fx-background-color: -color-base-7;");
        cancelBtn.setMouseTransparent(false);
        cancelBtn.setTooltip(new Tooltip("退出 (Esc)"));
        cancelBtn.setOnAction(e -> closeScreenshot());

        toolbar.getItems().addAll(saveBtn, cancelBtn,copyBtn);
        toolbar.setCursor(javafx.scene.Cursor.DEFAULT);

        return toolbar;
    }

    /**
     * 截取全屏
     */
    private WritableImage captureScreen(int captureWidth, int captureHeight) {
        try {
            Robot robot = new Robot();
            java.awt.Rectangle screenRect = new java.awt.Rectangle(captureWidth, captureHeight);
            return SwingFXUtils.toFXImage(robot.createScreenCapture(screenRect),null);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 保存当前截图
     */
    private void saveCurrentScreenshot() {
        Image image = getSelectionRectImage();
        if(image == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存截图");
        String userHome = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(userHome, HomeDirectoryEnum.PICTURES.getValue()));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG图片 (*.png)", "*.png"));
        String timeStamp = ToolKitUtil.dateToString(LocalDateTime.now(), DatePatternEnum.DATE_TIME_PATTERN_2);
        fileChooser.setInitialFileName(timeStamp + ".png");
        if(initialDirectory != null && initialDirectory.isDirectory())
            fileChooser.setInitialDirectory(initialDirectory);
        File file = fileChooser.showSaveDialog(screenshotStage);
        if(file == null) return;
        try {
            initialDirectory = file.getParentFile();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            closeScreenshot();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 复制到剪贴板
     */
    private void copyToClipboard() {
        Image image = getSelectionRectImage();
        if(image == null) return;
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        if(screenshotConsumer != null) screenshotConsumer.accept(image);

        Transferable transferable = new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{ DataFlavor.imageFlavor };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.imageFlavor.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (isDataFlavorSupported(flavor)) {
                    return bufferedImage;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        };

        // 设置到系统剪贴板
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
        closeScreenshot();
    }
    private Image getSelectionRectImage(){
        int x = (int) Math.round(selectionRect.getX());
        int y = (int) Math.round(selectionRect.getY());
        int width = (int) Math.round(selectionRect.getWidth());
        int height = (int) Math.round(selectionRect.getHeight());
        Image image = fullscreenImageView.getImage();
        if(image == null) return null;
        return ToolKitFXUtil.cropImage(image,x,y,width,height);
    }

    /**
     * 关闭截图窗口
     */
    private void closeScreenshot() {
        if (screenshotStage != null) screenshotStage.close();
        if(isShowing != null && isShowing) {
            primaryStage.setIconified(false);
            primaryStage.requestFocus();
        }
    }
}
