package cn.com.toolkit.tools.cpbio.application;

import cn.com.toolkit.framework.core.support.BaseFxApplication;
import cn.com.toolkit.tools.cpbio.support.CpbioFxmlLoader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



public class CpbioToolApplication extends BaseFxApplication {
    @Override
    public void startWithLog(Stage stage) throws Exception{
        try{
            FXMLLoader fxmlLoader = new CpbioFxmlLoader();
            Parent root = fxmlLoader.load(getClass().getResource("/views/sap-2-doris.fxml").openStream());
            Scene scene = new Scene(root, 600, 400);
            stage.setTitle("SAP表转doris");
            stage.setScene(scene);
            stage.show();
        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}
