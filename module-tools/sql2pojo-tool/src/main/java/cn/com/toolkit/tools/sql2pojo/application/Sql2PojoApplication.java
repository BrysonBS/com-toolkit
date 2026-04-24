package cn.com.toolkit.tools.sql2pojo.application;

import cn.com.toolkit.framework.core.support.BaseFxApplication;
import cn.com.toolkit.tools.sql2pojo.support.Sql2PojoFxmlLoader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Sql2PojoApplication extends BaseFxApplication {
    @Override
    public void startWithLog(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new Sql2PojoFxmlLoader();
        Parent root = fxmlLoader.load(Sql2PojoApplication.class.getResource("/views/sql2pojo-tool.fxml").openStream());
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("sql2pojo-tool");
        stage.setScene(scene);
        stage.show();
    }
}
