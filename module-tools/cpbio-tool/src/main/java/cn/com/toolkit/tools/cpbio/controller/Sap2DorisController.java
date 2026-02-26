package cn.com.toolkit.tools.cpbio.controller;

import cn.com.toolkit.framework.core.editor.EditorCodeArea;
import cn.com.toolkit.framework.core.editor.LanguageType;
import cn.com.toolkit.tools.cpbio.domain.dto.SapTableInfoDTO;
import cn.com.toolkit.tools.cpbio.service.SapTableInfoService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class Sap2DorisController {
    private final SapTableInfoService sapTableInfoService;
    @FXML private ComboBox<String> keywordComboBox;
    @FXML private StackPane contentStackPane;
    private EditorCodeArea editorPane;
    @FXML
    private void initialize() {
        editorPane = new EditorCodeArea(true,LanguageType.SQL);
        contentStackPane.getChildren().add(new VirtualizedScrollPane<>(editorPane));
    }




    public void handleGenerate(ActionEvent event) {
        String value = keywordComboBox.getValue();
        if (StringUtils.isBlank(value)) throw new RuntimeException("请先选择表!");
        List<SapTableInfoDTO> dataList = sapTableInfoService.selectFieldList(value);
        if(dataList == null || dataList.isEmpty())
            throw new RuntimeException(String.format("表[%s]不存在!",value));
        String ddl = sapTableInfoService.generateDDL(dataList);
        editorPane.setText(ddl);
    }

    public void handleEnterPressed(KeyEvent keyEvent) {
        String keyword = keywordComboBox.getValue();
        if(StringUtils.isBlank(keyword)) return;
        if(keyEvent.getCode() != KeyCode.ENTER) return;
        List<SapTableInfoDTO> sapTableInfoDTOList =  sapTableInfoService.selectTableList(keyword);
        if(sapTableInfoDTOList == null || sapTableInfoDTOList.isEmpty()) return;
        List<String> itemList = sapTableInfoDTOList.stream().map(SapTableInfoDTO::getTableName).toList();
        keywordComboBox.setItems(FXCollections.observableList(itemList));
        keywordComboBox.show();
    }

}
