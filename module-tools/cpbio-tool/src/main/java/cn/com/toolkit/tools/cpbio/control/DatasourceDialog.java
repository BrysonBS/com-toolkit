package cn.com.toolkit.tools.cpbio.control;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.LinkedHashMap;
import java.util.Map;

public class DatasourceDialog extends Dialog<Map<String,String>> {
    private final LinkedHashMap<String,TextField> paramMap = new LinkedHashMap<>();
    private final double textFieldPrefWidth = 350d;
    private final GridPane grid = new GridPane(10,5);

    public DatasourceDialog(LinkedHashMap<String,String> configMap) {
        super();
        if(configMap != null ) {
            configMap.forEach((k, v) -> {
                TextField textField = new TextField(v);
                this.paramMap.put(k,textField);
            });
        }
        initialize();
    }
    private void initialize(){
        grid.setPadding(new Insets(10, 10, 0, 10));
        int rowIndex = 0;
        for (Map.Entry<String, TextField> entry : paramMap.entrySet()) {
            TextField textField = entry.getValue();
            textField.setPrefWidth(textFieldPrefWidth);
            grid.addRow(rowIndex++,new Label(entry.getKey() + ":"),textField);
        }
        this.getDialogPane().setContent(grid);

        ButtonType confirmButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(confirmButtonType);
        // 转换结果
        this.setResultConverter(dialogButton -> {
            if(dialogButton != confirmButtonType) return null;
            LinkedHashMap<String,String> result = new LinkedHashMap<>();
            paramMap.forEach((k,v)->result.put(k,v.getText()));
            return result;
        });
    }
}
