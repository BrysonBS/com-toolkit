package cn.com.toolkit.tools.excel.controller;

import atlantafx.base.theme.Styles;
import cn.com.toolkit.framework.core.enums.HomeDirectoryEnum;
import cn.com.toolkit.framework.core.util.Notifications;
import cn.com.toolkit.framework.core.util.ToolKitUtil;
import cn.com.toolkit.tools.excel.domain.po.ExcelInfo;
import cn.com.toolkit.tools.excel.support.ExcelCopySupport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.reactfx.util.Tuple2;
import org.reactfx.util.Tuples;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelController {
    private final ObservableList<ExcelInfo> dataObservableList = FXCollections.observableArrayList();
    @FXML private CheckBox sheetNumCheckBox;
    @FXML private TextField sheetNumTextField;
    @FXML private RadioButton splitSheetRadioButton;
    @FXML private RadioButton splitColRadioButton;
    @FXML private TextField splitColTextField;
    @FXML private RadioButton splitAvgRowRadioButton;
    @FXML private Spinner<Integer> splitAvgRowSpinner;
    @FXML private RadioButton splitNumRowRadioButton;
    @FXML private Spinner<Integer> splitNumRowSpinner;
    @FXML private RadioButton mergeSheetRadioButton;
    @FXML private TableView<ExcelInfo> excelTableView;
    @FXML private TableColumn<ExcelInfo,String> operateColumn;
    @FXML private TableColumn<ExcelInfo,Integer> indexColumn;
    @FXML private TableColumn<ExcelInfo,Long> sizeColumn;
    private File importDirectory;
    private File outputDirectory;
    private File outputFile;

    @FXML
    private void initialize(){
        excelTableView.setItems(dataObservableList);
        indexColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else setText(String.valueOf(getIndex() + 1));
            }
        });
        sizeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long bytes, boolean empty) {
                super.updateItem(bytes, empty);
                setText(empty || bytes == null ? "" : ToolKitUtil.formatFileSize(bytes));
            }
        });
        operateColumn.setCellFactory(column -> new TableCell<>() {
            final Button deleteBtn = new Button(null, new FontIcon(FontAwesomeSolid.TRASH));
            {
                deleteBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
                deleteBtn.setOnAction(e -> {
                    ExcelInfo item = getTableView().getItems().get(getIndex());
                    dataObservableList.remove(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(deleteBtn);
            }
        });
        sheetNumTextField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (!newText.matches("[0-9,]*")) return null;
            if (newText.contains(",,")) return null;
            if (newText.startsWith(",")) return null;
            return change;
        }));
        splitColTextField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (!newText.matches("[a-zA-Z\\-,]*")) return null;
            if (newText.contains("--")) return null;
            if (newText.contains(",,")) return null;
            if (newText.contains("-,") || newText.contains(",-")) return null;
            if (newText.startsWith("-") || newText.startsWith(",")) return null;
            return change;
        }));
    }
    
    public void handleImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(importDirectory == null ? new File(System.getProperty("user.home"), HomeDirectoryEnum.DOWNLOADS.getValue()) : importDirectory);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件 (*.xlsx, *.xls)", "*.xlsx", "*.xls"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File file : selectedFiles) {
                importDirectory = file.getParentFile();
                ExcelInfo excelInfo = new ExcelInfo(file);
                dataObservableList.add(excelInfo);
            }
        }
    }

    public void handleExecute(ActionEvent event){
        List<File> excelFileList = dataObservableList
                .stream()
                .map(ExcelInfo::getFile)
                .filter(Objects::nonNull)
                .toList();
        if(excelFileList.isEmpty()) {
            Notifications.warning("请先选择文件", Pos.TOP_RIGHT);
            return;
        }

        String sheetNum = sheetNumTextField.getText();
        Set<Integer> sheetNoSet = null;
        if(sheetNumCheckBox.isSelected() && StringUtils.isNotBlank(sheetNum)){
            sheetNoSet = Arrays.stream(sheetNum.split(","))
                    .filter(NumberUtils::isDigits)
                    .map(NumberUtils::toInt)
                    .collect(Collectors.toSet());
        }
        if(splitSheetRadioButton.isSelected()){
            openDirectoryChooser();
            if(outputDirectory == null) return;
            for (File file : excelFileList) {
                ExcelCopySupport excelCopySupport = new ExcelCopySupport();
                excelCopySupport.splitSheet(file,outputDirectory,sheetNoSet);
            }
            Notifications.success("执行成功!", Pos.TOP_RIGHT);
        }
        else if(splitAvgRowRadioButton.isSelected()){
            openDirectoryChooser();
            if(outputDirectory == null) return;
            Integer avg = splitAvgRowSpinner.getValue();
            if(avg == null || avg <= 0) return;
            for (File file : excelFileList) {
                ExcelCopySupport excelCopySupport = new ExcelCopySupport();
                excelCopySupport.splitAvgRow(file,outputDirectory,sheetNoSet,avg);
            }
            Notifications.success("执行成功!", Pos.TOP_RIGHT);
        }
        else if(splitNumRowRadioButton.isSelected()){
            openDirectoryChooser();
            if(outputDirectory == null) return;
            Integer num = splitNumRowSpinner.getValue();
            if(num == null || num <= 0) return;
            for (File file : excelFileList) {
                ExcelCopySupport excelCopySupport = new ExcelCopySupport();
                excelCopySupport.splitNumRow(file,outputDirectory,sheetNoSet,num);
            }
            Notifications.success("执行成功!", Pos.TOP_RIGHT);
        }
        else if(splitColRadioButton.isSelected()){
            String value = splitColTextField.getText();
            if(StringUtils.isBlank(value)) {
                Notifications.warning("请先输入需要拆分的列",Pos.TOP_RIGHT);
                return;
            }
            openDirectoryChooser();
            if(outputDirectory == null) return;
            List<Tuple2<String,String>> colList = Arrays.stream(value.split(","))
                    .map(e -> e.split("-"))
                    .filter(arr -> arr.length >= 1)
                    .map(arr -> Tuples.t(arr[0], arr.length == 1 ? null : arr[1]))
                    .toList();
            for (File file : excelFileList) {
                ExcelCopySupport excelCopySupport = new ExcelCopySupport();
                excelCopySupport.splitCol(file,outputDirectory,sheetNoSet,colList);
            }
            Notifications.success("执行成功!", Pos.TOP_RIGHT);
        }
        else if(mergeSheetRadioButton.isSelected()){
            openSaveFileChooser();
            if(outputFile == null) return;
            ExcelCopySupport excelCopySupport = new ExcelCopySupport();
            excelCopySupport.mergeSheet(excelFileList,outputFile);
            Notifications.success("执行成功!", Pos.TOP_RIGHT);
        }


    }

    //按Sheet拆分,每个Sheet为一份
    private void splitSheet(File directory,Set<Integer> sheetNoSet) throws IOException {

    }


    private void openDirectoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("保存文件夹");
        directoryChooser.setInitialDirectory(outputDirectory == null ? new File(System.getProperty("user.home"), HomeDirectoryEnum.DOCUMENTS.getValue()) : outputDirectory);
        outputDirectory = directoryChooser.showDialog(null);
    }
    private void openSaveFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文件");
        fileChooser.setInitialDirectory(outputDirectory == null ? new File(System.getProperty("user.home"), HomeDirectoryEnum.DOCUMENTS.getValue()) : outputDirectory);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件 (*.xlsx)", "*.xlsx"));
        outputFile = fileChooser.showSaveDialog(null);
        if(outputFile == null) return;
        outputDirectory = outputFile.getParentFile();
    }
}
