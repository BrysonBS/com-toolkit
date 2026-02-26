package cn.com.toolkit.app.application;

import cn.com.toolkit.framework.core.support.BaseFxApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ToolkitApplication extends BaseFxApplication {

    @Override
    public void startWithLog(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ToolkitApplication.class.getResource("/views/toolkit-app.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("toolkit");
        stage.setScene(scene);
        stage.show();
    }
}
