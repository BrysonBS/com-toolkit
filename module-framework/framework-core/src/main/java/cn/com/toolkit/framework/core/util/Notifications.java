package cn.com.toolkit.framework.core.util;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;


public class Notifications {
    private static final String NOTIFICATION_STAGE_TITLE = "notificationPopupStageTitle";
    public static void info(String message){
        show(message, new FontIcon(FontAwesomeSolid.INFO_CIRCLE),Pos.TOP_RIGHT,Styles.ACCENT, Styles.ELEVATED_1);
    }
    public static void success(String message){
        show(message, new FontIcon(FontAwesomeSolid.CHECK_CIRCLE),Pos.TOP_RIGHT,Styles.SUCCESS, Styles.ELEVATED_1);
    }
    public static void warning(String message){
        show(message, new FontIcon(FontAwesomeSolid.TIMES_CIRCLE),Pos.TOP_RIGHT,Styles.WARNING, Styles.ELEVATED_1);
    }
    public static void error(String message){
        show(message, new FontIcon(FontAwesomeSolid.EXCLAMATION_CIRCLE),Pos.TOP_RIGHT,Styles.DANGER, Styles.ELEVATED_1);
    }
    public static void show(String message,FontIcon icon,Pos pos,String... styleClass){
        show(message,icon,pos,2, 0,0,styleClass);
    }
    public static void show(String message,FontIcon icon, Pos pos,int delaySeconds, double width, double height, String... styleClass){
        Stage primaryStage = null;
        Stage popupStage = null;
        for (Window window : Stage.getWindows()) {
            if(window instanceof Stage stage){
                if(primaryStage == null) primaryStage = stage;
                if(NOTIFICATION_STAGE_TITLE.equals(stage.getTitle())) popupStage = stage;
            }
        }
        if(primaryStage == null) return;

        Notification notification = new Notification(message,icon);
        if(styleClass != null) notification.getStyleClass().addAll(styleClass);
        if(height != 0){
            notification.setPrefHeight(height);
            notification.setMaxHeight(height);
        }
        else{
            notification.setPrefHeight(Region.USE_PREF_SIZE);
            notification.setMaxHeight(Region.USE_PREF_SIZE);
        }
        if(width != 0) {
            notification.setPrefWidth(width);
            notification.setMaxWidth(width);
        }

        if(popupStage == null) {
            popupStage = new Stage();
            popupStage.setTitle(NOTIFICATION_STAGE_TITLE);

            StackPane stackPane = new StackPane();
            stackPane.setBackground(Background.fill(Color.TRANSPARENT));
            stackPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
            stackPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
            StackPane.setAlignment(notification, pos);

            Scene popupScene = new Scene(stackPane);
            updatePopupPosition(primaryStage,popupStage,true);

            popupScene.setFill(Color.TRANSPARENT);
            popupStage.initOwner(primaryStage);
            popupStage.initStyle(StageStyle.TRANSPARENT);
            popupStage.setScene(popupScene);
        }

        final StackPane popupStackPane = (StackPane) (popupStage.getScene().getRoot());
        updatePopupPosition(primaryStage,popupStage,false);
        popupStackPane.getChildren().add(notification);

        popupStage.show();
        Stage finalPopupStage = popupStage;
        notification.setOnClose(e -> {
            final Timeline out = slideOutUpDown(pos,notification,300);
            out.setOnFinished(f -> {
                popupStackPane.getChildren().clear();
                finalPopupStage.hide();
            });
            out.playFromStart();
        });
        Timeline in = slideInUpDown(pos,notification,300);
        in.playFromStart();

        PauseTransition delay = new PauseTransition(Duration.seconds(delaySeconds));
        delay.setOnFinished(e -> {
            final Timeline out = slideOutUpDown(pos,notification,300);
            out.setOnFinished(f -> {
                popupStackPane.getChildren().clear();
                finalPopupStage.hide();
            });
            out.playFromStart();
        });
        delay.play();
    }
    private static void updatePopupPosition(Stage primaryStage,Stage popupStage,boolean isInit){
        Runnable runnable = () -> Platform.runLater(() -> {
            Scene primaryScene = primaryStage.getScene();
            double sceneWidth = primaryScene.getWidth();
            double sceneHeight = primaryScene.getHeight();
            Node root = primaryScene.getRoot();
            popupStage.setX(root.localToScreen(0,0).getX());
            popupStage.setY(root.localToScreen(0,0).getY());
            popupStage.setWidth(sceneWidth);
            popupStage.setHeight(sceneHeight);
        });
        runnable.run();
        if(!isInit) return;
        primaryStage.xProperty().addListener((obs, oldVal, newVal) -> runnable.run());
        primaryStage.yProperty().addListener((obs, oldVal, newVal) -> runnable.run());
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> runnable.run());
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> runnable.run());
    }
    private static Timeline slideInUpDown(Pos pos,Node node,int delay){
        return switch (pos){
            case BOTTOM_RIGHT,BOTTOM_CENTER,BOTTOM_LEFT -> Animations.slideInUp(node, Duration.millis(delay));
            default -> Animations.slideInDown(node, Duration.millis(delay));
        };
    }
    private static Timeline slideOutUpDown(Pos pos,Node node,int delay){
        return switch (pos){
            case BOTTOM_RIGHT,BOTTOM_CENTER,BOTTOM_LEFT -> Animations.slideOutDown(node, Duration.millis(delay));
            default -> Animations.slideOutUp(node, Duration.millis(delay));
        };
    }
}
