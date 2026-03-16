package cn.com.toolkit.framework.core.support;

import atlantafx.base.theme.PrimerDark;
import cn.com.toolkit.framework.core.util.ToolKitFXUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class BaseFxApplication extends Application {
    private static final Double MAX_VALUE = Double.MAX_VALUE;
    protected static Logger log = LoggerFactory.getLogger(BaseFxApplication.class);
    protected Stage primaryStage;
    @Override
    public void init(){
        try{
            // 设置默认未捕获异常处理器
            setupGlobalExceptionHandling();
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

    @Override
    public void start(Stage stage){
        try{
            primaryStage = stage;
            startWithLog(stage);
            Scene scene = stage.getScene();
            if(scene != null) scene.getStylesheets().add(getClass().getResource("/assets/styles/main.css").toExternalForm());
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }
    public void startWithLog(Stage stage) throws Exception{

    }

    private void setupGlobalExceptionHandling() {
        // 处理UI线程异常
        Thread.currentThread().setUncaughtExceptionHandler(
                (thread, throwable) -> handleUncaughtException(throwable)
        );
        // 处理非UI线程异常
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, throwable) -> Platform.runLater(() -> handleUncaughtException(throwable))
        );
    }

    private void handleUncaughtException(Throwable throwable) {
        log.error("Uncaught exception", throwable);
        Alert dialog = createExceptionDialog(throwable);
        if (dialog != null) dialog.showAndWait();
    }
    private Alert createExceptionDialog(Throwable throwable) {
        Throwable cause = throwable;
        while(cause.getCause() != null) cause = cause.getCause();

        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(cause.getMessage());

        try (StringWriter sw = new StringWriter(); PrintWriter printWriter = new PrintWriter(sw)) {
            cause.printStackTrace(printWriter);

            var label = new Label("Full stacktrace:");

            var textArea = new TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setMaxWidth(MAX_VALUE);
            textArea.setMaxHeight(MAX_VALUE);

            var content = new VBox(5, label, textArea);
            content.setMaxWidth(MAX_VALUE);

            alert.getDialogPane().setExpandableContent(content);
            if(primaryStage == null) primaryStage = ToolKitFXUtil.getPrimaryStage();
            if(primaryStage != null) alert.initOwner(primaryStage);
            return alert;
        } catch (IOException e) {
            log.error("SpringFxApplication.createExceptionDialog.IOException: ", e);
            return null;
        }
    }
}
