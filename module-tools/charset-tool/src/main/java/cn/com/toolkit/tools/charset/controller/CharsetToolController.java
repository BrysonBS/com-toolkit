package cn.com.toolkit.tools.charset.controller;

import atlantafx.base.util.Animations;
import cn.com.toolkit.framework.core.util.Notifications;
import cn.com.toolkit.tools.charset.support.ImageTypeDetector;
import cn.com.toolkit.tools.charset.support.RadixConverter;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class CharsetToolController {
    private static final String IMAGE_TO_BASE64 = "图片转Base64";
    private static final String BASE64_TO_IMAGE = "Base64转图片";
    private static final String URL_ENCODE = "URL编码";
    private static final String URL_DECODE = "URL解码";

    private static final List<String> singleSelectList = List.of(BASE64_TO_IMAGE,IMAGE_TO_BASE64,URL_ENCODE,URL_DECODE);
    private static final List<String> charsetList = List.of("US-ASCII","ISO-8859-1","UTF-8","UTF-16","UTF-32","GBK","GB2312","GB18030","Big5");
    private static final List<String> radixList = List.of("2进制","4进制","8进制","10进制","16进制","32进制");

    private static final ObservableList<String> beforeObservableList = FXCollections.observableArrayList();
    private static final ObservableList<String> afterObservableList = FXCollections.observableArrayList();
    @FXML private Label importImageLabel;
    @FXML private Label exportImageLabel;
    @FXML private StackPane leftStackPane;
    @FXML private StackPane rightStackPane;
    @FXML private Button arrowRightButton;
    @FXML private ImageView importImageView;
    @FXML private ImageView base64ImageView;
    @FXML private TextArea beforeTextArea;
    @FXML private TextArea afterTextArea;
    @FXML private ComboBox<String> beforeComboBox;
    @FXML private ComboBox<String> afterComboBox;

    @FXML
    private void initialize(){
        initComboBox();
        initImageEvent();
    }

    public void handleConvert(ActionEvent event) {
        afterTextArea.clear();
        base64ImageView.setImage(null);
        base64ImageView.setUserData(null);

        String beforeCharset = beforeComboBox.getValue();
        String afterCharset = afterComboBox.getValue();
        if(StringUtils.isBlank(beforeCharset) ||
                !singleSelectList.contains(beforeCharset) && StringUtils.isBlank(afterCharset)){
            Notifications.error("请先选择编码", Pos.TOP_RIGHT);
            return;
        }
        if(IMAGE_TO_BASE64.equals(beforeCharset) && importImageView.getImage() == null){
            Notifications.error("请先选择图片", Pos.TOP_RIGHT);
            return;
        }
        if(!IMAGE_TO_BASE64.equals(beforeCharset) && StringUtils.isEmpty(beforeTextArea.getText())) {
            Notifications.error("请先输入要转码的内容", Pos.TOP_RIGHT);
            return;
        }
        if(IMAGE_TO_BASE64.equals(beforeCharset)){
            Object imageBytes = importImageView.getUserData();
            if(imageBytes == null) return;
            String base64String = Base64.getEncoder().encodeToString((byte[])imageBytes);
            if(base64String.length() > (1L << 24)){
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("保存到文件");
                String userHome = System.getProperty("user.home");
                fileChooser.setInitialDirectory(new File(userHome, "Documents"));
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("文本文件 (*.txt)", "*.txt")
                );
                File file = fileChooser.showSaveDialog(null);
                if(file == null) return;
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(base64String.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else afterTextArea.setText(base64String);
        }
        else if(BASE64_TO_IMAGE.equals(beforeCharset)){
            //去除可能的数据头(如 "data:image/png;base64,")
            String base64String = beforeTextArea.getText();
            handleBase64ToImage(base64String);
        }
        else if(URL_ENCODE.equals(beforeCharset)){
            afterTextArea.setText(URLEncoder.encode(beforeTextArea.getText(), StandardCharsets.UTF_8));
        }
        else if(URL_DECODE.equals(beforeCharset)){
            afterTextArea.setText(URLDecoder.decode(beforeTextArea.getText(), StandardCharsets.UTF_8));
        }
        else if(radixList.contains(beforeCharset)){
            String number = beforeTextArea.getText();
            if(!NumberUtils.isParsable(number)){
                Notifications.error("请输入数值", Pos.TOP_RIGHT);
                return;
            }
            int fromRadix = NumberUtils.toInt(beforeComboBox.getValue()
                    .chars()
                    .takeWhile(Character::isDigit)
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.joining()));
            int toRadix = NumberUtils.toInt(afterComboBox.getValue()
                    .chars()
                    .takeWhile(Character::isDigit)
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.joining()));

            afterTextArea.setText(RadixConverter.convert(number,fromRadix,toRadix));
        }
        else{
            byte[] bytes = beforeTextArea.getText().getBytes(Charset.forName(beforeComboBox.getValue()));
            afterTextArea.setText(new String(bytes, Charset.forName(afterComboBox.getValue())));
        }
    }
    public void handleExportImage(){
        if(!base64ImageView.isVisible() || base64ImageView.getImage() == null) return;
        Object userData = base64ImageView.getUserData();
        if(userData == null) return;
        byte[] imageBytes = (byte[]) userData;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存图片");
        String userHome = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(userHome, "Pictures"));
        // 2. 检测图片类型
        String imageType = ImageTypeDetector.detectImageType(imageBytes);
        String extension = ImageTypeDetector.getExtension(imageBytes);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(imageType + " 图片 (*" + extension + ")", "*" + extension),
                new FileChooser.ExtensionFilter("所有文件 (*.*)", "*.*")
        );
        File file = fileChooser.showSaveDialog(null);
        if(file == null) return;
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageBytes);
            Notifications.success("保存成功!", Pos.TOP_RIGHT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void initComboBox(){
        beforeObservableList.addAll(singleSelectList);
        beforeObservableList.addAll(charsetList);
        beforeObservableList.addAll(radixList);
        beforeComboBox.setItems(beforeObservableList);
        afterComboBox.setItems(afterObservableList);

        beforeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            afterObservableList.clear();

            beforeComboBox.setEditable(false);
            arrowRightButton.setVisible(true);
            arrowRightButton.setManaged(true);
            afterComboBox.setEditable(false);
            afterComboBox.setVisible(true);
            afterComboBox.setManaged(true);
            beforeTextArea.setDisable(false);
            beforeTextArea.clear();
            beforeTextArea.setContextMenu(null);
            importImageLabel.setVisible(false);
            importImageView.setVisible(false);
            importImageView.setImage(null);
            importImageView.setUserData(null);
            base64ImageView.setVisible(false);
            base64ImageView.setImage(null);
            base64ImageView.setUserData(null);
            afterTextArea.setDisable(false);
            afterTextArea.clear();

            if(singleSelectList.contains(newValue)) {
                arrowRightButton.setVisible(false);
                arrowRightButton.setManaged(false);
                afterComboBox.setVisible(false);
                afterComboBox.setManaged(false);

                if(IMAGE_TO_BASE64.equals(newValue)){
                    beforeTextArea.setDisable(true);
                    showImportImageLabel();
                } else if(BASE64_TO_IMAGE.equals(newValue)) {
                    base64ImageView.setVisible(true);
                    afterTextArea.setDisable(true);
                    beforeTextArea.setContextMenu(importBase64FileContextMenu());
                }
            }
            else if(charsetList.contains(newValue)) {
                beforeComboBox.setEditable(true);
                afterComboBox.setEditable(true);
                afterObservableList.addAll(charsetList);
            }
            else if(radixList.contains(newValue)) afterObservableList.addAll(radixList);
        });
    }
    private void showImportImageLabel(){
        if(importImageLabel.isVisible()) return;
        importImageLabel.setVisible(true);
        importImageLabel.setOpacity(0);
        Timeline in = Animations.fadeIn(importImageLabel, Duration.millis(500));
        in.playFromStart();
    }
    private void initImageEvent(){
        importImageView.fitWidthProperty().bind(leftStackPane.widthProperty().subtract(15));
        importImageView.fitHeightProperty().bind(leftStackPane.heightProperty().subtract(15));
        base64ImageView.fitWidthProperty().bind(rightStackPane.widthProperty().subtract(15));
        base64ImageView.fitHeightProperty().bind(rightStackPane.heightProperty().subtract(15));


        leftStackPane.setOnMouseClicked(e -> {
            if(e.getButton() != MouseButton.PRIMARY) return;
            if(!IMAGE_TO_BASE64.equals(beforeComboBox.getValue())) return;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择图片");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Downloads"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("支持的格式"
                    , Arrays.stream(ImageIO.getReaderFormatNames()).map("*"::concat).toList()));
            File imageFile = fileChooser.showOpenDialog(null);
            if(imageFile == null) return;
            try {
                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                Image image = new Image(imageFile.toURI().toString());
                importImageView.setVisible(true);
                importImageView.setUserData(imageBytes);
                importImageView.setImage(image);
                importImageLabel.setVisible(false);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.5);
        colorAdjust.setContrast(0.1);
        leftStackPane.setOnMouseEntered(e -> {
            if(!IMAGE_TO_BASE64.equals(beforeComboBox.getValue())) return;
            importImageView.setEffect(colorAdjust);
            showImportImageLabel();
        });
        leftStackPane.setOnMouseExited(e -> {
            if(!IMAGE_TO_BASE64.equals(beforeComboBox.getValue())) return;
            importImageView.setEffect(null);
            if(importImageView.getImage() == null) return;
            importImageLabel.setVisible(false);
            importImageLabel.setOpacity(1);
            Timeline out = Animations.fadeOut(importImageLabel, Duration.millis(500));
            out.playFromStart();
        });


        rightStackPane.setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY) handleExportImage();
        });
        rightStackPane.setOnMouseEntered(e -> {
            if(!BASE64_TO_IMAGE.equals(beforeComboBox.getValue())
                    || base64ImageView.getImage() == null) return;
            base64ImageView.setEffect(colorAdjust);
            if(exportImageLabel.isVisible()) return;
            exportImageLabel.setVisible(true);
            exportImageLabel.setOpacity(0);
            Timeline in = Animations.fadeIn(exportImageLabel, Duration.millis(500));
            in.playFromStart();
        });
        rightStackPane.setOnMouseExited(e -> {
            if(!BASE64_TO_IMAGE.equals(beforeComboBox.getValue())) return;
            base64ImageView.setEffect(null);
            exportImageLabel.setVisible(false);
            exportImageLabel.setOpacity(1);
            Timeline out = Animations.fadeOut(exportImageLabel, Duration.millis(500));
            out.playFromStart();
        });

    }
    private ContextMenu importBase64FileContextMenu(){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem importMenu = new MenuItem("从文件导入");
        importMenu.setOnAction(e -> {
            if(!BASE64_TO_IMAGE.equals(beforeComboBox.getValue())) return;
            beforeTextArea.clear();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择文件");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Downloads"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("所有文件 (*.*)", "*.*"));
            File file = fileChooser.showOpenDialog(null);
            if(file == null) return;
            try {
                String base64String = Files.readString(file.toPath());
                handleBase64ToImage(base64String);
                Notifications.success("转换成功!", Pos.TOP_RIGHT);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        contextMenu.getItems().add(importMenu);
        return contextMenu;
    }
    private void handleBase64ToImage(String base64String){
        if (base64String.contains(","))
            base64String = base64String.substring(base64String.indexOf(",") + 1);
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64String);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
        Image image = new Image(byteArrayInputStream);
        base64ImageView.setImage(image);
        base64ImageView.setUserData(imageBytes);
    }
}
