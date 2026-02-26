package cn.com.toolkit.tools.json.application;

import cn.com.toolkit.framework.core.support.BaseFxApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JsonToolApplication extends BaseFxApplication {
    @Override
    public void startWithLog(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(JsonToolApplication.class.getResource("/views/json-format.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("json-format");
        stage.setScene(scene);
        stage.show();
    }
}
