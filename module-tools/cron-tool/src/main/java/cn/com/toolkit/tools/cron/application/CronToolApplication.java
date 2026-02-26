package cn.com.toolkit.tools.cron.application;

import cn.com.toolkit.framework.core.support.BaseFxApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CronToolApplication extends BaseFxApplication {
    @Override
    public void startWithLog(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(CronToolApplication.class.getResource("/views/cron-tool.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 800, 500);
        stage.setTitle("cron-tool");
        stage.setScene(scene);
        stage.show();
    }
}
