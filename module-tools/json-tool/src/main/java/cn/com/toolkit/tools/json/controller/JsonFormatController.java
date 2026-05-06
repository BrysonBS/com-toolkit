package cn.com.toolkit.tools.json.controller;

import cn.com.toolkit.framework.core.editor.EditorCodeArea;
import cn.com.toolkit.framework.core.editor.LanguageType;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;

public class JsonFormatController {
    @FXML private StackPane contentStackPane;
    private EditorCodeArea editorPane;
    private ObjectMapper objectMapper;
    @FXML
    private void initialize(){
        editorPane = new EditorCodeArea(true, LanguageType.JSON);
        contentStackPane.getChildren().add(new VirtualizedScrollPane<>(editorPane));
        objectMapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build();

    }
    public void handleFormat(ActionEvent event) {
        String text = editorPane.getText();
        if(StringUtils.isBlank(text)) return;
        try {
            editorPane.setText(objectMapper.readTree(text).toPrettyString());
        } catch (JsonProcessingException e) {
            handleException(e);
        }
    }
    public void handleCompress(ActionEvent event) {
        String text = editorPane.getText();
        if(StringUtils.isBlank(text)) return;
        try {
            editorPane.setText(objectMapper.readTree(text).toString());
        } catch (JsonProcessingException e) {
            handleException(e);
        }
    }

    public void handleEscape(ActionEvent event) {
        String text = editorPane.getText();
        if(StringUtils.isBlank(text)) return;
        try {
            editorPane.setText(objectMapper.writeValueAsString(text));
        } catch (JsonProcessingException e) {
            handleException(e);
        }
    }

    public void handleUnescape(ActionEvent event) {
        String text = editorPane.getText();
        if(StringUtils.isBlank(text)) return;
        try {
            editorPane.setText(objectMapper.readValue(text,String.class));
        } catch (JsonProcessingException e) {
            handleException(e);
        }
    }

    private void handleException(JsonProcessingException e){
        JsonLocation jsonLocation = e.getLocation();
        int position = (int) jsonLocation.getCharOffset();
        int length = editorPane.getLength();
        editorPane.moveTo(position);
        editorPane.setStyleUnderline(Math.max(0,position - 2), Math.min(length,position + 2));
        editorPane.requestFollowCaret();
        editorPane.requestFocus();
        throw new RuntimeException(jsonLocation.toString());
    }
}
