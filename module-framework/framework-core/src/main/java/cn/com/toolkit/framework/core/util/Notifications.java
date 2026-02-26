package cn.com.toolkit.framework.core.util;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;


public class Notifications {
    public static void info(String message,Pos pos){
        show(message, new FontIcon(FontAwesomeSolid.INFO_CIRCLE),pos,Styles.ACCENT, Styles.ELEVATED_1);
    }
    public static void success(String message, Pos pos){
        show(message, new FontIcon(FontAwesomeSolid.CHECK_CIRCLE),pos,Styles.SUCCESS, Styles.ELEVATED_1);
    }
    public static void warning(String message,Pos pos){
        show(message, new FontIcon(FontAwesomeSolid.TIMES_CIRCLE),pos,Styles.WARNING, Styles.ELEVATED_1);
    }
    public static void error(String message,Pos pos){
        show(message, new FontIcon(FontAwesomeSolid.EXCLAMATION_CIRCLE),pos,Styles.DANGER, Styles.ELEVATED_1);
    }
    public static void show(String message,FontIcon icon,Pos pos,String... styleClass){
        show(null,message,icon, 0,0,pos,new Insets(10, 10, 0, 0),styleClass);
    }
    public static void show(StackPane stackPane, String message, FontIcon icon, double width, double height, Pos pos, Insets insets, String... styleClass){
        if(stackPane == null){
            Stage primaryStage = Stage.getWindows().stream()
                    .filter(Stage.class::isInstance)
                    .map(Stage.class::cast)
                    .findFirst()
                    .orElse(null);
            if(primaryStage == null) return;
            Parent root = primaryStage.getScene().getRoot();
            if(root instanceof StackPane rootStackPane) stackPane = rootStackPane;
            else{
                stackPane = new StackPane();
                stackPane.getChildren().add(root);
                primaryStage.getScene().setRoot(stackPane);
            }
        }
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

        StackPane.setAlignment(notification, pos);
        StackPane.setMargin(notification, insets);
        final StackPane finalStackPane = stackPane;

        final Timeline out = Animations.slideOutUp(notification, Duration.millis(500));
        out.setOnFinished(f -> finalStackPane.getChildren().remove(notification));
        notification.setOnClose(e -> out.playFromStart());

        Timeline in = Animations.slideInDown(notification, Duration.millis(500));
        if (!stackPane.getChildren().contains(notification)) {
            stackPane.getChildren().add(notification);
        }
        in.playFromStart();
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> out.playFromStart());
        delay.play();
    }
}
