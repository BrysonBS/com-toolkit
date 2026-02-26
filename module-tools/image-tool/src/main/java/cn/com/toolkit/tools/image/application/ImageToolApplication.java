package cn.com.toolkit.tools.image.application;

import cn.com.toolkit.framework.core.support.BaseFxApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ImageToolApplication extends BaseFxApplication {
    @Override
    public void startWithLog(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ImageToolApplication.class.getResource("/views/image-tool.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 800, 500);
        stage.setTitle("image-tool");
        stage.setScene(scene);
        stage.show();
    }
}
