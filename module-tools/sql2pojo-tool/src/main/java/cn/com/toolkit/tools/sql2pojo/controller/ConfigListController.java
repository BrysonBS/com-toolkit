package cn.com.toolkit.tools.sql2pojo.controller;

import atlantafx.base.theme.Styles;
import cn.com.toolkit.framework.core.support.MybatisPlusManager;
import cn.com.toolkit.framework.core.util.Notifications;
import cn.com.toolkit.tools.sql2pojo.domain.bo.SysDatabaseConfigFX;
import cn.com.toolkit.tools.sql2pojo.service.SysDatabaseConfigService;
import cn.com.toolkit.tools.sql2pojo.service.impl.SysDatabaseConfigServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;


public class ConfigListController {
    @Getter
    @Setter
    private SysDatabaseConfigFX result;
    @Getter
    private boolean ok;
    private SysDatabaseConfigService sysDatabaseConfigService;
    private final ObservableList<SysDatabaseConfigFX> dataObservableList = FXCollections.observableArrayList();
    @FXML private TableView<SysDatabaseConfigFX> databaseConfigTableView;
    @FXML private TableColumn<SysDatabaseConfigFX,String> operateColumn;

    @FXML
    public void initialize(){
        ok = false;
        sysDatabaseConfigService = MybatisPlusManager.getNewServiceImpl(SysDatabaseConfigServiceImpl.class);

        databaseConfigTableView.setItems(dataObservableList);
        operateColumn.setCellFactory(column -> new TableCell<>() {
            final Button editBtn = new Button(null, new FontIcon(FontAwesomeSolid.EDIT));
            final Button deleteBtn = new Button(null, new FontIcon(FontAwesomeSolid.TRASH));
            final HBox buttonsBox = new HBox(5, editBtn, deleteBtn);

            {
                buttonsBox.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.ACCENT);
                deleteBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);

                editBtn.setOnAction(e -> {
                    SysDatabaseConfigFX item = getTableView().getItems().get(getIndex());
                    setResult(item);
                    ok = true;
                    close();
                });
                deleteBtn.setOnAction(e -> {
                    SysDatabaseConfigFX item = getTableView().getItems().get(getIndex());
                    sysDatabaseConfigService.removeById(item.getId());
                    dataObservableList.remove(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(buttonsBox);
            }
        });
    }
    public ObservableList<SysDatabaseConfigFX> dataObservableList() {
        return dataObservableList;
    }
    @FXML
    private void handleOk(ActionEvent event) {
        SysDatabaseConfigFX selectedItem = databaseConfigTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null && !dataObservableList.isEmpty()) {
            Notifications.error("请先选择配置!");
            return;
        }
        setResult(selectedItem);
        ok = true;
        close();
    }
    @FXML
    private void handleCancel(ActionEvent event) {
        setResult(null);
        close();
    }
    public void close(){
        ((Stage)databaseConfigTableView.getScene().getWindow()).close();
    }

}
