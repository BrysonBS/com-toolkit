package cn.com.toolkit.tools.charset.application;

import cn.com.toolkit.framework.core.support.BaseFxApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CharsetToolApplication extends BaseFxApplication {
    @Override
    public void startWithLog(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(CharsetToolApplication.class.getResource("/views/charset-tool.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("charset-tool");
        stage.setScene(scene);
        stage.show();
    }
}
