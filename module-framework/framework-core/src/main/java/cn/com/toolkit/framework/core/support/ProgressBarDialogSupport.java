package cn.com.toolkit.framework.core.support;

import cn.com.toolkit.framework.core.util.ToolKitFXUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class ProgressBarDialogSupport {
    private final ProgressBar progressBar;
    private final SimpleDoubleProperty progress;
    private final Stage progressBarStage;
    private final Label progressBarTextLabel;
    private final AtomicInteger progressBarCurrent;
    @Setter
    private int max;

    public ProgressBarDialogSupport(String title){
        progressBar = new ProgressBar(0);
        progress = new SimpleDoubleProperty(0);
        progressBarStage = new Stage();
        progressBarTextLabel = new Label();
        progressBarCurrent = new AtomicInteger(0);

        progressBar.progressProperty().bind(progress);
        progressBar.setPrefWidth(300);
        StackPane barStackPane = new StackPane(progressBar, progressBarTextLabel);
        barStackPane.getStyleClass().add("example");
        VBox dialogContent = new VBox(15);
        if(StringUtils.isNotBlank(title)){
            Label statusLabel = new Label(title);
            dialogContent.getChildren().add(statusLabel);
        }
        dialogContent.getChildren().add(barStackPane);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setStyle("-fx-alignment: center;");
        progressBarStage.initModality(Modality.WINDOW_MODAL);
        progressBarStage.initStyle(StageStyle.UNDECORATED);
        progressBarStage.setScene(new Scene(dialogContent, 400, 120));
    }
    public void setOwner(Window owner){
        progressBarStage.initOwner(owner);
    }
    public int getCurrent(){
        return progressBarCurrent.get();
    }
    public void increment(String barText){
        int current = progressBarCurrent.incrementAndGet();
        Platform.runLater(() -> {
            progressBarTextLabel.setText(barText);
            progress.set((double) current / max);
        });
    }
    public void increment(){
        int current = progressBarCurrent.incrementAndGet();
        Platform.runLater(() -> {
            progressBarTextLabel.setText(current + "/" + max);
            progress.set((double) current / max);
        });
    }
    public void reset(int max){
        this.max = max;
        progressBarCurrent.set(0);
        progress.set(0);
        progressBarTextLabel.setText("");
    }
    public void show(){
        if(progressBarStage.getOwner() == null) progressBarStage.initOwner(ToolKitFXUtil.getPrimaryStage());
        final Window owner = progressBarStage.getOwner();
        if (owner != null) {
            progressBarStage.sizeToScene();
            Platform.runLater(() -> {
                double centerX = owner.getX() + (owner.getWidth() - progressBarStage.getWidth()) / 2;
                double centerY = owner.getY() + (owner.getHeight() - progressBarStage.getHeight()) / 2;
                progressBarStage.setX(centerX);
                progressBarStage.setY(centerY);
            });
        }
        progressBarStage.show();
    }
    public void hide(){
        progressBarStage.hide();
    }
}
