package cn.com.toolkit.tools.image.controller;

import atlantafx.base.theme.Styles;
import cn.com.toolkit.framework.core.util.Notifications;
import cn.com.toolkit.tools.image.domain.bo.ImageInfo;
import cn.com.toolkit.tools.image.support.ImageSupport;
import com.twelvemonkeys.imageio.plugins.bmp.ICOImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.bmp.ICOImageWriterSpi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

public class ImageToolController {
    private static Logger log = LoggerFactory.getLogger(ImageToolController.class);
    @FXML private ObservableList<ImageInfo> dataObservableList = FXCollections.observableArrayList();
    @FXML private TableView<ImageInfo> imageTableView;
    @FXML private TableColumn<ImageInfo,Boolean> selectColumn;
    @FXML private TableColumn<ImageInfo,Integer> indexColumn;
    @FXML private TableColumn<ImageInfo,String> operateColumn;
    @FXML private TableColumn<ImageInfo,Long> sizeColumn;
    @FXML private TableColumn<ImageInfo,Integer> widthColumn;
    @FXML private TableColumn<ImageInfo,Integer> heightColumn;
    @FXML private TableColumn<ImageInfo,Boolean> keepAspectRatioColumn;
    @FXML private TableColumn<ImageInfo,String> extensionColumn;

    @FXML
    public void initialize(){
        imageTableView.setItems(dataObservableList);
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        indexColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else setText(String.valueOf(getIndex() + 1));
            }
        });
        operateColumn.setCellFactory(column -> new TableCell<>() {
            final Button deleteBtn = new Button(null, new FontIcon(FontAwesomeSolid.TRASH));

            {
                deleteBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
                deleteBtn.setOnAction(e -> {
                    ImageInfo imageInfo = getTableView().getItems().get(getIndex());
                    dataObservableList.remove(imageInfo);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(deleteBtn);
            }
        });
        sizeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long bytes, boolean empty) {
                super.updateItem(bytes, empty);
                setText(empty || bytes == null ? "" : formatFileSize(bytes));
            }
        });
        widthColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        heightColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        keepAspectRatioColumn.setCellFactory(CheckBoxTableCell.forTableColumn(keepAspectRatioColumn));
        extensionColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(
                Stream.concat(Arrays.stream(ImageIO.getWriterFormatNames()), Stream.of("webp"))
                        .map(String::toLowerCase)
                        .distinct()
                        .toList()
        )));
    }

    public void handleImport(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Downloads"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("支持的格式",readExtensions()));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (File file : selectedFiles) {
                BufferedImage bufferedImage = ImageIO.read(file);
                bufferedImage.getWidth();
                ImageInfo imageInfo = new ImageInfo(file,file.getName(),file.length()
                ,bufferedImage.getWidth(),bufferedImage.getHeight(),file.getName().substring(file.getName().lastIndexOf(".") + 1));
                dataObservableList.add(imageInfo);
            }
        }
    }
    @FXML
    private void handleSelectAll(ActionEvent event) {
        CheckBox checkBox = (CheckBox) event.getSource();
        dataObservableList.forEach(e -> e.setSelect(checkBox.isSelected()));
    }
    @FXML
    private void handleTransferSelect(ActionEvent event) throws IOException {
        if(dataObservableList.isEmpty()) return;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择保存目录");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Downloads"));
        File selectedDirectory = directoryChooser.showDialog(null);
        if(selectedDirectory == null) return;
        String basePath = selectedDirectory.getAbsolutePath();

        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new ICOImageReaderSpi());
        registry.registerServiceProvider(new ICOImageWriterSpi());
        List<Integer> successList = new ArrayList<>();
        for (ImageInfo imageInfo : dataObservableList.filtered(ImageInfo::getSelect)) {
            String name = imageInfo.getName();
            String newName = name.substring(0,name.lastIndexOf(".") + 1) + imageInfo.getExtension();
            File outputFile = new File(basePath, newName);
            BufferedImage originalImage = ImageIO.read(imageInfo.getFile());
            if(imageInfo.getExtension().equalsIgnoreCase("ico")
            || imageInfo.getExtension().equalsIgnoreCase("icns")){
                try {
                    int[] size = imageInfo.getExtension().equalsIgnoreCase("icns")?
                        new int[]{16,32,64,128,256} : new int[]{16,32,48,64,96,128,256};
                    ImageSupport.convertToIco(originalImage,outputFile,size,imageInfo.getExtension());
                    successList.add(dataObservableList.indexOf(imageInfo) + 1);
                }catch (Exception e){
                    log.error(e.getMessage(),e);
                }
                continue;
            }
            else if(imageInfo.getExtension().equalsIgnoreCase("wbmp"))
                originalImage = ImageSupport.convertToBinaryImage(originalImage);
            if(imageInfo.needAspectRatio())
                originalImage = ImageSupport.resizeImage(originalImage,imageInfo.getWidth(),imageInfo.getHeight(),imageInfo.getKeepAspectRatio());
            boolean success = ImageIO.write(originalImage, imageInfo.getExtension(), outputFile);
            if(success) successList.add(dataObservableList.indexOf(imageInfo) + 1);
        }
        if(successList.isEmpty())
            Notifications.error("转换失败!", Pos.TOP_RIGHT);
        else if(successList.size() == 1)
            Notifications.success("转换成功!", Pos.TOP_RIGHT);
        else
            Notifications.success("序号: " + successList + "转换成功!", Pos.TOP_RIGHT);
    }
    @FXML
    private void handleRemoveSelect(ActionEvent event) {
        dataObservableList.removeIf(ImageInfo::getSelect);
    }
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    private List<String> readExtensions(){
        return Arrays.stream(ImageIO.getReaderFormatNames())
                .map("*"::concat)
                .toList();
    }
}
