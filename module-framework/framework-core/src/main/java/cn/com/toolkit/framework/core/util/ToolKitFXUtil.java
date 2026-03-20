package cn.com.toolkit.framework.core.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.image.BufferedImage;

public class ToolKitFXUtil {
    public static Stage getPrimaryStage(){
        return Stage.getWindows().stream()
                .filter(Stage.class::isInstance)
                .map(Stage.class::cast)
                .findFirst()
                .orElse(null);
    }
    public static Image cropImage(Image source, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return source;
        // 边界检查
        x = Math.max(0, Math.min(x, (int) source.getWidth() - 1));
        y = Math.max(0, Math.min(y, (int) source.getHeight() - 1));
        width = (int) Math.min(width, source.getWidth() - x);
        height = (int) Math.min(height, source.getHeight() - y);
        PixelReader reader = source.getPixelReader();
        WritableImage result = new WritableImage(width, height);
        // 使用setPixels批量操作
        result.getPixelWriter().setPixels(0, 0, width, height, reader, x, y);
        return result;
    }
    public static BufferedImage convertIkonliIconToImage(Ikon icon,int size,String colorStyle){
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setStyle(fontIcon.getStyle() + "-fx-icon-color: " + colorStyle + ";-fx-icon-size:" + size + "px;");
        Pane pane = new StackPane(fontIcon);
        pane.setPadding(Insets.EMPTY);
        pane.setStyle(pane.getStyle() + "-fx-background-color: transparent;");
        pane.setPrefSize(size, size);
        Scene tempScene = new Scene(pane, size, size);
        tempScene.setFill(Color.TRANSPARENT);
        WritableImage writableImage = new WritableImage(size, size);
        pane.snapshot(null, writableImage);
        return SwingFXUtils.fromFXImage(writableImage, null);
    }
}
