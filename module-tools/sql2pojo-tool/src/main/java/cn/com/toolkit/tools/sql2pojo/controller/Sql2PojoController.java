package cn.com.toolkit.tools.sql2pojo.controller;

import cn.com.toolkit.framework.core.editor.EditorCodeArea;
import cn.com.toolkit.framework.core.editor.LanguageType;
import cn.com.toolkit.framework.core.support.MybatisPlusManager;
import cn.com.toolkit.framework.core.util.JasyptUtils;
import cn.com.toolkit.framework.core.util.Notifications;
import cn.com.toolkit.framework.core.util.ToolKitFXUtil;
import cn.com.toolkit.tools.sql2pojo.domain.po.SysDatabaseConfig;
import cn.com.toolkit.tools.sql2pojo.service.SysDatabaseConfigService;
import cn.com.toolkit.tools.sql2pojo.service.impl.SysDatabaseConfigServiceImpl;
import cn.com.toolkit.tools.sql2pojo.support.CodeGenerator;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;

public class Sql2PojoController {
    private ClassLoader classLoader;
    @FXML private Tab mapperXmlTab;
    @FXML private TabPane codeTablePane;
    @FXML private Tab sqlTab;
    @FXML private Tab pojoTab;
    private EditorCodeArea sqlCodeArea;
    private EditorCodeArea pojoCodeArea;
    private EditorCodeArea mapperXmlCodeArea;

    private SysDatabaseConfig sysDatabaseConfig;
    public Button databaseConfigButton;
    private SysDatabaseConfigService sysDatabaseConfigService;
    @FXML
    private void initialize(){
        classLoader = this.getClass().getClassLoader();
        codeTablePane.getSelectionModel().select(sqlTab);
        sqlCodeArea = new EditorCodeArea(true, LanguageType.SQL);
        StackPane sqlStackPane = new StackPane(new VirtualizedScrollPane<>(sqlCodeArea));
        VBox.setVgrow(sqlStackPane, Priority.ALWAYS);
        sqlTab.setContent(sqlStackPane);

        pojoCodeArea = new EditorCodeArea(true,LanguageType.JAVA);
        pojoCodeArea.setEditable(false);
        StackPane pojoStackPane = new StackPane(new VirtualizedScrollPane<>(pojoCodeArea));
        VBox.setVgrow(pojoStackPane, Priority.ALWAYS);
        pojoTab.setContent(pojoStackPane);

        mapperXmlCodeArea = new EditorCodeArea(true,LanguageType.XML);
        mapperXmlCodeArea.setEditable(false);
        StackPane mapperXmlStackPane = new StackPane(new VirtualizedScrollPane<>(mapperXmlCodeArea));
        VBox.setVgrow(mapperXmlStackPane, Priority.ALWAYS);
        mapperXmlTab.setContent(mapperXmlStackPane);

        sysDatabaseConfigService = MybatisPlusManager.getSingletonServiceImpl(SysDatabaseConfigServiceImpl.class);
        sysDatabaseConfig = sysDatabaseConfigService
                .list(Wrappers.<SysDatabaseConfig>lambdaQuery()
                        .eq(SysDatabaseConfig::getIsDefault, true))
                .stream()
                .peek(e -> e.setPassword(JasyptUtils.decryptAuto(e.getPassword(),e.getUsername())))
                .findFirst()
                .orElse(null);
    }

    public void handleConnection(ActionEvent event) throws IOException {
        URL resource = Sql2PojoController.class.getResource("/views/database-config.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setClassLoader(classLoader);
        loader.setLocation(resource);
        Parent root = loader.load();
        DatabaseConfigController controller = loader.getController();
        if(sysDatabaseConfig != null) controller.setSysDatabaseConfig(sysDatabaseConfig);

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(new Scene(root));
        popupStage.initOwner(ToolKitFXUtil.getPrimaryStage());
        popupStage.setTitle("连接配置");
        popupStage.showAndWait();
        if(controller.isConfirmed()){
            sysDatabaseConfig = controller.getSysDatabaseConfig();
        }
    }
    @FXML
    private void handleGenerate(ActionEvent event) {
        if(sysDatabaseConfig == null){
            Notifications.error("请先配置数据库连接!");
            return;
        }
        String sql = sqlCodeArea.getText();
        if(StringUtils.isBlank(sql)){
            Notifications.error("请先填写SQL语句!");
            return;
        }
        try (Connection conn = DriverManager.getConnection(
                sysDatabaseConfig.getUrl(),
                sysDatabaseConfig.getUsername(),
                sysDatabaseConfig.getPassword())
        ) {
            if(conn == null || conn.isClosed()){
                Notifications.error("连接数据库失败!");
                return;
            }
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                ResultSetMetaData metaData = preparedStatement.getMetaData();
                if(metaData == null || metaData.getColumnCount() == 0) return;
                HashMap<String,String> columnMap = new HashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columnMap.put(metaData.getColumnName(i), metaData.getColumnTypeName(i));
                }
                String className = "POJO";
                String code = CodeGenerator.generatePojo(null,className,columnMap);
                if(StringUtils.isNotBlank(code)) {
                    pojoCodeArea.setText(code);
                    codeTablePane.getSelectionModel().select(pojoTab);
                }
                String mapperXml = CodeGenerator.generateMapperXmlWithResultMap(className,columnMap);
                if(StringUtils.isNotBlank(mapperXml)) {
                    mapperXmlCodeArea.setText(mapperXml);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
