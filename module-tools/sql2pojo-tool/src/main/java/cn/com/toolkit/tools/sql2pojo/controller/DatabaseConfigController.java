package cn.com.toolkit.tools.sql2pojo.controller;

import cn.com.toolkit.framework.core.annotation.Transaction;
import cn.com.toolkit.framework.core.support.MybatisPlusManager;
import cn.com.toolkit.framework.core.util.JasyptUtils;
import cn.com.toolkit.framework.core.util.Notifications;
import cn.com.toolkit.framework.core.util.ToolKitFXUtil;
import cn.com.toolkit.tools.sql2pojo.domain.bo.SysDatabaseConfigFX;
import cn.com.toolkit.tools.sql2pojo.domain.po.SysDatabaseConfig;
import cn.com.toolkit.tools.sql2pojo.service.SysDatabaseConfigService;
import cn.com.toolkit.tools.sql2pojo.service.impl.SysDatabaseConfigServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfigController {
    private ClassLoader classLoader;
    private final SysDatabaseConfigFX sysDatabaseConfigFX = new SysDatabaseConfigFX();
    @FXML private TextField nameTextField;
    @FXML private TextField urlTextField;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordPasswordField;
    private SysDatabaseConfigService sysDatabaseConfigService;
    @Getter
    private boolean confirmed;

    @FXML
    private void initialize(){
        classLoader = this.getClass().getClassLoader();
        confirmed = false;
        sysDatabaseConfigService = MybatisPlusManager.getSingletonServiceImpl(SysDatabaseConfigServiceImpl.class);
        nameTextField.textProperty().bindBidirectional(sysDatabaseConfigFX.nameProperty());
        urlTextField.textProperty().bindBidirectional(sysDatabaseConfigFX.urlProperty());
        usernameTextField.textProperty().bindBidirectional(sysDatabaseConfigFX.usernameProperty());
        passwordPasswordField.textProperty().bindBidirectional(sysDatabaseConfigFX.passwordProperty());
    }

    @FXML
    private void handleSelect(ActionEvent event) {
        try {
            URL resource = DatabaseConfigController.class.getResource("/views/config-list.fxml");
            FXMLLoader loader = new FXMLLoader();
            loader.setClassLoader(classLoader);
            loader.setLocation(resource);
            Parent root = loader.load();
            ConfigListController configListController = loader.getController();

            configListController.dataObservableList()
                    .addAll(sysDatabaseConfigService.list()
                            .stream()
                            .peek(e -> e.setPassword(JasyptUtils.decryptAuto(e.getPassword(),e.getUsername())))
                            .map(SysDatabaseConfigFX::new)
                            .toList());
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));
            popupStage.initOwner(ToolKitFXUtil.getPrimaryStage());
            popupStage.setTitle("连接配置");
            popupStage.showAndWait();
            if(configListController.isOk()){
                SysDatabaseConfigFX sysDatabaseConfigFX = configListController.getResult();
                setSysDatabaseConfig(sysDatabaseConfigFX == null ? null : sysDatabaseConfigFX.getSysDatabaseConfig());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    private void handleTestConnection(ActionEvent event) {
        if(!valid()) return;
        try (Connection conn = DriverManager.getConnection(
                sysDatabaseConfigFX.getUrl(),
                sysDatabaseConfigFX.getUsername(),
                sysDatabaseConfigFX.getPassword())
        ) {
            if(conn != null && !conn.isClosed())
                Notifications.success("连接成功");
            else Notifications.error("连接失败!");
        } catch (SQLException e) {
            //Notifications.error("连接失败!" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    @Transaction
    @FXML
    private void handleSave(ActionEvent event) {
        SysDatabaseConfig entity = getSysDatabaseConfig();
        if(entity == null) return;
        entity.setId(null);
        entity.setPassword(JasyptUtils.encryptAuto(entity.getPassword(),entity.getUsername()));
        boolean result = sysDatabaseConfigService.update(entity
                ,Wrappers.<SysDatabaseConfig>lambdaUpdate()
                .eq(SysDatabaseConfig::getName,entity.getName())
        ) || sysDatabaseConfigService.save(entity);
        if(result) Notifications.success("保存成功!");
    }
    @FXML
    private void handleConfirm(ActionEvent event) {
        SysDatabaseConfig sysDatabaseConfig = getSysDatabaseConfig();
        if(sysDatabaseConfig == null) return;
        //修改默认配置
        sysDatabaseConfigService.updateDefault(sysDatabaseConfig);
        confirmed = true;
        close();
    }
    public void close(){
        ((Stage)nameTextField.getScene().getWindow()).close();
    }
    public SysDatabaseConfig getSysDatabaseConfig(){
        if(sysDatabaseConfigFX == null || !valid()) return null;
        return sysDatabaseConfigFX.getSysDatabaseConfig();
    }
    public void setSysDatabaseConfig(SysDatabaseConfig sysDatabaseConfig){
        sysDatabaseConfigFX.setSysDatabaseConfig(sysDatabaseConfig);
        sysDatabaseConfigFX.setIsDefault(true);
    }
    private boolean valid(){
        if(StringUtils.isEmpty(sysDatabaseConfigFX.getName())){
            Notifications.error("名称不能为空!");
            return false;
        }
        if(StringUtils.isEmpty(sysDatabaseConfigFX.getUrl())){
            Notifications.error("url不能为空!");
            return false;
        }
        if(StringUtils.isEmpty(sysDatabaseConfigFX.getUsername())){
            Notifications.error("username不能为空!");
            return false;
        }
        if(StringUtils.isEmpty(sysDatabaseConfigFX.getPassword())){
            Notifications.error("password不能为空!");
            return false;
        }
        return true;
    }
}
